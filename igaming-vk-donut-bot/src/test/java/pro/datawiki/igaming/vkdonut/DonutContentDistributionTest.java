package pro.datawiki.igaming.vkdonut;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import pro.datawiki.igaming.vkdonut.controller.DonutAdminController;
import pro.datawiki.igaming.vkdonut.service.DonutDonorService;
import pro.datawiki.igaming.vkdonut.service.DonutSignalBroadcaster;
import pro.datawiki.igaming.vkdonut.service.PortalSignalClient;
import pro.datawiki.igaming.vkdonut.service.VkApiClient;
import pro.datawiki.igaming.vkdonut.service.VkCommunityWallPoster;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DonutContentDistributionTest {

    private VkApiClient vkApiClient;
    private DonutDonorService donorService;
    private PortalSignalClient portalSignalClient;
    private DonutSignalBroadcaster broadcaster;
    private VkCommunityWallPoster communityWallPoster;
    private DonutAdminController controller;

    @BeforeEach
    void setUp() {
        vkApiClient = mock(VkApiClient.class);
        donorService = mock(DonutDonorService.class);
        portalSignalClient = mock(PortalSignalClient.class);
        communityWallPoster = mock(VkCommunityWallPoster.class);

        when(vkApiClient.postWall(anyString(), anyInt())).thenReturn(Map.of("post_id", 12345));
        when(communityWallPoster.publishCustomPost(any(), anyString())).thenReturn(Map.of("post_id", 99));

        broadcaster = new DonutSignalBroadcaster(donorService, vkApiClient, portalSignalClient);
        controller = new DonutAdminController(donorService, broadcaster, communityWallPoster);
    }

    @Test
    void testPublishExclusivePostToDonutWall() {
        String title = "Инсайд: коридор 22.4% на Евролигу";
        String content = "Реал Мадрид - Барселона. Тотал больше 160.5 @ 2.20 vs ТМ 164.5 @ 2.15.";

        Map<String, Object> res = broadcaster.publishExclusivePost(title, content, -1);

        assertNotNull(res);
        assertEquals(12345, res.get("post_id"));

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> durationCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(vkApiClient).postWall(msgCaptor.capture(), durationCaptor.capture());
        String postedMsg = msgCaptor.getValue();

        assertTrue(postedMsg.contains("🍩 Инсайд"));
        assertTrue(postedMsg.contains("Реал Мадрид - Барселона"));
        assertTrue(postedMsg.contains("Ставки на спорт сопряжены с финансовыми рисками")); // Mandatory disclaimer per Rule 9
        assertEquals(-1, durationCaptor.getValue(), "donut_paid_duration must be -1 for permanent donor exclusivity");
    }

    @Test
    void testPublishPostViaAdminController() {
        Map<String, Object> req = Map.of(
                "title", "Мануал по антифроду в VK Donut",
                "content", "Прогрев профилей Camoufox и мобильные прокси СПб.",
                "donut_paid_duration", -1
        );

        // Use the Donut-exclusive endpoint (POST /admin/donut/posts)
        ResponseEntity<Map<String, Object>> response = controller.publishDonutPost(req);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(-1, response.getBody().get("donut_paid_duration"));

        verify(vkApiClient).postWall(anyString(), eq(-1));
    }
}
