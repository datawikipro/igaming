package pro.datawiki.igaming.source.sport888.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "888sport")
public class Sport888Config {

    private Api api = new Api();
    private Fetch fetch = new Fetch();

    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }
    public Fetch getFetch() { return fetch; }
    public void setFetch(Fetch fetch) { this.fetch = fetch; }

    public static class Api {
        private String baseUrl;
        private String brand = "ub";
        private String locale = "en_GB";
        private String market = "GB";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getLocale() { return locale; }
        public void setLocale(String locale) { this.locale = locale; }
        public String getMarket() { return market; }
        public void setMarket(String market) { this.market = market; }
    }

    public static class Fetch {
        private long delayMs = 15000;

        public long getDelayMs() { return delayMs; }
        public void setDelayMs(long delayMs) { this.delayMs = delayMs; }
    }
}
