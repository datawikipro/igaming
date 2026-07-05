package pro.datawiki.accounts.llm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.accounts.llm.service.LlmPollingScheduler;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/llm")
@CrossOrigin(origins = "*")
public class LlmController {

    @Autowired
    private LlmPollingScheduler llmPollingScheduler;

    @RequestMapping(value = "/poll", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> triggerPoll() {
        if (llmPollingScheduler.isPolling()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Polling is already in progress"));
        }
        
        Thread.ofVirtual().start(() -> llmPollingScheduler.pollAllCredentials());
        
        return ResponseEntity.ok(Map.of("message", "Poll triggered asynchronously"));
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isPolling", llmPollingScheduler.isPolling());
        status.put("lastPollAt", llmPollingScheduler.getLastPollAt());
        status.put("credentialCount", llmPollingScheduler.getLastCredentialCount());
        
        return ResponseEntity.ok(status);
    }
}
