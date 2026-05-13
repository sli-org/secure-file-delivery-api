package za.co.api.statement.service;

import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.common.api.paging.PaginatedListDTO;
import za.co.common.api.paging.PagingDTO;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;
import za.co.api.statement.entity.StatementEntity;
import za.co.api.statement.repository.StatementRepository;
import za.co.api.statement.transformer.StatementTransformer;
import za.co.common.exception.DuplicateResourceException;
import za.co.common.exception.ResourceNotFoundException;
import za.co.common.exception.ServiceException;
import za.co.common.exception.ValidationError;
import za.co.common.exception.ValidationException;
import za.co.common.security.service.ClaimsService;
import za.co.common.util.ExceptionUtil;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
/**
 * Service for Statement business logic with database persistence.
 * Handles statement upload, retrieval, listing, and soft-delete operations.
 * Integrates with Azure Blob Storage for PDF file storage.
 */
@Service
@Slf4j
@Transactional
public class StatementService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_RETENTION_DAYS = 365;
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String SHA_256_ALGORITHM = "SHA-256";
    private static final int MAX_CUSTOMER_ID_LENGTH = 50;
    private static final int PAGINATION_LIMIT = 2;
    private static final int MAX_ACCOUNT_NUMBER_LENGTH = 20;
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    private BlobServiceClient blobServiceClient;
    private final StatementRepository repository;
    private final StatementTransformer transformer;
    private final StatementEventService eventService;
    private final DownloadLinkService downloadLinkService;
    private final ClaimsService claimsService;

    public StatementService(
            StatementRepository repository,
            StatementTransformer transformer,
            StatementEventService eventService,
            DownloadLinkService downloadLinkService,
            ClaimsService claimsService) {
        this.repository = repository;
        this.transformer = transformer;
        this.eventService = eventService;
        this.downloadLinkService = downloadLinkService;
        this.claimsService = claimsService;
    }

    @PostConstruct
    public void initAzureStorage() {
        if (azureStorageConnectionString != null && !azureStorageConnectionString.isEmpty()) {
            try {
                blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(azureStorageConnectionString)
                        .buildClient();
                log.info("Azure Blob Storage client initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Azure Blob Storage client: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Azure Storage connection string not configured");
        }
    }
    /**
     * Uploads a new statement PDF to blob storage and creates metadata record.
     * Implements BR1 (File Upload) and BR4 (Account Number Masking).
     *
     * @param file the PDF file to upload
     * @param customerId the customer identifier
     * @param statementDate the statement date
     * @param statementType the statement type
     * @param accountNumber the account number
     * @param authentication the authentication context for audit fields
     * @return the statement DTO with masked account number
     */
    public StatementDTO uploadStatement(
            MultipartFile file,
            String customerId,
            LocalDateTime statementDate,
            StatementTypeCode statementType,
            String accountNumber,
            Authentication authentication) {
        log.info("Uploading statement for customer: {}", customerId);

        List<ValidationError> errors = new ArrayList<>();
        validateUploadRequest(file, customerId, statementDate, statementType, accountNumber, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed.", errors);
        }

        try {
            byte[] fileBytes = file.getBytes();
            validatePdfMagicBytes(fileBytes, errors);
            if (!errors.isEmpty()) {
                throw new ValidationException("Validation failed.", errors);
            }

            String contentHash = computeSha256Hash(fileBytes);

            // Duplicate content check: reject if identical file already exists for this customer
            if (repository.existsByContentHashAndCustomerIdAndStatusNot(
                    contentHash, customerId, StatementStatusCode.DELETED)) {
                throw new DuplicateResourceException(
                        "A statement with identical content already exists for customer: " + customerId);
            }

            String sanitizedCustomerId = sanitizePathSegment(customerId);
            String blobPath = String.format("statements/%s/%s.pdf", sanitizedCustomerId, java.util.UUID.randomUUID());

            // Upload to Azure Blob Storage (BR1)
            uploadToBlobStorage(blobPath, fileBytes);

            // Sanitize filename to prevent header injection
            String safeFileName = sanitizeFileName(file.getOriginalFilename());

            // Create statement entity
            StatementEntity entity = StatementEntity.builder()
                    .customerId(customerId)
                    .statementDate(statementDate)
                    .statementType(statementType)
                    .accountNumber(accountNumber)
                    .fileName(safeFileName)
                    .fileSize((long) fileBytes.length)
                    .contentHash(contentHash)
                    .blobPath(blobPath)
                    .status(StatementStatusCode.AVAILABLE)
                    .retentionDays(DEFAULT_RETENTION_DAYS)
                    .createdBy(claimsService.getClientId(authentication).orElse("system"))
                    .createdAt(LocalDateTime.now())
                    .build();

            StatementEntity saved = repository.save(entity);
            StatementDTO resultDto = transformer.toDTO(saved);

            eventService.publishStatementUploadedEvent(resultDto);
            log.info("Uploaded statement with ID: {}", saved.getId());
            return resultDto;

        } catch (ValidationException | DuplicateResourceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception while uploading statement: {}", e.getMessage(), e);
            throw ExceptionUtil.createGenericError(e, "uploading", "statement");
        }
    }

    /**
     * Retrieves a statement by ID with masked account number (BR4).
     *
     * @param id the statement ID
     * @param authentication the authentication context
     * @return the statement DTO
     */
    @Transactional(readOnly = true)
    public StatementDTO find(String id, Authentication authentication) {
        log.info("Retrieving statement with ID: {}", id);

        List<ValidationError> errors = new ArrayList<>();
        validateId(id, "id", errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed.", errors);
        }

        try {
            StatementEntity entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Statement with ID: " + id + " not found.", null));

            log.info("Statement found successfully with ID: {}", id);
            return transformer.toDTO(entity);

        } catch (ResourceNotFoundException e) {
            log.error("ResourceNotFoundException for statement ID {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception while finding statement ID {}: {}", id, e.getMessage(), e);
            throw ExceptionUtil.createGenericError(e, "finding", "statement");
        }
    }

    /**
     * Lists statements with filtering and pagination
     * Excludes DELETED statements.
     *
     * @param customerId required customer identifier
     * @param statementType optional type filter
     * @param status optional status filter
     * @param fromDate optional from date filter
     * @param toDate optional to date filter
     * @param limit max results (default 10, max 100)
     * @param offset pagination offset
     * @param authentication the authentication context
     * @return paginated list of statements
     */
    @Transactional(readOnly = true)
    public PaginatedListDTO<StatementDTO> findAll(
            String customerId,
            StatementTypeCode statementType,
            StatementStatusCode status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int limit,
            int offset,
            Authentication authentication) {
        log.info("Listing statements for customer: {} with limit: {}, offset: {}", customerId, limit, offset);

        List<ValidationError> errors = new ArrayList<>();
        if (customerId == null || customerId.trim().isEmpty()) {
            errors.add(new ValidationError("customerId", "Customer ID is required.", null));
        }
        if (limit <= 0) {
            errors.add(new ValidationError("limit", "Limit must be greater than zero.", null));
        }
        if (limit > MAX_PAGE_SIZE) {
            errors.add(new ValidationError("limit", "Limit cannot exceed " + MAX_PAGE_SIZE + ".", null));
        }
        if (offset < 0) {
            errors.add(new ValidationError("offset", "Offset cannot be negative.", null));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed.", errors);
        }

        try {
            int page = offset / limit;
            Pageable pageable = PageRequest.of(page, limit);
            
            // Use the simple query first to verify it works
            Page<StatementEntity> entityPage = repository.findByCustomerId(customerId, pageable);

            List<StatementDTO> dtos = entityPage.getContent().stream()
                    .map(transformer::toDTO)
                    .toList();

            PagingDTO paging = new PagingDTO(limit, offset, entityPage.getTotalElements());
            log.info("Statements found: {} (total: {})", dtos.size(), entityPage.getTotalElements());
            return new PaginatedListDTO<>(dtos, paging);

        } catch (Exception e) {
            log.error("Unexpected exception while listing statements: {}", e.getMessage(), e);
            throw ExceptionUtil.createGenericError(e, "listing", "statements");
        }
    }
    /**
     * Soft-deletes a statement and revokes all active download links (BR5).
     * Blob is NOT deleted (retained for compliance).
     *
     * @param id the statement ID
     * @param authentication the authentication context
     */
    public void delete(String id, Authentication authentication) {
        log.info("Deleting statement with ID: {}", id);

        List<ValidationError> errors = new ArrayList<>();
        validateId(id, "id", errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed.", errors);
        }

        try {
            StatementEntity entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Statement with ID: " + id + " not found.", null));

            if (entity.getStatus() == StatementStatusCode.DELETED) {
                log.info("Statement with ID: {} is already deleted, returning silently", id);
                return;
            }

            entity.setStatus(StatementStatusCode.DELETED);
            entity.setUpdatedBy(claimsService.getClientId(authentication).orElse("system"));
            entity.setUpdatedAt(LocalDateTime.now());
            repository.save(entity);

            // Revoke all active download links (BR5)
            downloadLinkService.revokeAllActiveLinks(id);

            eventService.publishStatementDeletedEvent(id);
            log.info("Statement with ID: {} soft-deleted successfully", id);

        } catch (ResourceNotFoundException e) {
            log.error("ResourceNotFoundException while deleting statement ID {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception while deleting statement ID {}: {}", id, e.getMessage(), e);
            throw ExceptionUtil.createGenericError(e, "deleting", "statement");
        }
    }

    /**
     * Downloads a statement PDF from blob storage.
     * Not transactional — this method only calls an external HTTP endpoint and does not interact with the database.
     *
     * @param blobPath the blob storage path
     * @return the PDF file bytes
     */
    public byte[] downloadFromBlobStorage(String blobPath) {
        log.info("Downloading from blob storage: {}", blobPath);
        
        try {
            String[] pathParts = blobPath.split("/", PAGINATION_LIMIT);
            String containerName = pathParts[0];
            String blobName = pathParts[1];
            
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            containerClient.getBlobClient(blobName).downloadStream(outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Error downloading from blob storage: {}", e.getMessage(), e);
            throw new ServiceException("Failed to download file from storage: " + e.getMessage(), "BLOB_DOWNLOAD_FAILED");
        }
    }    

    private void validateUploadRequest(
            MultipartFile file, String customerId, LocalDateTime statementDate,
            StatementTypeCode statementType, String accountNumber, List<ValidationError> errors) {

        if (file == null || file.isEmpty()) {
            errors.add(new ValidationError("file", "PDF file is required.", null));
        } else {
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                errors.add(new ValidationError("file", "File size cannot exceed 10MB.", null));
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals(PDF_CONTENT_TYPE)) {
                errors.add(new ValidationError("file", "Only PDF content type is accepted.", null));
            }
        }
        if (customerId == null || customerId.trim().isEmpty()) {
            errors.add(new ValidationError("customerId", "Customer ID is required.", null));
        } else if (customerId.length() > MAX_CUSTOMER_ID_LENGTH) {
            errors.add(new ValidationError("customerId", "Customer ID cannot exceed " + MAX_CUSTOMER_ID_LENGTH + " characters.", null));
        }
        if (statementDate == null) {
            errors.add(new ValidationError("statementDate", "Statement date is required.", null));
        }
        if (statementType == null) {
            errors.add(new ValidationError("statementType", "Statement type is required.", null));
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            errors.add(new ValidationError("accountNumber", "Account number is required.", null));
        } else if (accountNumber.length() > MAX_ACCOUNT_NUMBER_LENGTH) {
            errors.add(new ValidationError("accountNumber", "Account number cannot exceed " + MAX_ACCOUNT_NUMBER_LENGTH + " characters.", null));
        }
    }

    /**
     * Validates that the file starts with PDF magic bytes (%PDF).
     *
     * @param fileBytes the raw file content to validate
     * @param errors the list to collect validation errors
     */
    private void validatePdfMagicBytes(byte[] fileBytes, List<ValidationError> errors) {
        if (fileBytes.length < PDF_MAGIC_BYTES.length) {
            errors.add(new ValidationError("file", "File is too small to be a valid PDF.", null));
            return;
        }
        for (int i = 0; i < PDF_MAGIC_BYTES.length; i++) {
            if (fileBytes[i] != PDF_MAGIC_BYTES[i]) {
                errors.add(new ValidationError("file", "File content does not match PDF format.", null));
                return;
            }
        }
    }

    /**
     * Sanitizes a string for safe use as a blob storage path segment.
     * Removes path traversal characters and non-alphanumeric characters.
     *
     * @param input the raw path segment string
     * @return sanitized string safe for use in blob paths
     */
    private String sanitizePathSegment(String input) {
        if (input == null) {
            return "unknown";
        }
        // Extract just the filename portion (remove any path separators)
        String name = Paths.get(input).getFileName().toString();
        // Replace any non-safe characters
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Sanitizes a filename to prevent header injection and path traversal.
     * Strips path components and removes control characters.
     *
     * @param originalFilename the original filename from the uploaded file
     * @return sanitized filename safe for storage and Content-Disposition headers
     */
    private String sanitizeFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "statement.pdf";
        }
        // Strip path components
        String name = Paths.get(originalFilename).getFileName().toString();
        // Remove control characters and non-printable chars
        name = name.replaceAll("[\\x00-\\x1F\\x7F]", "");
        // Limit length
        if (name.length() > MAX_FILENAME_LENGTH) {
            name = name.substring(0, MAX_FILENAME_LENGTH);
        }
        return name.isEmpty() ? "statement.pdf" : name;
    }

    /**
     * Validates that an ID is non-blank and is a valid UUID format.
     *
     * @param id the ID value to validate
     * @param fieldName the field name for error reporting
     * @param errors the list to collect validation errors
     */
    private void validateId(String id, String fieldName, List<ValidationError> errors) {
        if (id == null || id.trim().isEmpty()) {
            errors.add(new ValidationError(fieldName, "ID cannot be null or empty.", null));
        } else {
            try {
                java.util.UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError(fieldName, "ID must be a valid UUID format.", null));
            }
        }
    }
    private void uploadToBlobStorage(String blobPath, byte[] fileBytes) {
        log.info("Uploading to blob storage: {}", blobPath);
        
        try {
            // Extract container name and blob name
            // blobPath format: "statements/CUST-001/uuid.pdf"
            String[] pathParts = blobPath.split("/", PAGINATION_LIMIT);
            String containerName = pathParts[0]; // "statements"
            String blobName = pathParts[1]; // "CUST-001/uuid.pdf"
            
            // Get or create container
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            containerClient.createIfNotExists();
            
            // Upload the blob
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
            containerClient.getBlobClient(blobName).upload(inputStream, fileBytes.length, true);
            
            log.info("Successfully uploaded to blob storage: {}", blobPath);
            
        } catch (Exception e) {
            log.error("Failed to upload to blob storage: {}", e.getMessage(), e);
            throw new ServiceException("Failed to upload file to storage: " + e.getMessage(), "BLOB_UPLOAD_FAILED");
        }
    }    

    private String computeSha256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(Byte.toUnsignedInt(b));
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtil.createGenericError(e, "computing hash for", "statement file");
        }
    }
}
