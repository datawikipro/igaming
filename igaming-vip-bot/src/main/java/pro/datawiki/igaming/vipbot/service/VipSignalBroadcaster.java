package pro.datawiki.igaming.vipbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.vipbot.model.VipMember;

import java.util.List;
import java.util.Map;

/**
 * Handles broadcasting exclusive VIP surebet signals to all active VIP members.
 *
 * Signals are fetched from the igaming-portal API and dispatched as
 * formatted Telegram messages to every ACTIVE VIP member's private chat.
 *
 * Mandatory disclaimer is appended to every signal per AGENTS.md Rule #9.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VipSignalBroadcaster {

    private static final String DISCLAIMER =
            "\n\n⚠️ <i>Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно.</i>";

    private final VipMemberService memberService;
    private final TelegramApiClient telegramApi;
    private final PortalSignalClient signalClient;

    @Value("${vip.channel.id}")
    private Long vipChannelId;

    public Long getVipChannelId() {
        return vipChannelId;
    }

    // -------------------------------------------------------
    // Scheduled Broadcast
    // -------------------------------------------------------

    /**
     * Fetch latest premium signals from portal and broadcast to all active VIP members.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRateString = "${vip.broadcast.interval-ms:300000}",
               initialDelayString = "${vip.broadcast.initial-delay-ms:30000}")
    public void broadcastLatestSignals() {
        List<Map<String, Object>> signals = signalClient.fetchPremiumSignals();
        if (signals.isEmpty()) {
            log.debug("No new VIP signals to broadcast.");
            return;
        }

        List<VipMember> activeMembers = memberService.getActiveMembers();
        if (activeMembers.isEmpty()) {
            log.info("No active VIP members to broadcast to.");
            return;
        }

        log.info("Broadcasting {} VIP signal(s) to {} active member(s).", signals.size(), activeMembers.size());

        for (Map<String, Object> signal : signals) {
            String message = formatSignalMessage(signal);
            // Broadcast to channel (all members see it at once)
            telegramApi.sendMessage(vipChannelId, message);
        }
    }

    // -------------------------------------------------------
    // Manual Broadcast (via API trigger)
    // -------------------------------------------------------

    /**
     * Broadcast a custom admin message to all active VIP members (DM).
     *
     * @param text The message text (HTML supported)
     */
    public void broadcastAdminMessage(String text) {
        List<VipMember> activeMembers = memberService.getActiveMembers();
        log.info("Admin broadcast to {} VIP members.", activeMembers.size());
        String fullText = text + DISCLAIMER;
        for (VipMember member : activeMembers) {
            telegramApi.sendMessage(member.getTelegramUserId(), fullText);
        }
    }

    /**
     * Publishes an exclusive post to the VIP channel with content protection (anti-leak/anti-forward).
     *
     * @param title          Title of the exclusive post
     * @param content        Body text (HTML supported)
     * @param protectContent Whether to prevent forwarding/saving
     * @return Formatted message delivered
     */
    public String publishExclusivePost(String title, String content, boolean protectContent) {
        StringBuilder sb = new StringBuilder();
        if (title != null && !title.isBlank()) {
            sb.append("🔐 <b>").append(title.trim()).append("</b>\n\n");
        } else {
            sb.append("🔐 <b>VIP Эксклюзив | SmartBet.guru</b>\n\n");
        }
        sb.append(content != null ? content.trim() : "");
        sb.append(DISCLAIMER);

        String fullMessage = sb.toString();
        log.info("Publishing exclusive post to VIP channel {} (protectContent={})", vipChannelId, protectContent);
        telegramApi.sendMessage(vipChannelId, fullMessage, protectContent);
        return fullMessage;
    }

    // -------------------------------------------------------
    // Message Formatting
    // -------------------------------------------------------

    private String formatSignalMessage(Map<String, Object> signal) {
        String bk1 = stringify(signal.get("bk1"));
        String bk2 = stringify(signal.get("bk2"));
        String match = stringify(signal.get("match"));
        String sport = stringify(signal.get("sport"));
        Object yieldObj = signal.get("yield_pct");
        double yieldPct = yieldObj instanceof Number num ? num.doubleValue() : 0.0;
        String bet1 = stringify(signal.get("bet1_type"));
        String bet2 = stringify(signal.get("bet2_type"));
        Object odds1Obj = signal.get("odds1");
        Object odds2Obj = signal.get("odds2");
        double odds1 = odds1Obj instanceof Number num ? num.doubleValue() : 0.0;
        double odds2 = odds2Obj instanceof Number num ? num.doubleValue() : 0.0;

        Object safetyObj = signal.get("safety_score");
        Integer safetyScore = safetyObj instanceof Number num ? num.intValue() : null;
        Object maxStakeObj = signal.get("recommended_max_stake_rub");
        Double maxStake = maxStakeObj instanceof Number num ? num.doubleValue() : null;
        Object stabilityObj = signal.get("observed_stability_seconds");
        Integer stabilitySec = stabilityObj instanceof Number num ? num.intValue() : null;
        Object isDomesticObj = signal.get("is_domestic");
        boolean isDomestic = Boolean.TRUE.equals(isDomesticObj);

        StringBuilder sb = new StringBuilder();
        sb.append("🔐 <b>VIP Сигнал | Вилка SmartBet.guru</b>\n\n");
        sb.append(String.format("⚽ <b>%s</b> (%s)\n\n", match, sport));
        sb.append(String.format("📊 <b>Плечо 1:</b> %s → %s @ <b>%.2f</b>\n", bk1, bet1, odds1));
        sb.append(String.format("📊 <b>Плечо 2:</b> %s → %s @ <b>%.2f</b>\n\n", bk2, bet2, odds2));
        sb.append(String.format("💰 <b>Доходность: +%.1f%%</b> (гарантированный профит)\n", yieldPct));

        if (safetyScore != null) {
            String shieldIcon = safetyScore >= 80 ? "🛡️" : (safetyScore >= 50 ? "🟡" : "⚠️");
            sb.append(String.format("%s <b>Индекс безопасности счета: %d/100</b>", shieldIcon, safetyScore));
            if (isDomestic) {
                sb.append(" [🏠 Домашний рынок / Home Bias]");
            }
            sb.append("\n");
        }
        if (maxStake != null && maxStake > 0) {
            sb.append(String.format("🎯 <b>Безопасный сайзинг:</b> до %,.0f ₽ (квантованная сумма)\n", maxStake));
        }
        if (stabilitySec != null && stabilitySec > 0) {
            int mins = stabilitySec / 60;
            int secs = stabilitySec % 60;
            String stabStr = mins > 0 ? String.format("%d мин %02d сек", mins, secs) : String.format("%d сек", secs);
            sb.append(String.format("⏱️ <b>Устойчивость в линии:</b> %s\n", stabStr));
        }

        sb.append("\n🔗 <a href=\"https://smartbet.guru/surebets\">Открыть SmartBet.guru</a>");
        sb.append(DISCLAIMER);
        return sb.toString();
    }

    private String stringify(Object obj) {
        return obj != null ? obj.toString() : "N/A";
    }
}
