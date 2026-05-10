package pro.datawiki.igaming.llm.provider;

import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;

public interface LlmProvider {
    LlmResponse generate(LlmRequest request);
    String getName();
}
