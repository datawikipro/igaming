package pro.datawiki.igaming.capture.esports.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.capture.esports.domain.EsportsRawEventEntity;
import pro.datawiki.igaming.capture.esports.repository.EsportsRawEventRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalizes raw hierarchical esports payloads (CS2, Dota 2, Valorant)
 * into flat tabular records structured for Parquet/DuckDB ML training pipelines
 * for the Quantum Cyber Analyst.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EsportsParquetExporterService {

    private final EsportsRawEventRepository esportsRawEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.capture.esports.export.dir:target/parquet-export}")
    private String exportDirectory;

    /**
     * Exports all MAPPED and processed esports events to a normalized dataset file.
     */
    public ExportResult exportFlatDataset(String gameFilter) {
        log.info("📊 [PARQUET-EXPORT] Starting dataset export (filter: {})...", gameFilter);
        List<EsportsRawEventEntity> entities = (gameFilter != null && !gameFilter.isBlank())
                ? esportsRawEventRepository.findByGame(gameFilter.toUpperCase())
                : esportsRawEventRepository.findAll();

        List<FlatEsportsRecord> flatRecords = new ArrayList<>();

        for (EsportsRawEventEntity entity : entities) {
            try {
                FlatEsportsRecord record = flattenRecord(entity);
                if (record != null) {
                    flatRecords.add(record);
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to flatten event {}: {}", entity.getEventId(), e.getMessage());
            }
        }

        File outDir = new File(exportDirectory);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        String fileName = "esports_features_" + System.currentTimeMillis() + ".jsonl";
        File outFile = new File(outDir, fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outFile, StandardCharsets.UTF_8))) {
            for (FlatEsportsRecord rec : flatRecords) {
                writer.println(objectMapper.writeValueAsString(rec));
            }
        } catch (Exception e) {
            log.error("Failed to write flat dataset: {}", e.getMessage(), e);
            throw new RuntimeException("Parquet export failed", e);
        }

        log.info("✅ [PARQUET-EXPORT] Successfully exported {} flat ML records to {}",
                flatRecords.size(), outFile.getAbsolutePath());

        return ExportResult.builder()
                .recordsExported(flatRecords.size())
                .exportPath(outFile.getAbsolutePath())
                .completedAt(Instant.now().toString())
                .build();
    }

    private FlatEsportsRecord flattenRecord(EsportsRawEventEntity entity) {
        if (entity.getRawPayloadJson() == null || entity.getRawPayloadJson().isBlank()) {
            return null;
        }

        try {
            JsonNode payload = objectMapper.readTree(entity.getRawPayloadJson());
            FlatEsportsRecord.FlatEsportsRecordBuilder b = FlatEsportsRecord.builder()
                    .eventId(entity.getEventId())
                    .game(entity.getGame())
                    .tournamentRaw(entity.getTournamentRaw())
                    .team1Raw(entity.getTeam1Raw())
                    .team2Raw(entity.getTeam2Raw())
                    .matchDate(entity.getMatchDate())
                    .mdmMatchId(entity.getMdmMatchId())
                    .mdmHomeTeamId(entity.getMdmHomeTeamId())
                    .mdmAwayTeamId(entity.getMdmAwayTeamId())
                    .mdmStatus(entity.getMdmStatus());

            if ("CS2".equalsIgnoreCase(entity.getGame())) {
                flattenCs2(payload, b);
            } else if ("DOTA2".equalsIgnoreCase(entity.getGame())) {
                flattenDota2(payload, b);
            } else if ("VALORANT".equalsIgnoreCase(entity.getGame())) {
                flattenValorant(payload, b);
            }

            return b.build();
        } catch (Exception e) {
            return null;
        }
    }

    private void flattenCs2(JsonNode payload, FlatEsportsRecord.FlatEsportsRecordBuilder b) {
        JsonNode players = payload.path("players");
        if (players.isArray()) {
            double r1Sum = 0, r2Sum = 0;
            int r1Count = 0, r2Count = 0;
            for (JsonNode p : players) {
                double rating = p.path("rating20").asDouble(1.0);
                String team = p.path("team").asText();
                if (team.equalsIgnoreCase(b.team1Raw)) {
                    r1Sum += rating;
                    r1Count++;
                } else {
                    r2Sum += rating;
                    r2Count++;
                }
            }
            if (r1Count > 0) b.team1AvgRating(r1Sum / r1Count);
            if (r2Count > 0) b.team2AvgRating(r2Sum / r2Count);
        }
        b.featureVectorName("esports_cs2_tactical_dna");
    }

    private void flattenDota2(JsonNode payload, FlatEsportsRecord.FlatEsportsRecordBuilder b) {
        JsonNode goldAdv = payload.path("radiantGoldAdvTimeline");
        if (goldAdv.isArray() && goldAdv.size() >= 10) {
            b.dotaGoldAdv10m(goldAdv.get(9).asInt());
        }
        b.featureVectorName("esports_dota2_tactical_dna");
    }

    private void flattenValorant(JsonNode payload, FlatEsportsRecord.FlatEsportsRecordBuilder b) {
        b.featureVectorName("esports_valorant_tactical_dna");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlatEsportsRecord {
        private String eventId;
        private String game;
        private String tournamentRaw;
        private String team1Raw;
        private String team2Raw;
        private Long matchDate;
        private Long mdmMatchId;
        private Long mdmHomeTeamId;
        private Long mdmAwayTeamId;
        private String mdmStatus;
        private String featureVectorName;

        // Flattened ML features
        private Double team1AvgRating;
        private Double team2AvgRating;
        private Integer dotaGoldAdv10m;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportResult {
        private int recordsExported;
        private String exportPath;
        private String completedAt;
    }
}
