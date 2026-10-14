package com.civiceye.ai.ai;

import com.civiceye.ai.complaint.ComplaintCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KeywordCategoryClassifier Unit & Benchmark Tests")
class KeywordCategoryClassifierTest {

    private KeywordCategoryClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new KeywordCategoryClassifier();
    }

    @Test
    @DisplayName("Classifies road damage and potholes accurately into ROADS_AND_FOOTPATHS")
    void classifyRoadsAndFootpaths() {
        AiRecommendation rec = classifier.classify(
                "Deep pothole on flyover ramp",
                "Damaged asphalt and crater measuring 3 feet on road causing vehicular congestion"
        );

        assertEquals(ComplaintCategory.ROADS_AND_FOOTPATHS.name(), rec.getSuggestedCategory());
        assertTrue(rec.getConfidenceScore() >= 0.75, "Confidence score should be high for prominent keywords");
        assertTrue(rec.getExplanation().contains("ROADS_AND_FOOTPATHS"));
        assertTrue(rec.getExplanation().toLowerCase().contains("pothole") || rec.getExplanation().toLowerCase().contains("asphalt"));
    }

    @Test
    @DisplayName("Classifies waste and overflowing dumpsters into GARBAGE_AND_CLEANLINESS")
    void classifyGarbageAndCleanliness() {
        AiRecommendation rec = classifier.classify(
                "Garbage piling outside vegetable market",
                "Overflowing dustbins and commercial waste emitting foul odor and stench"
        );

        assertEquals(ComplaintCategory.GARBAGE_AND_CLEANLINESS.name(), rec.getSuggestedCategory());
        assertTrue(rec.getConfidenceScore() >= 0.80);
        assertTrue(rec.getExplanation().contains("GARBAGE_AND_CLEANLINESS"));
    }

    @Test
    @DisplayName("Classifies broken water mains into WATER_SUPPLY_AND_LEAKAGE")
    void classifyWaterSupplyAndLeakage() {
        AiRecommendation rec = classifier.classify(
                "High pressure drinking water pipeline cracked",
                "Clean tap water leaking heavily from main municipal water supply line"
        );

        assertEquals(ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE.name(), rec.getSuggestedCategory());
        assertTrue(rec.getConfidenceScore() >= 0.80);
        assertTrue(rec.getExplanation().contains("WATER_SUPPLY_AND_LEAKAGE"));
    }

    @Test
    @DisplayName("Classifies open manholes and sewage into DRAINAGE_AND_SEWAGE")
    void classifyDrainageAndSewage() {
        AiRecommendation rec = classifier.classify(
                "Open manhole cover missing on footpath",
                "Contaminated sewage leak and blocked drain overflowing on road"
        );

        assertEquals(ComplaintCategory.DRAINAGE_AND_SEWAGE.name(), rec.getSuggestedCategory());
        assertTrue(rec.getConfidenceScore() >= 0.80);
        assertTrue(rec.getExplanation().contains("DRAINAGE_AND_SEWAGE"));
    }

    @Test
    @DisplayName("Classifies dangling wires and blackouts into STREETLIGHTS_AND_ELECTRICITY")
    void classifyStreetlightsAndElectricity() {
        AiRecommendation rec = classifier.classify(
                "Live electric wire sparking on transformer",
                "Dangling power cable and broken streetlight causing total blackout at night"
        );

        assertEquals(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY.name(), rec.getSuggestedCategory());
        assertTrue(rec.getConfidenceScore() >= 0.80);
        assertTrue(rec.getExplanation().contains("STREETLIGHTS_AND_ELECTRICITY"));
    }

    @Test
    @DisplayName("Classifies dengue vector and medical waste into PUBLIC_HEALTH_HAZARDS")
    void classifyPublicHealthHazards() {
        AiRecommendation rec = classifier.classify(
                "Mosquito breeding and dengue outbreak risk",
                "Illegal dumping of hospital medical waste and stagnant water causing severe epidemic hazard"
        );

        assertEquals(ComplaintCategory.PUBLIC_HEALTH_HAZARDS.name(), rec.getSuggestedCategory());
        assertTrue(rec.getConfidenceScore() >= 0.80);
        assertTrue(rec.getExplanation().contains("PUBLIC_HEALTH_HAZARDS"));
    }

    @Test
    @DisplayName("Gracefully handles blank or non-civic input with fallback category")
    void classifyFallbackAndBlank() {
        AiRecommendation emptyRec = classifier.classify("", "");
        assertEquals(ComplaintCategory.OTHER.name(), emptyRec.getSuggestedCategory());
        assertEquals(0.50, emptyRec.getConfidenceScore());

        AiRecommendation nullRec = classifier.classify(null, null);
        assertEquals(ComplaintCategory.OTHER.name(), nullRec.getSuggestedCategory());

        AiRecommendation irrelevant = classifier.classify("Lost pet cat", "My fluffy white cat went missing in the backyard");
        assertEquals(ComplaintCategory.OTHER.name(), irrelevant.getSuggestedCategory());
    }

    @Test
    @DisplayName("Benchmark Precision Test: >95% accuracy against seeded civic benchmark complaint suite")
    void benchmarkAccuracyOver95Percent() {
        record BenchmarkCase(String title, String description, ComplaintCategory expected) {}

        List<BenchmarkCase> benchmarks = List.of(
                // Roads
                new BenchmarkCase("Collapsed Bridge section on Outer Ring Highway", "Catastrophic bridge collapse and road cave-in near toll gate", ComplaintCategory.ROADS_AND_FOOTPATHS),
                new BenchmarkCase("Crater and pothole on flyover", "Asphalt peeling on highway", ComplaintCategory.ROADS_AND_FOOTPATHS),
                new BenchmarkCase("Broken sidewalk paving", "Damaged footpath pavers", ComplaintCategory.ROADS_AND_FOOTPATHS),
                // Sanitation
                new BenchmarkCase("Garbage piling and stench near park", "Overflowing bin and municipal trash accumulating", ComplaintCategory.GARBAGE_AND_CLEANLINESS),
                new BenchmarkCase("Rotting animal carcass on road", "Dead animal on curb creating odor", ComplaintCategory.GARBAGE_AND_CLEANLINESS),
                new BenchmarkCase("Commercial rubbish dumped at corner", "Litter and solid waste uncollected", ComplaintCategory.GARBAGE_AND_CLEANLINESS),
                // Water
                new BenchmarkCase("Water pipe burst flooding street", "Drinking water pipeline leak under pavement", ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE),
                new BenchmarkCase("Contaminated muddy tap water", "Dirty water supply reaching households", ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE),
                new BenchmarkCase("Low water pressure in municipal line", "Water tanker needed as pipeline valve broken", ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE),
                // Drainage
                new BenchmarkCase("Open manhole on pedestrian path", "Missing manhole cover dangerous for walkers", ComplaintCategory.DRAINAGE_AND_SEWAGE),
                new BenchmarkCase("Sewage leak flooding alleyway", "Blocked drain and sewer overflow with black water", ComplaintCategory.DRAINAGE_AND_SEWAGE),
                new BenchmarkCase("Severe monsoon waterlogging", "Clogged storm drain backing up into street", ComplaintCategory.DRAINAGE_AND_SEWAGE),
                // Electricity
                new BenchmarkCase("Live electric wire sparking near school", "High voltage cable dangling from electric pole", ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY),
                new BenchmarkCase("Streetlight not working on main avenue", "Complete night blackout due to broken street lamp", ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY),
                new BenchmarkCase("Transformer short circuit explosion", "Power outage after sparking transformer", ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY),
                // Public Health
                new BenchmarkCase("Mosquito breeding dengue hotspot", "Stagnant puddle leading to malaria outbreak and fumigation needed", ComplaintCategory.PUBLIC_HEALTH_HAZARDS),
                new BenchmarkCase("Hospital medical waste in ditch", "Contaminated syringes and chemical health hazard", ComplaintCategory.PUBLIC_HEALTH_HAZARDS),
                new BenchmarkCase("Stray dog menace with rabies bite", "Pack of aggressive stray dogs attacking pedestrians", ComplaintCategory.PUBLIC_HEALTH_HAZARDS),
                // Other
                new BenchmarkCase("Boundary wall paint dispute", "Neighbor residential fence color disagreement", ComplaintCategory.OTHER),
                new BenchmarkCase("Private property tenant conflict", "Dispute over parking space allocation", ComplaintCategory.OTHER)
        );

        int correctCount = 0;
        for (BenchmarkCase testCase : benchmarks) {
            ComplaintCategory predicted = classifier.classifyCategory(testCase.title() + " " + testCase.description());
            if (predicted == testCase.expected()) {
                correctCount++;
            } else {
                System.err.printf("MISMATCH: '%s' predicted %s but expected %s%n",
                        testCase.title(), predicted, testCase.expected());
            }
        }

        double accuracy = ((double) correctCount) / benchmarks.size();
        assertTrue(accuracy >= 0.95, String.format("Expected accuracy >= 95%%, but was %.2f%% (%d/%d)",
                accuracy * 100.0, correctCount, benchmarks.size()));
    }
}
