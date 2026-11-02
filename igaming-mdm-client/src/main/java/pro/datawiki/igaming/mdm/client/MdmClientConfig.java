package pro.datawiki.igaming.mdm.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.mdm.enabled", havingValue = "true", matchIfMissing = true)
@Import(MdmIngestionClient.class)
public class MdmClientConfig {
}
