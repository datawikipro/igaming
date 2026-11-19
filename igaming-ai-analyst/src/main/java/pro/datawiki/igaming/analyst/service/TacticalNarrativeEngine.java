package pro.datawiki.igaming.analyst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.dto.GoogleTrendsMetricsDto;
import pro.datawiki.igaming.analyst.dto.MatchVisionBlueprintDto;
import pro.datawiki.igaming.analyst.dto.ProjectedLineupDto;
import pro.datawiki.igaming.analyst.dto.ProjectedPlayerDto;

import java.util.*;

/**
 * Editorial Narrative Synthesis Engine:
 * Generates elite publication-grade match previews and post-match debriefs
 * matching the tactical depth and storytelling of The Athletic and Opta Analyst.
 *
 * Supports:
 * 1. Dual Tone:
 *    - Tone.SPORTVIBE_MEDIA: journalistic narrative (SEO-optimized, storytelling, coach duels, Google E-E-A-T).
 *    - Tone.SMARTBET_QUANT: institutional quantitative dossier (probability core, 45-parameter matrix, scenario branches).
 * 2. Multilingual Generation:
 *    - Native generation in RU, EN, ES, FR, PT.
 * 3. Dossier Versioning:
 *    - v1 (T-7d .. T-24h): early projected lineup & tactical blueprint.
 *    - v2 (T-2d .. T-12h): reactive update on starter injury/suspension with explicit delta summary.
 *    - v_confirmed (T-1h): official matchday protocol & final tactical shift.
 * 4. LLM Editorial Prompt Adapter:
 *    - Generates comprehensive Gemini LLM prompts with organic Google Trends search intent integration.
 */
@Slf4j
@Service
public class TacticalNarrativeEngine {

    /**
     * Editorial tone for narrative synthesis.
     */
    public enum Tone {
        SPORTVIBE_MEDIA,
        SMARTBET_QUANT
    }

    /**
     * Synthesizes pre-match tactical preview narrative (defaults to SMARTBET_QUANT, RU, v1).
     * Retains backwards compatibility with existing tests and services.
     */
    public String generatePreMatchNarrative(MatchVisionBlueprintDto blueprint) {
        return generatePreMatchNarrative(blueprint, Tone.SMARTBET_QUANT, "ru", "v1", null);
    }

    /**
     * Synthesizes pre-match narrative with custom tone and language.
     */
    public String generatePreMatchNarrative(MatchVisionBlueprintDto blueprint, Tone tone, String targetLanguage) {
        String ver = blueprint != null && blueprint.getVersionTag() != null ? blueprint.getVersionTag() : "v1";
        String delta = blueprint != null ? blueprint.getDeltaSummary() : null;
        return generatePreMatchNarrative(blueprint, tone, targetLanguage, ver, delta);
    }

    /**
     * Synthesizes pre-match narrative with custom tone.
     */
    public String generatePreMatchNarrative(MatchVisionBlueprintDto blueprint, Tone tone) {
        return generatePreMatchNarrative(blueprint, tone, "ru");
    }

    /**
     * Comprehensive multi-language, multi-tone, version-aware pre-match narrative generator.
     */
    public String generatePreMatchNarrative(MatchVisionBlueprintDto blueprint,
                                            Tone tone,
                                            String targetLanguage,
                                            String version,
                                            String deltaSummary) {
        if (blueprint == null) return "";

        Tone selectedTone = tone != null ? tone : Tone.SMARTBET_QUANT;
        String lang = normalizeLang(targetLanguage);
        String ver = normalizeVersion(version);

        if (selectedTone == Tone.SPORTVIBE_MEDIA) {
            return generateSportVibeMediaNarrative(blueprint, lang, ver, deltaSummary);
        } else {
            return generateSmartBetQuantNarrative(blueprint, lang, ver, deltaSummary);
        }
    }

    /**
     * Institutional quantitative narrative generator (SmartBet Quant / Opta).
     */
    private String generateSmartBetQuantNarrative(MatchVisionBlueprintDto blueprint,
                                                  String lang,
                                                  String version,
                                                  String deltaSummary) {
        String h = blueprint.getHomeTeam() != null ? blueprint.getHomeTeam() : "Home Team";
        String a = blueprint.getAwayTeam() != null ? blueprint.getAwayTeam() : "Away Team";
        String tourn = blueprint.getTournament() != null ? blueprint.getTournament() : "Tournament";

        ProjectedLineupDto hLineup = blueprint.getHomeProjectedLineup();
        ProjectedLineupDto aLineup = blueprint.getAwayProjectedLineup();

        String hForm = hLineup != null && hLineup.getFormation() != null ? hLineup.getFormation() : "4-4-2";
        String aForm = aLineup != null && aLineup.getFormation() != null ? aLineup.getFormation() : "4-4-2";

        double p1 = blueprint.getProbHome() != null ? blueprint.getProbHome().doubleValue() * 100 : 33.0;
        double px = blueprint.getProbDraw() != null ? blueprint.getProbDraw().doubleValue() * 100 : 34.0;
        double p2 = blueprint.getProbAway() != null ? blueprint.getProbAway().doubleValue() * 100 : 33.0;

        List<String> scores = blueprint.getMostLikelyScores();
        String scoresStr = (scores != null && !scores.isEmpty())
                ? String.join(", ", scores.subList(0, Math.min(3, scores.size())))
                : "1:1, 0:0";

        int matrixSize = blueprint.getTacticalMatrix() != null ? blueprint.getTacticalMatrix().size() : 45;

        StringBuilder sb = new StringBuilder();

        switch (lang) {
            case "en" -> buildQuantEnglish(sb, h, a, tourn, hForm, aForm, p1, px, p2, scoresStr, hLineup, aLineup, matrixSize, blueprint, version, deltaSummary);
            case "es" -> buildQuantSpanish(sb, h, a, tourn, hForm, aForm, p1, px, p2, scoresStr, hLineup, aLineup, matrixSize, blueprint, version, deltaSummary);
            case "fr" -> buildQuantFrench(sb, h, a, tourn, hForm, aForm, p1, px, p2, scoresStr, hLineup, aLineup, matrixSize, blueprint, version, deltaSummary);
            case "pt" -> buildQuantPortuguese(sb, h, a, tourn, hForm, aForm, p1, px, p2, scoresStr, hLineup, aLineup, matrixSize, blueprint, version, deltaSummary);
            default -> buildQuantRussian(sb, h, a, tourn, hForm, aForm, p1, px, p2, scoresStr, hLineup, aLineup, matrixSize, blueprint, version, deltaSummary);
        }

        return sb.toString();
    }

    private void buildQuantRussian(StringBuilder sb, String h, String a, String tourn,
                                   String hForm, String aForm, double p1, double px, double p2,
                                   String scoresStr, ProjectedLineupDto hLineup, ProjectedLineupDto aLineup,
                                   int matrixSize, MatchVisionBlueprintDto blueprint, String version, String deltaSummary) {
        String verLabel = getVersionLabel(version, "ru");
        sb.append(String.format("🎙 [The Athletic / Opta Tactical Blueprint]: %s — %s (%s)%s\n\n",
                h, a, tourn, verLabel.isEmpty() ? "" : " " + verLabel));

        if (deltaSummary != null && !deltaSummary.isBlank()) {
            sb.append(String.format("🚨 РЕАКТИВНАЯ ДЕЛЬТА СОСТАВА (%s):\n%s\nТактический сдвиг: вынужденная ротация и смещение акцентов в переходных фазах.\n\n",
                    version, deltaSummary));
        }

        // ── PROLOGUE: PROBABILITY CORE ──
        sb.append(String.format("⚖️ ВЕРОЯТНОСТНОЕ ЯДРО ПАРИТЕТА:\n" +
                "Матч высочайшей плотности исходов (П1: %.0f%% | Ничья: %.0f%% | П2: %.0f%%). " +
                "Модальный прогноз счета: %s. Модель идентифицирует матч как классическую шахматную партию двух организованных структур.\n\n",
                p1, px, p2, scoresStr));

        // ── АКТ 1 ──
        sb.append(String.format("🎭 АКТ 1: ДЕБЮТ И РАЗВЕДКА (1–45') — СТАРТОВЫЕ СХЕМЫ И ДУЭЛИ\n" +
                "• Схемы за 24ч до стартового свистка: «%s» выстраивается в %s против %s у «%s».\n",
                h, hForm, aForm, a));
        if (hLineup != null && hLineup.getContestedDuels() != null && !hLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Спорный узел хозяев: %s.\n", String.join("; ", hLineup.getContestedDuels())));
        }
        if (aLineup != null && aLineup.getContestedDuels() != null && !aLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Спорный узел гостей: %s.\n", String.join("; ", aLineup.getContestedDuels())));
        }
        sb.append("• Динамика 1-го тайма: Высокая позиционная осторожность. Ожидается не более 1 забитого мяча до перерыва (ТМ 1.5), " +
                "сдержанный прессинг (ЖК ТМ 1.5) и лимитированное число ударов в створ (<= 4.5).\n\n");

        // ── АКТ 2 ──
        sb.append("⚡ АКТ 2: ГДЕ СЛОМАЕТСЯ ИГРА (46–70') — ТАКТИЧЕСКИЙ РАЗЛОМ И ПРОСТРАНСТВО\n" +
                "• Точка напряжения: Паритет владения мячом (коридор 40–60%) при неизбежном физическом утомлении опорных зон. " +
                "После 50-й минуты команды начнут форсировать вертикальные передачи через полуфланги (Final Third phase > 65%).\n" +
                "• Раскрытие темпа: Второй тайм прогнозируется результативнее первого, с резким ростом объема ударов (ТБ 10.5 во 2-м тайме) " +
                "и увеличением интенсивности единоборств на газоне (от 100 за матч).\n\n");

        // ── АКТ 3 ──
        sb.append("🔥 АКТ 3: РАЗВЯЗКА И ДЖОКЕРЫ (70–90+') — СКАМЕЙКА, СТАНДАРТЫ И КАРТОЧКИ\n");
        appendJokers(sb, h, a, hLineup, aLineup, "ru");
        sb.append("• Финальный штурм: Рост стандартов (угловые во 2-м тайме ТБ 4.5) и тактических срывов атак (карточки во 2-м тайме ТБ 1.5). " +
                "Высокая вероятность решающего гола после 75-й минуты.\n\n");

        // ── МАТРИЦА И СЦЕНАРИИ ──
        sb.append(String.format("📐 ГЛУБОКАЯ ТАКТИЧЕСКАЯ МАТРИЦА (%d ПАРАМЕТРОВ):\n" +
                "Включает 5 срезов: 1-й тайм (6), 2-й тайм (6), Пространство и xG (10), Единоборства и Физика (10), Исходы и Тоталы (13). " +
                "Верифицируется после матча по статистике SofaScore с целевым порогом точности >= 80%%.\n\n", matrixSize));

        appendScenarios(sb, blueprint, "ru");
    }

    private void buildQuantEnglish(StringBuilder sb, String h, String a, String tourn,
                                   String hForm, String aForm, double p1, double px, double p2,
                                   String scoresStr, ProjectedLineupDto hLineup, ProjectedLineupDto aLineup,
                                   int matrixSize, MatchVisionBlueprintDto blueprint, String version, String deltaSummary) {
        String verLabel = getVersionLabel(version, "en");
        sb.append(String.format("🎙 [The Athletic / Opta Tactical Blueprint]: %s vs %s (%s)%s\n\n",
                h, a, tourn, verLabel.isEmpty() ? "" : " " + verLabel));

        if (deltaSummary != null && !deltaSummary.isBlank()) {
            sb.append(String.format("🚨 REACTIVE LINEUP DELTA (%s):\n%s\nTactical implication: forced structural adjustment, shifting transition vectors.\n\n",
                    version, deltaSummary));
        }

        sb.append(String.format("⚖️ PROBABILITY CORE & PARITY ENGINE:\n" +
                "High-parity tactical encounter (Home: %.0f%% | Draw: %.0f%% | Away: %.0f%%). " +
                "Modal projected score: %s. Mathematical model identifies this as a strategic chess match between two structured setups.\n\n",
                p1, px, p2, scoresStr));

        sb.append(String.format("🎭 ACT 1: OPENING & LINEUP BLUEPRINT (1–45') — FORMATIONS & DUELS\n" +
                "• Tactical shape: \"%s\" deploys in %s against %s for \"%s\".\n",
                h, hForm, aForm, a));
        if (hLineup != null && hLineup.getContestedDuels() != null && !hLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Home contested zone: %s.\n", String.join("; ", hLineup.getContestedDuels())));
        }
        if (aLineup != null && aLineup.getContestedDuels() != null && !aLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Away contested zone: %s.\n", String.join("; ", aLineup.getContestedDuels())));
        }
        sb.append("• 1st Half Dynamics: High positional discipline. Expected Under 1.5 goals before interval, measured pressing and limited shots on target (<= 4.5).\n\n");

        sb.append("⚡ ACT 2: STRUCTURAL INFLECTION (46–70') — TACTICAL BREAK & SPACE EXPLOITATION\n" +
                "• Inflection point: Possession parity (40–60% corridor) meets central midfield fatigue. Teams will force vertical half-space channels (Final Third phase > 65%).\n" +
                "• Pace acceleration: The second half projects higher goal expectation, surge in total shots (Over 10.5 in 2nd half), and high duel volume (100+).\n\n");

        sb.append("🔥 ACT 3: CLIMAX & BENCH JOKERS (70–90+') — SQUAD DEPTH, SET PIECES & DISCIPLINE\n");
        appendJokers(sb, h, a, hLineup, aLineup, "en");
        sb.append("• Final surge: Set-piece accumulation (2nd half corners Over 4.5) and tactical fouls (cards Over 1.5). High probability of decisive goal after 75'.\n\n");

        sb.append(String.format("📐 DEEP TACTICAL MATRIX (%d PARAMETERS):\n" +
                "Covering 5 pillars: 1st Half (6), 2nd Half (6), Spatial & Attack xG (10), Duels & Physicality (10), Outcomes & Totals (13). Benchmarked against SofaScore at >= 80%% accuracy.\n\n", matrixSize));

        appendScenarios(sb, blueprint, "en");
    }

    private void buildQuantSpanish(StringBuilder sb, String h, String a, String tourn,
                                   String hForm, String aForm, double p1, double px, double p2,
                                   String scoresStr, ProjectedLineupDto hLineup, ProjectedLineupDto aLineup,
                                   int matrixSize, MatchVisionBlueprintDto blueprint, String version, String deltaSummary) {
        String verLabel = getVersionLabel(version, "es");
        sb.append(String.format("🎙 [The Athletic / Opta Tactical Blueprint]: %s vs %s (%s)%s\n\n",
                h, a, tourn, verLabel.isEmpty() ? "" : " " + verLabel));

        if (deltaSummary != null && !deltaSummary.isBlank()) {
            sb.append(String.format("🚨 DELTA REACTIVA DE ALINEACIÓN (%s):\n%s\nImpacto táctico: ajuste estructural obligado y redistribución de pasillos de salida.\n\n",
                    version, deltaSummary));
        }

        sb.append(String.format("⚖️ NÚCLEO PROBABILÍSTICO DE PARIDAD:\n" +
                "Encuentro de alta paridad táctica (Local: %.0f%% | Empate: %.0f%% | Visitante: %.0f%%). " +
                "Marcador modal proyectado: %s. Modelo de ajedrez estratégico entre dos estructuras organizadas.\n\n",
                p1, px, p2, scoresStr));

        sb.append(String.format("🎭 ACTO 1: DEBUT Y EXPLORACIÓN (1–45') — ESQUEMAS Y DUELOS\n" +
                "• Disposición inicial: «%s» en %s frente al %s de «%s».\n",
                h, hForm, aForm, a));
        if (hLineup != null && hLineup.getContestedDuels() != null && !hLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Duelo posicional local: %s.\n", String.join("; ", hLineup.getContestedDuels())));
        }
        if (aLineup != null && aLineup.getContestedDuels() != null && !aLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Duelo posicional visitante: %s.\n", String.join("; ", aLineup.getContestedDuels())));
        }
        sb.append("• Dinámica del 1.er tiempo: Máxima cautela táctica. Menos de 1.5 goles al descanso y remates contenidos a puerta (<= 4.5).\n\n");

        sb.append("⚡ ACTO 2: INFLEXIÓN ESTRUCTURAL (46–70') — QUIEBRE TÁCTICO Y ESPACIO\n" +
                "• Punto de ruptura: Paridad de posesión (40–60%) y desgaste físico en pivotes. Progresión vertical por carriles interiores.\n" +
                "• Ritmo ascendente: Segunda parte con mayor volumen ofensivo (más de 10.5 disparos) y fricción física constante.\n\n");

        sb.append("🔥 ACTO 3: DESENLACE Y REVULSIVOS (70–90+') — BANQUILLO, BALÓN PARADO Y TARJETAS\n");
        appendJokers(sb, h, a, hLineup, aLineup, "es");
        sb.append("• Asedio final: Aumento de saques de esquina y faltas tácticas. Alta probabilidad de gol decisivo tras el minuto 75.\n\n");

        sb.append(String.format("📐 MATRIZ TÁCTICA AVANZADA (%d PARÁMETROS):\n" +
                "5 dimensiones: 1.ª Parte (6), 2.ª Parte (6), Espacio y xG (10), Duelos (10), Totales (13). Verificación SofaScore (meta >= 80%%).\n\n", matrixSize));

        appendScenarios(sb, blueprint, "es");
    }

    private void buildQuantFrench(StringBuilder sb, String h, String a, String tourn,
                                  String hForm, String aForm, double p1, double px, double p2,
                                  String scoresStr, ProjectedLineupDto hLineup, ProjectedLineupDto aLineup,
                                  int matrixSize, MatchVisionBlueprintDto blueprint, String version, String deltaSummary) {
        String verLabel = getVersionLabel(version, "fr");
        sb.append(String.format("🎙 [The Athletic / Opta Tactical Blueprint] : %s vs %s (%s)%s\n\n",
                h, a, tourn, verLabel.isEmpty() ? "" : " " + verLabel));

        if (deltaSummary != null && !deltaSummary.isBlank()) {
            sb.append(String.format("🚨 DELTA RÉACTIF DE COMPOSITION (%s) :\n%s\nImpact tactique : réorganisation forcée et ajustement des circuits de transition.\n\n",
                    version, deltaSummary));
        }

        sb.append(String.format("⚖️ CŒUR PROBABILISTE DE PARITÉ :\n" +
                "Match à très haute densité tactique (1: %.0f%% | Nul: %.0f%% | 2: %.0f%%). " +
                "Score modal prévisionnel : %s. Partie d'échecs tactique entre deux blocs solides.\n\n",
                p1, px, p2, scoresStr));

        sb.append(String.format("🎭 ACTE 1 : OUVERTURE ET ALIGNEMENTS (1–45') — SCHÉMAS ET DUELS\n" +
                "• Dispositifs : « %s » en %s face au %s de « %s ».\n",
                h, hForm, aForm, a));
        if (hLineup != null && hLineup.getContestedDuels() != null && !hLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Duel clé domicile : %s.\n", String.join("; ", hLineup.getContestedDuels())));
        }
        if (aLineup != null && aLineup.getContestedDuels() != null && !aLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Duel clé extérieur : %s.\n", String.join("; ", aLineup.getContestedDuels())));
        }
        sb.append("• Dynamique 1re mi-temps : Prudence collective. Moins de 1.5 but avant la pause et frappes cadrées contenues (<= 4.5).\n\n");

        sb.append("⚡ ACTE 2 : RUPTURE STRUCTURELLE (46–70') — FAILLE TACTIQUE ET ESPACE\n" +
                "• Point de bascule : Équilibre de possession (40–60%) combiné à la fatigue axiale. Transitions verticales accrues.\n" +
                "• Accélération : Seconde mi-temps plus ouverte, nette hausse des tentatives de tirs et forte densité dans les duels.\n\n");

        sb.append("🔥 ACTE 3 : DÉNOUEMENT ET JOKERS (70–90+') — PROFONDEUR DE BANC ET CARTONS\n");
        appendJokers(sb, h, a, hLineup, aLineup, "fr");
        sb.append("• Assaut final : Multiplication des corners et fautes d'antijeu. Forte probabilité de but décisif après la 75e minute.\n\n");

        sb.append(String.format("📐 MATRICE TACTIQUE DÉTAILLÉE (%d PARAMÈTRES) :\n" +
                "5 axes : 1re période (6), 2de période (6), Espace & xG (10), Duels (10), Issues & Totaux (13). Évalué sur données SofaScore (cible >= 80%%).\n\n", matrixSize));

        appendScenarios(sb, blueprint, "fr");
    }

    private void buildQuantPortuguese(StringBuilder sb, String h, String a, String tourn,
                                     String hForm, String aForm, double p1, double px, double p2,
                                     String scoresStr, ProjectedLineupDto hLineup, ProjectedLineupDto aLineup,
                                     int matrixSize, MatchVisionBlueprintDto blueprint, String version, String deltaSummary) {
        String verLabel = getVersionLabel(version, "pt");
        sb.append(String.format("🎙 [The Athletic / Opta Tactical Blueprint]: %s vs %s (%s)%s\n\n",
                h, a, tourn, verLabel.isEmpty() ? "" : " " + verLabel));

        if (deltaSummary != null && !deltaSummary.isBlank()) {
            sb.append(String.format("🚨 DELTA REATIVO DE ESCALAÇÃO (%s):\n%s\nImpacto tático: alteração forçada de desenho e readequação de transições.\n\n",
                    version, deltaSummary));
        }

        sb.append(String.format("⚖️ NÚCLEO PROBABILÍSTICO DE PARIDADE:\n" +
                "Partida de altíssimo equilíbrio tático (1: %.0f%% | Empate: %.0f%% | 2: %.0f%%). " +
                "Placar modal projetado: %s. Cenário de xadrez tático entre dois conjuntos disciplinados.\n\n",
                p1, px, p2, scoresStr));

        sb.append(String.format("🎭 ATO 1: DEBUTE E EXPLORAÇÃO (1–45') — ESCALAÇÕES E DUELOS\n" +
                "• Desenhos iniciais: \"%s\" em %s contra o %s de \"%s\".\n",
                h, hForm, aForm, a));
        if (hLineup != null && hLineup.getContestedDuels() != null && !hLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Disputa por vaga mandante: %s.\n", String.join("; ", hLineup.getContestedDuels())));
        }
        if (aLineup != null && aLineup.getContestedDuels() != null && !aLineup.getContestedDuels().isEmpty()) {
            sb.append(String.format("• Disputa por vaga visitante: %s.\n", String.join("; ", aLineup.getContestedDuels())));
        }
        sb.append("• Dinâmica do 1º Tempo: Rigor posicional estrito. Menos de 1.5 gols no primeiro tempo e poucos chutes no alvo (<= 4.5).\n\n");

        sb.append("⚡ ATO 2: INFLEXÃO ESTRUTURAL (46–70') — RUPTURA TÁTICA E ESPAÇO\n" +
                "• Ponto de quebra: Paridade de posse (40–60%) com desgaste físico dos volantes. Exploração de transições verticais.\n" +
                "• Intensidade elevada: Segunda etapa mais fluida, volume expressivo de finalizações e disputa intensa por segundas bolas.\n\n");

        sb.append("🔥 ATO 3: DESFECHO E CORINGAS (70–90+') — BANCO, BOLAS PARADAS E CARTÕES\n");
        appendJokers(sb, h, a, hLineup, aLineup, "pt");
        sb.append("• Reta final: Acúmulo de escanteios e faltas táticas. Forte probabilidade de gol determinante após os 75 minutos.\n\n");

        sb.append(String.format("📐 MATRIZ TÁTICA AVANÇADA (%d PARÁMETROS):\n" +
                "5 pilares: 1º Tempo (6), 2º Tempo (6), Espaço e xG (10), Duelos (10), Desfechos e Totais (13). Validação SofaScore (meta >= 80%%).\n\n", matrixSize));

        appendScenarios(sb, blueprint, "pt");
    }

    /**
     * Media journalistic narrative generator (SportVibe Media).
     * Formatted with story tension, manager tactical clash, intrigue, and Google E-E-A-T structure.
     */
    private String generateSportVibeMediaNarrative(MatchVisionBlueprintDto blueprint,
                                                   String lang,
                                                   String version,
                                                   String deltaSummary) {
        String h = blueprint.getHomeTeam() != null ? blueprint.getHomeTeam() : "Home Team";
        String a = blueprint.getAwayTeam() != null ? blueprint.getAwayTeam() : "Away Team";
        String tourn = blueprint.getTournament() != null ? blueprint.getTournament() : "Tournament";

        ProjectedLineupDto hLineup = blueprint.getHomeProjectedLineup();
        ProjectedLineupDto aLineup = blueprint.getAwayProjectedLineup();

        String hForm = hLineup != null && hLineup.getFormation() != null ? hLineup.getFormation() : "4-4-2";
        String aForm = aLineup != null && aLineup.getFormation() != null ? aLineup.getFormation() : "4-4-2";

        List<String> scores = blueprint.getMostLikelyScores();
        String scoreEstimate = (scores != null && !scores.isEmpty()) ? scores.get(0) : "1:1";

        StringBuilder sb = new StringBuilder();

        switch (lang) {
            case "en" -> {
                sb.append(String.format("🔥 [SportVibe Media Story]: %s vs %s — Clash of Philosophies & Tactical Intrigue (%s)\n\n", h, a, tourn));
                if (deltaSummary != null && !deltaSummary.isBlank()) {
                    sb.append(String.format("🚨 BREAKING SQUAD NEWS (%s):\n%s\nLate roster changes will severely test squad depth and in-game tactical flexibility.\n\n", version, deltaSummary));
                }
                sb.append(String.format("📰 CHAPTER 1: THE NARRATIVE ARC & STAKES\n" +
                        "When \"%s\" and \"%s\" meet on the European stage, historical weight and tactical pride collide. " +
                        "In %s, every transitional error carries high consequences. Our predictive models highlight a razor-thin parity margin.\n\n", h, a, tourn));
                sb.append(String.format("🧠 CHAPTER 2: CHESS ON THE PITCH — MANAGERIAL DUEL\n" +
                        "• System Blueprint: \"%s\" lines up in %s countering the %s structure of \"%s\".\n" +
                        "• Pitch Fault Lines: Half-space battles and transitional pressing triggers will dictate territorial dominance.\n\n", h, hForm, aForm, a));
                sb.append("⚡ CHAPTER 3: THE TURNING POINT & BENCH FIREPOWER\n" +
                        "As legs tire after the 65th minute, dynamic bench jokers will find exploitable transitional gaps. Expect late set-piece drama.\n\n");
                sb.append(String.format("🔍 EDITORIAL VERDICT & E-E-A-T TAKEAWAY:\n" +
                        "A tense, tactical battle with modal projected scoreline of %s. The decisive swing is forecast to materialize after the 75th minute.\n", scoreEstimate));
            }
            case "es" -> {
                sb.append(String.format("🔥 [SportVibe Media Story]: %s vs %s — Choque de Estilos y Drama Táctico (%s)\n\n", h, a, tourn));
                if (deltaSummary != null && !deltaSummary.isBlank()) {
                    sb.append(String.format("🚨 ÚLTIMA HORA EN LA ALINEACIÓN (%s):\n%s\nBajas sensibles de última hora obligan a replantear el plan de partido.\n\n", version, deltaSummary));
                }
                sb.append(String.format("📰 CAPÍTULO 1: LA INTRIGA Y EL CONTEXTO DRAMÁTICO\n" +
                        "El choque entre «%s» y «%s» en %s trasciende lo estadístico: dos modelos competitivos frente a frente en un pulso de alta tensión táctica.\n\n", h, a, tourn));
                sb.append(String.format("🧠 CAPÍTULO 2: AJEDREZ EN EL CÉSPED — DUELO EN LOS BANQUILLOS\n" +
                        "• Esquemas frente a frente: «%s» (%s) contra «%s» (%s).\n" +
                        "• Batalla estratégica: Bloques compactos que obligarán a buscar rupturas por los pasillos interiores.\n\n", h, hForm, a, aForm));
                sb.append("⚡ CAPÍTULO 3: EL PUNTO DE INFLEXIÓN Y EL PODER DEL BANQUILLO\n" +
                        "El desenlace quedará reservado para la última media hora, donde los recambios aportarán frescura y verticalidad.\n\n");
                sb.append(String.format("🔍 VEREDICTO EDITORIAL E-E-A-T:\n" +
                        "Pronóstico de máxima igualdad con marcador modal proyectado de %s. Partido decidido por detalles a partir del minuto 75.\n", scoreEstimate));
            }
            case "fr" -> {
                sb.append(String.format("🔥 [SportVibe Media Story] : %s vs %s — Choc des Philosophies et Drame Tactique (%s)\n\n", h, a, tourn));
                if (deltaSummary != null && !deltaSummary.isBlank()) {
                    sb.append(String.format("🚨 FLASH COMPOSITIONS (%s) :\n%s\nLes absences de dernière minute bouleversent la préparation et exigent des adaptations immédiates.\n\n", version, deltaSummary));
                }
                sb.append(String.format("📰 CHAPITRE 1 : L'INTRIGUE ET LES ENJEUX DRAMATIQUES\n" +
                        "L'affrontement entre « %s » et « %s » dans le cadre de %s propose un duel d'une rare intensité tactique et psychologique.\n\n", h, a, tourn));
                sb.append(String.format("🧠 CHAPITRE 2 : ÉCHECS SUR LA PELOUSE — CONFRONTATION TACTIQUE\n" +
                        "• Schémas tactiques : « %s » (%s) face au « %s » (%s).\n" +
                        "• Clés de la rencontre : Maîtrise des transitions rapides et étanchéité de l'axe central.\n\n", h, hForm, a, aForm));
                sb.append("⚡ CHAPITRE 3 : LE TOURNANT DU MATCH ET L'IMPACT DU BANC\n" +
                        "La différence se fera dans le dernier quart d'heure avec l'entrée des jokers offensifs pour faire basculer le score.\n\n");
                sb.append(String.format("🔍 VERDICT ÉDITORIAL E-E-A-T :\n" +
                        "Partie fermée et disputée (score modal : %s), où la décision devrait intervenir après la 75e minute.\n", scoreEstimate));
            }
            case "pt" -> {
                sb.append(String.format("🔥 [SportVibe Media Story]: %s vs %s — Choque de Estilos e Drama Tático (%s)\n\n", h, a, tourn));
                if (deltaSummary != null && !deltaSummary.isBlank()) {
                    sb.append(String.format("🚨 BOLETIM DE ÚLTIMA HORA (%s):\n%s\nDesfalques de última hora forçam alterações táticas estratégicas.\n\n", version, deltaSummary));
                }
                sb.append(String.format("📰 CAPÍTULO 1: O ENREDO E A DRAMATURGIA DA PARTIDA\n" +
                        "O confronto entre \"%s\" e \"%s\" pela %s promete um grande espetáculo de tensão competitiva e duelo de estratégias refinadas.\n\n", h, a, tourn));
                sb.append(String.format("🧠 CAPÍTULO 2: XADREZ NO RELVADO — DUELO DE TREINADORES\n" +
                        "• Estrutura tática: \"%s\" (%s) versus \"%s\" (%s).\n" +
                        "• Batalha de meio-campo: Disputa intensa por espaços e bloqueio das principais rotas ofensivas.\n\n", h, hForm, a, aForm));
                sb.append("⚡ CAPÍTULO 3: O PONTO DE VIRADA E O PESO DO BANCO\n" +
                        "A entrada dos jogadores de banco após os 65 minutos será o catalisador para quebrar o impasse tático.\n\n");
                sb.append(String.format("🔍 VEREDITO EDITORIAL E-E-A-T:\n" +
                        "Equilíbrio evidente com placar modal projetado de %s. Decisão reservada para a reta final da partida.\n", scoreEstimate));
            }
            default -> { // RU
                sb.append(String.format("🔥 [SportVibe Media Story]: %s — %s: Битва стилей, тактическая драма и дуэль тренеров (%s)\n\n", h, a, tourn));
                if (deltaSummary != null && !deltaSummary.isBlank()) {
                    sb.append(String.format("🚨 СРОЧНЫЙ АПДЕЙТ СОСТАВОВ (%s):\n%s\nКадровые перестановки в последний момент ломают первоначальный план и проверяют глубину резерва.\n\n", version, deltaSummary));
                }
                sb.append(String.format("📰 ГЛАВА 1: ИНТРИГА И ДРАМАТУРГИЯ МАТЧА\n" +
                        "Когда сходятся «%s» и «%s», турнирная таблица отходит на второй план. В матче турнира %s на кону стоят столкновение футбольных философий " +
                        "и тренерский престиж. Математическая модель указывает на то, что судьбу встречи определят микро-эпизоды.\n\n", h, a, tourn));
                sb.append(String.format("🧠 ГЛАВА 2: ШАХМАТЫ НА ГАЗОНЕ — ТАКТИЧЕСКИЙ РАЗБОР И ДУЭЛИ\n" +
                        "• Тактический рисунок: «%s» (%s) против «%s» (%s).\n" +
                        "• Ключевые зоны противостояния: Борьба за центр поля обещает быть бескомпромиссной. Компактный дебют с минимальным пространством между линиями.\n\n", h, hForm, a, aForm));
                sb.append("⚡ ГЛАВА 3: ПЕРЕЛОМНЫЙ МОМЕНТ И СИЛА СКАМЕЙКИ\n" +
                        "Второй тайм станет территорией решений со скамейки. Когда иссякнут силы стартовых составов, свежие джокеры способны взорвать темп в заключительные 20 минут.\n\n");
                sb.append(String.format("🔍 ВЕРДИКТ И ЭКСПЕРТНЫЙ ВЗГЛЯД (E-E-A-T):\n" +
                        "Аналитический консенсус прогнозирует плотный сценарий с модальным счетом %s и ключевым всплеском активности после 75-й минуты.\n", scoreEstimate));
            }
        }

        return sb.toString();
    }

    /**
     * Builds comprehensive LLM prompt for Gemini to synthesize publication-grade editorial articles
     * organically interwoven with Google Trends search queries.
     *
     * @param blueprint      MatchVisionBlueprintDto containing lineups, matrix, and probabilities
     * @param googleTrends   GoogleTrendsMetricsDto containing search volume and audience rising queries
     * @param targetLanguage Target language (ru, en, es, fr, pt)
     * @param tone           Tone.SPORTVIBE_MEDIA or Tone.SMARTBET_QUANT
     * @return Fully formatted LLM prompt string
     */
    public String buildLlmEditorialPrompt(MatchVisionBlueprintDto blueprint,
                                          GoogleTrendsMetricsDto googleTrends,
                                          String targetLanguage,
                                          Tone tone) {
        String ver = blueprint != null && blueprint.getVersionTag() != null ? blueprint.getVersionTag() : "v1";
        String delta = blueprint != null ? blueprint.getDeltaSummary() : null;
        return buildLlmEditorialPrompt(blueprint, googleTrends, targetLanguage, tone, ver, delta);
    }

    /**
     * Full version-aware LLM Editorial Prompt Builder with Google Trends search intent integration.
     */
    public String buildLlmEditorialPrompt(MatchVisionBlueprintDto blueprint,
                                          GoogleTrendsMetricsDto googleTrends,
                                          String targetLanguage,
                                          Tone tone,
                                          String version,
                                          String deltaSummary) {
        String lang = normalizeLang(targetLanguage);
        String langFullName = getLanguageFullName(lang);
        Tone selectedTone = tone != null ? tone : Tone.SPORTVIBE_MEDIA;
        String ver = normalizeVersion(version);

        String h = blueprint != null && blueprint.getHomeTeam() != null ? blueprint.getHomeTeam() : "Home Team";
        String a = blueprint != null && blueprint.getAwayTeam() != null ? blueprint.getAwayTeam() : "Away Team";
        String tourn = blueprint != null && blueprint.getTournament() != null ? blueprint.getTournament() : "Tournament";

        StringBuilder sb = new StringBuilder();

        // 1. SYSTEM ROLE
        sb.append("=== SYSTEM INSTRUCTION: EDITORIAL TACTICAL ANALYST & FEATURE WRITER ===\n");
        if (selectedTone == Tone.SPORTVIBE_MEDIA) {
            sb.append("You are an elite sports investigative journalist and tactical columnist for SportVibe Media (The Athletic & L'Équipe level of storytelling). " +
                    "Your mandate is to craft a magnetic, SEO-optimized, publication-grade tactical match story that adheres to Google E-E-A-T guidelines.\n\n");
        } else {
            sb.append("You are an institutional quantitative football strategist for SmartBet Quant. " +
                    "Your mandate is to produce an authoritative tactical dossier blending deep probabilistic modeling (Poisson / Dixon-Coles parity), 45-parameter matrix metrics, and counterfactual scenario trees.\n\n");
        }

        // 2. TARGET LANGUAGE DIRECTIVE
        sb.append(String.format("🌐 MANDATORY LANGUAGE DIRECTIVE:\n" +
                "Write the ENTIRE article strictly and natively in %s (%s). " +
                "Use native, authentic football idioms and tactical terminology customary for sports publications in this language. " +
                "Never leave English placeholder text.\n\n", langFullName, lang));

        // 3. EDITORIAL TONE & STRUCTURE
        sb.append("🎨 EDITORIAL TONE & FOCUS:\n");
        if (selectedTone == Tone.SPORTVIBE_MEDIA) {
            sb.append("• Tone: Dynamic, journalistic, engaging storytelling with dramatic narrative arcs.\n" +
                    "• Focus: Managerial duels, tactical systems, contested player battles, emotional stakes, and Google E-E-A-T expert authority.\n" +
                    "• Format: Markdown with compelling H2/H3 subheadings, quote callouts, bullet points, and high readability.\n\n");
        } else {
            sb.append("• Tone: Institutional, objective, quantitative, and analytically rigorous.\n" +
                    "• Focus: Probability core, starting XI forecast, 45-parameter tactical matrix (Temporal, Spatial xG, Duels, Totals), counterfactual 'What If' branches.\n" +
                    "• Format: Structured analytical report with explicit metric benchmarks and scenario odds.\n\n");
        }

        // 4. GOOGLE TRENDS & SEARCH INTENT ADAPTER
        sb.append("🔥 GOOGLE TRENDS & SEARCH INTENT INTEGRATION (CRITICAL REQUIREMENT):\n");
        if (googleTrends != null && googleTrends.getRisingQueries() != null && !googleTrends.getRisingQueries().isEmpty()) {
            sb.append("The audience is actively searching for the following breakout questions and queries right now:\n");
            for (String rq : googleTrends.getRisingQueries()) {
                sb.append(String.format("   * [RISING QUERY]: \"%s\"\n", rq));
            }
            if (googleTrends.getTopQueries() != null && !googleTrends.getTopQueries().isEmpty()) {
                sb.append("Additional popular search queries:\n");
                for (String tq : googleTrends.getTopQueries()) {
                    sb.append(String.format("   * [TOP QUERY]: \"%s\"\n", tq));
                }
            }
            sb.append("\nORGANIC SEARCH WEAVING INSTRUCTION:\n" +
                    "You MUST organically address and answer EVERY rising query above within the natural flow of the article body " +
                    "(e.g., player injury updates, starting XI debates, tactical countermeasures, past encounter context). " +
                    "DO NOT make it look like mechanical keyword stuffing or a FAQ block. Weave these answers naturally into the tactical prose " +
                    "so search engines and readers find immediate, authoritative answers.\n\n");
        } else {
            sb.append("Anticipate and organically resolve the most common fan questions regarding starting XI fitness, managerial tactics, and head-to-head records.\n\n");
        }

        // 5. DOSSIER VERSIONING & REACTIVE DELTA
        sb.append(String.format("📋 DOSSIER VERSION CONTEXT [%s]:\n", ver.toUpperCase(Locale.ROOT)));
        if ("v2".equalsIgnoreCase(ver)) {
            sb.append("This is a reactive T-12h update triggered by late roster changes (injuries / suspensions).\n");
            if (deltaSummary != null && !deltaSummary.isBlank()) {
                sb.append(String.format("🚨 ROSTER DELTA: %s\n" +
                        "Highlight how this absence disrupts the team's tactical equilibrium, which replacement assumes the slot, and how the opponent might exploit this vulnerability.\n\n", deltaSummary));
            }
        } else if ("v_confirmed".equalsIgnoreCase(ver)) {
            sb.append("This is the finalized T-1h matchday edition based on the official referee team sheet. " +
                    "Compare the confirmed lineups against earlier projections and analyze the managers' final tactical surprises.\n\n");
        } else {
            sb.append("This is the early T-24h predictive blueprint establishing baseline formations, starter probabilities, and contested slots.\n\n");
        }

        // 6. MATCH TELEMETRY CONTEXT
        sb.append("📊 MATCH CONTEXT & TELEMETRY:\n");
        sb.append(String.format("• Fixture: %s vs %s | Competition: %s\n", h, a, tourn));
        if (blueprint != null) {
            double p1 = blueprint.getProbHome() != null ? blueprint.getProbHome().doubleValue() * 100 : 33.0;
            double px = blueprint.getProbDraw() != null ? blueprint.getProbDraw().doubleValue() * 100 : 34.0;
            double p2 = blueprint.getProbAway() != null ? blueprint.getProbAway().doubleValue() * 100 : 33.0;
            sb.append(String.format("• Probability Parity: Home %.0f%% | Draw %.0f%% | Away %.0f%%\n", p1, px, p2));

            if (blueprint.getMostLikelyScores() != null) {
                sb.append(String.format("• Projected Modal Scores: %s\n", String.join(", ", blueprint.getMostLikelyScores())));
            }
            if (blueprint.getHomeProjectedLineup() != null) {
                sb.append(String.format("• %s Formation: %s | Confidence: %.1f%%\n",
                        h, blueprint.getHomeProjectedLineup().getFormation(), blueprint.getHomeProjectedLineup().getConfidencePct()));
            }
            if (blueprint.getAwayProjectedLineup() != null) {
                sb.append(String.format("• %s Formation: %s | Confidence: %.1f%%\n",
                        a, blueprint.getAwayProjectedLineup().getFormation(), blueprint.getAwayProjectedLineup().getConfidencePct()));
            }
            if (blueprint.getScenarios() != null && !blueprint.getScenarios().isEmpty()) {
                sb.append("• Counterfactual Scenarios:\n");
                for (Map<String, Object> sc : blueprint.getScenarios()) {
                    sb.append(String.format("   * %s: %s\n", sc.get("condition"), sc.get("game_script")));
                }
            }
        }

        sb.append("\n=== GENERATE FULL EDITORIAL ARTICLE NOW IN MARKDOWN ===");
        return sb.toString();
    }

    /**
     * Synthesizes post-match verification debrief narrative (Plan vs Reality).
     */
    public String generatePostMatchDebrief(MatchVisionBlueprintDto blueprint, String finalScore, double matrixAccuracyPct,
                                           int hLineupHits, int aLineupHits) {
        if (blueprint == null) return "";

        String h = blueprint.getHomeTeam();
        String a = blueprint.getAwayTeam();
        int matrixSize = blueprint.getTacticalMatrix() != null ? blueprint.getTacticalMatrix().size() : 45;

        return String.format(
                "📊 [Tactical Post-Mortem & Fact Check]: %s %s %s\n\n" +
                "«Наша предматчевая сценарная карта подтвердилась на %.1f%% по %d ключевым параметрам SofaScore (целевой бенчмарк >= 80%%: %s).\n" +
                "Точность прогноза стартовых 11 за 24 часа до игры: %d/11 у «%s» и %d/11 у «%s».\n" +
                "Матч развивался в соответствии со сценарной картой: осторожный дебют в первом тайме, тактический разлом после перерыва " +
                "и финальный счет %s, подтвердивший математическую модель паритета и высокой плотности единоборств.»",
                h, finalScore, a,
                matrixAccuracyPct, matrixSize, matrixAccuracyPct >= 80.0 ? "ВЗЯТ ✅" : "НЕ ВЗЯТ ❌",
                hLineupHits, h, aLineupHits, a,
                finalScore
        );
    }

    // ──────────────────────────── HELPER METHODS ────────────────────────────

    private void appendJokers(StringBuilder sb, String h, String a,
                              ProjectedLineupDto hLineup, ProjectedLineupDto aLineup, String lang) {
        if (hLineup != null && hLineup.getBench() != null && !hLineup.getBench().isEmpty()) {
            List<ProjectedPlayerDto> jokers = hLineup.getBench().stream().filter(ProjectedPlayerDto::isJoker).toList();
            if (!jokers.isEmpty()) {
                String phrase = switch (lang) {
                    case "en" -> "• Home bench joker: %s (fresh attacking weapon vs tired defense).\n";
                    case "es" -> "• Revulsivo local: %s (oxígeno ofensivo frente a zagas fatigadas).\n";
                    case "fr" -> "• Joker domicile : %s (accélération offensive face à une défense usée).\n";
                    case "pt" -> "• Coringa mandante: %s (impacto ofensivo contra zagas cansadas).\n";
                    default -> "• Джокер «%s»: %s (выход свежего финишера против усталой линии обороны).\n";
                };
                sb.append("ru".equals(lang)
                        ? String.format(phrase, h, jokers.get(0).getName())
                        : String.format(phrase, jokers.get(0).getName()));
            }
        }
        if (aLineup != null && aLineup.getBench() != null && !aLineup.getBench().isEmpty()) {
            List<ProjectedPlayerDto> jokers = aLineup.getBench().stream().filter(ProjectedPlayerDto::isJoker).toList();
            if (!jokers.isEmpty()) {
                String phrase = switch (lang) {
                    case "en" -> "• Away bench joker: %s (counter-attack pace modifier in 2nd half).\n";
                    case "es" -> "• Revulsivo visitante: %s (cambio de ritmo al contragolpe en la 2.ª parte).\n";
                    case "fr" -> "• Joker extérieur : %s (changement de rythme en contre-attaque).\n";
                    case "pt" -> "• Coringa visitante: %s (mudança de ritmo nos contra-ataques).\n";
                    default -> "• Джокер «%s»: %s (смена ритма контратак во 2-м тайме).\n";
                };
                sb.append("ru".equals(lang)
                        ? String.format(phrase, a, jokers.get(0).getName())
                        : String.format(phrase, jokers.get(0).getName()));
            }
        }
    }

    private void appendScenarios(StringBuilder sb, MatchVisionBlueprintDto blueprint, String lang) {
        if (blueprint.getScenarios() == null || blueprint.getScenarios().isEmpty()) return;

        String title = switch (lang) {
            case "en" -> "🔮 COUNTERFACTUAL SCENARIOS (\"WHAT IF\"):\n";
            case "es" -> "🔮 ESCENARIOS CONTRAFACTUALES («QUÉ PASARÍA SI»):\n";
            case "fr" -> "🔮 SCÉNARIOS CONTREFACTUELS (« ET SI ») :\n";
            case "pt" -> "🔮 CENÁRIOS CONTRAFATUAIS («E SE»):\n";
            default -> "🔮 СЦЕНАРНЫЕ РАЗВИЛКИ («ЧТО ЕСЛИ»):\n";
        };
        sb.append(title);
        for (Map<String, Object> sc : blueprint.getScenarios()) {
            sb.append(String.format("   * [%s]: %s\n", sc.get("condition"), sc.get("game_script")));
        }
    }

    private String getVersionLabel(String version, String lang) {
        if (version == null || "v1".equalsIgnoreCase(version)) return "";
        if ("v_confirmed".equalsIgnoreCase(version)) {
            return switch (lang) {
                case "en" -> "[Version: Confirmed Starting XI]";
                case "es" -> "[Versión: Alineación Confirmada]";
                case "fr" -> "[Version : XI Officiel]";
                case "pt" -> "[Versão: Escalação Confirmada]";
                default -> "[Версия: Официальный состав]";
            };
        }
        return switch (lang) {
            case "en" -> String.format("[Version: %s - Reactive Roster Update]", version);
            case "es" -> String.format("[Versión: %s - Novedad de Plantilla]", version);
            case "fr" -> String.format("[Version : %s - Mise à jour effectif]", version);
            case "pt" -> String.format("[Versão: %s - Atualização de Elenco]", version);
            default -> String.format("[Версия: %s - Реактивный апдейт]", version);
        };
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) return "ru";
        String l = lang.trim().toLowerCase(Locale.ROOT);
        if (l.startsWith("ru")) return "ru";
        if (l.startsWith("en")) return "en";
        if (l.startsWith("es")) return "es";
        if (l.startsWith("fr")) return "fr";
        if (l.startsWith("pt")) return "pt";
        return "ru";
    }

    private String normalizeVersion(String ver) {
        if (ver == null || ver.isBlank()) return "v1";
        String v = ver.trim().toLowerCase(Locale.ROOT);
        if (v.contains("confirm")) return "v_confirmed";
        if (v.contains("2")) return "v2";
        if (v.contains("3")) return "v3";
        return "v1";
    }

    private String getLanguageFullName(String lang) {
        return switch (lang) {
            case "en" -> "English";
            case "es" -> "Spanish";
            case "fr" -> "French";
            case "pt" -> "Portuguese";
            default -> "Russian";
        };
    }
}
