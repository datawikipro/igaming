package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmQueueLink;
import pro.datawiki.igaming.llm.admin.repository.LlmQueueLinkRepository;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/queue-links")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmQueueLinkController {

    private final LlmQueueLinkRepository queueLinkRepository;

    @GetMapping
    public List<LlmQueueLink> getQueueLinks() {
        return queueLinkRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<LlmQueueLink> createQueueLink(@RequestBody LlmQueueLink link) {
        log.info("Creating queue link: {}/{} -> {}/{}", 
            link.getSourceProvider(), link.getSourceModel(), 
            link.getTargetProvider(), link.getTargetModel());
        return ResponseEntity.ok(queueLinkRepository.save(link));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQueueLink(@PathVariable Long id) {
        log.info("Deleting queue link with id: {}", id);
        queueLinkRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
