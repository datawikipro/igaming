package pro.datawiki.igaming.bots.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotProfile {
    private Long userId;
    private String username;
    private String fullName;
    private String avatar;
    private String country;
    private String city;
    private String currency; // RUB or USD
    private List<String> preferredBookmakers;
}
