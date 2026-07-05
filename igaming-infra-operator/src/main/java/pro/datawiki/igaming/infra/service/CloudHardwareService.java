package pro.datawiki.igaming.infra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.infra.entity.CloudAccount;
import pro.datawiki.igaming.infra.repository.CloudAccountRepository;
import pro.datawiki.igaming.infra.service.provider.CloudNodeInfo;
import pro.datawiki.igaming.infra.service.provider.CloudProvisioningProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudHardwareService {

    private final CloudAccountRepository cloudAccountRepository;
    private final List<CloudProvisioningProvider> providers;

    public Map<String, CloudNodeInfo> getInstancesInfo() {
        Map<String, CloudNodeInfo> infoMap = new HashMap<>();
        List<CloudAccount> accounts = cloudAccountRepository.findByIsActiveTrueOrderByPriorityAsc();
        for (CloudAccount account : accounts) {
            for (CloudProvisioningProvider provider : providers) {
                if (provider.supports(account.getProvider())) {
                    try {
                        infoMap.putAll(provider.getInstancesInfo(account));
                    } catch (Exception e) {
                        log.error("Failed to fetch hardware info for account {} using provider {}", 
                                account.getName(), account.getProvider(), e);
                    }
                }
            }
        }
        return infoMap;
    }

    public boolean resetInstance(String instanceName) {
        List<CloudAccount> accounts = cloudAccountRepository.findByIsActiveTrueOrderByPriorityAsc();
        for (CloudAccount account : accounts) {
            for (CloudProvisioningProvider provider : providers) {
                if (provider.supports(account.getProvider())) {
                    try {
                        if (provider.resetInstance(account, instanceName)) {
                            return true;
                        }
                    } catch (Exception e) {
                        log.error("Failed to reset instance {} in account {}", instanceName, account.getName(), e);
                    }
                }
            }
        }
        return false;
    }
}
