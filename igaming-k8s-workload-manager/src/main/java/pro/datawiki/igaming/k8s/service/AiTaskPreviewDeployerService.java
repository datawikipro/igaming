package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.accounts.domain.dto.AiTaskDto;
import pro.datawiki.accounts.domain.dto.UpdateAiTaskStatusRequest;
import pro.datawiki.accounts.domain.entity.AiTaskStatus;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskPreviewDeployerService {

    private final KubernetesClient kubernetesClient;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${k8s.namespace:accounts}")
    private String namespace;

    @Value("${accounts.service.url:http://accounts-service:8080}")
    private String accountsServiceUrl;

    public void deployPreview(AiTaskDto taskDto) {
        if (taskDto.getImageTag() == null || taskDto.getImageTag().isBlank()) {
            log.warn("Cannot deploy preview for Task ID {}: imageTag is missing.", taskDto.getId());
            return;
        }

        String repoName = extractRepoName(taskDto.getGitRepo());
        String deploymentName = "preview-" + repoName + "-task-" + taskDto.getId();
        log.info("Deploying preview container {} with image {} for Task ID: {}", deploymentName, taskDto.getImageTag(), taskDto.getId());

        Map<String, String> labels = new HashMap<>();
        labels.put("app", deploymentName);
        labels.put("task-id", String.valueOf(taskDto.getId()));
        labels.put("preview-workload", "true");

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(deploymentName)
                    .withNamespace(namespace)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withNewSelector()
                        .withMatchLabels(labels)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(labels)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName("preview-app")
                                .withImage(taskDto.getImageTag())
                                .withImagePullPolicy("Always")
                                .withNewResources()
                                    .addToRequests("cpu", new Quantity("200m"))
                                    .addToRequests("memory", new Quantity("512Mi"))
                                    .addToLimits("cpu", new Quantity("1000m"))
                                    .addToLimits("memory", new Quantity("2Gi"))
                                .endResources()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();

        try {
            kubernetesClient.apps().deployments().inNamespace(namespace).resource(deployment).createOrReplace();
            log.info("Successfully deployed preview workload {} for AI Task {}", deploymentName, taskDto.getId());

            // Report completion back to accounts-service
            UpdateAiTaskStatusRequest statusUpdate = UpdateAiTaskStatusRequest.builder()
                    .status(AiTaskStatus.COMPLETED)
                    .deploymentName(deploymentName)
                    .commentText("Successfully built image " + taskDto.getImageTag() + ", deployed preview workload " + deploymentName + " to Kubernetes cluster, and completed task.")
                    .build();

            restTemplate.put(accountsServiceUrl + "/api/ai-tasks/" + taskDto.getId() + "/status", statusUpdate);
        } catch (Exception e) {
            log.error("Failed to deploy preview workload for AI Task {}", taskDto.getId(), e);
            UpdateAiTaskStatusRequest statusUpdate = UpdateAiTaskStatusRequest.builder()
                    .status(AiTaskStatus.FAILED)
                    .errorDetails("Failed to deploy preview workload: " + e.getMessage())
                    .commentText("Deployment failed: " + e.getMessage())
                    .build();

            restTemplate.put(accountsServiceUrl + "/api/ai-tasks/" + taskDto.getId() + "/status", statusUpdate);
        }
    }

    private String extractRepoName(String gitRepo) {
        if (gitRepo == null || gitRepo.isBlank()) return "app";
        String clean = gitRepo.replaceAll("\\.git$", "");
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            return clean.substring(lastSlash + 1).toLowerCase().replaceAll("[^a-z0-9-]", "");
        }
        return clean.toLowerCase().replaceAll("[^a-z0-9-]", "");
    }
}
