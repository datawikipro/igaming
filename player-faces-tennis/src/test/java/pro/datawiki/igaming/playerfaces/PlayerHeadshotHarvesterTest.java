package pro.datawiki.igaming.playerfaces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;
import pro.datawiki.igaming.playerfaces.harvester.PlayerHeadshotHarvester;
import pro.datawiki.igaming.playerfaces.harvester.SofaScorePlayerHarvester;
import pro.datawiki.igaming.playerfaces.harvester.TheSportsDbHarvester;
import pro.datawiki.igaming.playerfaces.harvester.WikidataHeadshotHarvester;
import pro.datawiki.igaming.playerfaces.harvester.WikimediaCommonsHarvester;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerHeadshotHarvesterTest {

    private TheSportsDbHarvester theSportsDbHarvester;
    private WikidataHeadshotHarvester wikidataHarvester;
    private WikimediaCommonsHarvester wikimediaHarvester;
    private SofaScorePlayerHarvester sofaScoreHarvester;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        theSportsDbHarvester = new TheSportsDbHarvester(builder);
        wikidataHarvester = new WikidataHeadshotHarvester(builder);
        wikimediaHarvester = new WikimediaCommonsHarvester(builder);
        sofaScoreHarvester = new SofaScorePlayerHarvester(builder);
    }

    @Test
    @DisplayName("Should verify provider identifiers and default ordering")
    void testProviderIdentifiersAndOrder() {
        assertThat(theSportsDbHarvester.getProvider()).isEqualTo(AvatarSourceProvider.THE_SPORTS_DB);
        assertThat(wikidataHarvester.getProvider()).isEqualTo(AvatarSourceProvider.WIKIDATA);
        assertThat(wikimediaHarvester.getProvider()).isEqualTo(AvatarSourceProvider.WIKIMEDIA);
        assertThat(sofaScoreHarvester.getProvider()).isEqualTo(AvatarSourceProvider.SOFASCORE);

        assertThat(theSportsDbHarvester.getOrder()).isLessThan(wikidataHarvester.getOrder());
        assertThat(wikidataHarvester.getOrder()).isLessThan(wikimediaHarvester.getOrder());
        assertThat(wikimediaHarvester.getOrder()).isLessThan(sofaScoreHarvester.getOrder());
    }

    @Test
    @DisplayName("Should gracefully return Optional.empty on blank or null inputs")
    void testBlankInputs() {
        List<PlayerHeadshotHarvester> harvesters = List.of(
                theSportsDbHarvester, wikidataHarvester, wikimediaHarvester, sofaScoreHarvester
        );

        for (PlayerHeadshotHarvester h : harvesters) {
            assertThat(h.harvest(null, SportType.TENNIS, TourType.ATP)).isEmpty();
            assertThat(h.harvest("", SportType.TENNIS, TourType.ATP)).isEmpty();
            assertThat(h.harvest("   ", SportType.TENNIS, TourType.ATP)).isEmpty();
        }
    }
}
