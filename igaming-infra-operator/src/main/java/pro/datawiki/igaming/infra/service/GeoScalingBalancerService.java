package pro.datawiki.igaming.infra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.infra.entity.CloudAccount;
import pro.datawiki.igaming.infra.entity.ManagedInstance;
import pro.datawiki.igaming.infra.repository.CloudAccountRepository;
import pro.datawiki.igaming.infra.repository.ManagedInstanceRepository;
import pro.datawiki.igaming.infra.service.provider.CloudProvisioningProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoScalingBalancerService {

    private final CloudAccountRepository cloudAccountRepository;
    private final ManagedInstanceRepository instanceRepository;
    private final List<CloudProvisioningProvider> providers;

    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    /**
     * Provisions a node in the specified region.
     * Searches for an active CloudAccount that matches the region (or uses a
     * default if generic).
     */
    @Transactional
    public String provisionNode(String region, String model, String role, String machineType, boolean isFallback) {
        log.info("Requesting node provision: Region={}, Model={}, Role={}", region, model, role);

        List<CloudAccount> accounts = cloudAccountRepository.findByIsActiveTrueOrderByPriorityAsc();
        List<CloudAccount> matchingAccounts = new java.util.ArrayList<>();

        for (CloudAccount acc : accounts) {
            if (region == null || "ANY".equalsIgnoreCase(region) ||
                    (acc.getDefaultRegion() != null
                            && acc.getDefaultRegion().toLowerCase().startsWith(region.toLowerCase()))) {
                matchingAccounts.add(acc);
            }
        }

        if (matchingAccounts.isEmpty()) {
            throw new RuntimeException("No active cloud accounts available for provisioning!");
        }

        int startIndex = Math.abs(roundRobinCounter.getAndIncrement()) % matchingAccounts.size();
        
        java.util.List<String> failedAccountsLogs = new java.util.ArrayList<>();
        java.util.List<String> allErrors = new java.util.ArrayList<>();
        Exception lastException = null;

        for (int attempt = 0; attempt < matchingAccounts.size(); attempt++) {
            CloudAccount selectedAccount = matchingAccounts.get((startIndex + attempt) % matchingAccounts.size());
            try {
                String instanceName = provisionWithAccount(selectedAccount, model, role, machineType, isFallback);
                
                for (String failLog : failedAccountsLogs) {
                    log.warn("Previous provision attempt failed: {}", failLog);
                }
                return instanceName;
            } catch (Exception e) {
                lastException = e;
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                String logEntry = "Account " + selectedAccount.getName() + " failed: " + errorMsg;
                
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

    @Transactional
    public String provisionNodeByAccount(String accountName, String model, String role, String machineType) {
        log.info("Requesting node provision: Account={}, Model={}, Role={}", accountName, model, role);

        CloudAccount selectedAccount = cloudAccountRepository.findByIsActiveTrueOrderByPriorityAsc().stream()
                .filter(a -> a.getName().equalsIgnoreCase(accountName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active cloud account not found: " + accountName));

        return provisionWithAccount(selectedAccount, model, role, machineType, false);
    }

    private String provisionWithAccount(CloudAccount selectedAccount, String model, String role, String machineType,
            boolean isFallback) {
        CloudProvisioningProvider targetProvider = getProvider(selectedAccount.getProvider());
        String targetRegion = selectedAccount.getDefaultRegion();
        String targetZone = selectedAccount.getDefaultZone();

        String instanceName = targetProvider.provisionNode(selectedAccount, model, role, machineType, targetRegion,
                targetZone);

        ManagedInstance managedInstance = ManagedInstance.builder()
                .instanceName(instanceName)
                .provisioningModel(model.toUpperCase())
                .role(role.toUpperCase())
                .machineType(machineType)
                .isFallback(isFallback)
                .status("PROVISIONING")
                .provider(selectedAccount.getProvider())
                .region(targetRegion)
                .cloudAccountId(selectedAccount.getId())
                .createdAt(LocalDateTime.now())
                .build();

        instanceRepository.save(managedInstance);
        log.info("Registered new instance {} in DB (Account: {})", instanceName, selectedAccount.getName());

        return instanceName;
    }

    @Transactional
    public void terminateInstance(String instanceName) {
        log.info("Requesting termination for instance: {}", instanceName);
        ManagedInstance instance = instanceRepository.findById(instanceName).orElse(null);

        if (instance != null) {
            instance.setStatus("TERMINATING");
            instanceRepository.save(instance);

            if (instance.getCloudAccountId() != null) {
                CloudAccount account = cloudAccountRepository.findById(instance.getCloudAccountId()).orElse(null);
                if (account != null) {
                    CloudProvisioningProvider provider = getProvider(account.getProvider());
                    provider.terminateNode(account, instanceName, instance.getRegion(), account.getDefaultZone());
                    instanceRepository.deleteById(instanceName);
                    return;
                }
            }
        }

        // For spot instances (not in DB) or instances without linked account ID
        log.info("Searching for instance {} across all active accounts to terminate", instanceName);
        for (CloudAccount account : cloudAccountRepository.findByIsActiveTrueOrderByPriorityAsc()) {
            try {
                CloudProvisioningProvider provider = getProvider(account.getProvider());
                String reg = account.getDefaultRegion() != null && !account.getDefaultRegion().isBlank() ? account.getDefaultRegion() : "europe-north2";
                String z = account.getDefaultZone() != null && !account.getDefaultZone().isBlank() ? account.getDefaultZone() : "europe-north2-b";
                provider.terminateNode(account, instanceName, reg, z);
                log.info("Terminated instance {} in account: {}", instanceName, account.getName());
            } catch (Exception e) {
                log.debug("Instance {} not found in account {}", instanceName, account.getName());
            }
        }

        instanceRepository.deleteById(instanceName);
    }

    @Transactional
    public void cleanupProvisioning(String instanceName) {
        log.info("Cleaning up DB record for stuck provisioning: {}", instanceName);
        instanceRepository.deleteById(instanceName);
    }

    private CloudProvisioningProvider getProvider(String providerName) {
        return providers.stream()
                .filter(p -> p.supports(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No provisioning provider found for: " + providerName));
    }
}