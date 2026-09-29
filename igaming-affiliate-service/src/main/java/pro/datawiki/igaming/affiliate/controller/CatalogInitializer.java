package pro.datawiki.igaming.affiliate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.affiliate.model.AffiliateOffer;
import pro.datawiki.igaming.affiliate.model.AffiliatePartner;
import pro.datawiki.igaming.affiliate.repository.AffiliateOfferRepository;
import pro.datawiki.igaming.affiliate.repository.AffiliatePartnerRepository;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogInitializer implements CommandLineRunner {

    private final AffiliatePartnerRepository partnerRepository;
    private final AffiliateOfferRepository offerRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (partnerRepository.count() > 0) {
            log.info("Affiliate 52-bookmaker catalog already initialized. Count={}", partnerRepository.count());
            return;
        }

        log.info("Initializing 52+ Bookmaker Affiliate Catalog for SmartBet.guru...");

        List<BookmakerSeed> seeds = List.of(
            // --- РФ Букмекеры (ЦУПИС / ЕРАИ) ---
            new BookmakerSeed("winline", "Винлайн", "Uffiliates / Winline Partners", "https://winline.ru",
                "https://winline.ru/signup/?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("4500.00"), "RUB"),

            new BookmakerSeed("fonbet", "Фонбет", "Uffiliates / Fonbet Affiliates", "https://fon.bet",
                "https://fon.bet/promo/freebet/?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET80", "CPA", new BigDecimal("5000.00"), "RUB"),

            new BookmakerSeed("pari", "ПАРИ", "Uffiliates / Pari Partners", "https://pari.ru",
                "https://pari.ru/promo/?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("4200.00"), "RUB"),

            new BookmakerSeed("betcity", "Бетсити", "Betcity Affiliates", "https://betcity.ru",
                "https://betcity.ru/reg/?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("3500.00"), "RUB"),

            new BookmakerSeed("baltbet", "Балтбет", "Baltbet Affiliates", "https://baltbet.ru",
                "https://baltbet.ru/registration/?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("3000.00"), "RUB"),

            new BookmakerSeed("ligastavok", "Лига Ставок", "Liga Stavok Affiliates", "https://ligastavok.ru",
                "https://ligastavok.ru/auth/register/?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("4000.00"), "RUB"),

            new BookmakerSeed("leon", "Леон", "Leon Affiliates", "https://leon.ru",
                "https://leon.ru/registration?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "RUB"),

            new BookmakerSeed("olimpbet", "Олимпбет", "Olimpbet Affiliates", "https://olimp.bet",
                "https://olimp.bet/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("3800.00"), "RUB"),

            new BookmakerSeed("tennisi", "Тенниси", "Tennisi Affiliates", "https://tennisi.bet",
                "https://tennisi.bet/signup?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("2500.00"), "RUB"),

            new BookmakerSeed("zenit", "Зенит", "Zenit Affiliates", "https://zenit.win",
                "https://zenit.win/reg?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("3000.00"), "RUB"),

            new BookmakerSeed("betboom", "БетБум", "BetBoom Affiliates", "https://betboom.ru",
                "https://betboom.ru/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("4000.00"), "RUB"),

            new BookmakerSeed("marathonbet", "Марафонбет", "Marathonbet Affiliates", "https://marathonbet.ru",
                "https://marathonbet.ru/su/join.htm?pref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "RUB"),

            new BookmakerSeed("sportbet", "Спортбет", "Sportbet Affiliates", "https://sportbet.ru",
                "https://sportbet.ru/reg?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("2000.00"), "RUB"),

            new BookmakerSeed("bettery", "Беттери", "Bettery Affiliates", "https://bettery.ru",
                "https://bettery.ru/reg?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("2500.00"), "RUB"),

            new BookmakerSeed("betm", "Бет-М", "BetM Affiliates", "https://bet-m.ru",
                "https://bet-m.ru/reg?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("2200.00"), "RUB"),

            // --- Международные / Оффшорные Букмекеры ---
            new BookmakerSeed("pinnacle", "Pinnacle", "Pinnacle Affiliates", "https://pinnacle.com",
                "https://pinnacle.com/signup?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("1xbet", "1xBet", "1xPartners", "https://1xbet.com",
                "https://1xbet.com/registration/?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET80", "REVSHARE", new BigDecimal("40.00"), "USD"),

            new BookmakerSeed("melbet", "Melbet", "Melbet Affiliates", "https://melbet.com",
                "https://melbet.com/reg?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "USD"),

            new BookmakerSeed("betwinner", "Betwinner", "Betwinner Affiliates", "https://betwinner.com",
                "https://betwinner.com/registration?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "USD"),

            new BookmakerSeed("888starz", "888starz", "888starz Partners", "https://888starz.bet",
                "https://888starz.bet/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("45.00"), "USD"),

            new BookmakerSeed("bet365", "Bet365", "Bet365 Affiliates", "https://bet365.com",
                "https://bet365.com/?affiliate=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("unibet", "Unibet", "Kindred Affiliates", "https://unibet.com",
                "https://unibet.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("bwin", "Bwin", "Entain Partners", "https://bwin.com",
                "https://bwin.com/en/sports?wm=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("sbobet", "Sbobet", "Sbobet Affiliates", "https://sbobet.com",
                "https://sbobet.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("dafabet", "Dafabet", "Dafabet Affiliates", "https://dafabet.com",
                "https://dafabet.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "USD"),

            new BookmakerSeed("betsson", "Betsson", "Betsson Group Affiliates", "https://betsson.com",
                "https://betsson.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("betsafe", "Betsafe", "Betsson Group", "https://betsafe.com",
                "https://betsafe.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("nordicbet", "Nordicbet", "Betsson Group", "https://nordicbet.com",
                "https://nordicbet.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("mrgreen", "MrGreen", "William Hill Group", "https://mrgreen.com",
                "https://mrgreen.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("leovegas", "LeoVegas", "LeoVegas Affiliates", "https://leovegas.com",
                "https://leovegas.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("888sport", "888sport", "888 Holdings", "https://888sport.com",
                "https://888sport.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("betfair", "Betfair", "Flutter Entertainment", "https://betfair.com",
                "https://betfair.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("smarkets", "Smarkets", "Smarkets Affiliates", "https://smarkets.com",
                "https://smarkets.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("25.00"), "GBP"),

            new BookmakerSeed("matchbook", "Matchbook", "Triplebet Limited", "https://matchbook.com",
                "https://matchbook.com/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("25.00"), "EUR"),

            new BookmakerSeed("stoiximan", "Stoiximan", "Kaizen Gaming", "https://stoiximan.gr",
                "https://stoiximan.gr/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("betano", "Betano", "Kaizen Gaming", "https://betano.com",
                "https://betano.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("digitain", "Digitain", "Digitain Network", "https://digitain.com",
                "https://digitain.com/demo?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "EUR"),

            new BookmakerSeed("atg", "ATG", "ATG Affiliates Sweden", "https://atg.se",
                "https://atg.se/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("25.00"), "SEK"),

            // --- Американские & Латиноамериканские БК ---
            new BookmakerSeed("draftkings", "DraftKings", "DraftKings Affiliates", "https://draftkings.com",
                "https://draftkings.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("100.00"), "USD"),

            new BookmakerSeed("fanduel", "FanDuel", "FanDuel Affiliates", "https://fanduel.com",
                "https://fanduel.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("100.00"), "USD"),

            new BookmakerSeed("betmgm", "BetMGM", "BetMGM Affiliates", "https://betmgm.com",
                "https://betmgm.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("120.00"), "USD"),

            new BookmakerSeed("caesars", "Caesars Sportsbook", "Caesars Affiliates", "https://caesars.com",
                "https://caesars.com/sportsbook/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("120.00"), "USD"),

            new BookmakerSeed("bovada", "Bovada", "Bovada Affiliates", "https://bovada.lv",
                "https://bovada.lv/join?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "USD"),

            new BookmakerSeed("wplay", "Wplay", "Wplay Affiliates Colombia", "https://wplay.co",
                "https://wplay.co/registro?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("50000.00"), "COP"),

            new BookmakerSeed("caliente", "Caliente", "Caliente Interactive Mexico", "https://caliente.mx",
                "https://caliente.mx/registro?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "CPA", new BigDecimal("500.00"), "MXN"),

            new BookmakerSeed("betnacional", "Betnacional", "NSX Group Brazil", "https://betnacional.com",
                "https://betnacional.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "BRL"),

            new BookmakerSeed("pixbet", "Pixbet", "Pixbet Brazil", "https://pixbet.com",
                "https://pixbet.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "BRL"),

            new BookmakerSeed("galera", "Galera.bet", "Galera Group Brazil", "https://galera.bet",
                "https://galera.bet/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "BRL"),

            new BookmakerSeed("estrelabet", "EstrelaBet", "EstrelaBet Brazil", "https://estrelabet.com",
                "https://estrelabet.com/register?ref=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "BRL"),

            // --- Крипто-Букмекеры ---
            new BookmakerSeed("stake", "Stake", "Stake Affiliates", "https://stake.com",
                "https://stake.com/?c=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("45.00"), "USD"),

            new BookmakerSeed("rollbit", "Rollbit", "Rollbit Partners", "https://rollbit.com",
                "https://rollbit.com/referral/smartbet?subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("40.00"), "USD"),

            new BookmakerSeed("bcgame", "BC.Game", "BC.Game Affiliates", "https://bc.game",
                "https://bc.game/i-smartbet-n/?subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("45.00"), "USD"),

            new BookmakerSeed("duelbits", "Duelbits", "Duelbits Affiliates", "https://duelbits.com",
                "https://duelbits.com/?a=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("35.00"), "USD"),

            new BookmakerSeed("cloudbet", "Cloudbet", "Cloudbet Affiliates", "https://cloudbet.com",
                "https://cloudbet.com/en/?af_token=smartbet&subid={click_id}&utm_source={utm_source}&utm_campaign={utm_campaign}",
                "SMARTBET", "REVSHARE", new BigDecimal("30.00"), "USD")
        );

        for (BookmakerSeed s : seeds) {
            AffiliatePartner partner = AffiliatePartner.builder()
                    .bookmakerId(s.bookmakerId)
                    .name(s.name)
                    .networkName(s.networkName)
                    .websiteUrl(s.websiteUrl)
                    .loginUrl(s.websiteUrl + "/affiliates")
                    .status("ACTIVE")
                    .build();
            AffiliatePartner savedPartner = partnerRepository.save(partner);

            AffiliateOffer offer = AffiliateOffer.builder()
                    .partner(savedPartner)
                    .bookmakerId(s.bookmakerId)
                    .offerName(s.name + " Default Affiliate Offer")
                    .modelType(s.modelType)
                    .baseRate(s.baseRate)
                    .currency(s.currency)
                    .promoCode(s.promoCode)
                    .trackingUrlTemplate(s.trackingTemplate)
                    .deepLinkSupported(true)
                    .isActive(true)
                    .build();
            offerRepository.save(offer);
        }

        log.info("Successfully initialized {} bookmaker affiliate programs!", seeds.size());
    }

    private record BookmakerSeed(
            String bookmakerId,
            String name,
            String networkName,
            String websiteUrl,
            String trackingTemplate,
            String promoCode,
            String modelType,
            BigDecimal baseRate,
            String currency
    ) {}
}
