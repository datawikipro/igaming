package pro.datawiki.igaming.playerfaces.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.service.PlayerAvatarCdnService;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Controller providing fast CDN distribution of optimized player headshots,
 * WebP images, and dynamic Cyberpunk SVG vector avatars.
 */
@Slf4j
@RestController
@RequestMapping("/cdn/avatars")
@RequiredArgsConstructor
public class PlayerAvatarCdnController {

    private final PlayerAvatarCdnService avatarCdnService;

    /**
     * Serves image asset by filename with HTTP caching headers.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getAvatarAsset(@PathVariable String filename) {
        byte[] data = avatarCdnService.getAvatar(filename);
        String contentType = avatarCdnService.getContentType(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(data);
    }

    /**
     * Generates a dynamic futuristic Cyberpunk SVG vector avatar on demand.
     */
    @GetMapping(value = "/svg/{slug}", produces = "image/svg+xml;charset=UTF-8")
    public ResponseEntity<String> getDynamicCyberpunkSvg(
            @PathVariable String slug,
            @RequestParam(required = false, defaultValue = "TENNIS") SportType sport,
            @RequestParam(required = false) Integer seed,
            @RequestParam(required = false) Integer ranking) {

        String displayName = slug.replace("-", " ").replace("_", " ");
        String svg = avatarCdnService.generateCyberpunkSvg(displayName, sport, seed, ranking);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/svg+xml;charset=UTF-8")
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(svg);
    }
}
