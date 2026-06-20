package pro.datawiki.igaming.source.sport888.dto.spectate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpectateSelection {
    private Long id;
    private String name;
    private String decimal_price;
    private String type;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDecimal_price() { return decimal_price; }
    public void setDecimal_price(String decimal_price) { this.decimal_price = decimal_price; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
