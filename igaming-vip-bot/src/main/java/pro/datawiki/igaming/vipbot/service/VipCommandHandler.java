package pro.datawiki.igaming.vipbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.vipbot.model.VipMember;
import pro.datawiki.igaming.vipbot.model.VipStatus;
import pro.datawiki.igaming.vipbot.telegram.TelegramCallbackQuery;
import pro.datawiki.igaming.vipbot.telegram.TelegramUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles user commands and inline keyboard callbacks for @SmartBetVipBot.
 *
 * Supported commands:
 *   /start   — Welcome message + subscription instructions
 *   /status  — Show current VIP membership status
 *   /help    — Show available commands
 *   /link    — (ACTIVE only) Get the VIP channel invite link
 *   /stats   — (ACTIVE only) Brief platform statistics teaser
 *
 * Callback data:
 *   vip:subscribe  — Respond with payment/portal link
 *   vip:status     — Show membership status inline
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VipCommandHandler {

    private static final String DISCLAIMER =
            "\n\n⚠️ <i>Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно.</i>";

    private final VipMemberService memberService;
    private final TelegramApiClient telegramApi;

    @Value("${vip.channel.id}")
    private Long vipChannelId;

    @Value("${vip.portal.signup-url:https://smartbet.guru/vip}")
    private String portalSignupUrl;

    // -------------------------------------------------------
    // Command Handler
    // -------------------------------------------------------

    public void handle(Long chatId, TelegramUser user, String text) {
        String cmd = text.contains(" ") ? text.split(" ")[0].toLowerCase() : text.toLowerCase();

        switch (cmd) {
            case "/start"  -> onStart(chatId, user);
            case "/status" -> onStatus(chatId, user);
            case "/help"   -> onHelp(chatId);
            case "/link"   -> onLink(chatId, user);
            case "/stats"  -> onStats(chatId, user);
            default        -> {
                // Ignore unknown messages silently
                log.debug("Unknown command '{}' from userId={}", cmd, user.getId());
            }
        }
    }

    // -------------------------------------------------------
    // Callback Handler
    // -------------------------------------------------------

    public void handleCallback(TelegramCallbackQuery cbq) {
        String data = cbq.getData();
        Long chatId = cbq.getMessage() != null && cbq.getMessage().getChat() != null
                ? cbq.getMessage().getChat().getId()
                : cbq.getFrom().getId();

        switch (data) {
            case "vip:subscribe" -> {
                telegramApi.answerCallbackQuery(cbq.getId(), null);
                onStart(chatId, cbq.getFrom()); // re-show start with subscribe button
            }
            case "vip:status" -> {
                telegramApi.answerCallbackQuery(cbq.getId(), null);
                onStatus(chatId, cbq.getFrom());
            }
            default -> telegramApi.answerCallbackQuery(cbq.getId(), "Неизвестная команда");
        }
    }

    // -------------------------------------------------------
    // Command Implementations
    // -------------------------------------------------------

    private void onStart(Long chatId, TelegramUser user) {
        memberService.onUserStarted(user.getId(), user.getUsername(), user.getFirstName());

        String welcome = String.format(
            "👋 Привет, <b>%s</b>! Добро пожаловать в <b>@SmartBetVipBot</b> — " +
            "закрытое VIP-сообщество SmartBet.guru.\n\n" +
            "🏆 <b>Что вы получаете как VIP-участник:</b>\n" +
            "  • 🔥 Эксклюзивные вилки с доходностью <b>+5%%–20%%</b>\n" +
            "  • ⚡ Сигналы в реальном времени\n" +
            "  • 🎁 Фрибет-дайджест с расчётом 80%% гарантированного кэша\n" +
            "  • 🤝 Приоритетная поддержка\n\n" +
            "💳 Для подключения VIP зайдите на портал и оформите подписку.\n" +
            "После оплаты вам будет предоставлен доступ в закрытый канал автоматически.\n" +
            DISCLAIMER,
            user.getFirstName() != null ? user.getFirstName() : "беттор"
        );

        Map<String, Object> keyboard = buildInlineKeyboard(List.of(
            List.of(
                buildButton("💳 Оформить VIP-подписку", portalSignupUrl, null),
                buildButton("📊 Мой статус", null, "vip:status")
            )
        ));

        telegramApi.sendMessageWithKeyboard(chatId, welcome, keyboard);
    }

    private void onStatus(Long chatId, TelegramUser user) {
        Optional<VipMember> opt = memberService.findByTelegramUserId(user.getId());

        if (opt.isEmpty()) {
            telegramApi.sendMessage(chatId,
                "ℹ️ Вы ещё не зарегистрированы в системе VIP.\n" +
                "Отправьте /start для начала регистрации.");
            return;
        }

        VipMember member = opt.get();
        String statusEmoji = switch (member.getStatus()) {
            case ACTIVE  -> "✅";
            case PENDING -> "⏳";
            case REVOKED -> "❌";
            case BANNED  -> "🚫";
        };

        String statusRu = switch (member.getStatus()) {
            case ACTIVE  -> "Активна";
            case PENDING -> "Ожидает подтверждения";
            case REVOKED -> "Отозвана";
            case BANNED  -> "Заблокирован";
        };

        String msg = String.format(
            "📋 <b>Ваш VIP-статус</b>\n\n" +
            "Telegram ID: <code>%d</code>\n" +
            "Статус подписки: %s <b>%s</b>\n" +
            (VipStatus.ACTIVE.equals(member.getStatus())
                ? "\nОтправьте /link чтобы получить ссылку-приглашение в VIP-канал."
                : "\nДля активации посетите <a href=\"%s\">smartbet.guru/vip</a>"),
            member.getTelegramUserId(), statusEmoji, statusRu, portalSignupUrl
        );

        telegramApi.sendMessage(chatId, msg);
    }

    private void onHelp(Long chatId) {
        telegramApi.sendMessage(chatId,
            "🤖 <b>Доступные команды @SmartBetVipBot:</b>\n\n" +
            "/start — Приветствие и инструкции по подписке\n" +
            "/status — Проверить статус VIP-подписки\n" +
            "/link — Получить ссылку в VIP-канал (только для ACTIVE)\n" +
            "/stats — Краткая статистика платформы\n" +
            "/help — Эта справка\n\n" +
            "По вопросам: @SmartBetSupport");
    }

    private void onLink(Long chatId, TelegramUser user) {
        Optional<VipMember> opt = memberService.findByTelegramUserId(user.getId());

        if (opt.isEmpty() || !VipStatus.ACTIVE.equals(opt.get().getStatus())) {
            telegramApi.sendMessage(chatId,
                "🔒 Ссылка доступна только для активных VIP-участников.\n" +
                "Оформите подписку: <a href=\"" + portalSignupUrl + "\">smartbet.guru/vip</a>");
            return;
        }

        String link = telegramApi.createChatInviteLink(vipChannelId,
                "VIP-link-" + user.getId(), 1);

        if (link != null) {
            telegramApi.sendMessage(chatId,
                "🔗 <b>Ваша персональная ссылка в VIP-канал:</b>\n" + link +
                "\n\n⚠️ Ссылка одноразовая и предназначена только для вас.");
        } else {
            telegramApi.sendMessage(chatId,
                "⚠️ Не удалось сгенерировать ссылку. Попробуйте позже или обратитесь в поддержку @SmartBetSupport.");
        }
    }

    private void onStats(Long chatId, TelegramUser user) {
        Optional<VipMember> opt = memberService.findByTelegramUserId(user.getId());
        boolean isActive = opt.isPresent() && VipStatus.ACTIVE.equals(opt.get().getStatus());

        int activeCount = memberService.getActiveMembers().size();

        if (!isActive) {
            telegramApi.sendMessage(chatId,
                String.format(
                    "📊 <b>SmartBet.guru — платформа для арбитражного беттинга</b>\n\n" +
                    "🔐 VIP-сообщество: <b>%d</b> активных участников\n" +
                    "🔥 Сигналы: эксклюзивные вилки +5%%–20%%\n\n" +
                    "Станьте VIP: <a href=\"%s\">smartbet.guru/vip</a>" + DISCLAIMER,
                    activeCount, portalSignupUrl
                ));
        } else {
            telegramApi.sendMessage(chatId,
                String.format(
                    "📊 <b>VIP-статистика SmartBet.guru</b>\n\n" +
                    "🔐 Активных VIP-участников: <b>%d</b>\n" +
                    "🔥 Сигналы категории: <b>Premium (>5%%)</b>\n" +
                    "💰 Средняя доходность сигналов: <b>+8.4%%</b>\n" +
                    "📈 Прибыльных вилок за месяц: <b>127</b>\n\n" +
                    "🔗 Платформа: <a href=\"https://smartbet.guru\">smartbet.guru</a>" + DISCLAIMER,
                    activeCount
                ));
        }
    }

    // -------------------------------------------------------
    // Keyboard Builders
    // -------------------------------------------------------

    private Map<String, Object> buildInlineKeyboard(List<List<Map<String, Object>>> rows) {
        Map<String, Object> keyboard = new HashMap<>();
        keyboard.put("inline_keyboard", rows);
        return keyboard;
    }

    private Map<String, Object> buildButton(String text, String url, String callbackData) {
        Map<String, Object> btn = new HashMap<>();
        btn.put("text", text);
        if (url != null) btn.put("url", url);
        if (callbackData != null) btn.put("callback_data", callbackData);
        return btn;
    }
}
