package pro.datawiki.igaming.mdm.service.media;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Local SSD Storage Service for media assets (club logos, athlete photos, SVG crests).
 * Provides read-through filesystem caching with zero external cloud dependencies.
 */
@Service
@Slf4j
public class VisualAssetStorageService {

    @Value("${igaming.media.storage-dir:./storage/media}")
    private String storageDirProperty;

    private Path rootStoragePath;

    public record MediaAsset(byte[] data, String contentType, String extension) {}

    @PostConstruct
    public void init() {
        this.rootStoragePath = Paths.get(storageDirProperty).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootStoragePath.resolve("teams"));
            Files.createDirectories(this.rootStoragePath.resolve("players"));
            log.info("VisualAssetStorageService initialized at: {}", this.rootStoragePath);
        } catch (IOException e) {
            log.error("Failed to initialize visual asset storage directory: {}", e.getMessage(), e);
        }
    }

    /**
     * Look up existing asset in local filesystem by entity category and ID.
     */
    public Optional<MediaAsset> findAsset(String category, Long id) {
        if (id == null) return Optional.empty();
        Path categoryDir = rootStoragePath.resolve(category);
        if (!Files.exists(categoryDir)) return Optional.empty();

        // Check common image extensions in order of preference
        String[] extensions = {"png", "webp", "svg", "jpg", "jpeg"};
        for (String ext : extensions) {
            Path file = categoryDir.resolve(id + "." + ext);
            if (Files.exists(file) && Files.isRegularFile(file)) {
                try {
                    byte[] bytes = Files.readAllBytes(file);
                    if (bytes.length > 0) {
                        return Optional.of(new MediaAsset(bytes, resolveMimeType(ext), ext));
                    }
                } catch (IOException e) {
                    log.warn("Error reading cached media asset {}: {}", file, e.getMessage());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Save asset bytes to disk.
     */
    public void saveAsset(String category, Long id, String extension, byte[] data) {
        if (id == null || data == null || data.length == 0) return;
        try {
            Path categoryDir = rootStoragePath.resolve(category);
            Files.createDirectories(categoryDir);

            String cleanExt = extension.replace(".", "").toLowerCase();
            Path targetFile = categoryDir.resolve(id + "." + cleanExt);
            Files.write(targetFile, data);
            log.debug("Saved media asset to disk: {} ({} bytes)", targetFile, data.length);
        } catch (IOException e) {
            log.error("Failed to save media asset for {}/{}: {}", category, id, e.getMessage());
        }
    }

    public static String resolveMimeType(String ext) {
        return switch (ext.toLowerCase()) {
            case "svg" -> "image/svg+xml";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "jpg", "jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }
}
