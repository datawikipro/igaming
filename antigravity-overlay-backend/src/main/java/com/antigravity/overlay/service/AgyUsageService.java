package com.antigravity.overlay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Launches agy.exe via a real Windows PTY (ConPTY via pty4j),
 * sends the /usage command, captures TUI output, and parses quota data.
 *
 * Flow per account:
 *   1. Refresh access_token with Google OAuth
 *   2. Backup ~/.gemini/oauth_creds.json and google_accounts.json
 *   3. Write account's credentials into oauth_creds.json and active user to google_accounts.json
 *   4. Launch agy.exe in a ConPTY — it reads the file on startup
 *   5. Wait for init, type /usage, wait for TUI to render
 *   6. Kill agy, restore original files and keyring
 *   7. Strip ANSI codes, parse percentage + reset-time lines
 */
@Service
public class AgyUsageService {

    private static final Logger log = LoggerFactory.getLogger(AgyUsageService.class);

    private static final Path OAUTH_FILE =
            Paths.get(System.getProperty("user.home"), ".gemini", "oauth_creds.json");

    private static final Path ACCOUNTS_FILE =
            Paths.get(System.getProperty("user.home"), ".gemini", "google_accounts.json");

    /** Only ONE agy instance may run at a time — they'd conflict over the shared oauth file. */
    private static final Object AGY_LOCK = new Object();

    private static final int WAIT_STARTUP_MS  = 6_000;  // agy init + Google auth
    private static final int WAIT_RENDER_MS   = 3_000;  // /usage dialog render time

    private static final Pattern PCT_PATTERN =
            Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{1,2})?)%");

    private static final Pattern RESETS_PATTERN =
            Pattern.compile(
                "(?:fully\\s+)?refresh(?:es)?\\s+in\\s+((?:\\d+\\s+(?:day|days|hour|hours|minute|minutes)(?:,\\s*)?)+)",
                Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ANSI_PATTERN =
            Pattern.compile("\\x1B(?:[@-Z\\\\-_]|\\[[0-?]*[ -/]*[@-~])");

    @Autowired
    private GoogleOAuthService googleOAuthService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Public result record ─────────────────────────────────────────────────

    public record QuotaBucket(
        double remainingFraction,    // 0.0 – 1.0
        LocalDateTime resetsAt       // null if unknown/exhausted
    ) {}

    public record AgyUsageResult(
        QuotaBucket geminiWeekly,
        QuotaBucket gemini5h,
        QuotaBucket claudeGptWeekly,
        QuotaBucket claudeGpt5h,
        String rawOutput             // kept for debugging
    ) {}

    // ── Main entry point ─────────────────────────────────────────────────────

    public AgyUsageResult fetchUsage(String email, String refreshToken) throws Exception {
        synchronized (AGY_LOCK) {
            log.info("[AgyUsage] Acquiring lock for account '{}', refreshing token...", email);

            // 1. Get fresh access token
            GoogleOAuthService.TokenResponse tokenRes =
                    googleOAuthService.refreshToken(refreshToken);

            // 2. Backup original oauth_creds.json
            String originalContent = Files.readString(OAUTH_FILE);

            // Backup original google_accounts.json
            String originalAccounts = Files.exists(ACCOUNTS_FILE) ? Files.readString(ACCOUNTS_FILE) : null;

            // Backup Windows Credential Manager so agy doesn't bypass oauth_creds.json
            WindowsCredentialManager.CredentialBackup credBackup =
                    WindowsCredentialManager.backupAndClear();

            try {
                // 3. Write account credentials to oauth_creds.json (for safety / compatibility)
                Map<String, Object> creds = new LinkedHashMap<>();
                creds.put("access_token",  tokenRes.accessToken);
                creds.put("scope",         "https://www.googleapis.com/auth/cloud-platform " +
                                           "https://www.googleapis.com/auth/userinfo.email " +
                                           "https://www.googleapis.com/auth/userinfo.profile " +
                                           "https://www.googleapis.com/auth/cclog " +
                                           "https://www.googleapis.com/auth/experimentsandconfigs " +
                                           "openid");
                creds.put("token_type",    "Bearer");
                creds.put("id_token",      tokenRes.idToken != null ? tokenRes.idToken : "");
                creds.put("expiry_date",   System.currentTimeMillis() + (long) tokenRes.expiresIn * 1000L);
                creds.put("refresh_token", refreshToken);
                Files.writeString(OAUTH_FILE, objectMapper.writeValueAsString(creds));

                // Write the active account to google_accounts.json
                if (originalAccounts != null) {
                    try {
                        com.fasterxml.jackson.databind.JsonNode accountsRoot = objectMapper.readTree(originalAccounts);
                        if (accountsRoot.isObject()) {
                            ((ObjectNode) accountsRoot).put("active", email);
                            Files.writeString(ACCOUNTS_FILE, objectMapper.writeValueAsString(accountsRoot));
                        }
                    } catch (Exception e) {
                        log.warn("[AgyUsage] Failed to parse existing google_accounts.json, writing new structure: {}", e.getMessage());
                        writeNewAccountsFile(email);
                    }
                } else {
                    writeNewAccountsFile(email);
                }

                // Write the credential directly to the Windows Credential Manager under 'gemini:antigravity'
                Map<String, Object> keyringCreds = new LinkedHashMap<>();
                Map<String, Object> tokenMap = new LinkedHashMap<>();
                tokenMap.put("access_token",  tokenRes.accessToken);
                tokenMap.put("refresh_token", refreshToken);
                tokenMap.put("token_type",    "Bearer");
                // format expiry time in ISO-8601 RFC3339 format with timezone offset
                String expiryStr = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
                        .plusSeconds(tokenRes.expiresIn)
                        .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                tokenMap.put("expiry",        expiryStr);
                keyringCreds.put("token",     tokenMap);
                keyringCreds.put("auth_method", "consumer");

                String secretJson = objectMapper.writeValueAsString(keyringCreds);
                WindowsCredentialManager.write("antigravity", secretJson);

                log.info("[AgyUsage] Credentials written to file & Windows keyring, launching agy.exe...");

                // 4-6. Launch agy, send /usage, collect output
                String rawOutput = launchAndCapture();
                log.info("[AgyUsage] Captured {} chars of output.\n=== RAW OUTPUT START ===\n{}\n=== RAW OUTPUT END ===", rawOutput.length(), rawOutput);

                // 7. Parse
                AgyUsageResult result = parse(rawOutput);
                log.info("[AgyUsage] Parsed — Gemini weekly: {}%, Gemini 5h: {}%, Claude/GPT weekly: {}%",
                         pct(result.geminiWeekly()),
                         pct(result.gemini5h()),
                         pct(result.claudeGptWeekly()));
                return result;

            } finally {
                // Restore Windows Credential Manager
                WindowsCredentialManager.restore(credBackup);

                // Always restore original oauth_creds.json
                try {
                    Files.writeString(OAUTH_FILE, originalContent);
                    log.info("[AgyUsage] Restored original oauth_creds.json.");
                } catch (Exception e) {
                    log.error("[AgyUsage] FAILED to restore oauth_creds.json — manual restore needed!", e);
                }

                // Always restore original google_accounts.json
                try {
                    if (originalAccounts != null) {
                        Files.writeString(ACCOUNTS_FILE, originalAccounts);
                        log.info("[AgyUsage] Restored original google_accounts.json.");
                    } else {
                        Files.deleteIfExists(ACCOUNTS_FILE);
                        log.info("[AgyUsage] Removed temporary google_accounts.json.");
                    }
                } catch (Exception e) {
                    log.error("[AgyUsage] FAILED to restore google_accounts.json — manual restore needed!", e);
                }
            }
        }
    }

    private void writeNewAccountsFile(String email) throws Exception {
        Map<String, Object> newAccs = new LinkedHashMap<>();
        newAccs.put("active", email);
        newAccs.put("old", new java.util.ArrayList<>());
        Files.writeString(ACCOUNTS_FILE, objectMapper.writeValueAsString(newAccs));
    }

    // ── PTY interaction ───────────────────────────────────────────────────────

    private String launchAndCapture() throws Exception {
        Map<String, String> env = new java.util.HashMap<>(System.getenv());
        env.keySet().removeIf(key -> key.toUpperCase().contains("ANTIGRAVITY") || key.toUpperCase().contains("AGY"));

        PtyProcess pty = new PtyProcessBuilder()
                .setCommand(new String[]{"agy.exe"})
                .setDirectory(System.getProperty("user.home"))
                .setEnvironment(env)
                .setInitialColumns(220)
                .setInitialRows(50)
                .start();

        StringBuilder sb = new StringBuilder();

        // Reader thread — drains PTY stdout continuously
        InputStream in = pty.getInputStream();
        Thread reader = Thread.ofVirtual().start(() -> {
            try {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) {
                    synchronized (sb) { sb.append(new String(buf, 0, n)); }
                }
            } catch (Exception ignored) { /* EOF after kill is normal */ }
        });

        try {
            // Wait for agy to initialise and authenticate
            Thread.sleep(WAIT_STARTUP_MS);

            // Send /usage command with keystroke delays so TUI autocomplete stabilizes
            OutputStream out = pty.getOutputStream();
            for (char c : "/usage".toCharArray()) {
                out.write(c);
                out.flush();
                Thread.sleep(100);
            }
            Thread.sleep(500);
            out.write('\r');
            out.flush();
            Thread.sleep(300);
            out.write('\r');
            out.flush();

            // Wait for the /usage dialog to fully render
            Thread.sleep(WAIT_RENDER_MS);

        } finally {
            pty.destroy();
            reader.join(2_000);
        }

        synchronized (sb) { return sb.toString(); }
    }

    // ── Output parsing ────────────────────────────────────────────────────────

    AgyUsageResult parse(String raw) {
        String text = stripAnsi(raw);
        String[] lines = text.split("[\\r\\n]+");

        // We walk lines and track which section+sub-section we are in.
        // Sections:    "gemini" | "claude_gpt"
        // Sub-sections: "weekly" | "5h"
        String section    = null;
        String subSection = null;

        Double geminiWeeklyPct    = null;  String geminiWeeklyReset    = null;
        Double gemini5hPct        = null;  String gemini5hReset        = null;
        Double claudeWeeklyPct    = null;  String claudeWeeklyReset    = null;
        Double claude5hPct        = null;

        for (String raw_line : lines) {
            String line = raw_line.trim();
            if (line.isEmpty()) continue;
            String lo = line.toLowerCase();

            // ── Section headers ──────────────────────────────────────────────
            if (lo.contains("gemini model")) {
                section = "gemini"; subSection = null; continue;
            }
            if ((lo.contains("claude") && lo.contains("gpt")) || lo.contains("claude and gpt")) {
                section = "claude_gpt"; subSection = null; continue;
            }

            // ── Sub-section headers ──────────────────────────────────────────
            if (lo.contains("weekly limit") || (lo.contains("weekly") && lo.contains("limit"))) {
                subSection = "weekly"; continue;
            }
            if (lo.contains("five hour") || lo.contains("5-hour") || lo.contains("5 hour")) {
                subSection = "5h"; continue;
            }

            if (section == null || subSection == null) continue;

            // ── Exhausted markers ────────────────────────────────────────────
            // When we see "hit your weekly limit" the next % line will say 0%,
            // but let's also pre-set to 0 in case the % line is missing.
            if (lo.contains("hit your weekly limit") || lo.contains("resource has been exhausted")) {
                if ("gemini".equals(section)) {
                    if ("weekly".equals(subSection) && geminiWeeklyPct  == null) geminiWeeklyPct  = 0.0;
                    if ("5h".equals(subSection)     && gemini5hPct      == null) gemini5hPct      = 0.0;
                } else if ("claude_gpt".equals(section)) {
                    if ("weekly".equals(subSection) && claudeWeeklyPct  == null) claudeWeeklyPct  = 0.0;
                    if ("5h".equals(subSection)     && claude5hPct      == null) claude5hPct      = 0.0;
                }
            }

            // ── Percentage value ─────────────────────────────────────────────
            Matcher pctM = PCT_PATTERN.matcher(line);
            if (pctM.find()) {
                double remaining = Double.parseDouble(pctM.group(1)) / 100.0;
                if ("gemini".equals(section)) {
                    if ("weekly".equals(subSection) && geminiWeeklyPct == null) geminiWeeklyPct = remaining;
                    if ("5h".equals(subSection)     && gemini5hPct    == null) gemini5hPct     = remaining;
                } else if ("claude_gpt".equals(section)) {
                    if ("weekly".equals(subSection) && claudeWeeklyPct == null) claudeWeeklyPct = remaining;
                    if ("5h".equals(subSection)     && claude5hPct     == null) claude5hPct     = remaining;
                }
            }

            // ── Reset-time string ────────────────────────────────────────────
            Matcher resetM = RESETS_PATTERN.matcher(line);
            if (resetM.find()) {
                String resetsIn = resetM.group(1).trim();
                if ("gemini".equals(section)) {
                    if ("weekly".equals(subSection) && geminiWeeklyReset == null) geminiWeeklyReset = resetsIn;
                    if ("5h".equals(subSection)     && gemini5hReset     == null) gemini5hReset     = resetsIn;
                } else if ("claude_gpt".equals(section)) {
                    if ("weekly".equals(subSection) && claudeWeeklyReset == null) claudeWeeklyReset = resetsIn;
                }
            }
        }

        return new AgyUsageResult(
            bucket(geminiWeeklyPct,  geminiWeeklyReset),
            bucket(gemini5hPct,      gemini5hReset),
            bucket(claudeWeeklyPct,  claudeWeeklyReset),
            bucket(claude5hPct,      null),
            raw
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private QuotaBucket bucket(Double pct, String resetsIn) {
        double fraction = (pct != null) ? Math.max(0.0, Math.min(1.0, pct)) : 1.0;
        LocalDateTime resetsAt = resetsIn != null ? parseResetsIn(resetsIn) : null;
        return new QuotaBucket(fraction, resetsAt);
    }

    /**
     * Converts "2 days, 12 hours" or "4 hours, 18 minutes" to a LocalDateTime.
     */
    static LocalDateTime parseResetsIn(String text) {
        if (text == null || text.isBlank()) return null;
        LocalDateTime dt = LocalDateTime.now();
        Pattern numUnit = Pattern.compile("(\\d+)\\s+(day|days|hour|hours|minute|minutes)", Pattern.CASE_INSENSITIVE);
        Matcher m = numUnit.matcher(text);
        while (m.find()) {
            int n = Integer.parseInt(m.group(1));
            String unit = m.group(2).toLowerCase();
            if (unit.startsWith("day"))    dt = dt.plusDays(n);
            if (unit.startsWith("hour"))   dt = dt.plusHours(n);
            if (unit.startsWith("minute")) dt = dt.plusMinutes(n);
        }
        return dt;
    }

    private String stripAnsi(String text) {
        return ANSI_PATTERN.matcher(text).replaceAll("");
    }

    private String pct(QuotaBucket b) {
        if (b == null) return "?";
        return String.format("%.0f", b.remainingFraction() * 100);
    }


}
