package za.co.api.statement.test.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestAzureConfig {

    @Bean
    @Primary
    public BlobContainerClient blobContainerClient() {
        BlobContainerClient mockClient = mock(BlobContainerClient.class);
        BlobContainerClientBuilder mockBuilder = mock(BlobContainerClientBuilder.class);
        
        when(mockBuilder.connectionString(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.containerName(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.buildClient()).thenReturn(mockClient);
        
        return mockClient;
    }
}