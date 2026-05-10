package pro.datawiki.igaming.llm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Component
public class GcpCredentialsManager {

    @Value("${google.cloud.keys-dir:keys}")
    private String keysDir;

    private final List<GoogleCredentials> credentialsList = new ArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @PostConstruct
    public void loadCredentials() {
        Path path = Paths.get(keysDir);
        if (Files.exists(path)) {
            try (Stream<Path> paths = Files.list(path)) {
                paths.filter(p -> p.toString().endsWith(".json"))
                        .forEach(this::loadCredentialFile);
            } catch (IOException e) {
                log.error("Failed to list keys directory: {}", e.getMessage());
            }
        }

        if (credentialsList.isEmpty()) {
            log.info("No service account keys found in {}. Falling back to ADC.", keysDir);
            try {
                credentialsList.add(GoogleCredentials.getApplicationDefault());
                log.info("✅ Successfully loaded Application Default Credentials (ADC)");
            } catch (IOException e) {
                log.warn("⚠️ No Application Default Credentials available: {}", e.getMessage());
            }
        } else {
            log.info("✅ Loaded {} service account keys from {}", credentialsList.size(), keysDir);
        }
    }

    private void loadCredentialFile(Path path) {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            GoogleCredentials creds = ServiceAccountCredentials.fromStream(fis)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
            credentialsList.add(creds);
            log.info("✅ Loaded key: {}", path.getFileName());
        } catch (IOException e) {
            log.error("❌ Failed to load key file {}: {}", path, e.getMessage());
        }
    }

    public GoogleCredentials getNextCredentials() {
        if (credentialsList.isEmpty()) {
            throw new IllegalStateException("No GCP credentials available");
        }
        int index = currentIndex.getAndIncrement() % credentialsList.size();
        return credentialsList.get(index);
    }
    
    public int getCredentialsCount() {
        return credentialsList.size();
    }
}
