package pro.datawiki.accounts.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.accounts.cloud.client.AccountsServiceClient;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.entity.CloudSnapshot;
import pro.datawiki.accounts.domain.entity.ManagedServer;
import pro.datawiki.accounts.domain.provider.CloudProvisioningProvider;
import pro.datawiki.accounts.domain.provider.ServerInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudPollingScheduler {

    private final AccountsServiceClient accountsServiceClient;
    private final List<CloudProvisioningProvider> providers;

    @Scheduled(fixedDelayString = "${cloud.poll.interval-ms:600000}")
    public void pollAll() {
        log.info("Starting Cloud Polling Scheduler cycle...");
        List<CloudCredential> credentials = accountsServiceClient.getActiveCloudCredentials();
        
        for (CloudCredential cred : credentials) {
            try {
                CloudProvisioningProvider provider = providers.stream()
                        .filter(p -> p.supports(cred.getProvider()))
                        .findFirst()
                        .orElse(null);

                if (provider == null) {
                    log.warn("No provider found for {}", cred.getProvider());
                    continue;
                }

                Map<String, ServerInfo> servers = provider.listServers(cred);
                
                int runningCount = 0;
                for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
                    String instanceName = entry.getKey();
                    ServerInfo info = entry.getValue();
                    
                    if ("RUNNING".equalsIgnoreCase(info.status())) {
                        runningCount++;
                    }

                    String role = info.labels() != null ? info.labels().getOrDefault("role", "MAIN").toUpperCase() : "MAIN";
                    String model = info.labels() != null ? info.labels().getOrDefault("model", "STANDARD").toUpperCase() : "STANDARD";
                    String region = info.labels() != null ? info.labels().getOrDefault("region", cred.getDefaultRegion()) : cred.getDefaultRegion();

                    ManagedServer server = ManagedServer.builder()
                            .instanceName(instanceName)
                            .account(cred.getAccount())
                            .cloudCredentialId(cred.getId())
                            .provider(cred.getProvider())
                            .region(region)
                            .zone(cred.getDefaultZone())
                            .machineType(info.machineType() != null ? info.machineType() : "unknown")
                            .provisioningModel(model)
                            .role(role)
                            .status(info.status())
                            .isFallback(false)
                            .build();

                    accountsServiceClient.saveOrUpdateServer(server);
                }

                Optional<BigDecimal> balance = provider.fetchBalance(cred);
                
                CloudSnapshot snap = CloudSnapshot.builder()
                        .cloudCredentialId(cred.getId())
                        .timestamp(LocalDateTime.now())
                        .balance(balance.orElse(null))
                        .currency("USD")
                        .serverCount(servers.size())
                        .runningCount(runningCount)
                        .build();
                        
                accountsServiceClient.saveCloudSnapshot(snap);
                
                log.info("Polled credential ID {}: {} total servers, {} running", cred.getId(), servers.size(), runningCount);

            } catch (Exception e) {
                log.error("Error polling credential ID {}", cred.getId(), e);
            }
        }
        log.info("Completed Cloud Polling Scheduler cycle.");
    }
}
