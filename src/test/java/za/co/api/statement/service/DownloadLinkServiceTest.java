package za.co.api.statement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import za.co.api.statement.dto.CreateDownloadLinkRequestDTO;
import za.co.api.statement.dto.DownloadLinkDTO;
import za.co.api.statement.dto.code.DownloadLinkStatusCode;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.entity.DownloadLinkEntity;
import za.co.api.statement.entity.StatementEntity;
import za.co.api.statement.repository.DownloadLinkRepository;
import za.co.api.statement.repository.StatementRepository;
import za.co.api.statement.test.data.StatementTestFixtures;
import za.co.api.statement.transformer.DownloadLinkTransformer;
import za.co.common.exception.ResourceNotFoundException;
import za.co.common.exception.ValidationException;
import za.co.common.security.service.ClaimsService;

@Tag("downloadlink")
@DisplayName("[SFD-130] DownloadLink Service Tests")
@ExtendWith(MockitoExtension.class)
class DownloadLinkServiceTest {

    @InjectMocks
    private DownloadLinkService downloadLinkService;

    @Mock
    private DownloadLinkRepository repository;

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private DownloadLinkTransformer transformer;

    @Mock
    private ClaimsService claimsService;

    @Mock
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(downloadLinkService, "hmacSecret", "test-hmac-secret-key-at-least-32-characters-long");
        ReflectionTestUtils.setField(downloadLinkService, "downloadBaseUrl", "http://localhost:8093/api/v1/statements/download");
    }

    // =====================================================
    // GENERATE DOWNLOAD LINK TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-131] Generate download link with valid statement returns DTO")
    void whenGenerateDownloadLink_withValidStatement_thenReturnsDto() {
        StatementEntity statement = StatementTestFixtures.validStatementEntity();
        DownloadLinkEntity savedLink = StatementTestFixtures.validDownloadLinkEntity();
        DownloadLinkDTO dto = StatementTestFixtures.validDownloadLinkDto();

        when(statementRepository.findById(eq(StatementTestFixtures.VALID_ID))).thenReturn(Optional.of(statement));
        when(repository.save(any(DownloadLinkEntity.class))).thenReturn(savedLink);
        when(transformer.toDTO(any(DownloadLinkEntity.class))).thenReturn(dto);
        when(claimsService.getClientId(any(Authentication.class))).thenReturn(Optional.of("test-user"));

        CreateDownloadLinkRequestDTO request = StatementTestFixtures.validCreateDownloadLinkRequest();
        DownloadLinkDTO result = downloadLinkService.generateDownloadLink(
                StatementTestFixtures.VALID_ID, request, mockAuthentication);

        assertThat(result).isNotNull();
        assertThat(result.getStatementId()).isEqualTo(StatementTestFixtures.VALID_ID);
        verify(repository).save(any(DownloadLinkEntity.class));
    }

    @Test
    @DisplayName("[SFD-132] Generate download link with non-existent statement throws ResourceNotFoundException")
    void whenGenerateDownloadLink_withNonExistentStatement_thenThrows() {
        when(statementRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> downloadLinkService.generateDownloadLink(
                StatementTestFixtures.NON_EXISTENT_ID, null, mockAuthentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("[SFD-133] Generate download link with deleted statement throws ValidationException")
    void whenGenerateDownloadLink_withDeletedStatement_thenThrows() {
        StatementEntity statement = StatementTestFixtures.validStatementEntity();
        statement.setStatus(StatementStatusCode.DELETED);

        when(statementRepository.findById(eq(StatementTestFixtures.VALID_ID))).thenReturn(Optional.of(statement));

        assertThatThrownBy(() -> downloadLinkService.generateDownloadLink(
                StatementTestFixtures.VALID_ID, null, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-134] Generate download link with null statementId throws ValidationException")
    void whenGenerateDownloadLink_withNullId_thenThrows() {
        assertThatThrownBy(() -> downloadLinkService.generateDownloadLink(null, null, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-137] Generate download link with invalid UUID format throws ValidationException")
    void whenGenerateDownloadLink_withInvalidUuidFormat_thenThrows() {
        assertThatThrownBy(() -> downloadLinkService.generateDownloadLink("not-a-uuid", null, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    // =====================================================
    // REVOKE LINKS TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-135] Revoke all active links sets status to REVOKED")
    void whenRevokeAllActiveLinks_thenSetsStatusToRevoked() {
        DownloadLinkEntity activeLink = StatementTestFixtures.validDownloadLinkEntity();
        List<DownloadLinkEntity> activeLinks = List.of(activeLink);

        when(repository.findByStatementIdAndStatus(
                eq(StatementTestFixtures.VALID_ID), eq(DownloadLinkStatusCode.ACTIVE)))
                .thenReturn(activeLinks);

        downloadLinkService.revokeAllActiveLinks(StatementTestFixtures.VALID_ID);

        assertThat(activeLink.getStatus()).isEqualTo(DownloadLinkStatusCode.REVOKED);
        verify(repository).saveAll(activeLinks);
    }

    @Test
    @DisplayName("[SFD-136] Revoke with no active links does nothing")
    void whenRevokeAllActiveLinks_withNoActiveLinks_thenDoesNothing() {
        when(repository.findByStatementIdAndStatus(anyString(), eq(DownloadLinkStatusCode.ACTIVE)))
                .thenReturn(List.of());

        downloadLinkService.revokeAllActiveLinks(StatementTestFixtures.VALID_ID);

        // No exception thrown, no saveAll called
    }
}
