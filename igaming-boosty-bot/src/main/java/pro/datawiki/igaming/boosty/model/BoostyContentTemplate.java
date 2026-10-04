package pro.datawiki.igaming.boosty.model;

/**
 * Content template types for Boosty posts published by SmartBet.guru.
 *
 * <p>Each template defines:
 * <ul>
 *   <li>The type of content (signal, digest, promo, educational…)</li>
 *   <li>Minimum required subscriber tier to access the full post</li>
 *   <li>A parameterised post title prefix and body skeleton used by
 *       {@link pro.datawiki.igaming.boosty.service.BoostyPublisherService}</li>
 * </ul>
 *
 * <p>Disclaimer is appended automatically by the publisher — do NOT include it in templates.
 */
public enum BoostyContentTemplate {

    // ─────────────────────────────────────────────────────────
    // FREE tier templates — visible to all subscribers
    // ─────────────────────────────────────────────────────────

    /**
     * Weekly public digest of top arbitrage opportunities (yield ≤ 5%).
     * Posted every Monday 10:00 MSK.
     */
    FREE_WEEKLY_DIGEST(
            BoostySubscriptionTier.FREE,
            "📊 Дайджест вилок SmartBet.guru: %s",
            """
            Привет, сообщество! Делимся лучшими публичными вилками за неделю 🎯
            
            %s
            
            🔗 Сайт: https://smartbet.guru
            📊 Boosty: https://boosty.to/smartbetguru
            
            💡 Хочешь видеть вилки 20%%+? Переходи на уровень Premium (2 500 ₽/мес):
            👉 https://boosty.to/smartbetguru
            """
    ),

    /**
     * Freebet promotion announcement with 80% guaranteed cash calculation.
     * Per AGENTS.md Rule #10: every freebet post MUST include η = 80% calculation.
     */
    FREE_FREEBET_PROMO(
            BoostySubscriptionTier.FREE,
            "🎁 Фрибет от %s → %s ₽ гарантированного кэша",
            """
            🔥 Горячая акция! Букмекер %s дарит фрибет %s ₽!
            
            💰 Как получить деньги гарантированно через вилку:
            — Фрибет: %s ₽ (SNR — ставка не возвращается)
            — Перекрытие на коэф. K₁ ≈ 4.5–6.0, K₂ ≈ 1.20–1.28
            — Формула: η = (K₁-1)(K₂-1)/K₂ ≈ 0.80
            — Итог: **%s ₽ чистыми на счёт при любом исходе** 🎯
            
            📱 Калькулятор: https://smartbet.guru/tools/freebet-calculator
            🔗 Партнёрская ссылка: %s
            """
    ),

    /**
     * Educational post: how matched betting / surebets work.
     * Posted bi-weekly, open to all.
     */
    FREE_EDUCATIONAL(
            BoostySubscriptionTier.FREE,
            "📚 Обучение: %s",
            """
            Разбираем тему: **%s**
            
            %s
            
            🔗 Подробнее: https://smartbet.guru/blog
            """
    ),

    // ─────────────────────────────────────────────────────────
    // BASIC tier templates
    // ─────────────────────────────────────────────────────────

    /**
     * Daily Telegram signal digest for Basic subscribers (yield ≤ 10%).
     * Posted every day at 09:00 MSK.
     */
    BASIC_DAILY_SIGNALS(
            BoostySubscriptionTier.BASIC,
            "⚡ Сигналы дня [Базовый] — %s",
            """
            Доброе утро, подписчики уровня «Базовый»! 🌅
            
            Топ вилок на сегодня (доходность до 10%%):
            %s
            
            📊 Полная линия на сайте: https://smartbet.guru
            💬 Вопросы? Пишите в комментарии.
            """
    ),

    // ─────────────────────────────────────────────────────────
    // PREMIUM tier templates (access: 2 500 ₽/мес+)
    // ─────────────────────────────────────────────────────────

    /**
     * Exclusive Premium surebet signal (yield 20%+).
     * Posted on demand when a high-value opportunity is detected.
     */
    PREMIUM_SUREBET_SIGNAL(
            BoostySubscriptionTier.PREMIUM,
            "🔐 [PREMIUM] Вилка 20%%+ — %s",
            """
            🎯 **Эксклюзивный Premium-сигнал!**
            
            📌 Событие: %s
            ⚖️ Вилка: %s vs %s
            💰 Доходность: **%s%%**
            🏦 Букмекеры: %s / %s
            📊 Ссылки: %s
            
            ✅ Расчёт ставок:
            %s
            
            ⏰ Актуально до: %s
            """
    ),

    /**
     * Exclusive Premium corridor / range bet signal.
     * Corridor = betting on total or handicap range across two bookmakers.
     */
    PREMIUM_CORRIDOR_SIGNAL(
            BoostySubscriptionTier.PREMIUM,
            "🔐 [PREMIUM] Коридор — %s",
            """
            🎯 **Коридор (Corridor Bet) — эксклюзив Premium!**
            
            📌 Событие: %s
            ⚖️ Диапазон: от %s до %s
            💰 Потенциальная доходность: **%s%%**
            🏦 Букмекеры: %s / %s
            📊 Ссылки: %s
            
            💡 Коридор выигрывает при счёте в диапазоне.
            ✅ Расчёт ставок:
            %s
            
            ⏰ Актуально до: %s
            """
    ),

    /**
     * Exclusive Premium +EV (positive expected value) betting opportunity.
     */
    PREMIUM_PLUS_EV_SIGNAL(
            BoostySubscriptionTier.PREMIUM,
            "🔐 [PREMIUM] +EV возможность — %s",
            """
            📈 **Ставка с положительным математическим ожиданием (+EV)!**
            
            📌 Событие: %s
            🎯 Исход: %s @ %s (коэф. у БК)
            📊 Оценка истинной вероятности: %s%%
            💰 Edge (преимущество): +%s%%
            🏦 Букмекер: %s
            🔗 Ссылка: %s
            
            ⏰ Актуально до: %s
            """),

    /**
     * Weekly Premium exclusive report: top opportunities of the week, stats.
     * Posted every Sunday 20:00 MSK.
     */
    PREMIUM_WEEKLY_REPORT(
            BoostySubscriptionTier.PREMIUM,
            "📊 [PREMIUM] Отчёт недели SmartBet.guru — %s",
            """
            🏆 **Эксклюзивный Premium-отчёт за неделю!**
            
            📅 Период: %s
            
            📊 Статистика:
            — Вилок найдено: %s
            — Средняя доходность: %s%%
            — Лучшая вилка: %s%%
            — Топ-3 букмекера: %s
            
            %s
            
            💬 Обсуждение — в комментариях ниже.
            📱 Всё в реальном времени: https://smartbet.guru
            """
    );

    // ─────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────

    /** Minimum subscription tier required to access this content. */
    private final BoostySubscriptionTier minTier;

    /**
     * Post title template.
     * Placeholders: {@code %s} → filled by the publisher service.
     */
    private final String titleTemplate;

    /**
     * Post body template.
     * Placeholders: {@code %s} → filled by the publisher service.
     * NOTE: disclaimer is appended automatically — do NOT include it here.
     */
    private final String bodyTemplate;

    // ─────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────

    BoostyContentTemplate(BoostySubscriptionTier minTier, String titleTemplate, String bodyTemplate) {
        this.minTier       = minTier;
        this.titleTemplate = titleTemplate;
        this.bodyTemplate  = bodyTemplate;
    }

    // ─────────────────────────────────────────────────────────
    // Accessors
    // ─────────────────────────────────────────────────────────

    public BoostySubscriptionTier getMinTier()       { return minTier;        }
    public String                 getTitleTemplate() { return titleTemplate;  }
    public String                 getBodyTemplate()  { return bodyTemplate;   }

    /**
     * Return the minimum Boosty price in RUB required to access this post.
     * Used as the {@code price} field when creating an exclusive post via API.
     *
     * @return 0 for FREE-tier templates, subscription price otherwise
     */
    public int getMinTierPriceRub() {
        return minTier.getPriceRub();
    }
}
