package pro.datawiki.accounts.domain.provider;

import java.util.Map;

/**
 * Lightweight info record returned by CloudProvisioningProvider.listServers().
 */
public record ServerInfo(
        String status,        // e.g. "RUNNING", "TERMINATED", "STAGING"
        String cpuPlatform,   // e.g. "Intel Cascade Lake"
        String machineType,   // e.g. "e2-standard-4"
        String scheduling,    // "Spot" or "Standard"
        Map<String, String> labels
) {}
