package pro.datawiki.accounts.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.accounts.domain.entity.*;

import java.util.List;

/**
 * Full dashboard response for a single Account.
 * Served by accounts-service GET /api/v1/accounts/{id}/dashboard
 * and consumed by igaming-admin-frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDashboardDto {

    private Long id;
    private String name;
    private String createdAt;
    private String updatedAt;

    /** All cloud credentials (without raw credentialsJson for security) */
    private List<CloudCredentialDto> cloudCredentials;

    /** All LLM credentials (without refreshToken for security) */
    private List<LlmCredentialDto> llmCredentials;

    /** Latest snapshot per cloudCredentialId */
    private List<CloudSnapshot> latestCloudSnapshots;

    /** Latest snapshot per llmCredentialId */
    private List<LlmQuotaSnapshot> latestLlmSnapshots;

    /** All active managed servers for this account */
    private List<ManagedServer> servers;
}
