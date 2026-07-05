package pro.datawiki.accounts.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.accounts.cloud.client.AccountsServiceClient;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.entity.ManagedServer;
import pro.datawiki.accounts.domain.provider.CloudProvisioningProvider;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoScalingService {

    private final AccountsServiceClient accountsServiceClient;
    private final List<CloudProvisioningProvider> providers;

    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public String provisionNode(String region, String model, String role, String machineType, boolean isFallback) {
        log.info("Requesting node provision: Region={}, Model={}, Role={}", region, model, role);

        List<CloudCredential> accounts = accountsServiceClient.getActiveCloudCredentials().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getPriority() != null ? a.getPriority() : 100, 
                        b.getPriority() != null ? b.getPriority() : 100))
                .collect(Collectors.toList());

        List<CloudCredential> matchingAccounts = new java.util.ArrayList<>();

        for (CloudCredential acc : accounts) {
            if (region == null || "ANY".equalsIgnoreCase(region) ||
                    (acc.getDefaultRegion() != null
                            && acc.getDefaultRegion().toLowerCase().startsWith(region.toLowerCase()))) {
                matchingAccounts.add(acc);
            }
        }

        if (matchingAccounts.isEmpty()) {
            throw new RuntimeException("No active cloud credentials available for provisioning!");
        }

        int startIndex = Math.abs(roundRobinCounter.getAndIncrement()) % matchingAccounts.size();
        
        java.util.List<String> failedAccountsLogs = new java.util.ArrayList<>();
        java.util.List<String> allErrors = new java.util.ArrayList<>();
        Exception lastException = null;

        for (int attempt = 0; attempt < matchingAccounts.size(); attempt++) {
            CloudCredential selectedAccount = matchingAccounts.get((startIndex + attempt) % matchingAccounts.size());
            try {
                String instanceName = provisionWithAccount(selectedAccount, model, role, machineType, isFallback);
                
                for (String failLog : failedAccountsLogs) {
                    log.warn("Previous provision attempt failed: {}", failLog);
                }
                return instanceName;
            } catch (Exception e) {
                lastException = e;
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                String logEntry = "Credential ID " + selectedAccount.getId() + " failed: " + errorMsg;
                
                allErrors.add(logEntry);
                
                boolean isLimitError = errorMsg.contains("QUOTA") 
                        || errorMsg.contains("ZONE_RESOURCE_POOL_EXHAUSTED") 
                        || errorMsg.contains("RESOURCE_EXHAUSTED") 
                        || errorMsg.toLowerCase().contains("limit")
                        || errorMsg.contains("rateLimitExceeded");
                
                if (!isLimitError) {
                    failedAccountsLogs.add(logEntry);
                }
            }
        }

        log.error("All {} accounts failed to provision node. Errors:", matchingAccounts.size());
        for (String err : allErrors) {
            log.error(err);
        }
        
        throw new RuntimeException("Failed to provision node on all available accounts", lastException);
    }

    public String provisionNodeByAccount(Long credentialId, String model, String role, String machineType) {
        log.info("Requesting node provision: CredentialId={}, Model={}, Role={}", credentialId, model, role);

        CloudCredential selectedAccount = accountsServiceClient.getActiveCloudCredentials().stream()
                .filter(a -> a.getId().equals(credentialId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active cloud credential not found: " + credentialId));

        return provisionWithAccount(selectedAccount, model, role, machineType, false);
    }

    private String provisionWithAccount(CloudCredential selectedAccount, String model, String role, String machineType,
            boolean isFallback) {
        CloudProvisioningProvider targetProvider = getProvider(selectedAccount.getProvider());
        String targetRegion = selectedAccount.getDefaultRegion();
        String targetZone = selectedAccount.getDefaultZone();

        String instanceName = targetProvider.provisionNode(selectedAccount, model, role, machineType, targetRegion, targetZone);

        ManagedServer managedInstance = ManagedServer.builder()
                .instanceName(instanceName)
                .provisioningModel(model.toUpperCase())
                .role(role.toUpperCase())
                .machineType(machineType)
                .isFallback(isFallback)
                .status("PROVISIONING")
                .provider(selectedAccount.getProvider())
                .region(targetRegion)
                .zone(targetZone)
                .cloudCredentialId(selectedAccount.getId())
                .account(selectedAccount.getAccount())
                .build();

        accountsServiceClient.saveOrUpdateServer(managedInstance);
        log.info("Registered new instance {} via accounts-service", instanceName);

        return instanceName;
    }

    public void terminateInstance(String instanceName) {
        log.info("Requesting termination for instance: {}", instanceName);
        
        ManagedServer instance = null;
        try {
            instance = accountsServiceClient.getAllServers().stream()
                    .filter(s -> s.getInstanceName().equals(instanceName))
                    .findFirst().orElse(null);
        } catch (Exception e) {
            log.warn("Could not fetch instance {} from accounts-service", instanceName, e);
        }

        if (instance != null) {
            accountsServiceClient.updateServerStatus(instanceName, "TERMINATING");

            if (instance.getCloudCredentialId() != null) {
                final Long cloudCredentialId = instance.getCloudCredentialId();
                CloudCredential account = accountsServiceClient.getActiveCloudCredentials().stream()
                        .filter(c -> c.getId().equals(cloudCredentialId))
                        .findFirst().orElse(null);
                        
                if (account != null) {
                    CloudProvisioningProvider provider = getProvider(account.getProvider());
                    provider.terminateNode(account, instanceName, instance.getRegion(), account.getDefaultZone());
                    accountsServiceClient.deleteServer(instanceName);
                    return;
                }
            }
        }

        log.info("Searching for instance {} across all active accounts to terminate", instanceName);
        for (CloudCredential account : accountsServiceClient.getActiveCloudCredentials()) {
            try {
                CloudProvisioningProvider provider = getProvider(account.getProvider());
                String reg = account.getDefaultRegion() != null && !account.getDefaultRegion().isBlank() ? account.getDefaultRegion() : "europe-north2";
                String z = account.getDefaultZone() != null && !account.getDefaultZone().isBlank() ? account.getDefaultZone() : "europe-north2-b";
                provider.terminateNode(account, instanceName, reg, z);
                log.info("Terminated instance {} in credential: {}", instanceName, account.getId());
            } catch (Exception e) {
                log.debug("Instance {} not found in credential {}", instanceName, account.getId());
            }
        }

        try {
            accountsServiceClient.deleteServer(instanceName);
        } catch(Exception e) {
            log.warn("Failed to delete server {} from accounts-service", instanceName);
        }
    }

    public void cleanupProvisioning(String instanceName) {
        log.info("Cleaning up record for stuck provisioning: {}", instanceName);
        accountsServiceClient.deleteServer(instanceName);
    }

    public CloudProvisioningProvider getProvider(String providerName) {
        return providers.stream()
                .filter(p -> p.supports(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No provisioning provider found for: " + providerName));
    }
}
