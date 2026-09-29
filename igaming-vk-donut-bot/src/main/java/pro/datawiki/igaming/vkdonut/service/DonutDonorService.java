package pro.datawiki.igaming.vkdonut.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.vkdonut.model.DonutDonor;
import pro.datawiki.igaming.vkdonut.model.DonutStatus;
import pro.datawiki.igaming.vkdonut.repository.DonutDonorRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Core business logic for VK Donut donor lifecycle management.
 *
 * Responsibilities:
 *  - Register new donors (create ACTIVE record on donut_subscription_create)
 *  - Renew subscriptions (prolong)
 *  - Update amount on price_changed events
 *  - Cancel or expire subscriptions (mark CANCELLED/EXPIRED, send farewell DM)
 *  - Query active donors for broadcast operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DonutDonorService {

    private static final String DISCLAIMER =
            "\n\n⚠️ Ставки на спорт сопряжены с финансовыми рисками. " +
            "Мы против лудомании и необдуманного беттинга. Играйте ответственно.";

    private final DonutDonorRepository repository;
    private final VkApiClient vkApiClient;

    // -------------------------------------------------------
    // Donut Event Handlers
    // -------------------------------------------------------

    /**
     * Handle donut_subscription_create — new donor subscribes.
     *
     * @param vkUserId VK user ID of the new donor
     * @param amount   Monthly donation amount in rubles
     */
    public void onSubscriptionCreate(Long vkUserId, Integer amount) {
        Optional<DonutDonor> existing = repository.findByVkUserId(vkUserId);
        if (existing.isPresent() && DonutStatus.ACTIVE.equals(existing.get().getStatus())) {
            log.info("donut_subscription_create: vkUserId={} already ACTIVE, ignoring duplicate", vkUserId);
            return;
        }

        // Resolve display name for welcome message
        String displayName = vkApiClient.resolveUserName(vkUserId);

        DonutDonor donor;
        if (existing.isPresent()) {
            // Re-activation of a previously lapsed donor
            donor = existing.get();
            donor.setStatus(DonutStatus.ACTIVE);
            donor.setAmountRub(amount);
            donor.setUpdatedAt(Instant.now());
            donor.setEndedAt(null);
        } else {
            // Brand-new donor
            String[] parts = displayName.split(" ", 2);
            donor = DonutDonor.builder()
                    .vkUserId(vkUserId)
                    .firstName(parts.length > 0 ? parts[0] : "")
                    .lastName(parts.length > 1 ? parts[1] : "")
                    .amountRub(amount)
                    .status(DonutStatus.ACTIVE)
                    .subscribedAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }
        repository.save(donor);
        log.info("VK Donut: new ACTIVE donor vkUserId={}, amount={}₽", vkUserId, amount);

        // Send welcome message with mandatory disclaimer
        String welcomeText = String.format(
            "🎉 Привет, %s!\n\n" +
            "Спасибо за подписку VK Donut на SmartBet.guru! " +
            "Ты теперь в числе Premium-донов и будешь получать эксклюзивные сигналы вилок " +
            "с доходностью 20%%+ прямо сюда в личные сообщения. 🔐\n\n" +
            "💰 Твой взнос: %d ₽/мес\n" +
            "🔗 Сайт: https://smartbet.guru" +
            DISCLAIMER,
            displayName, amount);
        vkApiClient.sendMessage(vkUserId, welcomeText);
    }

    /**
     * Handle donut_subscription_prolonged — donor renewed subscription.
     *
     * @param vkUserId VK user ID
     * @param amount   Renewal amount in rubles
     */
    public void onSubscriptionProlonged(Long vkUserId, Integer amount) {
        Optional<DonutDonor> opt = repository.findByVkUserId(vkUserId);
        DonutDonor donor = opt.orElseGet(() -> {
            log.warn("donut_subscription_prolonged: unknown vkUserId={}, creating record", vkUserId);
            return DonutDonor.builder()
                    .vkUserId(vkUserId)
                    .status(DonutStatus.ACTIVE)
                    .subscribedAt(Instant.now())
                    .build();
        });
        donor.setStatus(DonutStatus.ACTIVE);
        donor.setAmountRub(amount);
        donor.setUpdatedAt(Instant.now());
        donor.setEndedAt(null);
        repository.save(donor);
        log.info("VK Donut: subscription prolonged for vkUserId={}, amount={}₽", vkUserId, amount);
    }

    /**
     * Handle donut_subscription_cancelled — donor cancelled before expiry.
     *
     * @param vkUserId VK user ID
     */
    public void onSubscriptionCancelled(Long vkUserId) {
        updateEndedStatus(vkUserId, DonutStatus.CANCELLED);

        String farewell = "💔 Твоя Premium-подписка SmartBet.guru была отменена.\n\n" +
                "Ты всегда можешь возобновить её через VK Donut и снова получать эксклюзивные сигналы." +
                DISCLAIMER;
        vkApiClient.sendMessage(vkUserId, farewell);
    }

    /**
     * Handle donut_subscription_expired — subscription ended without renewal.
     *
     * @param vkUserId VK user ID
     */
    public void onSubscriptionExpired(Long vkUserId) {
        updateEndedStatus(vkUserId, DonutStatus.EXPIRED);

        String expiredText = "⏳ Твоя Premium-подписка SmartBet.guru истекла.\n\n" +
                "Возобнови подписку через VK Donut, чтобы продолжить получать эксклюзивные сигналы вилок с доходностью 20%%+." +
                DISCLAIMER;
        vkApiClient.sendMessage(vkUserId, expiredText);
    }

    /**
     * Handle donut_subscription_price_changed — donor's amount changed.
     *
     * @param vkUserId  VK user ID
     * @param amountNew New amount in rubles
     */
    public void onSubscriptionPriceChanged(Long vkUserId, Integer amountNew) {
        Optional<DonutDonor> opt = repository.findByVkUserId(vkUserId);
        if (opt.isEmpty()) {
            log.warn("donut_subscription_price_changed: unknown vkUserId={}", vkUserId);
            return;
        }
        DonutDonor donor = opt.get();
        int old = donor.getAmountRub() != null ? donor.getAmountRub() : 0;
        donor.setAmountRub(amountNew);
        donor.setUpdatedAt(Instant.now());
        repository.save(donor);
        log.info("VK Donut: price changed for vkUserId={}: {}₽ → {}₽", vkUserId, old, amountNew);
    }

    // -------------------------------------------------------
    // Queries
    // -------------------------------------------------------

    /**
     * Return all currently active donors (for broadcast operations).
     */
    public List<DonutDonor> getActiveDonors() {
        return repository.findAllByStatus(DonutStatus.ACTIVE);
    }

    /**
     * Find a donor by VK user ID.
     */
    public Optional<DonutDonor> findByVkUserId(Long vkUserId) {
        return repository.findByVkUserId(vkUserId);
    }

    // -------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------

    private void updateEndedStatus(Long vkUserId, DonutStatus newStatus) {
        Optional<DonutDonor> opt = repository.findByVkUserId(vkUserId);
        if (opt.isEmpty()) {
            log.warn("updateEndedStatus: unknown vkUserId={}", vkUserId);
            return;
        }
        DonutDonor donor = opt.get();
        donor.setStatus(newStatus);
        donor.setUpdatedAt(Instant.now());
        donor.setEndedAt(Instant.now());
        repository.save(donor);
        log.info("VK Donut: subscription {} for vkUserId={}", newStatus, vkUserId);
    }
}
