package com.civiceye.ai.complaint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckResponse {
    @JsonProperty("isDuplicate")
    private boolean isDuplicate;

    private Double highestScore;
    private DuplicateMatchDto primaryMatch;
    private List<DuplicateMatchDto> matches;
    private String recommendationMessage;

    public DuplicateCheckResponse() {
    }

    public DuplicateCheckResponse(boolean isDuplicate, Double highestScore, DuplicateMatchDto primaryMatch,
                                  List<DuplicateMatchDto> matches, String recommendationMessage) {
        this.isDuplicate = isDuplicate;
        this.highestScore = highestScore;
        this.primaryMatch = primaryMatch;
        this.matches = matches;
        this.recommendationMessage = recommendationMessage;
    }

    @JsonProperty("isDuplicate")
    public boolean isDuplicate() {
        return isDuplicate;
    }

    @JsonProperty("isDuplicate")
    public void setIsDuplicate(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setDuplicate(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public Double getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(Double highestScore) {
        this.highestScore = highestScore;
    }

    @JsonProperty("primaryMatch")
    public DuplicateMatchDto getPrimaryMatch() {
        return primaryMatch;
    }

    public void setPrimaryMatch(DuplicateMatchDto primaryMatch) {
        this.primaryMatch = primaryMatch;
    }

    @JsonProperty("topMatch")
    public DuplicateMatchDto getTopMatch() {
        return primaryMatch;
    }

    public List<DuplicateMatchDto> getMatches() {
        return matches;
    }

    public void setMatches(List<DuplicateMatchDto> matches) {
        this.matches = matches;
    }

    public String getRecommendationMessage() {
        return recommendationMessage;
    }

    public void setRecommendationMessage(String recommendationMessage) {
        this.recommendationMessage = recommendationMessage;
    }

    public static DuplicateCheckResponseBuilder builder() {
        return new DuplicateCheckResponseBuilder();
    }

    public static class DuplicateCheckResponseBuilder {
        private boolean isDuplicate;
        private Double highestScore;
        private DuplicateMatchDto primaryMatch;
        private List<DuplicateMatchDto> matches;
        private String recommendationMessage;

        public DuplicateCheckResponseBuilder isDuplicate(boolean isDuplicate) {
            this.isDuplicate = isDuplicate;
            return this;
        }

        public DuplicateCheckResponseBuilder highestScore(Double highestScore) {
            this.highestScore = highestScore;
            return this;
        }

        public DuplicateCheckResponseBuilder primaryMatch(DuplicateMatchDto primaryMatch) {
            this.primaryMatch = primaryMatch;
            return this;
        }

        public DuplicateCheckResponseBuilder topMatch(DuplicateMatchDto topMatch) {
            this.primaryMatch = topMatch;
            return this;
        }

        public DuplicateCheckResponseBuilder matches(List<DuplicateMatchDto> matches) {
            this.matches = matches;
            return this;
        }

        public DuplicateCheckResponseBuilder recommendationMessage(String recommendationMessage) {
            this.recommendationMessage = recommendationMessage;
            return this;
        }

        public DuplicateCheckResponse build() {
            return new DuplicateCheckResponse(isDuplicate, highestScore, primaryMatch, matches, recommendationMessage);
        }
    }
}
