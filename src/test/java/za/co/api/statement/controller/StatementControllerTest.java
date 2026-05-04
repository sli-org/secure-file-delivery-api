package za.co.api.statement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import za.co.common.api.paging.PaginatedListDTO;
import za.co.common.api.paging.PagingDTO;
import za.co.api.statement.dto.CreateDownloadLinkRequestDTO;
import za.co.api.statement.dto.DownloadLinkDTO;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;
import za.co.api.statement.entity.StatementEntity;
import za.co.api.statement.service.DownloadLinkService;
import za.co.api.statement.service.StatementService;
import za.co.api.statement.test.base.BaseControllerTest;
import za.co.api.statement.test.data.StatementTestFixtures;

@Tag("statement-controller")
@DisplayName("[SFD-300] Statement Controller Tests")
class StatementControllerTest extends BaseControllerTest {

    @InjectMocks
    private StatementController controller;

    @Mock
    private StatementService statementService;

    @Mock
    private DownloadLinkService downloadLinkService;

    @Mock
    private Authentication mockAuthentication;

    @Override
    protected Object getController() {
        return controller;
    }

    // =====================================================
    // FIND BY ID
    // =====================================================

    @Test
    @DisplayName("[SFD-301] GET /{id} returns 200 with statement DTO")
    void whenFind_thenReturns200() throws Exception {
        StatementDTO dto = StatementTestFixtures.validStatementDto();
        when(statementService.find(eq(StatementTestFixtures.VALID_ID), any(Authentication.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/statements/{id}", StatementTestFixtures.VALID_ID)
                        .principal(mockAuthentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(StatementTestFixtures.VALID_ID))
                .andExpect(jsonPath("$.customerId").value(StatementTestFixtures.CUSTOMER_ID));
    }

    // =====================================================
    // LIST STATEMENTS
    // =====================================================

    @Test
    @DisplayName("[SFD-302] GET / returns 200 with paginated list")
    void whenFindAll_thenReturns200() throws Exception {
        List<StatementDTO> dtos = List.of(StatementTestFixtures.validStatementDto());
        PagingDTO paging = new PagingDTO(10, 0, 1L);
        PaginatedListDTO<StatementDTO> result = new PaginatedListDTO<>(dtos, paging);

        when(statementService.findAll(anyString(), any(), any(), any(), any(), anyInt(), anyInt(), any(Authentication.class)))
                .thenReturn(result);

        mockMvc.perform(get("/api/v1/statements")
                        .param("customerId", "CUST-001")
                        .principal(mockAuthentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list[0].id").value(StatementTestFixtures.VALID_ID));
    }

    // =====================================================
    // UPLOAD STATEMENT (multipart)
    // =====================================================

    @Test
    @DisplayName("[SFD-303] POST / (multipart) returns 201 with created statement")
    void whenUpload_thenReturns201() throws Exception {
        StatementDTO dto = StatementTestFixtures.validStatementDto();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "PDF content".getBytes());

        when(statementService.uploadStatement(
                any(), anyString(), any(LocalDateTime.class), any(StatementTypeCode.class), anyString(), any(Authentication.class)))
                .thenReturn(dto);

        mockMvc.perform(multipart("/api/v1/statements")
                        .file(file)
                        .param("customerId", "CUST-001")
                        .param("statementDate", "2024-01-31T00:00:00")
                        .param("statementType", "MONTHLY")
                        .param("accountNumber", "1234567890")
                        .principal(mockAuthentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(StatementTestFixtures.VALID_ID));
    }

    // =====================================================
    // GENERATE DOWNLOAD LINK
    // =====================================================

    @Test
    @DisplayName("[SFD-304] POST /{id}/download-links returns 201 with download link")
    void whenGenerateDownloadLink_thenReturns201() throws Exception {
        DownloadLinkDTO linkDto = StatementTestFixtures.validDownloadLinkDto();

        when(downloadLinkService.generateDownloadLink(eq(StatementTestFixtures.VALID_ID), any(), any(Authentication.class)))
                .thenReturn(linkDto);

        CreateDownloadLinkRequestDTO request = StatementTestFixtures.validCreateDownloadLinkRequest();

        mockMvc.perform(post("/api/v1/statements/{id}/download-links", StatementTestFixtures.VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuthentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statementId").value(StatementTestFixtures.VALID_ID));
    }

    // =====================================================
    // DOWNLOAD (token-based, no auth)
    // =====================================================

    @Test
    @DisplayName("[SFD-305] GET /download/{token} returns 200 with PDF bytes")
    void whenDownload_thenReturns200WithPdf() throws Exception {
        StatementEntity entity = StatementTestFixtures.validStatementEntity();
        byte[] pdfBytes = "PDF content".getBytes();

        when(downloadLinkService.validateAndRecordDownload(eq("test-token"), anyString())).thenReturn(entity);
        when(statementService.downloadFromBlobStorage(anyString())).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/v1/statements/download/{token}", "test-token"))
                .andExpect(status().isOk());
    }

    // =====================================================
    // DELETE
    // =====================================================

    @Test
    @DisplayName("[SFD-306] DELETE /{id} returns 204")
    void whenDelete_thenReturns204() throws Exception {
        doNothing().when(statementService).delete(eq(StatementTestFixtures.VALID_ID), any(Authentication.class));

        mockMvc.perform(delete("/api/v1/statements/{id}", StatementTestFixtures.VALID_ID)
                        .principal(mockAuthentication))
                .andExpect(status().isNoContent());

        verify(statementService).delete(eq(StatementTestFixtures.VALID_ID), any(Authentication.class));
    }
}
