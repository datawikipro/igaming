package pro.datawiki.igaming.llm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;
import pro.datawiki.igaming.llm.service.LlmService;

@RestController
@RequestMapping("/api/v1/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmService llmService;

    @PostMapping("/generate")
    public LlmResponse generate(@RequestBody LlmRequest request) {
        return llmService.generate(request);
    }
}
