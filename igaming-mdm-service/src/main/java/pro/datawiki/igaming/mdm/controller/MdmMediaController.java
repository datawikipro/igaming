package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.mdm.config.ConditionalOnMdmCatalog;
import pro.datawiki.igaming.mdm.service.media.VisualAssetResolverService;
import pro.datawiki.igaming.mdm.service.media.VisualAssetStorageService.MediaAsset;

import java.util.concurrent.TimeUnit;

/**
 * Controller exposing cached visual media assets (team logos, athlete photos, SVG crests)
 * with HTTP immutable cache headers and read-through disk cache.
 */
@RestController
@RequestMapping("/api/v1/mdm/media")
@RequiredArgsConstructor
@ConditionalOnMdmCatalog
public class MdmMediaController {

    private final VisualAssetResolverService resolverService;

    /**
     * Stream or redirect team logo badge/crest.
     */
    @GetMapping("/teams/{id}/logo")
    public ResponseEntity<byte[]> getTeamLogo(@PathVariable("id") Long id) {
        MediaAsset asset = resolverService.resolveTeamLogo(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=2592000, immutable")
                .header(HttpHeaders.CONTENT_TYPE, asset.contentType())
                .header("X-Asset-Extension", asset.extension())
                .body(asset.data());
    }

    /**
     * Stream or redirect athlete portrait/avatar photo.
     */
    @GetMapping("/players/{id}/photo")
    public ResponseEntity<byte[]> getPlayerPhoto(@PathVariable("id") Long id) {
        MediaAsset asset = resolverService.resolvePlayerPhoto(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=2592000, immutable")
                .header(HttpHeaders.CONTENT_TYPE, asset.contentType())
                .header("X-Asset-Extension", asset.extension())
                .body(asset.data());
    }
}
