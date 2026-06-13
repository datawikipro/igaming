package pro.datawiki.igaming.infra.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "managed_instances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedInstance {
    @Id
    private String instanceName;
    private String provisioningModel; // SPOT, STANDARD, PHYSICAL
    private String machineType;
    private String role; // MASTER, WORKER, LOADER
    private Boolean isFallback;
    private String status; // PROVISIONING, ACTIVE, TERMINATING
    
    private String provider;
    private String region;
    private Long cloudAccountId;
    
    private LocalDateTime createdAt;
}
