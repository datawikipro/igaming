package pro.datawiki.igaming.llm.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pro.datawiki.igaming.llm.admin.dto.LlmRequest;
import pro.datawiki.igaming.llm.admin.dto.LlmResponse;
import pro.datawiki.igaming.llm.admin.dto.ModelQueueStats;

import java.net.URI;
import java.util.List;

@FeignClient(name = "llm-gateway-client", url = "http://placeholder")
public interface LlmGatewayClient {

    @PostMapping("/api/v1/llm/generate")
    LlmResponse generate(URI baseUri, @RequestBody LlmRequest request);

    @GetMapping("/api/v1/llm/queue/stats")
    List<ModelQueueStats> getQueueStats(URI baseUri);
}
