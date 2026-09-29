package pro.datawiki.igaming.vipbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.vipbot.model.VipMember;
import pro.datawiki.igaming.vipbot.model.VipStatus;
import pro.datawiki.igaming.vipbot.repository.VipMemberRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Core business logic for VIP member lifecycle management.
 *
 * Responsibilities:
 *  - Register new VIP members (PENDING → ACTIVE flow)
 *  - Approve / decline Telegram channel join requests
 *  - Revoke access when subscription expires
 *  - Query active members for broadcast operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VipMemberService {

    private final VipMemberRepository repository;
    private final TelegramApiClient telegramApi;

    @Value("${vip.channel.id}")
    private Long vipChannelId;

    // -------------------------------------------------------
    // Member Lifecycle
    // -------------------------------------------------------

    /**
     * Called when a user sends /start to the VIP bot in a private chat.
     * Creates a PENDING member record if not already registered.
     *
     * @param telegramUserId Telegram user ID
     * @param username       Telegram username (can be null)
     * @param firstName      Telegram first name
     */
    public void onUserStarted(Long telegramUserId, String username, String firstName) {
        Optional<VipMember> existing = repository.findByTelegramUserId(telegramUserId);
        if (existing.isPresent()) {
            log.info("VIP /start: existing member {} (status={})", telegramUserId, existing.get().getStatus());
            return;
        }

        VipMember member = VipMember.builder()
                .telegramUserId(telegramUserId)
                .username(username)
                .firstName(firstName)
                .status(VipStatus.PENDING)
                .joinedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        repository.save(member);
        log.info("VIP new PENDING member registered: telegramUserId={}, username={}", telegramUserId, username);
    }

    /**
     * Activate a VIP member (after payment/verification confirmed externally).
     * Approves the pending Telegram join request so the user gets channel access.
     *
     * @param telegramUserId Telegram user ID to activate
     * @return true if successfully activated
     */
    public boolean activateMember(Long telegramUserId) {
        Optional<VipMember> opt = repository.findByTelegramUserId(telegramUserId);
        if (opt.isEmpty()) {
            log.warn("activateMember: no record for telegramUserId={}", telegramUserId);
            return false;
        }
        VipMember member = opt.get();
        if (VipStatus.ACTIVE.equals(member.getStatus())) {
            log.info("activateMember: already ACTIVE telegramUserId={}", telegramUserId);
            return true;
        }

        member.setStatus(VipStatus.ACTIVE);
        member.setUpdatedAt(Instant.now());
        repository.save(member);

        // Approve the join request so Telegram grants channel access
        telegramApi.approveChatJoinRequest(vipChannelId, telegramUserId);

        log.info("VIP member ACTIVATED: telegramUserId={}, username={}", telegramUserId, member.getUsername());
        return true;
    }

    /**
     * Revoke VIP access (subscription expired or cancelled).
     * Kicks the user from the private channel.
     *
     * @param telegramUserId Telegram user ID to revoke
     */
    public void revokeMember(Long telegramUserId) {
        Optional<VipMember> opt = repository.findByTelegramUserId(telegramUserId);
        if (opt.isEmpty()) {
            log.warn("revokeMember: no record for telegramUserId={}", telegramUserId);
            return;
        }
        VipMember member = opt.get();
        member.setStatus(VipStatus.REVOKED);
        member.setUpdatedAt(Instant.now());
        repository.save(member);

        // Kick then immediately unban so they can re-join if they resubscribe
        telegramApi.banChatMember(vipChannelId, telegramUserId);
        telegramApi.unbanChatMember(vipChannelId, telegramUserId, true);

        log.info("VIP member REVOKED: telegramUserId={}, username={}", telegramUserId, member.getUsername());
    }

    /**
     * Handle an incoming chat_join_request update.
     * If the user is ACTIVE, auto-approve. Otherwise decline and instruct to subscribe.
     *
     * @param telegramUserId User requesting to join
     * @param username       Telegram username
     * @param firstName      First name
     * @param chatId         Channel chat ID (should match vipChannelId)
     */
    public void handleJoinRequest(Long telegramUserId, String username, String firstName, Long chatId) {
        Optional<VipMember> opt = repository.findByTelegramUserId(telegramUserId);

        if (opt.isPresent() && VipStatus.ACTIVE.equals(opt.get().getStatus())) {
            telegramApi.approveChatJoinRequest(chatId, telegramUserId);
            log.info("Auto-approved join request for ACTIVE member: telegramUserId={}", telegramUserId);
        } else {
            // Register as PENDING if unknown
            if (opt.isEmpty()) {
                onUserStarted(telegramUserId, username, firstName);
            }
            telegramApi.declineChatJoinRequest(chatId, telegramUserId);
            // Send a DM with instructions
            telegramApi.sendMessage(telegramUserId,
                    "❌ <b>Доступ в VIP-канал требует активной подписки SmartBet VIP.</b>\n\n" +
                    "Чтобы получить доступ, оформите подписку на <a href=\"https://smartbet.guru/vip\">smartbet.guru/vip</a> " +
                    "и свяжите ваш аккаунт через бота. 🔐");
            log.info("Declined join request for non-VIP user: telegramUserId={}", telegramUserId);
        }
    }

    // -------------------------------------------------------
    // Queries
    // -------------------------------------------------------

    /**
     * Return all active VIP members (for broadcast operations).
     */
    public List<VipMember> getActiveMembers() {
        return repository.findAllByStatus(VipStatus.ACTIVE);
    }

    /**
     * Return all pending members (awaiting activation).
     */
    public List<VipMember> getPendingMembers() {
        return repository.findAllByStatus(VipStatus.PENDING);
    }

    /**
     * Find a member by Telegram user ID.
     */
    public Optional<VipMember> findByTelegramUserId(Long telegramUserId) {
        return repository.findByTelegramUserId(telegramUserId);
    }
}
