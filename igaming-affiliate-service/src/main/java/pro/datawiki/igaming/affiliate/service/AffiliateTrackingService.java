package pro.datawiki.igaming.affiliate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.affiliate.model.AffiliateClick;
import pro.datawiki.igaming.affiliate.model.AffiliateConversion;
import pro.datawiki.igaming.affiliate.model.AffiliateOffer;
import pro.datawiki.igaming.affiliate.repository.AffiliateClickRepository;
import pro.datawiki.igaming.affiliate.repository.AffiliateConversionRepository;
import pro.datawiki.igaming.affiliate.repository.AffiliateOfferRepository;
import pro.datawiki.igaming.affiliate.repository.AffiliatePartnerRepository;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateTrackingService {

    private final AffiliateOfferRepository offerRepository;
    private final AffiliateClickRepository clickRepository;
    private final AffiliateConversionRepository conversionRepository;
    private final AffiliatePartnerRepository partnerRepository;

    @Transactional
    public String generateRedirectUrl(
            String bookmakerId,
            Map<String, String> utmParams,
            String clientIp,
            String userAgent,
            String referer
    ) {
        String normalizedBk = bookmakerId.toLowerCase().trim();
        Optional<AffiliateOffer> offerOpt = offerRepository.findFirstByBookmakerIdAndIsActiveTrue(normalizedBk);

        String clickId = "sb_" + UUID.randomUUID().toString().replace("-", "");
        String ipHash = hashSha256(clientIp != null ? clientIp : "127.0.0.1");

        String template;
        Long offerId = null;

        if (offerOpt.isPresent()) {
            AffiliateOffer offer = offerOpt.get();
            template = offer.getTrackingUrlTemplate();
            offerId = offer.getId();
        } else {
            template = "https://" + normalizedBk + ".com/?ref=smartbet&clickid={click_id}";
        }

        // Macro substitutions
        String targetUrl = template
                .replace("{click_id}", clickId)
                .replace("{subid}", utmParams.getOrDefault("subid", clickId))
                .replace("{sub_id}", utmParams.getOrDefault("subid", clickId))
                .replace("{utm_source}", utmParams.getOrDefault("utm_source", "smartbet"))
                .replace("{utm_medium}", utmParams.getOrDefault("utm_medium", "cpa"))
                .replace("{utm_campaign}", utmParams.getOrDefault("utm_campaign", "general"))
                .replace("{utm_content}", utmParams.getOrDefault("utm_content", ""))
                .replace("{utm_term}", utmParams.getOrDefault("utm_term", ""));

        // Preserve and append any remaining UTMs not replaced
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(targetUrl);
        for (Map.Entry<String, String> entry : utmParams.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (val != null && !val.isBlank() && !targetUrl.contains(key + "=")) {
                uriBuilder.queryParam(key, val);
            }
        }
        if (!targetUrl.contains("subid=") && !targetUrl.contains("click_id=") && !targetUrl.contains("clickid=")) {
            uriBuilder.queryParam("subid", clickId);
        }

        String finalRedirectUrl = uriBuilder.build().toUriString();

        AffiliateClick click = AffiliateClick.builder()
                .clickId(clickId)
                .bookmakerId(normalizedBk)
                .offerId(offerId)
                .utmSource(utmParams.get("utm_source"))
                .utmMedium(utmParams.get("utm_medium"))
                .utmCampaign(utmParams.get("utm_campaign"))
                .utmContent(utmParams.get("utm_content"))
                .utmTerm(utmParams.get("utm_term"))
                .subId(utmParams.get("subid"))
                .ipHash(ipHash)
                .userAgent(userAgent != null ? (userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent) : null)
                .referer(referer != null ? (referer.length() > 500 ? referer.substring(0, 500) : referer) : null)
                .redirectUrl(finalRedirectUrl.length() > 1000 ? finalRedirectUrl.substring(0, 1000) : finalRedirectUrl)
                .status("CLICKED")
                .build();

        clickRepository.save(click);
        log.info("Redirect recorded: bk={}, clickId={}, targetUrl={}", normalizedBk, clickId, finalRedirectUrl);

        return finalRedirectUrl;
    }

    @Transactional
    public AffiliateConversion recordConversion(
            String bookmakerId,
            String clickId,
            String conversionType,
            BigDecimal amount,
            BigDecimal payout,
            String currency,
            String rawPayload
    ) {
        String normalizedBk = bookmakerId.toLowerCase().trim();
        Optional<AffiliateClick> clickOpt = clickRepository.findByClickId(clickId);
        if (clickOpt.isPresent()) {
            AffiliateClick click = clickOpt.get();
            click.setStatus("CONVERTED");
            clickRepository.save(click);
        }

        AffiliateConversion conversion = AffiliateConversion.builder()
                .clickId(clickId)
                .bookmakerId(normalizedBk)
                .conversionType(conversionType != null ? conversionType : "REGISTRATION")
                .amount(amount)
                .payout(payout)
                .currency(currency != null ? currency : "RUB")
                .status("APPROVED")
                .postbackPayload(rawPayload)
                .build();

        AffiliateConversion saved = conversionRepository.save(conversion);
        log.info("Conversion recorded: bk={}, clickId={}, payout={}", normalizedBk, clickId, payout);
        return saved;
    }

    public Map<String, Object> getBookmakerStats(String bookmakerId) {
        String normalizedBk = bookmakerId.toLowerCase().trim();
        long clicks = clickRepository.countByBookmakerId(normalizedBk);
        long conversions = conversionRepository.countByBookmakerId(normalizedBk);
        double conversionRate = clicks > 0 ? ((double) conversions / clicks) * 100.0 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("bookmakerId", normalizedBk);
        stats.put("clicks", clicks);
        stats.put("conversions", conversions);
        stats.put("conversionRatePercent", Math.round(conversionRate * 100.0) / 100.0);
        return stats;
    }

    public static String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
