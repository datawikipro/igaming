package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmQueueSubscription;
import pro.datawiki.igaming.llm.admin.repository.LlmQueueSubscriptionRepository;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/queue-subscriptions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmQueueSubscriptionController {

    private final LlmQueueSubscriptionRepository queueSubscriptionRepository;

    @GetMapping
    public List<LlmQueueSubscription> getQueueSubscriptions() {
        return queueSubscriptionRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<LlmQueueSubscription> createQueueSubscription(@RequestBody LlmQueueSubscription subscription) {
        log.info("Creating queue subscription: Model ID {} -> Queue '{}'", 
            subscription.getModel().getId(), subscription.getQueueName());
        return ResponseEntity.ok(queueSubscriptionRepository.save(subscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQueueSubscription(@PathVariable Long id) {
        log.info("Deleting queue subscription with id: {}", id);
        queueSubscriptionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
