package pro.datawiki.igaming.affiliate.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.affiliate.model.AffiliateOffer;
import pro.datawiki.igaming.affiliate.model.AffiliatePartner;
import pro.datawiki.igaming.affiliate.repository.AffiliateOfferRepository;
import pro.datawiki.igaming.affiliate.repository.AffiliatePartnerRepository;
import pro.datawiki.igaming.affiliate.service.AffiliateTrackingService;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/affiliate/admin")
@RequiredArgsConstructor
public class AdminAffiliateController {

    private final AffiliateOfferRepository offerRepository;
    private final AffiliatePartnerRepository partnerRepository;
    private final AffiliateTrackingService trackingService;

    @Value("${affiliate.admin.secret:sb_admin_cf_secret_2026}")
    private String adminSecret;

    @Value("${affiliate.require-cf-access:false}")
    private boolean requireCfAccess;

    private boolean isAuthorized(HttpServletRequest request) {
        String cfEmail = request.getHeader("Cf-Access-Authenticated-User-Email");
        if (cfEmail != null && !cfEmail.isBlank()) {
            return true;
        }
        if (requireCfAccess) {
            return false;
        }
        String secret = request.getHeader("X-Affiliate-Admin-Secret");
        return adminSecret.equals(secret);
    }

    @GetMapping("/offers")
    public ResponseEntity<?> getAllOffers(HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized Zero Trust access"));
        }
        return ResponseEntity.ok(offerRepository.findAll());
    }

    @PostMapping("/offers")
    public ResponseEntity<?> saveOffer(@RequestBody AffiliateOffer offer, HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized Zero Trust access"));
        }
        AffiliateOffer saved = offerRepository.save(offer);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/partners")
    public ResponseEntity<?> getAllPartners(HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized Zero Trust access"));
        }
        return ResponseEntity.ok(partnerRepository.findAll());
    }

    @GetMapping("/stats/{bookmaker}")
    public ResponseEntity<?> getStats(@PathVariable("bookmaker") String bookmaker, HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized Zero Trust access"));
        }
        return ResponseEntity.ok(trackingService.getBookmakerStats(bookmaker));
    }
}
