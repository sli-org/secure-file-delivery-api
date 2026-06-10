package za.co.api.statement.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Dedicated service for Azure Blob Storage operations with resilience patterns.
 * Wraps operations with circuit breaker, retry, and rate limiter.
 * Provides streaming downloads to avoid loading entire PDFs into memory.
 * Supports byte-range requests for download resume.
 */
@Service
@Slf4j
public class BlobStorageService {

    private static final int BLOB_PATH_PARTS = 2;

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    private BlobServiceClient blobServiceClient;

    @PostConstruct
    void init() {
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("Azure Storage connection string not configured");
            return;
        }
        try {
            blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            log.info("Azure Blob Storage client initialised");
        } catch (Exception e) {
            log.error("Failed to initialise Azure Blob Storage: {}", e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = "blobStorage", fallbackMethod = "uploadFallback")
    @Retry(name = "blobStorage")
    @RateLimiter(name = "blobStorage")
    public void upload(String blobPath, byte[] data, boolean overwrite) {
        String[] parts = parseBlobPath(blobPath);
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(parts[0]);
        container.createIfNotExists();
        container.getBlobClient(parts[1])
                .upload(new ByteArrayInputStream(data), data.length, overwrite);
        log.info("Uploaded blob: {}", blobPath);
    }

    @CircuitBreaker(name = "blobStorage", fallbackMethod = "streamFallback")
    @Retry(name = "blobStorage")
    @RateLimiter(name = "blobStorage")
    public InputStream downloadStream(String blobPath) {
        String[] parts = parseBlobPath(blobPath);
        return blobServiceClient
                .getBlobContainerClient(parts[0])
                .getBlobClient(parts[1])
                .openInputStream();
    }

    @CircuitBreaker(name = "blobStorage", fallbackMethod = "streamFallback")
    @Retry(name = "blobStorage")
    @RateLimiter(name = "blobStorage")
    public InputStream downloadStreamRange(String blobPath, long startOffset) {
        String[] parts = parseBlobPath(blobPath);
        BlobRange range = new BlobRange(startOffset);
        DownloadRetryOptions retryOpts = new DownloadRetryOptions().setMaxRetryRequests(3);
        return blobServiceClient
                .getBlobContainerClient(parts[0])
                .getBlobClient(parts[1])
                .openInputStream(range, retryOpts, null);
    }

    @CircuitBreaker(name = "blobStorage", fallbackMethod = "sizeFallback")
    @Retry(name = "blobStorage")
    public long getBlobSize(String blobPath) {
        String[] parts = parseBlobPath(blobPath);
        return blobServiceClient
                .getBlobContainerClient(parts[0])
                .getBlobClient(parts[1])
                .getProperties().getBlobDownloadStreamProperties().getContentLength();
    }

    private void uploadFallback(String blobPath, byte[] data, boolean overwrite, Exception ex) {
        log.error("Circuit breaker OPEN for upload: {} - {}", blobPath, ex.getMessage());
        throw new za.co.common.exception.ServiceException(
                "Blob storage temporarily unavailable. Please retry.", "BLOB_CIRCUIT_OPEN");
    }

    private InputStream streamFallback(String blobPath, Exception ex) {
        log.error("Circuit breaker OPEN: {} - {}", blobPath, ex.getMessage());
        throw new za.co.common.exception.ServiceException(
                "Blob storage temporarily unavailable. Please retry.", "BLOB_CIRCUIT_OPEN");
    }

    private long sizeFallback(String blobPath, Exception ex) {
        log.error("Circuit breaker OPEN for size: {} - {}", blobPath, ex.getMessage());
        throw new za.co.common.exception.ServiceException(
                "Blob storage temporarily unavailable. Please retry.", "BLOB_CIRCUIT_OPEN");
    }

    private String[] parseBlobPath(String blobPath) {
        String[] parts = blobPath.split("/", BLOB_PATH_PARTS);
        if (parts.length < BLOB_PATH_PARTS) {
            throw new IllegalArgumentException("Invalid blob path: " + blobPath);
        }
        return parts;
    }
}