package com.civiceye.ai.ai;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeywordCategoryClassifier implements CategoryClassifier {
    
    @Override
    public AiRecommendation classify(String text) {
        // Simple mock implementation for MVP
        AiRecommendation rec = new AiRecommendation();
        if (text != null && text.toLowerCase().contains("pothole")) {
            rec.setRecommendedCategory("ROAD_DAMAGE");
            rec.setConfidenceScore(0.85);
            rec.setExplanation("Detected keyword 'pothole'");
        } else {
            rec.setRecommendedCategory("OTHER");
            rec.setConfidenceScore(0.5);
            rec.setExplanation("Default fallback category");
        }
        return rec;
    }
}
