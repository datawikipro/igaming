package pro.datawiki.igaming.source.betfair.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "betfair")
@Getter
@Setter
public class BetfairConfig {

    private Api api = new Api();
    private Fetch fetch = new Fetch();
    private Exchange exchange = new Exchange();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://www.betfair.com/sport";
        private String apingUrl = "https://api.betfair.com/exchange/betting/rest/v1.0";
        private String appKey;
        private String sessionToken;
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 15000;
        private boolean useProxy = false;
        private String proxyHost = "100.83.113.50";
        private int proxyPort = 3128;
    }

    @Getter
    @Setter
    public static class Exchange {
        private double commissionRate = 0.02; // standard 2% Betfair commission
        private boolean deductCommission = true;
    }
}
