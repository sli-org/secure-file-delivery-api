package za.co.api.statement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.dto.StatementEventDTO;
import za.co.api.statement.dto.code.StatementEventTypeCode;
import za.co.api.statement.test.data.StatementTestFixtures;

@Tag("statement-events")
@ExtendWith(MockitoExtension.class)
@DisplayName("[SFD-EV] Statement Event Service Tests")
class StatementEventServiceTest {

    @Mock
    private RabbitTemplate commonRabbitTemplate;

    @InjectMocks
    private StatementEventService statementEventService;

    private StatementDTO testDto;

    @BeforeEach
    void setUp() {
        testDto = StatementTestFixtures.validStatementDto();

        ReflectionTestUtils.setField(statementEventService, "exchangeName", "statement_event_exchange");
        ReflectionTestUtils.setField(statementEventService, "uploadedRoutingKey", "action.common.statement.uploaded");
        ReflectionTestUtils.setField(statementEventService, "downloadLinkCreatedRoutingKey", "action.common.downloadlink.created");
        ReflectionTestUtils.setField(statementEventService, "deletedRoutingKey", "action.common.statement.deleted");
    }

    @Test
    @DisplayName("[SFD-EV-001] Publish uploaded event uses correct exchange and routing key")
    void whenPublishUploadedEvent_thenUsesCorrectExchangeAndRoutingKey() {
        statementEventService.publishStatementUploadedEvent(testDto);

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(commonRabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                any(StatementEventDTO.class));

        assertEquals("statement_event_exchange", exchangeCaptor.getValue());
        assertEquals("action.common.statement.uploaded", routingKeyCaptor.getValue());
    }

    @Test
    @DisplayName("[SFD-EV-002] Uploaded event contains CREATED event type")
    void whenPublishUploadedEvent_thenEventTypeIsCreated() {
        statementEventService.publishStatementUploadedEvent(testDto);

        ArgumentCaptor<StatementEventDTO> eventCaptor = ArgumentCaptor.forClass(StatementEventDTO.class);
        verify(commonRabbitTemplate).convertAndSend(anyString(), anyString(), eventCaptor.capture());

        assertEquals(StatementEventTypeCode.CREATED, eventCaptor.getValue().getEventType());
    }

    @Test
    @DisplayName("[SFD-EV-003] Uploaded event contains timestamp")
    void whenPublishUploadedEvent_thenEventHasTimestamp() {
        statementEventService.publishStatementUploadedEvent(testDto);

        ArgumentCaptor<StatementEventDTO> eventCaptor = ArgumentCaptor.forClass(StatementEventDTO.class);
        verify(commonRabbitTemplate).convertAndSend(anyString(), anyString(), eventCaptor.capture());

        assertNotNull(eventCaptor.getValue().getTimestamp());
    }

    @Test
    @DisplayName("[SFD-EV-004] Uploaded event contains DTO data")
    void whenPublishUploadedEvent_thenEventContainsData() {
        statementEventService.publishStatementUploadedEvent(testDto);

        ArgumentCaptor<StatementEventDTO> eventCaptor = ArgumentCaptor.forClass(StatementEventDTO.class);
        verify(commonRabbitTemplate).convertAndSend(anyString(), anyString(), eventCaptor.capture());

        assertNotNull(eventCaptor.getValue().getData());
        assertThat(eventCaptor.getValue().getId()).isEqualTo(testDto.getId());
    }

    @Test
    @DisplayName("[SFD-EV-005] Download link created event uses correct routing key")
    void whenPublishDownloadLinkCreatedEvent_thenUsesCorrectRoutingKey() {
        statementEventService.publishDownloadLinkCreatedEvent(testDto);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(commonRabbitTemplate).convertAndSend(anyString(), routingKeyCaptor.capture(), any(StatementEventDTO.class));

        assertEquals("action.common.downloadlink.created", routingKeyCaptor.getValue());
    }

    @Test
    @DisplayName("[SFD-EV-006] Download link created event has UPDATED event type")
    void whenPublishDownloadLinkCreatedEvent_thenEventTypeIsUpdated() {
        statementEventService.publishDownloadLinkCreatedEvent(testDto);

        ArgumentCaptor<StatementEventDTO> eventCaptor = ArgumentCaptor.forClass(StatementEventDTO.class);
        verify(commonRabbitTemplate).convertAndSend(anyString(), anyString(), eventCaptor.capture());

        assertEquals(StatementEventTypeCode.UPDATED, eventCaptor.getValue().getEventType());
    }

    @Test
    @DisplayName("[SFD-EV-007] Deleted event uses correct routing key")
    void whenPublishDeletedEvent_thenUsesCorrectRoutingKey() {
        statementEventService.publishStatementDeletedEvent(StatementTestFixtures.VALID_ID);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(commonRabbitTemplate).convertAndSend(anyString(), routingKeyCaptor.capture(), any(StatementEventDTO.class));

        assertEquals("action.common.statement.deleted", routingKeyCaptor.getValue());
    }

    @Test
    @DisplayName("[SFD-EV-008] Deleted event has DELETED event type")
    void whenPublishDeletedEvent_thenEventTypeIsDeleted() {
        statementEventService.publishStatementDeletedEvent(StatementTestFixtures.VALID_ID);

        ArgumentCaptor<StatementEventDTO> eventCaptor = ArgumentCaptor.forClass(StatementEventDTO.class);
        verify(commonRabbitTemplate).convertAndSend(anyString(), anyString(), eventCaptor.capture());

        assertEquals(StatementEventTypeCode.DELETED, eventCaptor.getValue().getEventType());
    }

    @Test
    @DisplayName("[SFD-EV-009] Uploaded event with null DTO throws IllegalArgumentException")
    void whenPublishUploadedEvent_withNullDto_thenThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> statementEventService.publishStatementUploadedEvent(null));
    }

    @Test
    @DisplayName("[SFD-EV-010] Deleted event with null ID throws IllegalArgumentException")
    void whenPublishDeletedEvent_withNullId_thenThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> statementEventService.publishStatementDeletedEvent(null));
    }

    @Test
    @DisplayName("[SFD-EV-011] Deleted event with empty ID throws IllegalArgumentException")
    void whenPublishDeletedEvent_withEmptyId_thenThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> statementEventService.publishStatementDeletedEvent(""));
    }
}
