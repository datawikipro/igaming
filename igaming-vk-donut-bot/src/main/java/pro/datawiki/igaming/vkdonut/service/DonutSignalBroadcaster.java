package pro.datawiki.igaming.vkdonut.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.vkdonut.model.DonutDonor;

import java.util.List;
import java.util.Map;

/**
 * Scheduled broadcaster that sends exclusive VK Donut Premium surebet signals
 * to all active donors via VK Messages API.
 *
 * Mandatory gambling disclaimer is appended to every signal per AGENTS.md Rule #9
 * and social-media-bot spec.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DonutSignalBroadcaster {

    private static final String DISCLAIMER =
            "\n\n⚠️ Ставки на спорт сопряжены с финансовыми рисками. " +
            "Мы против лудомании и необдуманного беттинга. Играйте ответственно.";

    private final DonutDonorService donorService;
    private final VkApiClient vkApiClient;
    private final PortalSignalClient signalClient;

    // -------------------------------------------------------
    // Scheduled Broadcast
    // -------------------------------------------------------

    /**
     * Fetch latest premium signals and broadcast to all active Donut donors.
     * Runs every 5 minutes (configurable) with initial 30-second delay.
     */
    @Scheduled(fixedRateString = "${vk.donut.broadcast-interval-ms:300000}",
               initialDelayString = "${vk.donut.broadcast-initial-delay-ms:30000}")
    public void broadcastLatestSignals() {
        List<Map<String, Object>> signals = signalClient.fetchPremiumSignals();
        if (signals.isEmpty()) {
            log.debug("VK Donut broadcast: no new signals available.");
            return;
        }

        List<DonutDonor> activeDonors = donorService.getActiveDonors();
        if (activeDonors.isEmpty()) {
            log.info("VK Donut broadcast: no active donors to broadcast to.");
            return;
        }

        log.info("VK Donut: broadcasting {} signal(s) to {} active donor(s).", signals.size(), activeDonors.size());

        for (Map<String, Object> signal : signals) {
            String message = formatSignalMessage(signal);
            for (DonutDonor donor : activeDonors) {
                vkApiClient.sendMessage(donor.getVkUserId(), message);
            }
        }
    }

    // -------------------------------------------------------
    // Manual Admin Broadcast
    // -------------------------------------------------------

    /**
     * Broadcast a custom admin message to all active donors.
     *
     * @param text Message text (plain text)
     */
    public void broadcastAdminMessage(String text) {
        List<DonutDonor> activeDonors = donorService.getActiveDonors();
        log.info("VK Donut admin broadcast to {} donors.", activeDonors.size());
        String fullText = text + DISCLAIMER;
        for (DonutDonor donor : activeDonors) {
            vkApiClient.sendMessage(donor.getVkUserId(), fullText);
        }
    }

    // -------------------------------------------------------
    // Message Formatting
    // -------------------------------------------------------

    private String formatSignalMessage(Map<String, Object> signal) {
        String bk1   = stringify(signal.get("bk1"));
        String bk2   = stringify(signal.get("bk2"));
        String match = stringify(signal.get("match"));
        String sport = stringify(signal.get("sport"));
        double yieldPct = toDouble(signal.get("yield_pct"));
        String bet1  = stringify(signal.get("bet1_type"));
        String bet2  = stringify(signal.get("bet2_type"));
        double odds1 = toDouble(signal.get("odds1"));
        double odds2 = toDouble(signal.get("odds2"));

        return String.format(
            "🔐 VIP Сигнал | Вилка SmartBet.guru\n\n" +
            "⚽ %s (%s)\n\n" +
            "📊 Плечо 1: %s → %s @ %.2f\n" +
            "📊 Плечо 2: %s → %s @ %.2f\n\n" +
            "💰 Доходность: +%.1f%% (гарантированный профит)\n" +
            "🔗 Открыть: https://smartbet.guru/surebets" +
            DISCLAIMER,
            match, sport,
            bk1, bet1, odds1,
            bk2, bet2, odds2,
            yieldPct
        );
    }

    private String stringify(Object obj) {
        return obj != null ? obj.toString() : "N/A";
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number num) return num.doubleValue();
        return 0.0;
    }
}
