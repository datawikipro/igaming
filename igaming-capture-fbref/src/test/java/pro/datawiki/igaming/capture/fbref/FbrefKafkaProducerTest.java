package pro.datawiki.igaming.capture.fbref;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import pro.datawiki.igaming.capture.fbref.entity.FbrefRawReport;
import pro.datawiki.igaming.capture.fbref.entity.UnderstatRawShot;
import pro.datawiki.igaming.capture.fbref.kafka.FbrefKafkaProducer;
import pro.datawiki.igaming.capture.fbref.repository.FbrefRawReportRepository;
import pro.datawiki.igaming.capture.fbref.repository.UnderstatRawShotRepository;
import pro.datawiki.igaming.capture.fbref.service.FbrefIngestionService;
import pro.datawiki.igaming.dto.CanonicalMatchXgDto;
import pro.datawiki.igaming.dto.CanonicalShotMapDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FbrefKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private FbrefRawReportRepository rawReportRepository;

    @Mock
    private UnderstatRawShotRepository rawShotRepository;

    private FbrefKafkaProducer kafkaProducer;
    private FbrefIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        kafkaProducer = new FbrefKafkaProducer(kafkaTemplate);
        ReflectionTestUtils.setField(kafkaProducer, "xgTopic", "sports.canonical.xg");
        ReflectionTestUtils.setField(kafkaProducer, "shotsTopic", "sports.canonical.shots");
        ReflectionTestUtils.setField(kafkaProducer, "anomalyTopic", "mdm.anomaly.errors");

        ingestionService = new FbrefIngestionService(rawReportRepository, rawShotRepository, kafkaProducer);
    }

    @Test
    void shouldPublishCanonicalMatchXg() {
        CanonicalMatchXgDto dto = CanonicalMatchXgDto.builder()
                .matchId("fbref-match-12345")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .homeScore(2)
                .awayScore(1)
                .homeXg(2.45)
                .awayXg(0.85)
                .homeXag(1.90)
                .awayXag(0.60)
                .homeSca(28)
                .awaySca(12)
                .homeGca(4)
                .awayGca(1)
                .homeNpxg(2.45)
                .awayNpxg(0.85)
                .build();

        RecordMetadata metadata = new RecordMetadata(new TopicPartition("sports.canonical.xg", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq("sports.canonical.xg"), eq("fbref-match-12345"), any(CanonicalMatchXgDto.class)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        when(rawReportRepository.save(any(FbrefRawReport.class))).thenAnswer(invocation -> {
            FbrefRawReport r = invocation.getArgument(0);
            r.setId(101L);
            return r;
        });

        FbrefRawReport savedReport = ingestionService.processAndPublishXgReport(dto);

        assertThat(savedReport).isNotNull();
        assertThat(savedReport.getMatchId()).isEqualTo("fbref-match-12345");
        assertThat(savedReport.getHomeXg()).isEqualTo(2.45);
        verify(kafkaTemplate).send(eq("sports.canonical.xg"), eq("fbref-match-12345"), eq(dto));
    }

    @Test
    void shouldPublishCanonicalShotMap() {
        CanonicalShotMapDto dto = CanonicalShotMapDto.builder()
                .matchId("understat-match-999")
                .homeTeam("Liverpool")
                .awayTeam("Everton")
                .shots(List.of(
                        CanonicalShotMapDto.CanonicalShotDetailDto.builder()
                                .shotId("shot-01")
                                .minute(23)
                                .result("Goal")
                                .x(0.88)
                                .y(0.52)
                                .xg(0.42)
                                .player("Mohamed Salah")
                                .teamSide("h")
                                .situation("OpenPlay")
                                .shotType("LeftFoot")
                                .build()
                ))
                .build();

        RecordMetadata metadata = new RecordMetadata(new TopicPartition("sports.canonical.shots", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq("sports.canonical.shots"), eq("understat-match-999"), any(CanonicalShotMapDto.class)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        when(rawShotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<UnderstatRawShot> savedShots = ingestionService.processAndPublishShotMap(dto);

        assertThat(savedShots).hasSize(1);
        assertThat(savedShots.get(0).getPlayer()).isEqualTo("Mohamed Salah");
        verify(kafkaTemplate).send(eq("sports.canonical.shots"), eq("understat-match-999"), eq(dto));
    }
}
