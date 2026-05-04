package za.co.api.statement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import za.co.common.api.paging.PaginatedListDTO;
import za.co.api.statement.dto.CreateDownloadLinkRequestDTO;
import za.co.api.statement.dto.DownloadLinkDTO;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;
import za.co.api.statement.entity.StatementEntity;
import za.co.api.statement.service.DownloadLinkService;
import za.co.api.statement.service.StatementService;
import org.springframework.http.ProblemDetail;

/**
 * REST controller for managing statements and download links.
 *
 * <p>Provides endpoints for uploading statements, generating secure download links,
 * downloading PDFs via token-based authentication, and managing statement lifecycle.</p>
 *
 * <p>The download endpoint ({@code /download/{token}}) uses token-based authentication
 * (HMAC-signed tokens) instead of JWT, so it does not require Bearer token authentication.</p>
 */
@RestController
@RequestMapping("/api/v1/statements")
@Tag(name = "statements", description = "Operations related to statements and download links")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class StatementController {

    private final StatementService statementService;
    private final DownloadLinkService downloadLinkService;

    public StatementController(StatementService statementService, DownloadLinkService downloadLinkService) {
        this.statementService = statementService;
        this.downloadLinkService = downloadLinkService;
    }

    @Operation(
        summary = "Upload statement",
        description = "Uploads a new customer statement PDF. Accepts multipart form data with PDF file and metadata. "
                + "Stores the file in Azure Blob Storage and creates statement metadata record. "
                + "Requires 'statement:create' scope."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Statement uploaded successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatementDTO.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid file or metadata",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('common-api.admin') or hasAuthority('SCOPE_statement:create')")
    public ResponseEntity<StatementDTO> uploadStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam("customerId") String customerId,
            @RequestParam("statementDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime statementDate,
            @RequestParam("statementType") StatementTypeCode statementType,
            @RequestParam("accountNumber") String accountNumber,
            Authentication authentication) {
        StatementDTO result = statementService.uploadStatement(
                file, customerId, statementDate, statementType, accountNumber, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
        summary = "Get statement by ID",
        description = "Retrieves statement metadata by ID. Account number is masked. Requires 'statement:read' scope."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatementDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Statement not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('common-api.admin') or hasAuthority('SCOPE_statement:read')")
    public ResponseEntity<StatementDTO> find(@PathVariable String id, Authentication authentication) {
        StatementDTO statement = statementService.find(id, authentication);
        return ResponseEntity.ok(statement);
    }

    @Operation(
        summary = "List statements",
        description = "Lists statement metadata for a customer with filtering and pagination. "
                + "Excludes deleted statements. Requires 'statement:read' scope.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "customerId", description = "Customer identifier (required)",
                required = true, in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
            @io.swagger.v3.oas.annotations.Parameter(name = "statementType", description = "Filter by statement type",
                in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
            @io.swagger.v3.oas.annotations.Parameter(name = "status", description = "Filter by status",
                in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
            @io.swagger.v3.oas.annotations.Parameter(name = "fromDate", description = "Filter from date (inclusive, ISO 8601)",
                in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
            @io.swagger.v3.oas.annotations.Parameter(name = "toDate", description = "Filter to date (inclusive, ISO 8601)",
                in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
            @io.swagger.v3.oas.annotations.Parameter(name = "limit", description = "Maximum results (default 10, max 100)",
                in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY,
                schema = @Schema(type = "integer", defaultValue = "10")),
            @io.swagger.v3.oas.annotations.Parameter(name = "offset", description = "Pagination offset",
                in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY,
                schema = @Schema(type = "integer", defaultValue = "0"))
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedListDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Invalid parameters",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('common-api.admin') or hasAuthority('SCOPE_statement:read')")
    public ResponseEntity<PaginatedListDTO<StatementDTO>> findAll(
            @RequestParam String customerId,
            @RequestParam(required = false) StatementTypeCode statementType,
            @RequestParam(required = false) StatementStatusCode status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication) {
        PaginatedListDTO<StatementDTO> result = statementService.findAll(
                customerId, statementType, status, fromDate, toDate, limit, offset, authentication);
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Generate download link",
        description = "Generates a secure, time-limited download link for a statement. "
                + "Returns a signed token URL that can be shared with the customer. "
                + "Requires 'statement:create' scope."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Download link generated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DownloadLinkDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Statement not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation error - Statement not available",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{id}/download-links")
    @PreAuthorize("hasRole('common-api.admin') or hasAuthority('SCOPE_statement:create')")
    public ResponseEntity<DownloadLinkDTO> generateDownloadLink(
            @PathVariable String id,
            @Valid @RequestBody(required = false) CreateDownloadLinkRequestDTO request,
            Authentication authentication) {
        DownloadLinkDTO result = downloadLinkService.generateDownloadLink(id, request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
        summary = "Download statement PDF",
        description = "Downloads a statement PDF using a secure token. No JWT authentication required — "
                + "the signed token provides authorization. Streams the PDF directly from blob storage.",
        security = {}  // No security requirement for this endpoint
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF file stream",
            content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "401", description = "Invalid or tampered token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Statement not found or deleted",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "410", description = "Token expired or download limit reached",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/download/{token}")
    public ResponseEntity<byte[]> downloadStatement(
            @PathVariable String token,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        StatementEntity statement = downloadLinkService.validateAndRecordDownload(token, clientIp);

        byte[] pdfBytes = statementService.downloadFromBlobStorage(statement.getBlobPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", statement.getFileName());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @Operation(
        summary = "Delete statement",
        description = "Soft-deletes a statement (sets status to DELETED, revokes all active download links). "
                + "Blob file is retained for compliance. Requires 'statement:delete' scope."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Statement not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('common-api.admin') or hasAuthority('SCOPE_statement:delete')")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication authentication) {
        statementService.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
