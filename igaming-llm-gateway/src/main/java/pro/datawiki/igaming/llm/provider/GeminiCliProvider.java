package pro.datawiki.igaming.llm.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiCliProvider implements LlmProvider {

    @Value("${gemini-cli.command:gemini}")
    private String cliCommand;

    @Value("${gemini-cli.accounts-dir:gemini_accounts}")
    private String accountsDirConfig;

    private final Semaphore semaphore = new Semaphore(4);
    private final List<File> accountDirs = new ArrayList<>();
    private final AtomicInteger rotationIndex = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        File dir = new File(accountsDirConfig);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(File::isDirectory);
            if (files != null) {
                for (File f : files) {
                    File geminiSubDir = new File(f, ".gemini");
                    if (geminiSubDir.exists()) {
                        accountDirs.add(f);
                    }
                }
            }
            // Sort to ensure stable round-robin order
            accountDirs.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));
        }

        if (!accountDirs.isEmpty()) {
            log.info("✅ GeminiCliProvider initialized with {} accounts for rotation from {}", 
                    accountDirs.size(), accountsDirConfig);
        } else {
            log.info("ℹ️ GeminiCliProvider initialized using system default environment (no accounts folder found at {})", 
                    accountsDirConfig);
        }
    }

    @Override
    public String getName() {
        return "gemini-cli";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String modelName = request.getModel() != null ? request.getModel() : "gemini-cli-default";
        log.info("🚀 Gemini CLI Request: model={}", modelName);

        // Build command
        List<String> command = new ArrayList<>();
        command.add(cliCommand);
        
        if (request.getModel() != null && !request.getModel().equals("gemini-cli-default")) {
            command.add("-m");
            command.add(request.getModel());
        }

        command.add("-p");
        
        // Build full prompt including system prompt if present
        String fullPrompt = request.getPrompt();
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            fullPrompt = "[System Instruction: " + request.getSystemPrompt() + "]\n\n" + fullPrompt;
        }
        command.add(fullPrompt);

        // Process execution under semaphore limit to avoid CPU spikes
        try {
            if (!semaphore.tryAcquire(5, TimeUnit.MINUTES)) {
                throw new RuntimeException("Timeout waiting for Gemini CLI execution semaphore");
            }

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(false);
                
                // Configure environment
                processBuilder.environment().put("NO_UPDATE_NOTIFIER", "1");
                
                // Set HOME if we have multiple accounts for rotation
                if (!accountDirs.isEmpty()) {
                    int idx = rotationIndex.getAndIncrement() % accountDirs.size();
                    File accountDir = accountDirs.get(idx);
                    log.info("🔄 Rotating Gemini CLI to account: {} (HOME={})", accountDir.getName(), accountDir.getAbsolutePath());
                    processBuilder.environment().put("HOME", accountDir.getAbsolutePath());
                }

                Process process = processBuilder.start();

                // Read stdout and stderr asynchronously to avoid blocking issues
                StringBuilder stdout = new StringBuilder();
                StringBuilder stderr = new StringBuilder();

                Thread outThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stdout.append(line).append("\n");
                        }
                    } catch (Exception e) {
                        log.error("Error reading stdout: {}", e.getMessage());
                    }
                });

                Thread errThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stderr.append(line).append("\n");
                        }
                    } catch (Exception e) {
                        log.error("Error reading stderr: {}", e.getMessage());
                    }
                });

                outThread.start();
                errThread.start();

                boolean finished = process.waitFor(360, TimeUnit.SECONDS);
                
                if (!finished) {
                    process.destroyForcibly();
                    throw new RuntimeException("Gemini CLI timed out after 360 seconds");
                }

                outThread.join(1000);
                errThread.join(1000);

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    String errorMsg = stderr.toString().trim();
                    log.error("❌ Gemini CLI failed with exit code {}: {}", exitCode, errorMsg);
                    throw new RuntimeException("Gemini CLI execution failed: " + errorMsg);
                }

                String responseText = stdout.toString().trim();

                return LlmResponse.builder()
                        .text(responseText)
                        .model(modelName)
                        .provider(getName())
                        .tokensUsed(0) // CLI doesn't supply token metadata
                        .finishReason("stop")
                        .build();

            } finally {
                semaphore.release();
            }

        } catch (Exception e) {
            log.error("❌ Gemini CLI exception: {}", e.getMessage());
            throw new RuntimeException("Gemini CLI execution failed", e);
        }
    }
}
