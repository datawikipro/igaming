package pro.datawiki.igaming.vipbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import pro.datawiki.igaming.vipbot.controller.VipAdminController;
import pro.datawiki.igaming.vipbot.service.PortalSignalClient;
import pro.datawiki.igaming.vipbot.service.TelegramApiClient;
import pro.datawiki.igaming.vipbot.service.VipMemberService;
import pro.datawiki.igaming.vipbot.service.VipSignalBroadcaster;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VipContentDistributionTest {

    private TelegramApiClient telegramApi;
    private VipMemberService memberService;
    private PortalSignalClient portalSignalClient;
    private VipSignalBroadcaster broadcaster;
    private VipAdminController controller;

    private static final Long VIP_CHANNEL_ID = -1001234567890L;

    @BeforeEach
    void setUp() {
        telegramApi = mock(TelegramApiClient.class);
        memberService = mock(VipMemberService.class);
        portalSignalClient = mock(PortalSignalClient.class);

        broadcaster = new VipSignalBroadcaster(memberService, telegramApi, portalSignalClient);
        ReflectionTestUtils.setField(broadcaster, "vipChannelId", VIP_CHANNEL_ID);

        controller = new VipAdminController(memberService, broadcaster);
    }

    @Test
    void testPublishExclusivePostWithProtection() {
        String title = "Жирная связка: 18.5% коридор ЦСКА - Зенит";
        String content = "Handicap corridor: Pinnacle (+4.5 @ 2.15) vs Winline (-2.5 @ 2.25)";

        String formatted = broadcaster.publishExclusivePost(title, content, true);

        // Verify message content
        assertTrue(formatted.contains("Жирная связка"));
        assertTrue(formatted.contains("Handicap corridor"));
        // Check mandatory disclaimer per Rule 9
        assertTrue(formatted.contains("Ставки на спорт сопряжены с финансовыми рисками"));

        // Verify telegram call with protect_content = true
        ArgumentCaptor<Long> chatCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> protectCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(telegramApi).sendMessage(chatCaptor.capture(), msgCaptor.capture(), protectCaptor.capture());
        assertEquals(VIP_CHANNEL_ID, chatCaptor.getValue());
        assertTrue(protectCaptor.getValue(), "Telegram VIP posts must have protect_content=true to prevent forwarding");
    }

    @Test
    void testPublishPostViaAdminController() {
        Map<String, Object> req = Map.of(
                "title", "Дайджест фрибетов: 80% кэша",
                "content", "Фрибет 3 000 ₽ -> 2 400 ₽ гарантированного кэша",
                "protect_content", true
        );

        ResponseEntity<Map<String, Object>> response = controller.publishPost(req);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertTrue((Boolean) response.getBody().get("protect_content"));
        assertEquals(VIP_CHANNEL_ID, response.getBody().get("channel_id"));

        verify(telegramApi).sendMessage(eq(VIP_CHANNEL_ID), anyString(), eq(true));
    }
}
