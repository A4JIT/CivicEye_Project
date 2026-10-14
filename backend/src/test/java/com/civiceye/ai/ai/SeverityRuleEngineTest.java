package com.civiceye.ai.ai;

import com.civiceye.ai.complaint.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Severity & Priority Rule Engine Unit Tests")
class SeverityRuleEngineTest {

    private SeverityRuleEngine severityEngine;
    private PriorityRuleEngine priorityEngine;

    @BeforeEach
    void setUp() {
        severityEngine = new SeverityRuleEngine();
        priorityEngine = new PriorityRuleEngine(severityEngine);
    }

    @Test
    @DisplayName("Identifies collapsed bridge as acute critical hazard with CRITICAL priority")
    void testCollapsedBridgeCritical() {
        SeverityRuleEngine.SeverityEvaluation eval = severityEngine.evaluate(
                "Collapsed Bridge span on Highway",
                "Severe bridge collapse after torrential rain, traffic halted",
                "KM 14 Highway"
        );

        assertTrue(eval.isCriticalHazard());
        assertTrue(eval.getScore() >= 85, "Critical hazard should achieve score >= 85");
        assertTrue(eval.getExplanation().contains("CRITICAL"));

        Priority priority = priorityEngine.determinePriority(
                "Collapsed Bridge span on Highway",
                "Severe bridge collapse after torrential rain, traffic halted"
        );
        assertEquals(Priority.CRITICAL, priority);
    }

    @Test
    @DisplayName("Identifies live electric wire sparking near school zone as CRITICAL")
    void testLiveWireNearSchoolCritical() {
        SeverityRuleEngine.SeverityEvaluation eval = severityEngine.evaluate(
                "Live electric wire sparking actively",
                "Dangling storm-damaged cable next to St. Jude Primary School entrance",
                "School Zone, Sector 4"
        );

        assertTrue(eval.isCriticalHazard());
        assertTrue(eval.getScore() >= 85);
        assertTrue(eval.getDetectedFactors().stream().anyMatch(f -> f.contains("live wire") || f.contains("live electric wire")));
        assertTrue(eval.getDetectedFactors().stream().anyMatch(f -> f.contains("school") || f.contains("ZONE")));

        Priority priority = priorityEngine.determinePriority(
                "Live electric wire sparking actively",
                "Dangling storm-damaged cable next to school"
        );
        assertEquals(Priority.CRITICAL, priority);
    }

    @Test
    @DisplayName("Identifies open manhole on pedestrian path as acute hazard with CRITICAL priority")
    void testOpenManholeCritical() {
        SeverityRuleEngine.SeverityEvaluation eval = severityEngine.evaluate(
                "Open manhole cover missing on footpath",
                "Dangerous deep pit open to pedestrian traffic",
                "Pedestrian Crosswalk"
        );

        assertTrue(eval.isCriticalHazard());
        assertTrue(eval.getScore() >= 85);

        Priority priority = priorityEngine.determinePriority(
                "Open manhole cover missing on footpath",
                "Dangerous deep pit open to pedestrian traffic"
        );
        assertEquals(Priority.CRITICAL, priority);
    }

    @Test
    @DisplayName("Evaluates pipe burst and sewage leak near hospital as HIGH priority")
    void testSewageLeakNearHospitalHigh() {
        SeverityRuleEngine.SeverityEvaluation eval = severityEngine.evaluate(
                "Contaminated sewage leak and pipe burst",
                "Sewer overflowing near General Hospital emergency entrance causing two-wheeler accident risk",
                "City Hospital Lane"
        );

        assertFalse(eval.isCriticalHazard());
        assertTrue(eval.getScore() >= 50 && eval.getScore() < 85);

        PriorityRuleEngine.PriorityRecommendation rec = priorityEngine.evaluatePriority(
                "Contaminated sewage leak and pipe burst",
                "Sewer overflowing near hospital entrance",
                "City Hospital Lane"
        );
        assertEquals(Priority.HIGH, rec.getPriority());
        assertTrue(rec.getExplanation().contains("HIGH"));
    }

    @Test
    @DisplayName("Evaluates standard garbage piling as MEDIUM priority")
    void testGarbagePilingMedium() {
        SeverityRuleEngine.SeverityEvaluation eval = severityEngine.evaluate(
                "Garbage piling outside market",
                "Overflowing waste container with foul odor",
                "Market Lane"
        );

        assertFalse(eval.isCriticalHazard());
        assertTrue(eval.getScore() >= 28 && eval.getScore() < 50);

        Priority priority = priorityEngine.determinePriority(
                "Garbage piling outside market",
                "Overflowing waste container with foul odor"
        );
        assertEquals(Priority.MEDIUM, priority);
    }

    @Test
    @DisplayName("Evaluates cosmetic defects (faded paint, minor litter) as LOW priority")
    void testCosmeticDefectLow() {
        SeverityRuleEngine.SeverityEvaluation eval = severityEngine.evaluate(
                "Faded paint on street divider",
                "Minor litter and garden waste near sidewalk",
                "Residential Lane"
        );

        assertFalse(eval.isCriticalHazard());
        assertTrue(eval.getScore() < 28);

        Priority priority = priorityEngine.determinePriority(
                "Faded paint on street divider",
                "Minor litter and garden waste near sidewalk"
        );
        assertEquals(Priority.LOW, priority);
    }

    @Test
    @DisplayName("Handles empty and null texts cleanly without exceptions")
    void testEmptyAndNullSafety() {
        SeverityRuleEngine.SeverityEvaluation nullEval = severityEngine.evaluate(null, null, null);
        assertNotNull(nullEval);
        assertFalse(nullEval.isCriticalHazard());
        assertTrue(nullEval.getScore() >= 10);

        Priority priority = priorityEngine.determinePriority("", "");
        assertEquals(Priority.LOW, priority);
    }
}
