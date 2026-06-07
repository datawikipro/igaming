package pro.datawiki.igaming.source.sbobet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.browser.BrowserContextManager;
import pro.datawiki.igaming.source.core.browser.BrowserExecutor;
import pro.datawiki.igaming.source.core.browser.BrowserLaunchFactory;
import pro.datawiki.igaming.source.core.browser.BrowserProxyManager;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class SbobetTest {

    private static BrowserService browserService;

    @BeforeAll
    public static void setup() throws Exception {
        BrowserLaunchFactory launchFactory = new BrowserLaunchFactory();
        BrowserProxyManager proxyManager = mock(BrowserProxyManager.class);
        when(proxyManager.getCurrentProxyUri()).thenReturn(null);
        when(proxyManager.isVpnEnabled()).thenReturn(false);

        BrowserContextManager contextManager = new BrowserContextManager();

        java.lang.reflect.Field localeField = BrowserContextManager.class.getDeclaredField("browserLocale");
        localeField.setAccessible(true);
        localeField.set(contextManager, "ru-RU");

        java.lang.reflect.Field langField = BrowserContextManager.class.getDeclaredField("browserAcceptLanguage");
        langField.setAccessible(true);
        langField.set(contextManager, "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");

        java.lang.reflect.Field tzField = BrowserContextManager.class.getDeclaredField("browserTimezone");
        tzField.setAccessible(true);
        tzField.set(contextManager, "Europe/Moscow");

        BrowserExecutor executor = mock(BrowserExecutor.class);

        browserService = new BrowserService(launchFactory, proxyManager, contextManager, executor);

        java.lang.reflect.Field profileField = BrowserService.class.getDeclaredField("activeProfile");
        profileField.setAccessible(true);
        profileField.set(browserService, BrowserLaunchFactory.StealthProfile.BASIC);

        browserService.init();
    }

    @AfterAll
    public static void tearDown() {
        if (browserService != null) {
            browserService.shutdown();
        }
    }

    @Test
    public void testFetchOddsTransform() {
        System.out.println("=== STARTING SBOBET API CLIENT TRANSFORMATION TEST ===");
        try {
            String html = java.nio.file.Files.readString(
                java.nio.file.Paths.get("C:/Users/chernousov_a/IdeaProjects/igaming/sbobet_page.html")
            );

            // Mock dependencies
            BrowserService mockBrowser = mock(BrowserService.class);
            pro.datawiki.igaming.source.sbobet.config.SbobetConfig mockConfig = new pro.datawiki.igaming.source.sbobet.config.SbobetConfig();
            
            pro.datawiki.igaming.source.sbobet.service.SbobetApiErrorTracker mockErrorTracker = 
                mock(pro.datawiki.igaming.source.sbobet.service.SbobetApiErrorTracker.class);
            pro.datawiki.igaming.source.core.service.VpnManagerService mockVpnManager = 
                mock(pro.datawiki.igaming.source.core.service.VpnManagerService.class);

            // Stub BrowserService to return our mock HTML
            when(mockBrowser.navigateAndGetBody(anyString(), anyInt())).thenReturn(html);

            // Instantiate client
            pro.datawiki.igaming.source.sbobet.service.SbobetApiClient client = 
                new pro.datawiki.igaming.source.sbobet.service.SbobetApiClient(
                    mockBrowser, mockConfig, mockErrorTracker, mockVpnManager
                );

            // Fetch odds
            com.fasterxml.jackson.databind.JsonNode responseNode = client.fetchOdds("volleyball");

            // Verify mapping output
            assertNotNull(responseNode, "Response node should not be null");
            assertTrue(responseNode.has("leagues"), "Response node should contain leagues array");
            
            com.fasterxml.jackson.databind.JsonNode leagues = responseNode.get("leagues");
            System.out.println("Transformed output contains " + leagues.size() + " leagues:");
            
            assertTrue(leagues.size() > 0, "Should have mapped leagues");
            for (int i = 0; i < leagues.size(); i++) {
                com.fasterxml.jackson.databind.JsonNode league = leagues.get(i);
                System.out.println("  League: " + league.get("name").asText());
                com.fasterxml.jackson.databind.JsonNode events = league.get("events");
                System.out.println("    Events count: " + events.size());
                
                for (int j = 0; j < events.size(); j++) {
                    com.fasterxml.jackson.databind.JsonNode event = events.get(j);
                    System.out.println("      Match: " + event.get("home").asText() + " vs " + event.get("away").asText());
                    System.out.println("        Handicaps count: " + event.get("handicaps").size());
                    System.out.println("        Totals count: " + event.get("totals").size());
                    if (event.has("moneyline")) {
                        System.out.println("        Moneyline: " + event.get("moneyline").toString());
                    }
                }
            }

            // Print the full output formatted
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseNode);
            System.out.println("\n=== Pretty Transformed JSON output (first 1000 chars) ===\n" + 
                               prettyJson.substring(0, Math.min(1000, prettyJson.length())) + "\n...");

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test threw exception: " + e.getMessage());
        }
        System.out.println("=== SBOBET API CLIENT TRANSFORMATION TEST FINISHED ===");
    }

    @Test
    public void testSbobetNavigation() {
        System.out.println("=== STARTING SBOBET NAVIGATION TEST ===");
        try (var page = browserService.getSharedContext().newPage()) {
            System.out.println("Navigating to homepage: https://www.sbobet.com");
            page.navigate("https://www.sbobet.com");
            page.waitForTimeout(5000);
            System.out.println("Homepage Title: " + page.title());

            System.out.println("Navigating to Volleyball page...");
            page.navigate("https://www.sbobet.com/ru-RU/euro/volleyball");
            page.waitForTimeout(10000);
            System.out.println("Volleyball Page Title: " + page.title());

            String html = page.content();
            System.out.println("HTML length: " + html.length());
            java.nio.file.Files.writeString(
                java.nio.file.Paths.get("C:/Users/chernousov_a/IdeaProjects/igaming/sbobet_page.html"), 
                html
            );
            System.out.println("Page content saved to sbobet_page.html");

            // Evaluate Sbobet JS context
            System.out.println("=== EVALUATING SBOBET JS CONTEXT ===");
            try {
                Object dollarKeys = page.evaluate("() => Object.keys(window).filter(k => k.startsWith('$'))");
                System.out.println("Dollar keys: " + dollarKeys);

                Object hasP = page.evaluate("() => typeof window.$P !== 'undefined'");
                System.out.println("Has $P: " + hasP);

                if (Boolean.TRUE.equals(hasP)) {
                    Object pKeys = page.evaluate("() => Object.keys(window.$P)");
                    System.out.println("$P keys: " + pKeys);
                }

                Object hasM = page.evaluate("() => typeof window.$M !== 'undefined'");
                System.out.println("Has $M: " + hasM);

                Object hasT = page.evaluate("() => typeof window.$T !== 'undefined'");
                System.out.println("Has $T: " + hasT);
                
                // Let's try to inspect the registered models or data
                Object modelOd = page.evaluate("() => { try { return window.$M('od') ? 'Model od exists' : 'Model od is null'; } catch(e) { return 'Error: ' + e.message; } }");
                System.out.println("Model 'od' status: " + modelOd);

                // Let's print out what is returned by $P.getState()
                Object pState = page.evaluate("() => { try { " +
                    "var state = window.$P.getState(); " +
                    "if (!state) return 'State is null'; " +
                    "return typeof state + ' keys: ' + Object.keys(state).join(', '); " +
                    "} catch(e) { return 'Error: ' + e.message; } }");
                System.out.println("$P.getState() summary: " + pState);

                // Let's inspect all non-function fields in the 'od' model
                Object odData = page.evaluate("() => { try { " +
                    "var m = window.$M('od'); " +
                    "if (!m) return 'No model od'; " +
                    "var fields = []; " +
                    "for (var k in m) { " +
                    "  if (typeof m[k] !== 'function') { " +
                    "    var valStr = String(m[k]); " +
                    "    if (valStr.length > 200) valStr = valStr.substring(0, 200) + '...'; " +
                    "    fields.push(k + ' (' + typeof m[k] + '): ' + valStr); " +
                    "  } " +
                    "} " +
                    "return fields.join('\\n'); " +
                    "} catch(e) { return 'Error: ' + e.message; } }");
                System.out.println("Model 'od' fields:\n" + odData);

                // Let's check tokens
                Object tokens = page.evaluate("() => { try { " +
                    "var result = []; " +
                    "if (window.$T) { " +
                    "  result.push('Has $T'); " +
                    "  try { result.push('site token value: ' + JSON.stringify(window.$T('site'))); } catch(e) {} " +
                    "  try { result.push('od token value: ' + JSON.stringify(window.$T('od'))); } catch(e) {} " +
                    "} else { " +
                    "  result.push('No $T'); " +
                    "} " +
                    "return result.join('\\n'); " +
                    "} catch(e) { return 'Error: ' + e.message; } }");
                System.out.println("Tokens inspection:\n" + tokens);

                // Let's check if there is a global variable holding the data
                Object globalVars = page.evaluate("() => { try { " +
                    "var found = []; " +
                    "for (var k in window) { " +
                    "  if (k.toLowerCase().contains && (k.toLowerCase().contains('odds') || k.toLowerCase().contains('event') || k.toLowerCase().contains('match'))) { " +
                    "    found.push(k + ': ' + typeof window[k]); " +
                    "  } " +
                    "} " +
                    "return found.join(', '); " +
                    "} catch(e) { " +
                    "  // fallback if contains is not a function " +
                    "  var found = []; " +
                    "  for (var k in window) { " +
                    "    if (k.toLowerCase().indexOf('odds') >= 0 || k.toLowerCase().indexOf('event') >= 0 || k.toLowerCase().indexOf('match') >= 0) { " +
                    "      found.push(k + ': ' + typeof window[k]); " +
                    "    } " +
                    "  } " +
                    "  return found.join(', '); " +
                    "} }");
                System.out.println("Matching global variables: " + globalVars);

            } catch (Exception je) {
                System.err.println("JS Eval error: " + je.getMessage());
            }
            System.out.println("=== END OF JS CONTEXT EVALUATION ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHtmlDataExtraction() {
        System.out.println("=== STARTING HTML EXTRACTION TEST ===");
        try {
            String html = java.nio.file.Files.readString(
                java.nio.file.Paths.get("C:/Users/chernousov_a/IdeaProjects/igaming/sbobet_page.html")
            );
            System.out.println("Read HTML length: " + html.length());

            int startIdx = html.indexOf("$P.onUpdate('od',");
            if (startIdx == -1) {
                System.out.println("Could not find $P.onUpdate('od', in HTML");
                return;
            }
            System.out.println("Found $P.onUpdate('od', at index: " + startIdx);

            int openBracketIdx = html.indexOf("[", startIdx);
            if (openBracketIdx == -1) {
                System.out.println("Could not find opening [ after $P.onUpdate('od',");
                return;
            }

            int bracketCount = 0;
            int endIdx = -1;
            for (int i = openBracketIdx; i < html.length(); i++) {
                char c = html.charAt(i);
                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        endIdx = i;
                        break;
                    }
                }
            }

            if (endIdx == -1) {
                System.out.println("Could not find matching closing ]");
                return;
            }

            String jsonArrayStr = html.substring(openBracketIdx, endIdx + 1);
            System.out.println("Extracted JSON array length: " + jsonArrayStr.length());

            // Replace double commas with null for sparse arrays in JS
            while (jsonArrayStr.contains(",,")) {
                jsonArrayStr = jsonArrayStr.replace(",,", ",null,");
            }
            while (jsonArrayStr.contains(", ,")) {
                jsonArrayStr = jsonArrayStr.replace(", ,", ",null,");
            }

            // Let's parse it using Jackson
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonArrayStr);
            System.out.println("Jackson parsed successfully! Node type: " + rootNode.getNodeType());

            System.out.println("Root array size: " + rootNode.size());
            for (int i = 0; i < rootNode.size(); i++) {
                com.fasterxml.jackson.databind.JsonNode element = rootNode.get(i);
                System.out.println("  rootNode[" + i + "] type: " + element.getNodeType() + 
                                   ", size: " + (element.isContainerNode() ? String.valueOf(element.size()) : "N/A") +
                                   ", value: " + (element.isContainerNode() ? "" : element.toString()));
            }

            if (rootNode.size() > 2 && rootNode.get(2).isArray()) {
                com.fasterxml.jackson.databind.JsonNode dataList = rootNode.get(2);
                System.out.println("\n=== Inspecting rootNode[2] (dataList) ===");
                System.out.println("dataList size: " + dataList.size());
                
                for (int d = 0; d < dataList.size(); d++) {
                    com.fasterxml.jackson.databind.JsonNode dNode = dataList.get(d);
                    System.out.println("  dataList[" + d + "] type: " + dNode.getNodeType() + ", size: " + dNode.size());
                    
                    if (dNode.isArray() && dNode.size() > 1) {
                        // Let's look at the structure
                        com.fasterxml.jackson.databind.JsonNode firstVal = dNode.get(0);
                        com.fasterxml.jackson.databind.JsonNode secondVal = dNode.get(1);
                        System.out.println("    [0] value: " + firstVal);
                        System.out.println("    [1] type: " + secondVal.getNodeType() + ", size: " + secondVal.size());
                        
                        if (secondVal.isArray()) {
                            for (int e = 0; e < Math.min(5, secondVal.size()); e++) {
                                com.fasterxml.jackson.databind.JsonNode eventGroup = secondVal.get(e);
                                System.out.println("      eventGroup[" + e + "] type: " + eventGroup.getNodeType() + ", size: " + eventGroup.size());
                                if (eventGroup.isArray()) {
                                    for (int j = 0; j < eventGroup.size(); j++) {
                                        com.fasterxml.jackson.databind.JsonNode item = eventGroup.get(j);
                                        String itemStr = item.toString();
                                        if (itemStr.length() > 100) {
                                            itemStr = itemStr.substring(0, 100) + "...";
                                        }
                                        System.out.println("        item[" + j + "]: " + itemStr);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Print the other elements at the end of rootNode (usually dictionaries or metadata)
            for (int i = 3; i < rootNode.size(); i++) {
                com.fasterxml.jackson.databind.JsonNode metaNode = rootNode.get(i);
                System.out.println("\n=== Inspecting rootNode[" + i + "] ===");
                String metaStr = metaNode.toString();
                if (metaStr.length() > 500) {
                    metaStr = metaStr.substring(0, 500) + "...";
                }
                System.out.println(metaStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Component
    public static class DummyBookmakerService extends pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService {
        public DummyBookmakerService(pro.datawiki.igaming.source.core.repository.MatchCacheRepository matchCacheRepository,
                                     pro.datawiki.igaming.source.core.repository.SportCacheRepository sportCacheRepository,
                                     com.fasterxml.jackson.databind.ObjectMapper objectMapper,
                                     pro.datawiki.igaming.source.core.service.SportNormalizationService sportNormalizationService,
                                     pro.datawiki.igaming.source.core.service.MatchPersistenceService persistenceService) {
            super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        }
        @Override
        public String getBookmakerFamily() {
            return "sbobet";
        }
        @Override
        protected boolean loadSingleMatchCard(pro.datawiki.igaming.source.core.domain.MatchCache cache) {
            return false;
        }
    }
}
