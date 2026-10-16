package pro.datawiki.igaming.capture.sofascore.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SofaQueueServiceTest {

    private StringRedisTemplate redisTemplate;
    private ListOperations<String, String> listOps;
    private SetOperations<String, String> setOps;
    private ObjectMapper objectMapper;
    private SofaQueueService queueService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        listOps = mock(ListOperations.class);
        setOps = mock(SetOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        objectMapper = new ObjectMapper();
        queueService = new SofaQueueService(redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("Push live task directly into live queue without dedup check")
    void testPushLiveTask() {
        SofaTaskDto task = SofaTaskDto.builder()
                .eventId(1001L)
                .sport("football")
                .type(SofaTaskDto.TaskType.LIVE)
                .build();

        boolean pushed = queueService.pushTask(task);

        assertThat(pushed).isTrue();
        verify(listOps).leftPush(eq(SofaQueueService.QUEUE_LIVE), anyString());
        verify(setOps, never()).add(anyString(), anyString());
    }

    @Test
    @DisplayName("Push prematch task with dedup set check")
    void testPushPrematchTask() {
        when(setOps.add(eq(SofaQueueService.DEDUP_SET_KEY), eq("PREMATCH:2002"))).thenReturn(1L);

        SofaTaskDto task = SofaTaskDto.builder()
                .eventId(2002L)
                .sport("basketball")
                .type(SofaTaskDto.TaskType.PREMATCH)
                .build();

        boolean pushed = queueService.pushTask(task);

        assertThat(pushed).isTrue();
        verify(setOps).add(eq(SofaQueueService.DEDUP_SET_KEY), eq("PREMATCH:2002"));
        verify(listOps).leftPush(eq(SofaQueueService.QUEUE_PREMATCH), anyString());
    }

    @Test
    @DisplayName("Skip duplicate prematch task")
    void testSkipDuplicatePrematchTask() {
        when(setOps.add(eq(SofaQueueService.DEDUP_SET_KEY), eq("PREMATCH:2002"))).thenReturn(0L);

        SofaTaskDto task = SofaTaskDto.builder()
                .eventId(2002L)
                .sport("basketball")
                .type(SofaTaskDto.TaskType.PREMATCH)
                .build();

        boolean pushed = queueService.pushTask(task);

        assertThat(pushed).isFalse();
        verify(listOps, never()).leftPush(anyString(), anyString());
    }
}
