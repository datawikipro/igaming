package pro.datawiki.igaming.k8s.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.accounts.domain.dto.AiTaskDto;
import pro.datawiki.igaming.k8s.service.AiTaskJobLauncherService;
import pro.datawiki.igaming.k8s.service.AiTaskPreviewDeployerService;

@Slf4j
@RestController
@RequestMapping("/api/workload/ai-tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiTaskLauncherController {

    private final AiTaskJobLauncherService launcherService;
    private final AiTaskPreviewDeployerService previewDeployerService;

    @PostMapping("/launch")
    public ResponseEntity<Void> launchTaskJob(@RequestBody AiTaskDto taskDto) {
        log.info("Received request to launch job for task ID: {}", taskDto.getId());
        launcherService.launchJob(taskDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deploy-preview")
    public ResponseEntity<Void> deployPreview(@RequestBody AiTaskDto taskDto) {
        log.info("Received request to deploy preview for task ID: {}", taskDto.getId());
        previewDeployerService.deployPreview(taskDto);
        return ResponseEntity.ok().build();
    }
}
