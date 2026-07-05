package pro.datawiki.accounts.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Safe view of CloudCredential — credentialsJson, k3sToken, tailscaleAuthKey are masked.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudCredentialDto {
    private Long id;
    private Long accountId;
    private String provider;
    private String projectId;
    private String defaultRegion;
    private String defaultZone;
    private Boolean isActive;
    private Integer priority;
    private String createdAt;
    /** true if credentialsJson is set */
    private boolean hasCredentials;
    /** true if k3sToken is set */
    private boolean hasK3sToken;
    /** true if tailscaleAuthKey is set */
    private boolean hasTailscale;
    private String k3sServerUrl;
}
