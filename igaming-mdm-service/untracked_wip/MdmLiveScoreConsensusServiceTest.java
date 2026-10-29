package pro.datawiki.igaming.mdm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MDM Live Score Consensus & Batch Flusher Tests")
class MdmLiveScoreConsensusServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MdmMatchRepository matchRepository;

    private ObjectMapper objectMapper;
    private MdmLiveScoreConsensusService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        service = new MdmLiveScoreConsensusService(redisTemplate, jdbcTemplate, matchRepository, objectMapper);
    }

    @Test
    @DisplayName("Should enforce monotonic elapsed time: never roll clock backward")
    void shouldEnforceMonotonicElapsedTime() throws Exception {
        Long matchId = 100L;
        String redisKey = "live:match:100";

        // Existing state in Redis: Bookmaker A reported 15:00 (900 seconds)
        CanonicalLiveScoreDto existing = CanonicalLiveScoreDto.builder()
                .matchId(matchId)
                .sourceCode("fon-bet-ru")
                .status(MatchStatus.LIVE)
                .score1(1)
                .score2(0)
                .elapsedSeconds(900)
                .build();
        when(valueOperations.get(redisKey)).thenReturn(objectMapper.writeValueAsString(existing));

        // Incoming state from slower Bookmaker B: 14:55 (895 seconds)
        CanonicalLiveScoreDto incoming = CanonicalLiveScoreDto.builder()
                .matchId(matchId)
                .sourceCode("winline")
                .status(MatchStatus.LIVE)
                .score1(1)
                .score2(0)
                .elapsedSeconds(895)
                .build();

        CanonicalLiveScoreDto result = service.ingestLiveScore(incoming);

        // Effective seconds must retain the maximum (900)
        assertThat(result.getElapsedSeconds()).isEqualTo(900);

        // Verify Redis was updated with 900
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(redisKey), jsonCaptor.capture(), any(Duration.class));
        CanonicalLiveScoreDto saved = objectMapper.readValue(jsonCaptor.getValue(), CanonicalLiveScoreDto.class);
        assertThat(saved.getElapsedSeconds()).isEqualTo(900);

        // Verify match marked dirty
        verify(setOperations).add("live:dirty_mdm_matches", "100");
    }

    @Test
    @DisplayName("Should preserve FINISHED terminal status against stale LIVE ticks")
    void shouldPreserveFinishedTerminalStatus() throws Exception {
        Long matchId = 200L;
        String redisKey = "live:match:200";

        // Existing state: finished
        CanonicalLiveScoreDto finished = CanonicalLiveScoreDto.builder()
                .matchId(matchId)
                .status(MatchStatus.FINISHED)
                .score1(2)
                .score2(1)
                .elapsedSeconds(5400)
                .build();
        when(valueOperations.get(redisKey)).thenReturn(objectMapper.writeValueAsString(finished));

        // Stale incoming tick still marked LIVE
        CanonicalLiveScoreDto stale = CanonicalLiveScoreDto.builder()
                .matchId(matchId)
                .status(MatchStatus.LIVE)
                .score1(2)
                .score2(1)
                .elapsedSeconds(5390)
                .build();

        CanonicalLiveScoreDto result = service.ingestLiveScore(stale);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("Cache-Aside: should return from Redis on cache hit")
    void shouldReturnFromRedisOnHit() throws Exception {
        Long matchId = 300L;
        CanonicalLiveScoreDto cached = CanonicalLiveScoreDto.builder()
                .matchId(matchId)
                .status(MatchStatus.LIVE)
                .score1(0)
                .score2(0)
                .build();
        when(valueOperations.get("live:match:300")).thenReturn(objectMapper.writeValueAsString(cached));

        CanonicalLiveScoreDto result = service.getLiveScore(matchId);
        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(300L);

        // Should NOT hit database
        verifyNoInteractions(matchRepository);
    }

    @Test
    @DisplayName("Cache-Aside: should load from mdm_match, populate Redis and return on cache miss")
    void shouldLoadFromMdmMatchOnCacheMiss() {
        Long matchId = 400L;
        when(valueOperations.get("live:match:400")).thenReturn(null);

        MdmMatch mdmMatch = MdmMatch.builder()
                .id(matchId)
                .sourceOrigin("SOFASCORE")
                .status("LIVE")
                .score1("3")
                .score2("1")
                .build();
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(mdmMatch));

        CanonicalLiveScoreDto result = service.getLiveScore(matchId);
        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(400L);
        assertThat(result.getScore1()).isEqualTo(3);
        assertThat(result.getScore2()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.LIVE);

        // Verify populated in Redis with 48h TTL
        verify(valueOperations).set(eq("live:match:400"), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Batch Cache-Aside: should multiGet from Redis and fallback to mdm_match in batch for missing")
    void shouldMultiGetAndFallbackToBatchDbForMissing() throws Exception {
        Long id1 = 501L; // In Redis
        Long id2 = 502L; // Missing from Redis

        CanonicalLiveScoreDto live1 = CanonicalLiveScoreDto.builder()
                .matchId(id1)
                .status(MatchStatus.LIVE)
                .score1(1)
                .score2(0)
                .build();

        when(valueOperations.multiGet(List.of("live:match:501", "live:match:502")))
                .thenReturn(List.of(objectMapper.writeValueAsString(live1), ""));

        MdmMatch dbMatch = MdmMatch.builder()
                .id(id2)
                .sourceOrigin("FONBET")
                .status("LIVE")
                .score1("2")
                .score2("2")
                .build();
        when(matchRepository.findAllById(anyCollection())).thenReturn(List.of(dbMatch));

        Map<Long, CanonicalLiveScoreDto> result = service.getLiveScores(List.of(id1, id2));

        assertThat(result).hasSize(2);
        assertThat(result.get(id1).getScore1()).isEqualTo(1);
        assertThat(result.get(id2).getScore1()).isEqualTo(2);
        assertThat(result.get(id2).getScore2()).isEqualTo(2);

        // Verify batch population to Redis for missing match
        verify(valueOperations).multiSet(anyMap());
    }

    @Test
    @DisplayName("Batch Flusher: should execute single batchUpdate into mdm_match for all dirty matches")
    void shouldFlushDirtyMatchesExclusivelyViaBatchUpdate() throws Exception {
        Set<String> dirtyIds = Set.of("10", "20");
        when(setOperations.members("live:dirty_mdm_matches")).thenReturn(dirtyIds);

        CanonicalLiveScoreDto m1 = CanonicalLiveScoreDto.builder()
                .matchId(10L)
                .status(MatchStatus.LIVE)
                .score1(1)
                .score2(1)
                .build();
        CanonicalLiveScoreDto m2 = CanonicalLiveScoreDto.builder()
                .matchId(20L)
                .status(MatchStatus.FINISHED)
                .score1(2)
                .score2(0)
                .build();

        when(valueOperations.multiGet(anyList())).thenReturn(List.of(
                objectMapper.writeValueAsString(m1),
                objectMapper.writeValueAsString(m2)
        ));

        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[]{1, 1});

        service.flushDirtyMatchesBatch();

        // Verify jdbcTemplate.batchUpdate was called with exact SQL for mdm_match
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object[]>> batchCaptor = ArgumentCaptor.forClass(List.class);

        verify(jdbcTemplate, times(1)).batchUpdate(sqlCaptor.capture(), batchCaptor.capture());
        assertThat(sqlCaptor.getValue()).contains("UPDATE mdm_match SET score1 = ?, score2 = ?, status = ?, updated_at = NOW() WHERE id = ?");
        assertThat(batchCaptor.getValue()).hasSize(2);

        // Verify dirty set cleanup
        verify(setOperations).remove(eq("live:dirty_mdm_matches"), any(Object[].class));
    }
}
