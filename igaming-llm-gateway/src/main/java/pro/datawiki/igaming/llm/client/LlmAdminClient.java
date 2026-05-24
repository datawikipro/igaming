package pro.datawiki.igaming.llm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.domain.LlmTask;
import pro.datawiki.igaming.llm.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@FeignClient(name = "llm-admin-client", url = "${app.llm.admin-url}")
public interface LlmAdminClient {

    @GetMapping("/api/v1/admin/models/lookup")
    ModelLookupResponse lookupModel(@RequestParam("modelId") String modelId);

    @GetMapping("/api/v1/admin/queue-links")
    List<QueueLinkDto> getQueueLinks();

    @GetMapping("/api/v1/admin/gateway/stats")
    List<ModelQueueStats> getLocalQueueStats();

    @PostMapping("/api/v1/llm/submit")
    LlmSubmitResponse submit(@RequestBody LlmRequest request);

    @GetMapping("/api/v1/llm/result/{taskId}")
    Optional<LlmTask> getResult(@PathVariable("taskId") UUID taskId);

    @PostMapping("/api/v1/llm/generate")
    LlmResponse generate(@RequestBody LlmRequest request);

    @PostMapping("/api/v1/llm/worker/claim")
    Optional<LlmTask> claimTask(
            @RequestParam("providerType") String providerType,
            @RequestParam("modelName") String modelName,
            @RequestParam("workerId") String workerId);

    @PostMapping("/api/v1/llm/worker/complete")
    void completeTask(@RequestBody TaskCompleteRequest request);
}
