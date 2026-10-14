package com.civiceye.ai.ai;

import com.civiceye.ai.ai.rules.RiskIndicators;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeverityRuleEngine {

    public static class SeverityEvaluation {
        private final int score;
        private final boolean criticalHazard;
        private final List<String> detectedFactors;
        private final String explanation;

        public SeverityEvaluation(int score, boolean criticalHazard, List<String> detectedFactors, String explanation) {
            this.score = score;
            this.criticalHazard = criticalHazard;
            this.detectedFactors = detectedFactors;
            this.explanation = explanation;
        }

        public int getScore() { return score; }
        public boolean isCriticalHazard() { return criticalHazard; }
        public List<String> getDetectedFactors() { return detectedFactors; }
        public String getExplanation() { return explanation; }
    }

    public SeverityEvaluation evaluate(String text) {
        return evaluate(text, "", "");
    }

    public SeverityEvaluation evaluate(String title, String description, String location) {
        String combined = normalizeText((title != null ? title : "") + " " +
                (description != null ? description : "") + " " +
                (location != null ? location : ""));

        if (combined.trim().isEmpty()) {
            return new SeverityEvaluation(15, false, Collections.emptyList(), "Low priority default score for blank complaint text.");
        }

        boolean criticalFound = false;
        List<String> matchedFactors = new ArrayList<>();

        // 1. Check Critical Markers first
        int criticalScore = 0;
        for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getCriticalMarkers())) {
            if (combined.contains(entry.getKey())) {
                criticalScore = Math.max(criticalScore, 85 + (entry.getValue() / 5));
                criticalFound = true;
                matchedFactors.add("CRITICAL: " + entry.getKey());
            }
        }

        if (criticalFound) {
            for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getVulnerabilityZones())) {
                if (combined.contains(entry.getKey())) {
                    matchedFactors.add("ZONE: " + entry.getKey());
                }
            }
            for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getUrgencyModifiers())) {
                if (combined.contains(entry.getKey())) {
                    matchedFactors.add("URGENT: " + entry.getKey());
                }
            }
            int finalScore = Math.min(100, Math.max(88, criticalScore));
            String explanation = String.format("Critical hazard detected: [%s]. Escalated to top emergency triage tier with score %d/100.",
                    String.join(", ", matchedFactors), finalScore);
            return new SeverityEvaluation(finalScore, true, matchedFactors, explanation);
        }

        // Tiered calculation for non-critical complaints to prevent runaway summation
        int baseScore = 10;

        // 2. High Risk Markers (highest weight + dampened additional)
        int highRiskScore = 0;
        int highRiskMatches = 0;
        for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getHighRiskMarkers())) {
            if (combined.contains(entry.getKey()) && !isAlreadyCovered(entry.getKey(), matchedFactors)) {
                if (highRiskMatches == 0) {
                    highRiskScore += entry.getValue();
                } else {
                    highRiskScore += 6; // dampened additional high risk factor
                }
                highRiskMatches++;
                matchedFactors.add("HIGH_RISK: " + entry.getKey());
            }
        }
        highRiskScore = Math.min(42, highRiskScore);

        // 3. Medium Risk Markers
        int mediumRiskScore = 0;
        int mediumRiskMatches = 0;
        for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getMediumRiskMarkers())) {
            if (combined.contains(entry.getKey()) && !isAlreadyCovered(entry.getKey(), matchedFactors)) {
                if (mediumRiskMatches == 0) {
                    mediumRiskScore += entry.getValue();
                } else {
                    mediumRiskScore += 4; // dampened additional medium factor
                }
                mediumRiskMatches++;
                matchedFactors.add("DEFECT: " + entry.getKey());
            }
        }
        mediumRiskScore = Math.min(26, mediumRiskScore);

        // 4. Low Risk Markers
        int lowRiskScore = 0;
        for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getLowRiskMarkers())) {
            if (combined.contains(entry.getKey()) && !isAlreadyCovered(entry.getKey(), matchedFactors)) {
                lowRiskScore = Math.max(lowRiskScore, entry.getValue());
                matchedFactors.add("MINOR: " + entry.getKey());
            }
        }
        lowRiskScore = Math.min(12, lowRiskScore);

        // 5. Contextual Vulnerability Zones (capped at +16)
        int zoneScore = 0;
        for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getVulnerabilityZones())) {
            if (combined.contains(entry.getKey())) {
                zoneScore = Math.max(zoneScore, entry.getValue());
                matchedFactors.add("ZONE: " + entry.getKey());
            }
        }
        zoneScore = Math.min(16, zoneScore);

        // 6. Urgency Keywords (capped at +12)
        int urgencyScore = 0;
        for (Map.Entry<String, Integer> entry : sortByLengthDesc(RiskIndicators.getUrgencyModifiers())) {
            if (combined.contains(entry.getKey())) {
                urgencyScore = Math.max(urgencyScore, entry.getValue());
                matchedFactors.add("URGENT: " + entry.getKey());
            }
        }
        urgencyScore = Math.min(12, urgencyScore);

        int totalAccumulated;
        if (highRiskScore > 0) {
            totalAccumulated = baseScore + highRiskScore + zoneScore + urgencyScore;
            // High risk complaints should be between 50 and 82
            totalAccumulated = Math.min(82, Math.max(52, totalAccumulated));
        } else if (mediumRiskScore > 0) {
            totalAccumulated = baseScore + mediumRiskScore + zoneScore + urgencyScore;
            // Medium risk complaints should be between 28 and 48
            totalAccumulated = Math.min(48, Math.max(28, totalAccumulated));
        } else if (lowRiskScore > 0) {
            totalAccumulated = baseScore + lowRiskScore;
            totalAccumulated = Math.min(26, Math.max(12, totalAccumulated));
        } else {
            totalAccumulated = baseScore + zoneScore + urgencyScore;
            totalAccumulated = Math.min(26, totalAccumulated);
        }

        String explanation;
        if (!matchedFactors.isEmpty()) {
            explanation = String.format("Severity calculated at %d/100 based on signals: [%s]",
                    totalAccumulated, String.join(", ", matchedFactors.subList(0, Math.min(matchedFactors.size(), 4))));
        } else {
            explanation = "Routine municipal defect with no acute hazard triggers detected.";
        }

        return new SeverityEvaluation(totalAccumulated, false, matchedFactors, explanation);
    }

    public int calculateSeverityScore(String title, String description) {
        return evaluate(title, description, "").getScore();
    }

    private boolean isAlreadyCovered(String key, List<String> factors) {
        for (String factor : factors) {
            if (factor.toLowerCase().contains(key.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private List<Map.Entry<String, Integer>> sortByLengthDesc(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
        return list;
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
