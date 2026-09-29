package pro.datawiki.igaming.boosty.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.boosty.boosty.BoostySubscriber;
import pro.datawiki.igaming.boosty.model.BoostyDonor;
import pro.datawiki.igaming.boosty.model.BoostyDonorStatus;
import pro.datawiki.igaming.boosty.repository.BoostyDonorRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core business logic for Boosty donor lifecycle management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Upsert donors when they appear in the subscribers poll</li>
 *   <li>Mark donors as CANCELLED/EXPIRED when they disappear from the subscriber list</li>
 *   <li>Send Telegram welcome/farewell messages via {@link BoostyTelegramNotifier}</li>
 *   <li>Provide donor queries for broadcast and comment-filter operations</li>
 * </ul>
 *
 * <p>Mandatory disclaimer appended to all outbound messages per AGENTS.md Rule #10.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoostyDonorService {

    private static final String DISCLAIMER =
            "\n\n⚠️ Ставки на спорт сопряжены с финансовыми рисками. " +
            "Мы против лудомании и необдуманного беттинга. Играйте ответственно.";

    private final BoostyDonorRepository repository;
    private final BoostyTelegramNotifier telegramNotifier;

    // ─────────────────────────────────────────────────────────
    // Reconciliation: sync subscribers from Boosty API snapshot
    // ─────────────────────────────────────────────────────────

    /**
     * Reconcile the current Boosty subscriber list against the database.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Upsert every subscriber from the API snapshot (create ACTIVE or re-activate)</li>
     *   <li>Find any ACTIVE DB records that are missing from the current snapshot → mark EXPIRED</li>
     * </ol>
     *
     * @param currentSubscribers live subscriber list from Boosty API
     */
    @Transactional
    public void reconcile(List<BoostySubscriber> currentSubscribers) {
        Set<Long> apiUserIds = currentSubscribers.stream()
                .map(BoostySubscriber::getId)
                .collect(Collectors.toSet());

        // Step 1: upsert each subscriber from the API
        for (BoostySubscriber sub : currentSubscribers) {
            if (sub.getId() == null) {
                log.warn("BoostyDonorService: subscriber with null id skipped");
                continue;
            }
            upsertSubscriber(sub);
        }

        // Step 2: expire donors who disappeared from the subscriber list
        List<BoostyDonor> activeDonors = repository.findAllByStatus(BoostyDonorStatus.ACTIVE);
        for (BoostyDonor donor : activeDonors) {
            if (!apiUserIds.contains(donor.getBoostyUserId())) {
                log.info("BoostyDonorService: donor {} ({}) not in snapshot → marking EXPIRED",
                        donor.getBoostyUserId(), donor.getDisplayName());
                donor.setStatus(BoostyDonorStatus.EXPIRED);
                donor.setUpdatedAt(Instant.now());
                donor.setEndedAt(Instant.now());
                repository.save(donor);
                sendFarewellMessage(donor);
            }
        }

        log.debug("BoostyDonorService: reconcile complete — API={} active, DB ACTIVE={}",
                currentSubscribers.size(), activeDonors.size());
    }

    // ─────────────────────────────────────────────────────────
    // Queries
    // ─────────────────────────────────────────────────────────

    /** Return all currently active donors. */
    public List<BoostyDonor> getActiveDonors() {
        return repository.findAllByStatus(BoostyDonorStatus.ACTIVE);
    }

    /** Find donor by Boosty user ID. */
    public Optional<BoostyDonor> findByBoostyUserId(Long boostyUserId) {
        return repository.findByBoostyUserId(boostyUserId);
    }

    /** Check if the given Boosty user ID is an active donor. */
    public boolean isActiveDonor(Long boostyUserId) {
        return repository.findByBoostyUserId(boostyUserId)
                .map(d -> BoostyDonorStatus.ACTIVE.equals(d.getStatus()))
                .orElse(false);
    }

    // ─────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────

    private void upsertSubscriber(BoostySubscriber sub) {
        Optional<BoostyDonor> existing = repository.findByBoostyUserId(sub.getId());
        boolean isNew = existing.isEmpty();
        boolean wasInactive = existing.isPresent() &&
                !BoostyDonorStatus.ACTIVE.equals(existing.get().getStatus());

        BoostyDonor donor = existing.orElseGet(() -> BoostyDonor.builder()
                .boostyUserId(sub.getId())
                .subscribedAt(Instant.now())
                .build());

        donor.setUsername(sub.getUsername());
        donor.setDisplayName(sub.getName());
        donor.setAmountRub(sub.getPrice());
        donor.setSubscriptionLevel(
                sub.getLevel() != null ? sub.getLevel().getTitle() : null);
        donor.setStatus(BoostyDonorStatus.ACTIVE);
        donor.setUpdatedAt(Instant.now());
        donor.setEndedAt(null);
        repository.save(donor);

        if (isNew || wasInactive) {
            log.info("BoostyDonorService: {} donor boostyUserId={} ({}) amount={}₽ level={}",
                    isNew ? "NEW" : "RE-ACTIVATED",
                    donor.getBoostyUserId(), donor.getDisplayName(),
                    donor.getAmountRub(), donor.getSubscriptionLevel());
            sendWelcomeMessage(donor);
        }
    }

    private void sendWelcomeMessage(BoostyDonor donor) {
        String name = donor.getDisplayName() != null ? donor.getDisplayName() : "Донор";
        int amount = donor.getAmountRub() != null ? donor.getAmountRub() : 0;
        String level = donor.getSubscriptionLevel() != null ? donor.getSubscriptionLevel() : "Premium";

        String text = String.format(
                "🎉 Спасибо за поддержку на Boosty, %s!%n%n" +
                "Ты теперь в числе Premium-донов SmartBet.guru (уровень «%s») " +
                "и будешь получать эксклюзивные сигналы вилок с доходностью 20%%+. 🔐%n%n" +
                "💰 Твой взнос: %d ₽/мес%n" +
                "🔗 Сайт: https://smartbet.guru%n" +
                "📊 Boosty: https://boosty.to/smartbetguru" +
                DISCLAIMER,
                name, level, amount);

        telegramNotifier.sendToDonorChat(
                "🆕 Новый Boosty-донор: " + name + " (" + amount + " ₽/мес, уровень " + level + ")\n" + text);
    }

    private void sendFarewellMessage(BoostyDonor donor) {
        String name = donor.getDisplayName() != null ? donor.getDisplayName() : "Донор";

        String text = String.format(
                "💔 Подписка Boosty донора %s истекла.%n%n" +
                "Возобнови поддержку на https://boosty.to/smartbetguru, " +
                "чтобы продолжать получать эксклюзивные Premium-сигналы." +
                DISCLAIMER,
                name);

        telegramNotifier.sendToDonorChat("⏳ Boosty-донор отписался: " + name + "\n" + text);
    }
}
