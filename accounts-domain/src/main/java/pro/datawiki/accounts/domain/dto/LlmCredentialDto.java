package pro.datawiki.accounts.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Safe view of LlmCredential — refreshToken is masked.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmCredentialDto {
    private Long id;
    private Long accountId;
    private String email;
    private String provider;
    private Boolean isActive;
    private String region;
    private String createdAt;
    /** true if refreshToken is set */
    private boolean hasToken;
}
