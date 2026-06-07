package pro.datawiki.igaming.source.draftkings.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DraftKingsEventGroupResponse {
    
    private DraftKingsEventGroup eventGroup;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftKingsEventGroup {
        private int eventGroupId;
        private List<DraftKingsEvent> events;
        private List<DraftKingsOfferCategory> offerCategories;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftKingsEvent {
        private String eventId;
        private String name;
        private String startDate;
        private String teamName1;
        private String teamName2;
        private String eventStatus; // e.g. "Pre-Event", "Started", "Completed"
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftKingsOfferCategory {
        private int offerCategoryId;
        private String name;
        private List<DraftKingsOfferSubcategoryDescriptor> offerSubcategoryDescriptors;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftKingsOfferSubcategoryDescriptor {
        private int subcategoryId;
        private String name;
        private DraftKingsOfferSubcategory offerSubcategory;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftKingsOfferSubcategory {
        // DraftKings wraps offers in arrays of arrays
        private List<List<DraftKingsOffer>> offers;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftKingsOffer {
        private String eventId;
        private String offerId;
        private List<DraftKingsOutcome> outcomes;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DraftKingsOutcome {
        private String participant;
        private String oddsAmerican;
        private double oddsDecimal;
        private String label; // e.g. "Over", "Under"
        private String line;  // e.g. "2.5", "+7.5"
    }
}
