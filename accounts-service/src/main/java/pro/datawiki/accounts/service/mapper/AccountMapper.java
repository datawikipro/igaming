package pro.datawiki.accounts.service.mapper;

import pro.datawiki.accounts.domain.dto.CloudCredentialDto;
import pro.datawiki.accounts.domain.dto.LlmCredentialDto;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.entity.LlmCredential;

/**
 * Static mapping helpers — convert entities to safe DTOs, stripping all secret fields.
 */
public final class AccountMapper {

    private AccountMapper() {
        // utility class
    }

    /**
     * Maps a CloudCredential entity to CloudCredentialDto.
     * credentialsJson, k3sToken, and tailscaleAuthKey are NEVER included in output;
     * their presence is signalled by boolean flags.
     */
    public static CloudCredentialDto toDto(CloudCredential c) {
        return CloudCredentialDto.builder()
                .id(c.getId())
                .accountId(c.getAccount() != null ? c.getAccount().getId() : null)
                .provider(c.getProvider())
                .projectId(c.getProjectId())
                .defaultRegion(c.getDefaultRegion())
                .defaultZone(c.getDefaultZone())
                .k3sServerUrl(c.getK3sServerUrl())
                .isActive(c.getIsActive())
                .priority(c.getPriority())
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .hasCredentials(c.getCredentialsJson() != null && !c.getCredentialsJson().isBlank())
                .hasK3sToken(c.getK3sToken() != null && !c.getK3sToken().isBlank())
                .hasTailscale(c.getTailscaleAuthKey() != null && !c.getTailscaleAuthKey().isBlank())
                .build();
    }

    /**
     * Maps a LlmCredential entity to LlmCredentialDto.
     * refreshToken is NEVER included in output; its presence is signalled by hasToken flag.
     */
    public static LlmCredentialDto toDto(LlmCredential c) {
        return LlmCredentialDto.builder()
                .id(c.getId())
                .accountId(c.getAccount() != null ? c.getAccount().getId() : null)
                .email(c.getEmail())
                .provider(c.getProvider())
                .region(c.getRegion())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .hasToken(c.getRefreshToken() != null && !c.getRefreshToken().isBlank())
                .build();
    }
}
