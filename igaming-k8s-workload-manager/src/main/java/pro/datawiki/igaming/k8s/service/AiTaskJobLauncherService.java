package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.accounts.domain.dto.AiTaskDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskJobLauncherService {

    private final KubernetesClient kubernetesClient;

    @Value("${k8s.namespace:accounts}")
    private String namespace;

    @Value("${agy.runner.image:ghcr.io/datawikipro/agy-browser-runner:latest}")
    private String runnerImage;

    @Value("${accounts.service.url:http://accounts-service:8091}")
    private String accountsServiceUrl;

    public void launchJob(AiTaskDto taskDto) {
        String jobName = "ai-task-job-" + taskDto.getId() + "-" + System.currentTimeMillis() / 1000;
        log.info("Launching K8s Job {} for AI Task ID: {}", jobName, taskDto.getId());

        Map<String, String> labels = new HashMap<>();
        labels.put("app", "agy-browser-runner");
        labels.put("task-id", String.valueOf(taskDto.getId()));

        String projectCode = taskDto.getProjectCode() != null ? taskDto.getProjectCode() : "IGAMING";
        EnvVar envProjectCode = new EnvVarBuilder().withName("PROJECT_CODE").withValue(projectCode).build();
        EnvVar envTaskId = new EnvVarBuilder().withName("TASK_ID").withValue(String.valueOf(taskDto.getId())).build();
        EnvVar envGitRepo = new EnvVarBuilder().withName("GIT_REPO").withValue(taskDto.getGitRepo()).build();
        EnvVar envBaseBranch = new EnvVarBuilder().withName("BASE_BRANCH").withValue(taskDto.getBaseBranch() != null ? taskDto.getBaseBranch() : "main").build();
        EnvVar envTitle = new EnvVarBuilder().withName("TASK_TITLE").withValue(taskDto.getTitle()).build();
        EnvVar envDesc = new EnvVarBuilder().withName("TASK_DESCRIPTION").withValue(taskDto.getDescription() != null ? taskDto.getDescription() : "").build();
        EnvVar envAccountsUrl = new EnvVarBuilder().withName("ACCOUNTS_SERVICE_URL").withValue(accountsServiceUrl).build();
        EnvVar envGithubToken = new EnvVarBuilder()
                .withName("GITHUB_TOKEN")
                .withNewValueFrom()
                    .withNewSecretKeyRef()
                        .withName("github-token-secret")
                        .withKey("GITHUB_TOKEN")
                        .withOptional(true)
                    .endSecretKeyRef()
                .endValueFrom()
                .build();

        String secretName = "S3".equalsIgnoreCase(projectCode) ? "s3-kubeconfig-secret" :
                "CAREER_LAB".equalsIgnoreCase(projectCode) ? "career-lab-kubeconfig-secret" : "kubeconfig-secret";

        Volume kubeconfigVolume = new VolumeBuilder()
                .withName("kubeconfig-vol")
                .withNewSecret()
                    .withSecretName(secretName)
                    .withOptional(true)
                .endSecret()
                .build();

        VolumeMount kubeconfigMount = new VolumeMountBuilder()
                .withName("kubeconfig-vol")
                .withMountPath("/root/.kube")
                .withReadOnly(true)
                .build();

        Volume m2Volume = new VolumeBuilder()
                .withName("m2-cache-vol")
                .withNewPersistentVolumeClaim()
                    .withClaimName("maven-cache-pvc")
                .endPersistentVolumeClaim()
                .build();

        VolumeMount m2Mount = new VolumeMountBuilder()
                .withName("m2-cache-vol")
                .withMountPath("/root/.m2")
                .build();

        Volume sshVolume = new VolumeBuilder()
                .withName("ssh-vol")
                .withNewSecret()
                    .withSecretName("s3-ssh-secret")
                    .withDefaultMode(384) // 0600 octal
                    .withOptional(true)
                .endSecret()
                .build();

        VolumeMount sshMount = new VolumeMountBuilder()
                .withName("ssh-vol")
                .withMountPath("/root/.ssh")
                .withReadOnly(true)
                .build();

        Job job = new JobBuilder()
                .withNewMetadata()
                    .withName(jobName)
                    .withNamespace(namespace)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withActiveDeadlineSeconds(1800L) // 30 min max
                    .withBackoffLimit(0) // Don't restart on failure
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(labels)
                        .endMetadata()
                        .withNewSpec()
                            .withRestartPolicy("Never")
                            .withVolumes(kubeconfigVolume, m2Volume, sshVolume)
                            .addNewContainer()
                                .withName("agy-runner")
                                .withImage(runnerImage)
                                .withImagePullPolicy("Always")
                                .withEnv(envProjectCode, envTaskId, envGitRepo, envBaseBranch, envTitle, envDesc, envAccountsUrl, envGithubToken)
                                .withVolumeMounts(kubeconfigMount, m2Mount, sshMount)
                                .withNewResources()
                                    .addToRequests("cpu", new Quantity("500m"))
                                    .addToRequests("memory", new Quantity("1Gi"))
                                    .addToLimits("cpu", new Quantity("2000m"))
                                    .addToLimits("memory", new Quantity("4Gi"))
                                .endResources()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();

        try {
            kubernetesClient.batch().v1().jobs().inNamespace(namespace).resource(job).create();
            log.info("Successfully created K8s Job {} for AI Task {}", jobName, taskDto.getId());
        } catch (Exception e) {
            log.error("Error creating K8s Job for AI Task {}", taskDto.getId(), e);
            throw new RuntimeException("Failed to create K8s Job: " + e.getMessage(), e);
        }
    }
}
