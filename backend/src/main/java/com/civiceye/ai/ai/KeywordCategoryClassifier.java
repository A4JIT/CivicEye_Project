package com.civiceye.ai.ai;

import com.civiceye.ai.ai.rules.CategoryKeywords;
import com.civiceye.ai.complaint.ComplaintCategory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KeywordCategoryClassifier implements CategoryClassifier {

    @Override
    public AiRecommendation classify(String text) {
        return classify(text, "");
    }

    public AiRecommendation classify(String title, String description) {
        String combined = normalizeText((title != null ? title : "") + " " + (description != null ? description : ""));

        if (combined.trim().isEmpty()) {
            AiRecommendation fallback = new AiRecommendation();
            fallback.setSuggestedCategory(ComplaintCategory.OTHER.name());
            fallback.setConfidenceScore(0.50);
            fallback.setExplanation("Empty text provided; default fallback category assigned.");
            return fallback;
        }

        Map<ComplaintCategory, Double> categoryScores = new EnumMap<>(ComplaintCategory.class);
        Map<ComplaintCategory, List<String>> matchedKeywordsMap = new EnumMap<>(ComplaintCategory.class);

        for (Map.Entry<ComplaintCategory, Map<String, Double>> catEntry : CategoryKeywords.getAllKeywords().entrySet()) {
            ComplaintCategory cat = catEntry.getKey();
            Map<String, Double> dict = catEntry.getValue();

            double totalScore = 0.0;
            List<String> matched = new ArrayList<>();

            for (Map.Entry<String, Double> kwEntry : dict.entrySet()) {
                String phrase = kwEntry.getKey().toLowerCase();
                if (combined.contains(phrase)) {
                    totalScore += kwEntry.getValue();
                    matched.add(kwEntry.getKey());
                }
            }

            if (totalScore > 0) {
                categoryScores.put(cat, totalScore);
                matchedKeywordsMap.put(cat, matched);
            }
        }

        if (categoryScores.isEmpty()) {
            AiRecommendation fallback = new AiRecommendation();
            fallback.setSuggestedCategory(ComplaintCategory.OTHER.name());
            fallback.setConfidenceScore(0.50);
            fallback.setExplanation("No municipal keyword markers detected; assigned to general review category.");
            return fallback;
        }

        // Find top category
        List<Map.Entry<ComplaintCategory, Double>> sorted = new ArrayList<>(categoryScores.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        Map.Entry<ComplaintCategory, Double> top = sorted.get(0);
        ComplaintCategory bestCategory = top.getKey();
        double bestScore = top.getValue();
        List<String> bestKeywords = matchedKeywordsMap.getOrDefault(bestCategory, Collections.emptyList());

        // Confidence calculation based on score and relative margin
        double confidence;
        if (sorted.size() > 1) {
            double secondBest = sorted.get(1).getValue();
            double margin = (bestScore - secondBest) / bestScore;
            confidence = Math.min(0.98, Math.max(0.65, 0.75 + (margin * 0.20)));
        } else {
            confidence = Math.min(0.96, Math.max(0.70, 0.70 + (bestScore * 0.04)));
        }

        // Build explanatory text
        String kwString = String.join(", ", bestKeywords.subList(0, Math.min(bestKeywords.size(), 4)));
        String explanation = String.format("Classified as %s based on keywords: [%s] with score %.1f",
                bestCategory.name(), kwString, bestScore);

        AiRecommendation recommendation = new AiRecommendation();
        recommendation.setSuggestedCategory(bestCategory.name());
        recommendation.setConfidenceScore(Math.round(confidence * 100.0) / 100.0);
        recommendation.setExplanation(explanation);
        return recommendation;
    }

    public ComplaintCategory classifyCategory(String text) {
        AiRecommendation rec = classify(text);
        return CategoryKeywords.resolveCategory(rec.getSuggestedCategory());
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
