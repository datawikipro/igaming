package pro.datawiki.igaming.bots.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.bots.model.BotPair;
import pro.datawiki.igaming.bots.model.BotProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyndicateBotScheduler {

    private final BotRosterService rosterService;
    private final PortalApiClient apiClient;
    private final Random random = new Random();

    private static final List<String> POST_TEMPLATES = List.of(
            "Взяли с @%s отличную математическую вилку на %s! Я в %s закрыл %s в %s @ %.2f, напарник в %s перекрыл %s в %s @ %.2f. Чистая математика +%.1f%%, гарантированный профит в копилку синдиката 🚀",
            "Кросс-бук связка (%s ↔ %s) с @%s отработала идеально. Поймали расхождение линий на %s. Лимиты удержали, зафиксировали +%.1f%% математического плюса! Синдикат растёт 📈",
            "Синдикатная вилка на %s закрыта в 2 плеча с @%s! %s (%s) vs %s (%s), доходность +%.1f%%. Личные кошельки балансируем, суммарный пул строго вверх 🔒",
            "Отработали арбитражный коридор на %s вместе с @%s. Одно плечо в %s (%s), второе в %s (%s). Никакого азарта, чистый арбитраж +%.1f%% 🛡️",
            "Новая сделка синдиката: %s. Закрыли плечи за 4 секунды с @%s (%s ↔ %s). Ожидаем +%.1f%% к пулу!",
            "Синхронный вход по протоколу Handshake на %s! @%s проставил плечо в %s, я застраховал в %s. Арбитражная маржа +%.1f%% зафиксирована ⚡"
    );

    private static final List<String> COMMENT_TEMPLATES = List.of(
            "Красавцы! Отличный коэффициент выцепили 👍",
            "В Пиннакле и ДрафтКингс плотность хорошая, тоже на них заходили на днях.",
            "Грамотная связка! Синдикат уверенно идёт вверх 🚀",
            "Лимиты удержали? Отличная вилка!",
            "Парни, поздравляю с фиксацией! Ждём расчет матча.",
            "Чётко сработали! Кросс-бордер арбитраж рулит 🌍",
            "Отличная синхронизация, за 3 секунды перекрылись ⏱️",
            "Плюс в синдикатный баланс, лесенка продолжает расти 📈"
    );

    private static final List<String[]> SAMPLE_MATCHES = List.of(
            new String[]{"Манчестер Сити vs Ливерпуль", "Футбол (АПЛ)", "П1", "Х2"},
            new String[]{"Реал Мадрид vs Барселона", "Футбол (Ла Лига)", "ТБ(2.5)", "ТМ(2.5)"},
            new String[]{"Бавария vs Боруссия Д", "Футбол (Бундеслига)", "Ф1(-1)", "Ф2(+1)"},
            new String[]{"ПСЖ vs Марсель", "Футбол (Лига 1)", "Обе забьют - Да", "Обе забьют - Нет"},
            new String[]{"Интер vs Ювентус", "Футбол (Серия А)", "1Х", "П2"},
            new String[]{"Зенит vs Спартак Москва", "Футбол (РПЛ)", "П1", "Х2"},
            new String[]{"Фламенго vs Палмейрас", "Футбол (Бразилейрао)", "ТБ(2.0)", "ТМ(2.0)"},
            new String[]{"Бока Хуниорс vs Ривер Плейт", "Футбол (Аргентина)", "1Х", "П2"},
            new String[]{"Лейкерс vs Голден Стэйт", "Баскетбол (НБА)", "П1 с ОТ", "П2 с ОТ"},
            new String[]{"Бостон Селтикс vs Милуоки Бакс", "Баскетбол (НБА)", "ТБ(224.5)", "ТМ(224.5)"},
            new String[]{"Денвер Наггетс vs Финикс Санз", "Баскетбол (НБА)", "Ф1(-5.5)", "Ф2(+5.5)"},
            new String[]{"Реал Мадрид vs Олимпиакос", "Баскетбол (Евролига)", "П1", "П2"},
            new String[]{"Тампа-Бэй vs Флорида Пантерз", "Хоккей (НХЛ)", "П1 в матче", "П2 в матче"},
            new String[]{"Эдмонтон Ойлерз vs Вегас Голден Найтс", "Хоккей (НХЛ)", "ТБ(6.0)", "ТМ(6.0)"},
            new String[]{"СКА vs ЦСКА", "Хоккей (КХЛ)", "П1", "Х2"},
            new String[]{"Новак Джокович vs Карлос Алькарас", "Теннис (ATP)", "П1", "П2"},
            new String[]{"Даниил Медведев vs Янник Синнер", "Теннис (ATP)", "Ф1(+1.5 по сетам)", "Ф2(-1.5 по сетам)"},
            new String[]{"Канзас-Сити Чифс vs Сан-Франциско 49ers", "Американский футбол (NFL)", "П1 с ОТ", "П2 с ОТ"},
            new String[]{"Нью-Йорк Янкиз vs Бостон Ред Сокс", "Бейсбол (MLB)", "П1", "П2"}
    );

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Syndicate Bots Initializing 100 bots across 50 international pairs...");
        new Thread(() -> {
            try {
                Thread.sleep(4000); // Give portal a moment to boot
                initBotBalances();
                executeTradingRound(5); // Initial 5 diverse pairs trading for immediate rich state
            } catch (Exception e) {
                log.error("Startup bot sequence error: {}", e.getMessage());
            }
        }).start();
    }

    private void initBotBalances() {
        for (BotProfile bot : rosterService.getAllBots()) {
            BigDecimal amount = "RUB".equals(bot.getCurrency()) ? new BigDecimal("100000.00") : new BigDecimal("1000.00");
            apiClient.topUpWallet(bot.getUserId(), "ALL", amount, bot.getCurrency());
        }
        log.info("All 100 bots funded with initial balance of 100k RUB / 1k USD each");
    }

    @Scheduled(fixedRateString = "${syndicate.bots.interval-ms:3600000}", initialDelayString = "${syndicate.bots.initial-delay-ms:20000}")
    public void scheduledHourlyTrading() {
        log.info("Starting hourly autonomous trading round for syndicate bot pairs...");
        executeTradingRound(4);
    }

    public void executeTradingRound(int pairsToTrade) {
        List<BotPair> pairs = new ArrayList<>(rosterService.getPairs());
        Collections.shuffle(pairs);

        int count = Math.min(pairsToTrade, pairs.size());
        for (int i = 0; i < count; i++) {
            BotPair pair = pairs.get(i);
            processPairTrade(pair);
        }
    }

    private void processPairTrade(BotPair pair) {
        BotProfile bot1 = pair.getBot1();
        BotProfile bot2 = pair.getBot2();

        String[] matchInfo = SAMPLE_MATCHES.get(random.nextInt(SAMPLE_MATCHES.size()));
        String matchDesc = matchInfo[0];
        String sport = matchInfo[1];
        String leg1Type = matchInfo[2];
        String leg2Type = matchInfo[3];

        String leg1Bk = bot1.getPreferredBookmakers().get(random.nextInt(bot1.getPreferredBookmakers().size()));
        String leg2Bk = bot2.getPreferredBookmakers().get(random.nextInt(bot2.getPreferredBookmakers().size()));

        // Calculate a profitable surebet (Yield between 6.0% and 14.5%)
        double yieldPct = 6.0 + random.nextDouble() * 8.5;
        double odds1 = 2.15 + (random.nextDouble() * 0.7);
        // Harmonic sum formula: 1/odds1 + 1/odds2 = 1 / (1 + yield/100)
        double targetSum = 1.0 / (1.0 + (yieldPct / 100.0));
        double inv2 = targetSum - (1.0 / odds1);
        double odds2 = Math.max(1.5, 1.0 / inv2);

        // Stakes (approx 10,000 RUB total or $100 equivalent)
        BigDecimal totalStakeRub = new BigDecimal("10000.00");
        BigDecimal stake1 = totalStakeRub.multiply(BigDecimal.valueOf(1.0 / odds1 / targetSum)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal stake2 = totalStakeRub.subtract(stake1);

        long surebetId = System.currentTimeMillis() + random.nextInt(1000);
        long matchId = Math.abs(matchDesc.hashCode());

        // 1. Bot 1 places leg 1
        Map<String, Object> betReq1 = new HashMap<>();
        betReq1.put("surebet_id", surebetId);
        betReq1.put("match_id", matchId);
        betReq1.put("match_description", matchDesc);
        betReq1.put("seconds_to_start", 120);
        betReq1.put("legs", List.of(Map.of(
                "bookmaker", leg1Bk,
                "bet_type", leg1Type,
                "odds", odds1,
                "stake", stake1
        )));
        apiClient.placeBet(bot1.getUserId(), betReq1);

        // 2. Bot 2 places leg 2
        Map<String, Object> betReq2 = new HashMap<>();
        betReq2.put("surebet_id", surebetId);
        betReq2.put("match_id", matchId);
        betReq2.put("match_description", matchDesc);
        betReq2.put("seconds_to_start", 120);
        betReq2.put("legs", List.of(Map.of(
                "bookmaker", leg2Bk,
                "bet_type", leg2Type,
                "odds", odds2,
                "stake", stake2
        )));
        apiClient.placeBet(bot2.getUserId(), betReq2);

        // 3. Publish social post
        int templateIdx = random.nextInt(POST_TEMPLATES.size());
        String postText;
        if (templateIdx == 0) {
            postText = String.format(POST_TEMPLATES.get(0),
                    bot2.getUsername(), matchDesc, bot1.getCountry(), leg1Type, leg1Bk.toUpperCase(), odds1,
                    bot2.getCountry(), leg2Type, leg2Bk.toUpperCase(), odds2, yieldPct);
        } else if (templateIdx == 1) {
            postText = String.format(POST_TEMPLATES.get(1),
                    bot1.getCity(), bot2.getCity(), bot2.getUsername(), matchDesc, yieldPct);
        } else if (templateIdx == 2) {
            postText = String.format(POST_TEMPLATES.get(2),
                    matchDesc, bot2.getUsername(), leg1Bk.toUpperCase(), leg1Type, leg2Bk.toUpperCase(), leg2Type, yieldPct);
        } else if (templateIdx == 3) {
            postText = String.format(POST_TEMPLATES.get(3),
                    matchDesc, bot2.getUsername(), leg1Bk.toUpperCase(), bot1.getCity(), leg2Bk.toUpperCase(), bot2.getCity(), yieldPct);
        } else if (templateIdx == 4) {
            postText = String.format(POST_TEMPLATES.get(4),
                    matchDesc, bot2.getUsername(), bot1.getCountry(), bot2.getCountry(), yieldPct);
        } else {
            postText = String.format(POST_TEMPLATES.get(5),
                    matchDesc, bot2.getUsername(), leg2Bk.toUpperCase(), leg1Bk.toUpperCase(), yieldPct);
        }

        Map<String, Object> postReq = new HashMap<>();
        postReq.put("author_name", bot1.getUsername());
        postReq.put("author_avatar", bot1.getAvatar());
        postReq.put("author_country", bot1.getCountry());
        postReq.put("author_role", "SYNDICATE_OPERATOR");
        postReq.put("partner_user_id", bot2.getUserId());
        postReq.put("partner_name", bot2.getUsername());
        postReq.put("partner_avatar", bot2.getAvatar());
        postReq.put("partner_country", bot2.getCountry());
        postReq.put("match_id", matchId);
        postReq.put("match_description", matchDesc);
        postReq.put("sport", sport);
        postReq.put("surebet_id", surebetId);
        postReq.put("arb_yield", Math.round(yieldPct * 10.0) / 10.0);
        postReq.put("leg1_bookmaker", leg1Bk);
        postReq.put("leg1_bet_type", leg1Type);
        postReq.put("leg1_odds", Math.round(odds1 * 100.0) / 100.0);
        postReq.put("leg1_stake", stake1);
        postReq.put("leg2_bookmaker", leg2Bk);
        postReq.put("leg2_bet_type", leg2Type);
        postReq.put("leg2_odds", Math.round(odds2 * 100.0) / 100.0);
        postReq.put("leg2_stake", stake2);
        postReq.put("content", postText);

        Long postId = apiClient.createSocialPost(bot1.getUserId(), postReq);

        // 4. Sometimes another bot comments
        if (postId != null && random.nextBoolean()) {
            List<BotProfile> otherBots = rosterService.getAllBots().stream()
                    .filter(b -> !b.getUserId().equals(bot1.getUserId()) && !b.getUserId().equals(bot2.getUserId()))
                    .toList();

            BotProfile commenter = otherBots.get(random.nextInt(otherBots.size()));
            String commentText = COMMENT_TEMPLATES.get(random.nextInt(COMMENT_TEMPLATES.size()));

            Map<String, Object> commentReq = new HashMap<>();
            commentReq.put("author_name", commenter.getUsername());
            commentReq.put("author_avatar", commenter.getAvatar());
            commentReq.put("author_country", commenter.getCountry());
            commentReq.put("content", commentText);

            apiClient.addComment(commenter.getUserId(), postId, commentReq);
        }
    }
}
