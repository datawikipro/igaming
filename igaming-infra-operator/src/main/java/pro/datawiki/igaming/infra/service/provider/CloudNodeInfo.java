package pro.datawiki.igaming.infra.service.provider;

import java.util.Map;

public record CloudNodeInfo(
        String status,
        String cpuPlatform,
        String machineType,
        String scheduling,
        Map<String, String> labels
) {
    public CloudNodeInfo(String status, Map<String, String> labels) {
        this(status, "not defined", "not defined", "Standard", labels);
    }
}
