package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProxyWatcherService {

    private final KubernetesClient kubernetesClient;
    private final RestTemplate restTemplate;

    private Set<String> registeredIps = java.util.Collections.emptySet();

    public ProxyWatcherService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(fixedDelay = 30000)
    public void watchProxyPods() {
        try {
            List<Pod> proxyPods = kubernetesClient.pods().inAnyNamespace()
                    .withLabel("app", "proxy-exit")
                    .list().getItems();

            Set<String> currentRunningIps = proxyPods.stream()
                    .filter(p -> p.getStatus() != null && "Running".equals(p.getStatus().getPhase()))
                    .map(p -> p.getStatus().getHostIP())
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());

            for (String ip : currentRunningIps) {
                if (!registeredIps.contains(ip)) {
                    log.info("Registering NEW proxy exit node: {}", ip);
                    // Example API call to proxy aggregator
                    try {
                        // restTemplate.postForLocation("http://proxy-manager/api/proxies/register", java.util.Map.of("ip", ip));
                    } catch (Exception e) {
                        log.warn("Failed to register proxy IP {}", ip, e);
                    }
                }
            }

            for (String ip : registeredIps) {
                if (!currentRunningIps.contains(ip)) {
                    log.info("Unregistering DEAD proxy exit node: {}", ip);
                    try {
                        // restTemplate.delete("http://proxy-manager/api/proxies/unregister?ip=" + ip);
                    } catch (Exception e) {
                        log.warn("Failed to unregister proxy IP {}", ip, e);
                    }
                }
            }

            registeredIps = currentRunningIps;
        } catch (Exception e) {
            log.error("Failed to watch proxy pods", e);
        }
    }
}
