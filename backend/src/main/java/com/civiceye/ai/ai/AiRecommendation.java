package com.civiceye.ai.ai;

import lombok.Data;

@Data
public class AiRecommendation {
    private String recommendedCategory;
    private double confidenceScore;
    private String explanation;

    public AiRecommendation() {
    }

    public AiRecommendation(String recommendedCategory, double confidenceScore, String explanation) {
        this.recommendedCategory = recommendedCategory;
        this.confidenceScore = confidenceScore;
        this.explanation = explanation;
    }

    public String getRecommendedCategory() {
        return recommendedCategory;
    }

    public void setRecommendedCategory(String recommendedCategory) {
        this.recommendedCategory = recommendedCategory;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
