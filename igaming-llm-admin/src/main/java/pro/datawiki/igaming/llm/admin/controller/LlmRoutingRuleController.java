package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmRoutingRule;
import pro.datawiki.igaming.llm.admin.domain.LlmModel;
import pro.datawiki.igaming.llm.admin.repository.LlmRoutingRuleRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmModelRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/routing-rules")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmRoutingRuleController {

    private final LlmRoutingRuleRepository routingRuleRepository;
    private final LlmModelRepository modelRepository;

    @GetMapping
    public List<LlmRoutingRule> getRoutingRules() {
        return routingRuleRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<LlmRoutingRule> createRoutingRule(@RequestBody LlmRoutingRule rule,
                                                            @RequestParam Long targetModelId) {
        log.info("Creating routing rule: logicalType={}, googleRequired={}, urgency={}, targetModelId={}",
                rule.getLogicalType(), rule.getGoogleRequired(), rule.getUrgency(), targetModelId);
        
        return modelRepository.findById(targetModelId).map(model -> {
            rule.setTargetModel(model);
            return ResponseEntity.ok(routingRuleRepository.save(rule));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LlmRoutingRule> updateRoutingRule(@PathVariable Long id, 
                                                            @RequestBody LlmRoutingRule body,
                                                            @RequestParam(required = false) Long targetModelId) {
        log.info("Updating routing rule id={}: logicalType={}, googleRequired={}, urgency={}",
                id, body.getLogicalType(), body.getGoogleRequired(), body.getUrgency());

        return routingRuleRepository.findById(id).map(rule -> {
            rule.setLogicalType(body.getLogicalType());
            rule.setGoogleRequired(body.getGoogleRequired());
            rule.setUrgency(body.getUrgency());
            rule.setActive(body.isActive());
            
            if (targetModelId != null) {
                Optional<LlmModel> modelOpt = modelRepository.findById(targetModelId);
                if (modelOpt.isPresent()) {
                    rule.setTargetModel(modelOpt.get());
                } else {
                    return ResponseEntity.badRequest().<LlmRoutingRule>build();
                }
            }
            return ResponseEntity.ok(routingRuleRepository.save(rule));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutingRule(@PathVariable Long id) {
        log.info("Deleting routing rule with id: {}", id);
        routingRuleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
