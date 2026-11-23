package pro.datawiki.igaming.mdm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTeamRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchIntegrityValidator Doubles & Homogeneity Tests")
class MatchIntegrityValidatorTest {

    @Mock
    private AnomalyReportingService anomalyReportingService;
    @Mock
    private MdmTournamentTeamRepository tournamentTeamRepository;
    @Mock
    private MdmTeamRepository teamRepository;

    private MatchIntegrityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MatchIntegrityValidator(anomalyReportingService, tournamentTeamRepository, teamRepository);
    }

    @Test
    @DisplayName("Should detect malformed doubles when one team has slash and other does not (sport 4 tennis)")
    void shouldDetectAsymmetricDoublesInTennisSingles() {
        MatchResolutionRequest req = MatchResolutionRequest.builder()
                .sportId(4)
                .homeTeamName("Alcaraz C / Nadal R")
                .awayTeamName("Djokovic N")
                .build();

        assertThat(validator.isMalformedDoubles(req)).isTrue();
    }

    @Test
    @DisplayName("Should reject request with malformed doubles via validateRequest")
    void shouldThrowOnMalformedDoubles() {
        MatchResolutionRequest req = MatchResolutionRequest.builder()
                .sportId(4)
                .homeTeamName("Alcaraz C / Nadal R")
                .awayTeamName("Djokovic N")
                .build();

        assertThatThrownBy(() -> validator.validateRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Malformed doubles mismatch");
    }

    @Test
    @DisplayName("Should detect malformed doubles in sport 45 (tennis-doubles) when a singles player is passed")
    void shouldDetectSinglesInDoublesSport() {
        MatchResolutionRequest req = MatchResolutionRequest.builder()
                .sportId(45)
                .homeTeamName("Alcaraz C")
                .awayTeamName("Djokovic N / Federer R")
                .build();

        assertThat(validator.isMalformedDoubles(req)).isTrue();

        MatchResolutionRequest reqBothSingles = MatchResolutionRequest.builder()
                .sportId(45)
                .homeTeamName("Alcaraz C")
                .awayTeamName("Djokovic N")
                .build();

        assertThat(validator.isMalformedDoubles(reqBothSingles)).isTrue();
    }

    @Test
    @DisplayName("Should accept valid doubles in sport 45 (tennis-doubles)")
    void shouldAcceptValidDoublesInDoublesSport() {
        MatchResolutionRequest req = MatchResolutionRequest.builder()
                .sportId(45)
                .homeTeamName("Krawietz K / Puetz T")
                .awayTeamName("Ram R / Salisbury J")
                .build();

        assertThat(validator.isMalformedDoubles(req)).isFalse();
    }
}
