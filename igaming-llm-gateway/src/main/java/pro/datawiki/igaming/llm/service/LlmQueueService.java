package pro.datawiki.igaming.llm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.client.LlmAdminClient;
import pro.datawiki.igaming.llm.domain.LlmTask;
import pro.datawiki.igaming.llm.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmQueueService {

    private final LlmAdminClient adminClient;

    public LlmSubmitResponse submit(LlmRequest request) {
        log.debug("Proxying async submit request to admin: model={}", request.getModel());
        return adminClient.submit(request);
    }

    public Optional<LlmTask> getResult(UUID taskId) {
        log.debug("Proxying getResult request to admin: taskId={}", taskId);
        return adminClient.getResult(taskId);
    }

    public LlmResponse generate(LlmRequest request) {
        log.debug("Proxying sync generate request to admin: model={}", request.getModel());
        return adminClient.generate(request);
    }

    public Optional<LlmTask> claimTask(String providerType, String modelName, String workerId) {
        log.debug("Proxying claimTask request to admin: workerId={}, model={}", workerId, modelName);
        return adminClient.claimTask(providerType, modelName, workerId);
    }

    public void completeTask(TaskCompleteRequest request) {
        log.debug("Proxying completeTask request to admin: taskId={}, status={}", request.getTaskId(), request.getStatus());
        adminClient.completeTask(request);
    }

    public List<ModelQueueStats> getQueueStats() {
        log.debug("Proxying getQueueStats request to admin");
        return adminClient.getLocalQueueStats();
    }
}
