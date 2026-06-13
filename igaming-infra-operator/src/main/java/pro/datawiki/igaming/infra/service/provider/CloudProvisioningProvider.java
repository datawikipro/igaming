package pro.datawiki.igaming.infra.service.provider;

import pro.datawiki.igaming.infra.entity.CloudAccount;

public interface CloudProvisioningProvider {
    /**
     * @return true if this provider supports the given provider name (e.g. "GCP")
     */
    boolean supports(String provider);

    /**
     * Provisions a new worker node in the cloud.
     * @return The generated instance name
     */
    String provisionNode(CloudAccount account, String model, String role, String machineType, String region, String zone);

    /**
     * Terminates a worker node in the cloud.
     */
    void terminateNode(CloudAccount account, String instanceName, String region, String zone);
}
