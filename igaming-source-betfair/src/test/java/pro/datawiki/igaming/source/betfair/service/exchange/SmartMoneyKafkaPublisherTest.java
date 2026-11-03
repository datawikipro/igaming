package pro.datawiki.igaming.source.betfair.service.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.dto.exchange.SmartMoneyAlert;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SmartMoneyKafkaPublisherTest {

    private BetfairConfig config;
    private ObjectMapper objectMapper;
    private KafkaTemplate<String, String> kafkaTemplate;
    private SmartMoneyKafkaPublisher publisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        config = new BetfairConfig();
        config.getSmartMoney().setEnabled(true);
        config.getSmartMoney().setTopic("odds.smart-money");

        objectMapper = new ObjectMapper();
        kafkaTemplate = Mockito.mock(KafkaTemplate.class);

        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher = new SmartMoneyKafkaPublisher(config, objectMapper, kafkaTemplate);
    }

    @Test
    void testPublishAlert_Success() {
        SmartMoneyAlert alert = SmartMoneyAlert.builder()
                .alertId("sma-123456")
                .bookmaker("betfair")
                .eventId("bf-001")
                .matchName("Arsenal vs Chelsea")
                .runnerName("Arsenal")
                .selectionId("12345")
                .volumeDelta(15000.0)
                .volumeGrowthPercent(45.0)
                .severity("INSTITUTIONAL_LOAD")
                .direction("BACK_STEAM")
                .build();

        boolean result = publisher.publishAlert(alert);
        assertTrue(result);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals("odds.smart-money", topicCaptor.getValue());
        assertEquals("bf-001", keyCaptor.getValue());
        assertTrue(payloadCaptor.getValue().contains("\"alertId\":\"sma-123456\""));
        assertTrue(payloadCaptor.getValue().contains("\"direction\":\"BACK_STEAM\""));
        assertTrue(payloadCaptor.getValue().contains("\"volumeDelta\":15000.0"));
    }

    @Test
    void testPublishAlert_WhenDisabled_ReturnsFalse() {
        config.getSmartMoney().setEnabled(false);

        SmartMoneyAlert alert = SmartMoneyAlert.builder()
                .alertId("sma-999")
                .eventId("bf-002")
                .build();

        boolean result = publisher.publishAlert(alert);
        assertFalse(result);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void testPublishAlert_NullAlert_ReturnsFalse() {
        boolean result = publisher.publishAlert(null);
        assertFalse(result);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void testPublishAlert_StandaloneModeWithoutKafka_ReturnsTrue() {
        SmartMoneyKafkaPublisher standalonePublisher = new SmartMoneyKafkaPublisher(config, objectMapper, null);

        SmartMoneyAlert alert = SmartMoneyAlert.builder()
                .alertId("sma-standalone")
                .eventId("bf-003")
                .description("Standalone test alert")
                .build();

        boolean result = standalonePublisher.publishAlert(alert);
        assertTrue(result);
    }
}
