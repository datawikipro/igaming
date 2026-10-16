package pro.datawiki.igaming.capture.sofascore.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandResponse;

/**
 * Manages Redis task queues and deduplication for SofaScore crawler and loader.
 * Separates priorities:
 * - sofascore:queue:on-demand (P00 - Super Priority)
 * - sofascore:queue:live (P0)
 * - sofascore:queue:prematch (P1)
 * - sofascore:queue:history (P2)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SofaQueueService {

    public static final String QUEUE_ONDEMAND = "sofascore:queue:on-demand";
    public static final String QUEUE_LIVE = "sofascore:queue:live";
    public static final String QUEUE_PREMATCH = "sofascore:queue:prematch";
    public static final String QUEUE_HISTORY = "sofascore:queue:history";
    public static final String DEDUP_SET_KEY = "sofascore:dedup:event-ids";
    public static final String TASK_STATUS_KEY_PREFIX = "sofascore:task:status:";
    public static final String TASK_RESULT_KEY_PREFIX = "sofascore:task:result:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Enqueue a new task if not already processed recently.
     * Live and On-Demand events bypass deduplication so updated lineups/scores are always delivered.
     */
    public boolean pushTask(SofaTaskDto task) {
        if (task == null || task.getEventId() == null) {
            return false;
        }

        task.setQueuedAt(System.currentTimeMillis());

        // Deduplication for prematch and history
        if (task.getType() != SofaTaskDto.TaskType.LIVE && task.getType() != SofaTaskDto.TaskType.ON_DEMAND) {
            String dedupMember = task.getType().name() + ":" + task.getEventId();
            Long added = redisTemplate.opsForSet().add(DEDUP_SET_KEY, dedupMember);
            if (added == null || added <= 0) {
                return false; // Already queued or processed
            }
            redisTemplate.expire(DEDUP_SET_KEY, Duration.ofDays(7));
        }

        try {
            String json = objectMapper.writeValueAsString(task);
            String queueKey = resolveQueueKey(task.getType());
            redisTemplate.opsForList().leftPush(queueKey, json);
            return true;
        } catch (Exception e) {
            log.error("[SofaQueue] Error pushing task for eventId {}: {}", task.getEventId(), e.getMessage());
            return false;
        }
    }

    public boolean pushOnDemandTask(SofaTaskDto task) {
        if (task == null) return false;
        task.setType(SofaTaskDto.TaskType.ON_DEMAND);
        task.setPriority(0);
        if (task.getTaskId() != null) {
            setTaskStatus(task.getTaskId(), "QUEUED");
        }
        return pushTask(task);
    }

    public int pushBatch(List<SofaTaskDto> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        int pushed = 0;
        for (SofaTaskDto task : tasks) {
            if (pushTask(task)) {
                pushed++;
            }
        }
        return pushed;
    }

    /**
     * Pops next task from On-Demand (P00) queue with timeout.
     */
    public SofaTaskDto popOnDemand(long timeoutSeconds) {
        try {
            String json = redisTemplate.opsForList().rightPop(QUEUE_ONDEMAND, timeoutSeconds, TimeUnit.SECONDS);
            if (json != null) {
                return objectMapper.readValue(json, SofaTaskDto.class);
            }
        } catch (Exception e) {
            log.debug("[SofaQueue] Pop on-demand poll notice: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Pops next task checking On-Demand (P00), Live (P0) or Prematch (P1) queues.
     */
    public SofaTaskDto popLiveOrPrematch(long timeoutSeconds) {
        try {
            // First check On-Demand queue non-blocking (P00 Super Priority)
            String onDemandJson = redisTemplate.opsForList().rightPop(QUEUE_ONDEMAND);
            if (onDemandJson != null) {
                return objectMapper.readValue(onDemandJson, SofaTaskDto.class);
            }

            // Next check Live queue non-blocking (P0)
            String liveJson = redisTemplate.opsForList().rightPop(QUEUE_LIVE);
            if (liveJson != null) {
                return objectMapper.readValue(liveJson, SofaTaskDto.class);
            }

            // Next check Prematch queue non-blocking (P1)
            String prematchJson = redisTemplate.opsForList().rightPop(QUEUE_PREMATCH);
            if (prematchJson != null) {
                return objectMapper.readValue(prematchJson, SofaTaskDto.class);
            }

            // If empty, wait on On-Demand then Live queue up to timeoutSeconds
            String blockingJson = redisTemplate.opsForList().rightPop(QUEUE_LIVE, timeoutSeconds, TimeUnit.SECONDS);
            if (blockingJson != null) {
                return objectMapper.readValue(blockingJson, SofaTaskDto.class);
            }
        } catch (Exception e) {
            log.debug("[SofaQueue] Pop live/prematch poll notice: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Pops next task from History (P2) queue with blocking timeout.
     */
    public SofaTaskDto popHistory(long timeoutSeconds) {
        try {
            String json = redisTemplate.opsForList().rightPop(QUEUE_HISTORY, timeoutSeconds, TimeUnit.SECONDS);
            if (json != null) {
                return objectMapper.readValue(json, SofaTaskDto.class);
            }
        } catch (Exception e) {
            log.debug("[SofaQueue] Pop history poll notice: {}", e.getMessage());
        }
        return null;
    }

    public void setTaskStatus(String taskId, String status) {
        if (taskId == null) return;
        try {
            redisTemplate.opsForValue().set(TASK_STATUS_KEY_PREFIX + taskId, status, Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("[SofaQueue] Failed to record task status for {}: {}", taskId, e.getMessage());
        }
    }

    public String getTaskStatus(String taskId) {
        if (taskId == null) return null;
        try {
            return redisTemplate.opsForValue().get(TASK_STATUS_KEY_PREFIX + taskId);
        } catch (Exception e) {
            log.warn("[SofaQueue] Failed to read task status for {}: {}", taskId, e.getMessage());
            return null;
        }
    }

    public void saveTaskResult(String taskId, SofaOnDemandResponse response) {
        if (taskId == null || response == null) return;
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(TASK_RESULT_KEY_PREFIX + taskId, json, Duration.ofHours(24));
            setTaskStatus(taskId, response.getStatus());
        } catch (Exception e) {
            log.error("[SofaQueue] Failed to save task result for {}: {}", taskId, e.getMessage());
        }
    }

    public SofaOnDemandResponse getTaskResult(String taskId) {
        if (taskId == null) return null;
        try {
            String json = redisTemplate.opsForValue().get(TASK_RESULT_KEY_PREFIX + taskId);
            if (json != null) {
                return objectMapper.readValue(json, SofaOnDemandResponse.class);
            }
        } catch (Exception e) {
            log.error("[SofaQueue] Failed to deserialize task result for {}: {}", taskId, e.getMessage());
        }
        return null;
    }

    public long getOnDemandQueueSize() {
        Long size = redisTemplate.opsForList().size(QUEUE_ONDEMAND);
        return size != null ? size : 0L;
    }

    public long getLiveQueueSize() {
        Long size = redisTemplate.opsForList().size(QUEUE_LIVE);
        return size != null ? size : 0L;
    }

    public long getPrematchQueueSize() {
        Long size = redisTemplate.opsForList().size(QUEUE_PREMATCH);
        return size != null ? size : 0L;
    }

    public long getHistoryQueueSize() {
        Long size = redisTemplate.opsForList().size(QUEUE_HISTORY);
        return size != null ? size : 0L;
    }

    private String resolveQueueKey(SofaTaskDto.TaskType type) {
        if (type == null) return QUEUE_PREMATCH;
        return switch (type) {
            case ON_DEMAND -> QUEUE_ONDEMAND;
            case LIVE -> QUEUE_LIVE;
            case HISTORY -> QUEUE_HISTORY;
            default -> QUEUE_PREMATCH;
        };
    }
}
