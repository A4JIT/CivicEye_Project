package com.civiceye.ai.ai;

import com.civiceye.ai.complaint.Priority;
import org.springframework.stereotype.Service;

@Service
public class PriorityRuleEngine {

    private final SeverityRuleEngine severityRuleEngine;

    public PriorityRuleEngine(SeverityRuleEngine severityRuleEngine) {
        this.severityRuleEngine = severityRuleEngine;
    }

    public static class PriorityRecommendation {
        private final Priority priority;
        private final int severityScore;
        private final String explanation;

        public PriorityRecommendation(Priority priority, int severityScore, String explanation) {
            this.priority = priority;
            this.severityScore = severityScore;
            this.explanation = explanation;
        }

        public Priority getPriority() { return priority; }
        public int getSeverityScore() { return severityScore; }
        public String getExplanation() { return explanation; }
    }

    public PriorityRecommendation evaluatePriority(String title, String description, String location) {
        SeverityRuleEngine.SeverityEvaluation eval = severityRuleEngine.evaluate(title, description, location);
        int score = eval.getScore();

        Priority priority;
        if (eval.isCriticalHazard() || score >= 80) {
            priority = Priority.CRITICAL;
        } else if (score >= 50) {
            priority = Priority.HIGH;
        } else if (score >= 28) {
            priority = Priority.MEDIUM;
        } else {
            priority = Priority.LOW;
        }

        String explanation = String.format("Assigned %s priority (risk score: %d/100). %s",
                priority.name(), score, eval.getExplanation());

        return new PriorityRecommendation(priority, score, explanation);
    }

    public Priority determinePriority(String text) {
        return evaluatePriority(text, "", "").getPriority();
    }

    public Priority determinePriority(String title, String description) {
        return evaluatePriority(title, description, "").getPriority();
    }
}
