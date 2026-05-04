package za.co.api.statement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import za.co.api.statement.service.DownloadLinkService;
import za.co.api.statement.service.StatementService;
import za.co.api.statement.test.base.BaseControllerTest;
import za.co.api.statement.test.data.StatementTestFixtures;
import za.co.common.exception.ResourceNotFoundException;
import za.co.common.exception.ValidationException;

@Tag("statement-controller")
@DisplayName("[SFD-350] Statement Controller Negative Tests")
class StatementControllerNegativeTest extends BaseControllerTest {

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

    @Test
    @DisplayName("[SFD-351] GET /{id} returns 404 when not found")
    void whenFind_withNonExistentId_thenReturns404() throws Exception {
        when(statementService.find(anyString(), any(Authentication.class)))
                .thenThrow(new ResourceNotFoundException("Statement not found.", null));

        mockMvc.perform(get("/api/v1/statements/{id}", StatementTestFixtures.NON_EXISTENT_ID)
                        .principal(mockAuthentication))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[SFD-352] DELETE /{id} returns 404 when not found")
    void whenDelete_withNonExistentId_thenReturns404() throws Exception {
        // The delete method throws ResourceNotFoundException
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Statement not found.", null))
                .when(statementService).delete(anyString(), any(Authentication.class));

        mockMvc.perform(delete("/api/v1/statements/{id}", StatementTestFixtures.NON_EXISTENT_ID)
                        .principal(mockAuthentication))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[SFD-353] GET /download/{token} returns error for invalid token")
    void whenDownload_withInvalidToken_thenReturnsError() throws Exception {
        when(downloadLinkService.validateAndRecordDownload(eq("invalid-token"), anyString()))
                .thenThrow(new ResourceNotFoundException("Download link not found.", null));

        mockMvc.perform(get("/api/v1/statements/download/{token}", "invalid-token"))
                .andExpect(status().isNotFound());
    }
}
