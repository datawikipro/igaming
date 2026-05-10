package pro.datawiki.igaming.llm.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pro.datawiki.igaming.llm.admin.dto.LlmRequest;
import pro.datawiki.igaming.llm.admin.dto.LlmResponse;

import java.net.URI;

@FeignClient(name = "llm-gateway-client", url = "http://placeholder")
public interface LlmGatewayClient {

    @PostMapping("/api/v1/llm/generate")
    LlmResponse generate(URI baseUri, @RequestBody LlmRequest request);
}
