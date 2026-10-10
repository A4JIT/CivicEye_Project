package com.civiceye.ai.ai;

import lombok.Data;

@Data
public class AiRecommendation {
    private String recommendedCategory;
    private double confidenceScore;
    private String explanation;
}
