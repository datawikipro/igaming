package pro.datawiki.igaming.bots.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.bots.model.BotPair;
import pro.datawiki.igaming.bots.model.BotProfile;
import pro.datawiki.igaming.bots.service.BotRosterService;
import pro.datawiki.igaming.bots.service.SyndicateBotScheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bots")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BotsStatusController {

    private final BotRosterService rosterService;
    private final SyndicateBotScheduler scheduler;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "igaming-syndicate-bots");
        status.put("total_bots", rosterService.getAllBots().size());
        status.put("total_pairs", rosterService.getPairs().size());
        status.put("pairs", rosterService.getPairs());
        status.put("status", "ACTIVE");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerRound(@RequestParam(defaultValue = "1") int count) {
        scheduler.executeTradingRound(count);
        Map<String, Object> resp = new HashMap<>();
        resp.put("triggered", true);
        resp.put("pairs_processed", count);
        return ResponseEntity.ok(resp);
    }
}
