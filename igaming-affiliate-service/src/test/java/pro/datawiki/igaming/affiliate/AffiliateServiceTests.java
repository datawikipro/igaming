package pro.datawiki.igaming.affiliate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pro.datawiki.igaming.affiliate.model.AffiliateClick;
import pro.datawiki.igaming.affiliate.model.AffiliateConversion;
import pro.datawiki.igaming.affiliate.repository.AffiliateClickRepository;
import pro.datawiki.igaming.affiliate.repository.AffiliateConversionRepository;
import pro.datawiki.igaming.affiliate.repository.AffiliatePartnerRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AffiliateServiceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AffiliatePartnerRepository partnerRepository;

    @Autowired
    private AffiliateClickRepository clickRepository;

    @Autowired
    private AffiliateConversionRepository conversionRepository;

    @Test
    @DisplayName("52+ Bookmaker Affiliate Catalog Seeded on Startup")
    void testCatalogInitialization() {
        long count = partnerRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(52);

        assertThat(partnerRepository.findByBookmakerId("winline")).isPresent();
        assertThat(partnerRepository.findByBookmakerId("fonbet")).isPresent();
        assertThat(partnerRepository.findByBookmakerId("pinnacle")).isPresent();
        assertThat(partnerRepository.findByBookmakerId("1xbet")).isPresent();
        assertThat(partnerRepository.findByBookmakerId("draftkings")).isPresent();
        assertThat(partnerRepository.findByBookmakerId("stake")).isPresent();
    }

    @Test
    @DisplayName("302 Redirect with UTM Preservation and SHA-256 IP Hashing")
    void testRedirectTrackingAndIpHashing() throws Exception {
        mockMvc.perform(get("/go/winline")
                        .param("utm_source", "telegram")
                        .param("utm_campaign", "surebet_123")
                        .param("utm_medium", "channel")
                        .header("X-Forwarded-For", "188.242.33.93")
                        .header("User-Agent", "Mozilla/5.0 (SmartBet Bot)"))
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("winline.ru")))
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("utm_source=telegram")))
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("utm_campaign=surebet_123")));

        List<AffiliateClick> clicks = clickRepository.findAll();
        assertThat(clicks).isNotEmpty();
        AffiliateClick lastClick = clicks.get(clicks.size() - 1);
        assertThat(lastClick.getBookmakerId()).isEqualTo("winline");
        assertThat(lastClick.getUtmSource()).isEqualTo("telegram");
        assertThat(lastClick.getUtmCampaign()).isEqualTo("surebet_123");
        // Verify SHA-256 IP hash length (64 hex characters) and not raw IP
        assertThat(lastClick.getIpHash()).hasSize(64);
        assertThat(lastClick.getIpHash()).doesNotContain("188.242.33.93");
    }

    @Test
    @DisplayName("Postback Webhook Records Conversion and Updates Click Status")
    void testPostbackConversion() throws Exception {
        // First simulate a click to get a clickId
        mockMvc.perform(get("/go/fonbet")
                        .param("utm_source", "promo_radar")
                        .header("X-Forwarded-For", "195.10.20.30"))
                .andExpect(status().isFound());

        List<AffiliateClick> clicks = clickRepository.findAll();
        AffiliateClick click = clicks.get(clicks.size() - 1);
        String clickId = click.getClickId();

        // Send postback webhook
        mockMvc.perform(get("/api/affiliate/postback/fonbet")
                        .param("click_id", clickId)
                        .param("conversion_type", "FIRST_DEPOSIT")
                        .param("amount", "1000.00")
                        .param("payout", "5000.00")
                        .param("currency", "RUB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        List<AffiliateConversion> conversions = conversionRepository.findByClickId(clickId);
        assertThat(conversions).hasSize(1);
        AffiliateConversion conv = conversions.get(0);
        assertThat(conv.getPayout()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(conv.getConversionType()).isEqualTo("FIRST_DEPOSIT");

        AffiliateClick updatedClick = clickRepository.findByClickId(clickId).orElseThrow();
        assertThat(updatedClick.getStatus()).isEqualTo("CONVERTED");
    }

    @Test
    @DisplayName("Admin Security: Rejects Unauthorized Access and Accepts Secret/CF Email")
    void testAdminSecurity() throws Exception {
        // Without header -> 403 Forbidden
        mockMvc.perform(get("/api/affiliate/admin/offers"))
                .andExpect(status().isForbidden());

        // With invalid header -> 403 Forbidden
        mockMvc.perform(get("/api/affiliate/admin/offers")
                        .header("X-Affiliate-Admin-Secret", "wrong_secret"))
                .andExpect(status().isForbidden());

        // With valid secret -> 200 OK
        mockMvc.perform(get("/api/affiliate/admin/offers")
                        .header("X-Affiliate-Admin-Secret", "test_secret_123"))
                .andExpect(status().isOk());

        // With Cloudflare Access email -> 200 OK
        mockMvc.perform(get("/api/affiliate/admin/offers")
                        .header("Cf-Access-Authenticated-User-Email", "admin@smartbet.guru"))
                .andExpect(status().isOk());
    }
}
