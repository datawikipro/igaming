package pro.datawiki.igaming.boosty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import pro.datawiki.igaming.boosty.controller.BoostyAdminController;
import pro.datawiki.igaming.boosty.repository.BoostyDonorCommentRepository;
import pro.datawiki.igaming.boosty.service.BoostyApiClient;
import pro.datawiki.igaming.boosty.service.BoostyDonorService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BoostyContentDistributionTest {

    private BoostyApiClient apiClient;
    private BoostyDonorService donorService;
    private BoostyDonorCommentRepository commentRepository;
    private BoostyAdminController controller;

    @BeforeEach
    void setUp() {
        apiClient = mock(BoostyApiClient.class);
        donorService = mock(BoostyDonorService.class);
        commentRepository = mock(BoostyDonorCommentRepository.class);

        when(apiClient.publishPost(anyString(), anyString(), any(), any()))
                .thenReturn(Map.of("id", "boosty-post-uuid-999", "status", "published"));

        controller = new BoostyAdminController(donorService, commentRepository, apiClient);
    }

    @Test
    void testPublishExclusivePostToBoosty() {
        Map<String, Object> req = Map.of(
                "title", "Эксклюзивный гайд: конвертация фрибетов в 80% кэша",
                "content", "Разбор формулы (K1-1)(K2-1)/K2 на примере Винлайн 3000 руб.",
                "teaser", "Только для платных подписчиков уровня PRO и выше.",
                "min_tier_rub", 2500
        );

        ResponseEntity<Map<String, Object>> response = controller.publishPost(req);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));

        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> teaserCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> tierCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(apiClient).publishPost(titleCaptor.capture(), contentCaptor.capture(), teaserCaptor.capture(), tierCaptor.capture());

        assertEquals("Эксклюзивный гайд: конвертация фрибетов в 80% кэша", titleCaptor.getValue());
        assertTrue(contentCaptor.getValue().contains("Разбор формулы"));
        assertTrue(contentCaptor.getValue().contains("Ставки на спорт сопряжены с финансовыми рисками")); // Mandatory disclaimer
        assertEquals("Только для платных подписчиков уровня PRO и выше.", teaserCaptor.getValue());
        assertEquals(2500, tierCaptor.getValue());
    }
}
