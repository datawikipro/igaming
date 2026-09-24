package pro.datawiki.igaming.source.wplay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.wplay")
public class WplayConfig {
    private String baseUrl = "https://apuestas.wplay.co";
    private int connectTimeout = 10000;
    private int readTimeout = 15000;
    private int targetEventCount = 600;

    private List<String> sports = List.of(
            "/es/live",
            "/es/s/FOOT/F%C3%BAtbol",
            "/es/s/BASK/Baloncesto",
            "/es/s/BASE/B%C3%A9isbol",
            "/es/s/ICEH/Hockey",
            "/es/s/AMFO/F%C3%BAtbol-Americano",
            "/es/s/TABL/Tenis-de-Mesa",
            "/es/s/BOXI/Boxeo",
            "/es/s/ESPS/eSports",
            "/es/s/CYCL/Ciclismo-en-Ruta",
            "/es/s/DART/Dardos",
            "/es/s/GOLF/Golf",
            "/es/s/HAND/Balonmano",
            "/es/s/RUGL/Rugby-League",
            "/es/s/MMAF/UFC",
            "/es/s/MOTO/Automovilismo"
    );
}
