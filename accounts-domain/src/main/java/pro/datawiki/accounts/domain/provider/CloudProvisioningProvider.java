package pro.datawiki.accounts.domain.provider;

import pro.datawiki.accounts.domain.entity.CloudCredential;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Strategy interface implemented by each cloud provider (GCP, AWS, Yandex, Azure, DO, Oracle, Selectel, Vultr).
 * Consumed by cloud-poller to provision nodes, poll billing, and list servers.
 */
public interface CloudProvisioningProvider {

    /** Returns true if this provider handles the given providerName string */
    boolean supports(String providerName);

    /**
     * Provisions a new VM and joins it to the K3s cluster via startup script.
     *
     * @param cred         Cloud credential containing projectId, credentialsJson, k3sToken, tailscaleAuthKey
     * @param model        "SPOT" or "STANDARD"
     * @param role         "LOADER" or "MAIN"
     * @param machineType  Provider machine type string (e.g. "e2-standard-4")
     * @param region       Cloud region (e.g. "europe-north2")
     * @param zone         Cloud zone (e.g. "europe-north2-b")
     * @return             The provisioned instance name
     */
    String provisionNode(CloudCredential cred, String model, String role,
                         String machineType, String region, String zone);

    /**
     * Terminates a VM instance.
     *
     * @param cred         Cloud credential
     * @param instanceName Instance to terminate
     * @param region       Region where the instance lives
     * @param zone         Zone where the instance lives
     */
    void terminateNode(CloudCredential cred, String instanceName, String region, String zone);

    /**
     * Lists all running instances for this cloud credential.
     *
     * @return Map of instanceName → ServerInfo
     */
    Map<String, ServerInfo> listServers(CloudCredential cred);

    /**
     * Fetches the current billing balance for this cloud account.
     * Returns empty if the Billing API is not accessible or not supported.
     */
    Optional<BigDecimal> fetchBalance(CloudCredential cred);

    /**
     * Resets (hard-reboots) a VM instance.
     * Used for self-healing of NotReady K8s nodes.
     *
     * @return true if the reset was initiated successfully
     */
    boolean resetInstance(CloudCredential cred, String instanceName);
}
