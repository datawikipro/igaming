package com.antigravity.overlay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgyChatService {
    private static final Logger log = LoggerFactory.getLogger(AgyChatService.class);

    public String sendPrompt(String projectId, String prompt) throws Exception {
        List<String> command = new ArrayList<>();
        String binary = System.getProperty("os.name").toLowerCase().contains("win") ? "agy.exe" : "agy";
        command.add(binary);
        command.add("--project");
        command.add(projectId);
        command.add("--continue");
        command.add("--print");
        command.add(prompt);

        log.info("Executing chat command for project {}: {} --project {} --continue --print ...", projectId, binary, projectId);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("{} exited with code {}. Output:\n{}", binary, exitCode, output);
            throw new RuntimeException(binary + " failed: " + output.toString());
        }

        return output.toString();
    }
}
