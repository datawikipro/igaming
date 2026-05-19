package pro.datawiki.igaming.llm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pro.datawiki.igaming.llm.dto.ModelLookupResponse;
import pro.datawiki.igaming.llm.dto.QueueLinkDto;

import java.util.List;
import java.util.Optional;

/**
 * HTTP-клиент к igaming-llm-admin.
 * Gateway использует его только для:
 *  1. Разрешить modelId -> providerName (без хардкода)
 *  2. Lease API ключа при старте воркера (через worker, не gateway)
 */
@FeignClient(name = "llm-admin-client", url = "${app.llm.admin-url}")
public interface LlmAdminClient {

    /**
     * Найти провайдера по modelId.
     * GET /api/v1/admin/models/lookup?modelId=deepseek-chat
     */
    @GetMapping("/api/v1/admin/models/lookup")
    ModelLookupResponse lookupModel(@RequestParam("modelId") String modelId);

    @GetMapping("/api/v1/admin/queue-links")
    List<QueueLinkDto> getQueueLinks();
}
