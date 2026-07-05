package com.antigravity.overlay.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.antigravity.overlay.service.AgyChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AgyChatService chatService;

    private static final Path PROJECTS_DIR =
            Paths.get(System.getProperty("user.home"), ".gemini", "config", "projects");

    // DTO mapping for frontend communication
    public static class ProjectDto {
        public String id;
        public String name;
        public List<String> paths;
        public Map<String, Object> settings;
    }

    /**
     * Get list of all project configurations on the server.
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ProjectDto>> getProjects() {
        File dir = PROJECTS_DIR.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ProjectDto> list = new ArrayList<>();
        for (File file : files) {
            try {
                String content = Files.readString(file.toPath());
                JsonNode root = objectMapper.readTree(content);

                ProjectDto dto = new ProjectDto();
                dto.id = root.path("id").asText();
                dto.name = root.path("name").asText();
                dto.paths = new ArrayList<>();

                JsonNode resources = root.path("projectResources").path("resources");
                if (resources.isArray()) {
                    for (JsonNode res : resources) {
                        String folderUri = res.path("folderUri").asText();
                        dto.paths.add(fromUri(folderUri));
                    }
                }

                dto.settings = new LinkedHashMap<>();
                JsonNode settingsNode = root.path("settings");
                if (settingsNode.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = settingsNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        dto.settings.put(field.getKey(), field.getValue().asText());
                    }
                }

                // If id is missing, fall back to filename without extension
                if (dto.id == null || dto.id.trim().isEmpty()) {
                    String name = file.getName();
                    dto.id = name.substring(0, name.lastIndexOf('.'));
                }

                list.add(dto);
            } catch (Exception e) {
                log.error("Failed to parse project file: {}", file.getAbsolutePath(), e);
            }
        }

        // Sort projects alphabetically by name
        list.sort(Comparator.comparing(p -> p.name != null ? p.name.toLowerCase() : ""));
        return ResponseEntity.ok(list);
    }

    /**
     * Create or update a project configuration.
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> saveProject(@RequestBody ProjectDto dto) {
        if (dto.name == null || dto.name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project name is required"));
        }

        if (dto.paths == null || dto.paths.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "At least one directory path is required"));
        }

        // Generate ID if missing
        if (dto.id == null || dto.id.trim().isEmpty()) {
            dto.id = UUID.randomUUID().toString();
        }

        try {
            // Ensure projects directory exists
            Files.createDirectories(PROJECTS_DIR);

            ObjectNode root = objectMapper.createObjectNode();
            root.put("id", dto.id);
            root.put("name", dto.name);

            // Format resources array
            ObjectNode projectResources = objectMapper.createObjectNode();
            ArrayNode resourcesArray = objectMapper.createArrayNode();
            for (String path : dto.paths) {
                if (path == null || path.trim().isEmpty()) continue;
                ObjectNode resNode = objectMapper.createObjectNode();
                resNode.put("folderUri", toUri(path.trim()));
                resourcesArray.add(resNode);
            }
            projectResources.set("resources", resourcesArray);
            root.set("projectResources", projectResources);

            // Set settings/policies defaults
            ObjectNode settings = objectMapper.createObjectNode();
            if (dto.settings != null && !dto.settings.isEmpty()) {
                dto.settings.forEach((key, val) -> settings.put(key, String.valueOf(val)));
            } else {
                // Default fallback policies
                settings.put("fileAccessPolicy", "AGENT_SETTING_POLICY_ALLOW");
                settings.put("internetPolicy", "AGENT_SETTING_POLICY_ASK");
                settings.put("autoExecutionPolicy", "CASCADE_COMMANDS_AUTO_EXECUTION_EAGER");
                settings.put("artifactReviewMode", "ARTIFACT_REVIEW_MODE_TURBO");
            }
            root.set("settings", settings);

            // Save to disk
            Path filePath = PROJECTS_DIR.resolve(dto.id + ".json");
            Files.writeString(filePath, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));

            log.info("Successfully saved project config: {}", filePath.toAbsolutePath());
            return ResponseEntity.ok(dto);

        } catch (IOException e) {
            log.error("Failed to save project config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save project file: " + e.getMessage()));
        }
    }

    /**
     * Delete a project configuration.
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project ID is required"));
        }

        Path filePath = PROJECTS_DIR.resolve(id + ".json");
        File file = filePath.toFile();

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        if (file.delete()) {
            log.info("Deleted project file: {}", file.getAbsolutePath());
            return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
        } else {
            log.error("Failed to delete project file: {}", file.getAbsolutePath());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete project file from disk."));
        }
    }

    // ── Helper Conversion Methods ────────────────────────────────────────────

    public static String toUri(String path) {
        if (path == null) return "";
        path = path.replace("\\", "/");
        if (path.length() >= 2 && path.charAt(1) == ':') {
            char drive = Character.toLowerCase(path.charAt(0));
            path = drive + "%3A" + path.substring(2);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file://" + path;
    }

    public static String fromUri(String uri) {
        if (uri == null || !uri.startsWith("file://")) return uri;
        String path = uri.substring(7); // Strip "file://"
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        // Decode drive letter (e.g. c%3A -> C:)
        if (path.length() >= 4 && path.substring(1, 4).equalsIgnoreCase("%3A")) {
            char drive = Character.toUpperCase(path.charAt(0));
            path = drive + ":" + path.substring(4);
        }
        path = path.replace("/", "\\");
        return path;
    }

    public static class ChatRequestDto {
        public String prompt;
    }

    @PostMapping("/{id}/chat")
    @ResponseBody
    public ResponseEntity<?> sendChat(@PathVariable String id, @RequestBody ChatRequestDto request) {
        if (request.prompt == null || request.prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        try {
            String response = chatService.sendPrompt(id, request.prompt);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            log.error("Failed to run chat for project {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Agent failed to process prompt: " + e.getMessage()));
        }
    }
}
