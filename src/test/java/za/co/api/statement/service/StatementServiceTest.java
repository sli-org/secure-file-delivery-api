package za.co.api.statement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import za.co.common.api.paging.PaginatedListDTO;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;
import za.co.api.statement.entity.StatementEntity;
import za.co.api.statement.repository.StatementRepository;
import za.co.api.statement.test.data.StatementTestFixtures;
import za.co.api.statement.transformer.StatementTransformer;
import za.co.common.exception.DuplicateResourceException;
import za.co.common.exception.ResourceNotFoundException;
import za.co.common.exception.ValidationException;
import za.co.common.security.service.ClaimsService;

@Tag("statement")
@DisplayName("[SFD-100] Statement Service Tests")
@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @InjectMocks
    private StatementService statementService;

    @Mock
    private StatementRepository repository;

    @Mock
    private StatementTransformer transformer;

    @Mock
    private StatementEventService eventService;

    @Mock
    private DownloadLinkService downloadLinkService;

    @Mock
    private ClaimsService claimsService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(statementService, "blobStorageBaseUrl", "http://localhost:10000/devstoreaccount1");
    }

    // =====================================================
    // FIND TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-101] Find statement by ID returns DTO")
    void whenFind_withValidId_thenReturnsDto() {
        StatementEntity entity = StatementTestFixtures.validStatementEntity();
        StatementDTO dto = StatementTestFixtures.validStatementDto();

        when(repository.findById(eq(StatementTestFixtures.VALID_ID))).thenReturn(Optional.of(entity));
        when(transformer.toDTO(any(StatementEntity.class))).thenReturn(dto);

        StatementDTO result = statementService.find(StatementTestFixtures.VALID_ID, mockAuthentication);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(StatementTestFixtures.VALID_ID);
        verify(repository).findById(StatementTestFixtures.VALID_ID);
        verify(transformer).toDTO(entity);
    }

    @Test
    @DisplayName("[SFD-102] Find statement with non-existent ID throws ResourceNotFoundException")
    void whenFind_withNonExistentId_thenThrows() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.find(StatementTestFixtures.NON_EXISTENT_ID, mockAuthentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("[SFD-103] Find statement with null ID throws ValidationException")
    void whenFind_withNullId_thenThrowsValidation() {
        assertThatThrownBy(() -> statementService.find(null, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    // =====================================================
    // FIND ALL TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-104] FindAll returns paginated list")
    void whenFindAll_withValidParams_thenReturnsPaginatedList() {
        List<StatementEntity> entities = List.of(StatementTestFixtures.validStatementEntity());
        Page<StatementEntity> page = new PageImpl<>(entities);
        StatementDTO dto = StatementTestFixtures.validStatementDto();

        when(repository.findByFilters(anyString(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(transformer.toDTO(any(StatementEntity.class))).thenReturn(dto);

        PaginatedListDTO<StatementDTO> result = statementService.findAll(
                "CUST-001", null, null, null, null, 10, 0, mockAuthentication);

        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
    }

    @Test
    @DisplayName("[SFD-105] FindAll with null customerId throws ValidationException")
    void whenFindAll_withNullCustomerId_thenThrows() {
        assertThatThrownBy(() -> statementService.findAll(
                null, null, null, null, null, 10, 0, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-106] FindAll with invalid limit throws ValidationException")
    void whenFindAll_withInvalidLimit_thenThrows() {
        assertThatThrownBy(() -> statementService.findAll(
                "CUST-001", null, null, null, null, 0, 0, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    // =====================================================
    // DELETE TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-107] Delete statement soft-deletes and revokes links")
    void whenDelete_withValidId_thenSoftDeletesAndRevokesLinks() {
        StatementEntity entity = StatementTestFixtures.validStatementEntity();

        when(repository.findById(eq(StatementTestFixtures.VALID_ID))).thenReturn(Optional.of(entity));
        when(repository.save(any(StatementEntity.class))).thenReturn(entity);
        when(claimsService.getClientId(any(Authentication.class))).thenReturn(Optional.of("test-user"));

        statementService.delete(StatementTestFixtures.VALID_ID, mockAuthentication);

        assertThat(entity.getStatus()).isEqualTo(StatementStatusCode.DELETED);
        verify(repository).save(entity);
        verify(downloadLinkService).revokeAllActiveLinks(StatementTestFixtures.VALID_ID);
        verify(eventService).publishStatementDeletedEvent(StatementTestFixtures.VALID_ID);
    }

    @Test
    @DisplayName("[SFD-108] Delete with non-existent ID throws ResourceNotFoundException")
    void whenDelete_withNonExistentId_thenThrows() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.delete(StatementTestFixtures.NON_EXISTENT_ID, mockAuthentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("[SFD-109] Delete with null ID throws ValidationException")
    void whenDelete_withNullId_thenThrowsValidation() {
        assertThatThrownBy(() -> statementService.delete(null, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    // =====================================================
    // UPLOAD TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-110] Upload with valid data creates statement")
    void whenUpload_withValidData_thenCreatesStatement() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());
        StatementEntity savedEntity = StatementTestFixtures.validStatementEntity();
        StatementDTO resultDto = StatementTestFixtures.validStatementDto();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        when(repository.save(any(StatementEntity.class))).thenReturn(savedEntity);
        when(transformer.toDTO(any(StatementEntity.class))).thenReturn(resultDto);
        when(claimsService.getClientId(any(Authentication.class))).thenReturn(Optional.of("test-user"));

        StatementDTO result = statementService.uploadStatement(
                file, "CUST-001", LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication);

        assertThat(result).isNotNull();
        verify(repository).save(any(StatementEntity.class));
        verify(eventService).publishStatementUploadedEvent(any(StatementDTO.class));
    }

    @Test
    @DisplayName("[SFD-111] Upload with null file throws ValidationException")
    void whenUpload_withNullFile_thenThrowsValidation() {
        assertThatThrownBy(() -> statementService.uploadStatement(
                null, "CUST-001", LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-112] Upload with empty customerId throws ValidationException")
    void whenUpload_withEmptyCustomerId_thenThrowsValidation() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());

        assertThatThrownBy(() -> statementService.uploadStatement(
                file, "", LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    // =====================================================
    // DOWNLOAD FROM BLOB STORAGE TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-113] Download from blob storage returns bytes")
    void whenDownloadFromBlob_withValidPath_thenReturnsBytes() {
        byte[] pdfBytes = "PDF content".getBytes();
        ResponseEntity<byte[]> response = new ResponseEntity<>(pdfBytes, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(response);

        byte[] result = statementService.downloadFromBlobStorage("statements/test/file.pdf");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(pdfBytes);
    }

    // =====================================================
    // SANITIZATION & VALIDATION TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-114] Find with invalid UUID format throws ValidationException")
    void whenFind_withInvalidUuidFormat_thenThrowsValidation() {
        assertThatThrownBy(() -> statementService.find("not-a-uuid", mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-115] Delete with invalid UUID format throws ValidationException")
    void whenDelete_withInvalidUuidFormat_thenThrowsValidation() {
        assertThatThrownBy(() -> statementService.delete("not-a-uuid", mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-116] Upload with non-PDF file content throws ValidationException")
    void whenUpload_withNonPdfContent_thenThrowsValidation() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", "NOT-A-PDF-FILE".getBytes());

        assertThatThrownBy(() -> statementService.uploadStatement(
                file, "CUST-001", LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-117] Upload with path traversal in customerId sanitizes the blob path")
    void whenUpload_withPathTraversalCustomerId_thenSanitizesBlobPath() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());
        StatementEntity savedEntity = StatementTestFixtures.validStatementEntity();
        StatementDTO resultDto = StatementTestFixtures.validStatementDto();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        when(repository.save(any(StatementEntity.class))).thenReturn(savedEntity);
        when(transformer.toDTO(any(StatementEntity.class))).thenReturn(resultDto);
        when(claimsService.getClientId(any(Authentication.class))).thenReturn(Optional.of("test-user"));

        StatementDTO result = statementService.uploadStatement(
                file, "../../../etc", LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication);

        assertThat(result).isNotNull();
        // Verify the blob path was sanitized (no traversal characters in URL)
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));
        assertThat(urlCaptor.getValue()).doesNotContain("../").doesNotContain("..\\");
    }

    @Test
    @DisplayName("[SFD-118] Upload with excessively long customerId throws ValidationException")
    void whenUpload_withLongCustomerId_thenThrowsValidation() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());
        String longCustomerId = "A".repeat(51);

        assertThatThrownBy(() -> statementService.uploadStatement(
                file, longCustomerId, LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-119] Upload with excessively long accountNumber throws ValidationException")
    void whenUpload_withLongAccountNumber_thenThrowsValidation() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());
        String longAccountNumber = "1".repeat(21);

        assertThatThrownBy(() -> statementService.uploadStatement(
                file, "CUST-001", LocalDateTime.now(), StatementTypeCode.MONTHLY, longAccountNumber, mockAuthentication))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("[SFD-120] Upload with path traversal in filename sanitizes stored name")
    void whenUpload_withPathTraversalFilename_thenSanitizesStoredName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../etc/passwd", "application/pdf", "%PDF-1.4 test content".getBytes());
        StatementEntity savedEntity = StatementTestFixtures.validStatementEntity();
        StatementDTO resultDto = StatementTestFixtures.validStatementDto();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        when(repository.save(any(StatementEntity.class))).thenReturn(savedEntity);
        when(transformer.toDTO(any(StatementEntity.class))).thenReturn(resultDto);
        when(claimsService.getClientId(any(Authentication.class))).thenReturn(Optional.of("test-user"));

        statementService.uploadStatement(
                file, "CUST-001", LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication);

        // Verify the saved entity uses only the filename portion (no path traversal)
        verify(repository).save(argThat(entity ->
                !entity.getFileName().contains("..") && !entity.getFileName().contains("/")));
    }

    // =====================================================
    // IDEMPOTENCY TESTS
    // =====================================================

    @Test
    @DisplayName("[SFD-121] Upload duplicate content throws DuplicateResourceException")
    void whenUpload_withDuplicateContent_thenThrowsDuplicateResourceException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "%PDF-1.4 test content".getBytes());

        when(repository.existsByContentHashAndCustomerIdAndStatusNot(
                anyString(), eq("CUST-001"), eq(StatementStatusCode.DELETED))).thenReturn(true);

        assertThatThrownBy(() -> statementService.uploadStatement(
                file, "CUST-001", LocalDateTime.now(), StatementTypeCode.MONTHLY, "1234567890", mockAuthentication))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("identical content already exists");

        verify(repository, never()).save(any(StatementEntity.class));
    }

    @Test
    @DisplayName("[SFD-122] Delete already-deleted statement returns silently")
    void whenDelete_withAlreadyDeletedStatement_thenReturnsSilently() {
        StatementEntity deletedEntity = StatementTestFixtures.validStatementEntity();
        deletedEntity.setStatus(StatementStatusCode.DELETED);

        when(repository.findById(eq(StatementTestFixtures.VALID_ID))).thenReturn(Optional.of(deletedEntity));

        statementService.delete(StatementTestFixtures.VALID_ID, mockAuthentication);

        verify(repository, never()).save(any(StatementEntity.class));
        verify(downloadLinkService, never()).revokeAllActiveLinks(anyString());
        verify(eventService, never()).publishStatementDeletedEvent(anyString());
    }
}
