package pro.datawiki.igaming.notifications.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.notifications.entity.TelegramChannel;
import pro.datawiki.igaming.notifications.repository.TelegramChannelRepository;
import pro.datawiki.igaming.notifications.service.SystemHealthMonitorService;
import pro.datawiki.igaming.notifications.service.ThreadsTokenRefreshService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationsController {

    private final TelegramChannelRepository channelRepository;
    private final SystemHealthMonitorService systemHealthMonitorService;
    private final ThreadsTokenRefreshService threadsTokenRefreshService;

    @GetMapping("/health/system")
    public pro.datawiki.igaming.dto.SystemHealthDto getSystemHealth() {
        return systemHealthMonitorService.checkSystemHealth();
    }

    @GetMapping("/channels")
    public List<TelegramChannel> getChannels() {
        return channelRepository.findAll();
    }

    @GetMapping("/channels/active")
    public List<TelegramChannel> getActiveChannels() {
        return channelRepository.findByEnabledTrue();
    }

    @PostMapping("/channels")
    public TelegramChannel saveChannel(@RequestBody TelegramChannel channel) {
        return channelRepository.save(channel);
    }

    @PutMapping("/channels/{id}")
    public ResponseEntity<TelegramChannel> updateChannel(@PathVariable Long id, @RequestBody TelegramChannel details) {
        return channelRepository.findById(id).map(ch -> {
            ch.setName(details.getName());
            ch.setChatId(details.getChatId());
            ch.setRegion(details.getRegion());
            ch.setEnabled(details.isEnabled());
            ch.setLowProfitThreshold(details.getLowProfitThreshold());
            ch.setLowProfitIntervalMinutes(details.getLowProfitIntervalMinutes());
            ch.setHighProfitIntervalMinutes(details.getHighProfitIntervalMinutes());
            ch.setMinFreshnessMinutes(details.getMinFreshnessMinutes());
            return ResponseEntity.ok(channelRepository.save(ch));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/channels/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        if (channelRepository.existsById(id)) {
            channelRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/threads/token/refresh")
    public ResponseEntity<Map<String, Object>> refreshThreadsToken() {
        String newToken = threadsTokenRefreshService.refreshThreadsToken();
        return ResponseEntity.ok(Map.of(
                "token", newToken,
                "status", "success",
                "message", "Threads access token successfully refreshed and applied to deployments"
        ));
    }
}
