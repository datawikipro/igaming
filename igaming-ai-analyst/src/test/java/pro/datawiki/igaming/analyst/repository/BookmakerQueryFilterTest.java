package pro.datawiki.igaming.analyst.repository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookmakerQueryFilterTest {

    @Test
    void testResolveSearchTermsForKnownCanonical() {
        List<String> terms = BookmakerQueryFilter.resolveSearchTerms("fonbet");
        assertNotNull(terms);
        assertFalse(terms.isEmpty());
        assertTrue(terms.contains("fonbet"));
        assertTrue(terms.contains("фонбет"));
    }

    @Test
    void testResolveSearchTermsForCyrillic() {
        List<String> terms = BookmakerQueryFilter.resolveSearchTerms("винлайн");
        assertNotNull(terms);
        assertTrue(terms.contains("winline"));
    }

    @Test
    void testResolveSearchTermsFromBookmakerCode() {
        // Bookmaker registered via BookmakerCode enum
        List<String> terms = BookmakerQueryFilter.resolveSearchTerms("bet365");
        assertNotNull(terms);
        assertTrue(terms.contains("bet365"));
    }

    @Test
    void testResolveSearchTermsForUnknownDynamicFallback() {
        List<String> terms = BookmakerQueryFilter.resolveSearchTerms("some-rare-crypto-bookie");
        assertNotNull(terms);
        assertEquals(List.of("some-rare-crypto-bookie"), terms);
    }

    @Test
    void testResolveSearchTermsEmpty() {
        assertTrue(BookmakerQueryFilter.resolveSearchTerms("").isEmpty());
        assertTrue(BookmakerQueryFilter.resolveSearchTerms("   ").isEmpty());
        assertTrue(BookmakerQueryFilter.resolveSearchTerms(null).isEmpty());
    }
}
