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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void dumpSbobetNetwork() {
        System.out.println("=== STARTING SBOBET DUMP ===");
        try {
            browserService.dumpNetworkCalls("https://www.sbobet.com/ru-RU/euro/volleyball", 20000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=== SBOBET DUMP FINISHED ===");
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
            System.out.println("First 200 chars: " + jsonArrayStr.substring(0, Math.min(200, jsonArrayStr.length())));
            System.out.println("Last 200 chars: " + jsonArrayStr.substring(Math.max(0, jsonArrayStr.length() - 200)));

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

            // Let's inspect the elements in the array
            if (rootNode.isArray() && rootNode.size() > 2) {
                com.fasterxml.jackson.databind.JsonNode dataList = rootNode.get(2);
                System.out.println("Data list size: " + dataList.size());
                if (dataList.size() > 0) {
                    com.fasterxml.jackson.databind.JsonNode firstItem = dataList.get(0);
                    System.out.println("First item type: " + firstItem.getNodeType() + ", size: " + firstItem.size());
                    System.out.println("First item details: " + firstItem.toString().substring(0, Math.min(500, firstItem.toString().length())));
                }
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
