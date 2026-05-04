package za.co.api.statement.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.api.statement.dto.CreateDownloadLinkRequestDTO;
import za.co.api.statement.dto.DownloadLinkDTO;
import za.co.api.statement.dto.code.DownloadLinkStatusCode;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.entity.DownloadLinkEntity;
import za.co.api.statement.entity.StatementEntity;
import za.co.api.statement.repository.DownloadLinkRepository;
import za.co.api.statement.repository.StatementRepository;
import za.co.api.statement.transformer.DownloadLinkTransformer;
import za.co.common.exception.ResourceNotFoundException;
import za.co.common.exception.ServiceException;
import za.co.common.exception.ValidationError;
import za.co.common.exception.ValidationException;
import za.co.common.security.service.ClaimsService;
import za.co.common.util.ExceptionUtil;

/**
 * Service for DownloadLink business logic.
 * Handles download link generation, token validation, and download tracking.
 * Implements BR2 (Download Link Generation), BR3 (Download Validation), BR6 (Retention Policy).
 */
@Service
@Slf4j
@Transactional
public class DownloadLinkService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int DEFAULT_EXPIRES_IN_HOURS = 24;
    private static final int DEFAULT_MAX_DOWNLOADS = 1;

    @Value("${download.hmac-secret:${DOWNLOAD_HMAC_SECRET:change-me-in-production}}")
    private String hmacSecret;

    @Value("${download.base-url:${DOWNLOAD_BASE_URL:http://localhost:8093/api/v1/statements/download}}")
    private String downloadBaseUrl;

    private static final int TOKEN_PARTS_COUNT = 3;
    private static final int TOKEN_SIGNATURE_INDEX = 2;

    private final DownloadLinkRepository repository;
    private final StatementRepository statementRepository;
    private final DownloadLinkTransformer transformer;
    private final ClaimsService claimsService;

    public DownloadLinkService(
            DownloadLinkRepository repository,
            StatementRepository statementRepository,
            DownloadLinkTransformer transformer,
            ClaimsService claimsService) {
        this.repository = repository;
        this.statementRepository = statementRepository;
        this.transformer = transformer;
        this.claimsService = claimsService;
    }

    private static final int MIN_HMAC_SECRET_LENGTH = 32;

    /**
     * Validates HMAC secret configuration on startup.
     * Warns if using default value or short secret.
     */
    @PostConstruct
    void validateHmacSecret() {
        if ("change-me-in-production".equals(hmacSecret)) {
            log.warn("HMAC secret is using the default value. Set DOWNLOAD_HMAC_SECRET for production.");
        }
        if (hmacSecret.length() < MIN_HMAC_SECRET_LENGTH) {
            log.warn("HMAC secret is shorter than {} characters. Use a stronger secret.", MIN_HMAC_SECRET_LENGTH);
        }
    }

    /**
     * Generates a secure, time-limited download link for a statement (BR2).
     *
     * @param statementId the statement ID
     * @param request the download link creation request
     * @param authentication the authentication context
     * @return the download link DTO with downloadUrl
     */
    public DownloadLinkDTO generateDownloadLink(
            String statementId,
            CreateDownloadLinkRequestDTO request,
            Authentication authentication) {
        log.info("Generating download link for statement ID: {}", statementId);

        List<ValidationError> errors = new ArrayList<>();
        if (statementId == null || statementId.trim().isEmpty()) {
            errors.add(new ValidationError("statementId", "Statement ID is required.", null));
        } else {
            try {
                java.util.UUID.fromString(statementId);
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError("statementId", "Statement ID must be a valid UUID format.", null));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed.", errors);
        }

        try {
            // Verify statement exists and is AVAILABLE (BR6)
            StatementEntity statement = statementRepository.findById(statementId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Statement with ID: " + statementId + " not found.", null));

            if (statement.getStatus() != StatementStatusCode.AVAILABLE) {
                errors.add(new ValidationError("statementId",
                        "Statement is not available for download. Current status: " + statement.getStatus(), null));
                throw new ValidationException("Validation failed.", errors);
            }

            int expiresInHours = request != null ? request.getEffectiveExpiresInHours() : DEFAULT_EXPIRES_IN_HOURS;
            int maxDownloads = request != null ? request.getEffectiveMaxDownloads() : DEFAULT_MAX_DOWNLOADS;

            LocalDateTime expiresAt = LocalDateTime.now().plusHours(expiresInHours);
            long expiresEpochSec = expiresAt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();

            // Generate HMAC token (BR2)
            String token = generateToken(statementId, expiresEpochSec);
            String downloadUrl = downloadBaseUrl + "/" + token;

            // Create download link entity
            DownloadLinkEntity entity = DownloadLinkEntity.builder()
                    .statementId(statementId)
                    .token(token)
                    .expiresAt(expiresAt)
                    .maxDownloads(maxDownloads)
                    .downloadCount(0)
                    .status(DownloadLinkStatusCode.ACTIVE)
                    .createdBy(claimsService.getClientId(authentication).orElse("system"))
                    .createdAt(LocalDateTime.now())
                    .build();

            DownloadLinkEntity saved = repository.save(entity);
            DownloadLinkDTO resultDto = transformer.toDTO(saved);
            resultDto.setDownloadUrl(downloadUrl);

            log.info("Generated download link with ID: {} for statement: {}", saved.getId(), statementId);
            return resultDto;

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception while generating download link: {}", e.getMessage(), e);
            throw ExceptionUtil.createGenericError(e, "generating download link for", "statement");
        }
    }

    /**
     * Validates a download token and returns the statement entity if valid (BR3).
     *
     * @param token the HMAC-signed download token
     * @param clientIp the client's IP address
     * @return the statement entity for download
     */
    public StatementEntity validateAndRecordDownload(String token, String clientIp) {
        log.info("Validating download token");

        List<ValidationError> errors = new ArrayList<>();
        if (token == null || token.trim().isEmpty()) {
            errors.add(new ValidationError("token", "Download token is required.", null));
            throw new ValidationException("Validation failed.", errors);
        }

        try {
            // Validate token signature (BR3)
            TokenPayload payload = parseAndValidateToken(token);

            // Find the download link by token
            DownloadLinkEntity link = repository.findByToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Download link not found.", null));

            // Validate link status
            if (link.getStatus() != DownloadLinkStatusCode.ACTIVE) {
                throw new ServiceException(
                        "Download link is no longer active. Status: " + link.getStatus(), "LINK_INACTIVE");
            }

            // Validate expiry (BR3)
            if (LocalDateTime.now().isAfter(link.getExpiresAt())) {
                link.setStatus(DownloadLinkStatusCode.EXPIRED);
                repository.save(link);
                throw new ServiceException("Download link has expired.", "LINK_EXPIRED");
            }

            // Validate download count (BR3)
            if (link.getDownloadCount() >= link.getMaxDownloads()) {
                link.setStatus(DownloadLinkStatusCode.USED);
                repository.save(link);
                throw new ServiceException("Download limit reached.", "DOWNLOAD_LIMIT_REACHED");
            }

            // Validate statement status (BR3)
            StatementEntity statement = statementRepository.findById(payload.statementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Statement not found.", null));

            if (statement.getStatus() == StatementStatusCode.DELETED
                    || statement.getStatus() == StatementStatusCode.EXPIRED) {
                throw new ServiceException(
                        "Statement is not available. Status: " + statement.getStatus(), "STATEMENT_UNAVAILABLE");
            }

            // Record download (BR3)
            link.setDownloadCount(link.getDownloadCount() + 1);
            link.setDownloadedAt(LocalDateTime.now());
            link.setDownloadedByIp(clientIp);

            if (link.getDownloadCount() >= link.getMaxDownloads()) {
                link.setStatus(DownloadLinkStatusCode.USED);
            }
            repository.save(link);

            log.info("Download validated for statement ID: {}", statement.getId());
            return statement;

        } catch (ResourceNotFoundException | ValidationException | ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating download token: {}", e.getMessage(), e);
            throw ExceptionUtil.createGenericError(e, "validating", "download token");
        }
    }

    /**
     * Revokes all active download links for a statement (BR5).
     *
     * @param statementId the statement ID
     */
    public void revokeAllActiveLinks(String statementId) {
        log.info("Revoking all active links for statement ID: {}", statementId);

        List<DownloadLinkEntity> activeLinks = repository.findByStatementIdAndStatus(
                statementId, DownloadLinkStatusCode.ACTIVE);

        for (DownloadLinkEntity link : activeLinks) {
            link.setStatus(DownloadLinkStatusCode.REVOKED);
        }

        if (!activeLinks.isEmpty()) {
            repository.saveAll(activeLinks);
            log.info("Revoked {} active download links for statement ID: {}", activeLinks.size(), statementId);
        }
    }

    /**
     * Generates an HMAC-signed token (BR2).
     * Format: Base64URL(statementId:expiresEpochSec:hmacSignature)
     *
     * @param statementId the statement identifier
     * @param expiresEpochSec the token expiration time in epoch seconds
     * @return the Base64URL-encoded HMAC-signed token
     */
    private String generateToken(String statementId, long expiresEpochSec) {
        try {
            String payload = statementId + ":" + expiresEpochSec;
            String signature = computeHmac(payload);
            String fullToken = payload + ":" + signature;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    fullToken.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw ExceptionUtil.createGenericError(e, "generating token for", "download link");
        }
    }

    /**
     * Parses and validates an HMAC-signed token (BR3).
     *
     * @param token the Base64URL-encoded token string
     * @return the parsed and verified token payload
     */
    private TokenPayload parseAndValidateToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");

            if (parts.length != TOKEN_PARTS_COUNT) {
                throw new ServiceException("Invalid token format.", "INVALID_TOKEN");
            }

            String statementId = parts[0];
            long expiresEpochSec = Long.parseLong(parts[1]);
            String providedSignature = parts[TOKEN_SIGNATURE_INDEX];

            // Verify HMAC signature
            String expectedSignature = computeHmac(statementId + ":" + expiresEpochSec);
            if (!expectedSignature.equals(providedSignature)) {
                throw new ServiceException("Invalid token signature.", "INVALID_TOKEN");
            }

            return new TokenPayload(statementId, expiresEpochSec);

        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid token encoding.", "INVALID_TOKEN");
        }
    }

    private String computeHmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw ExceptionUtil.createGenericError(e, "computing HMAC for", "download token");
        }
    }

    private record TokenPayload(String statementId, long expiresEpochSec) {}
}
