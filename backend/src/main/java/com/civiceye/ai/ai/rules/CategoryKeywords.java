package com.civiceye.ai.ai.rules;

import com.civiceye.ai.complaint.ComplaintCategory;

import java.util.*;

public class CategoryKeywords {

    private static final Map<ComplaintCategory, Map<String, Double>> CATEGORY_KEYWORD_DICTIONARY = new EnumMap<>(ComplaintCategory.class);
    private static final Map<String, ComplaintCategory> ALIAS_MAP = new HashMap<>();

    static {
        // Alias mappings for alternate category nomenclature
        ALIAS_MAP.put("ROADS_INFRASTRUCTURE", ComplaintCategory.ROADS_AND_FOOTPATHS);
        ALIAS_MAP.put("SANITATION_WASTE", ComplaintCategory.GARBAGE_AND_CLEANLINESS);
        ALIAS_MAP.put("WATER_SUPPLY", ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE);
        ALIAS_MAP.put("DRAINAGE_FLOODING", ComplaintCategory.DRAINAGE_AND_SEWAGE);
        ALIAS_MAP.put("ELECTRICITY_POWER", ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY);
        ALIAS_MAP.put("PUBLIC_HEALTH_SAFETY", ComplaintCategory.PUBLIC_HEALTH_HAZARDS);

        // 1. Roads and Footpaths / Infrastructure
        Map<String, Double> roads = new LinkedHashMap<>();
        roads.put("collapsed bridge", 5.0);
        roads.put("bridge collapse", 5.0);
        roads.put("road cave-in", 5.0);
        roads.put("sinkhole", 4.5);
        roads.put("deep pothole", 4.5);
        roads.put("pothole", 4.0);
        roads.put("potholes", 4.0);
        roads.put("crater", 3.5);
        roads.put("asphalt", 3.5);
        roads.put("tar road", 3.5);
        roads.put("flyover", 3.5);
        roads.put("bridge", 3.0);
        roads.put("footpath", 3.5);
        roads.put("sidewalk", 3.5);
        roads.put("pavement", 3.0);
        roads.put("speed breaker", 3.0);
        roads.put("divider", 2.5);
        roads.put("cracked road", 3.5);
        roads.put("damaged road", 3.5);
        roads.put("road repair", 3.0);
        roads.put("highway", 2.5);
        roads.put("zebra crossing", 2.5);
        roads.put("road", 1.5);
        CATEGORY_KEYWORD_DICTIONARY.put(ComplaintCategory.ROADS_AND_FOOTPATHS, Collections.unmodifiableMap(roads));

        // 2. Garbage and Cleanliness / Sanitation
        Map<String, Double> garbage = new LinkedHashMap<>();
        garbage.put("garbage piling", 5.0);
        garbage.put("overflowing bin", 4.5);
        garbage.put("overflowing dustbin", 4.5);
        garbage.put("garbage dump", 4.5);
        garbage.put("commercial waste", 4.0);
        garbage.put("solid waste", 4.0);
        garbage.put("dead animal", 4.5);
        garbage.put("carcass", 4.5);
        garbage.put("garbage", 4.0);
        garbage.put("trash", 4.0);
        garbage.put("waste", 3.0);
        garbage.put("rubbish", 3.5);
        garbage.put("litter", 3.0);
        garbage.put("dustbin", 3.0);
        garbage.put("dumping", 3.5);
        garbage.put("rotting", 3.5);
        garbage.put("foul odor", 3.5);
        garbage.put("foul smell", 3.0);
        garbage.put("stench", 3.0);
        garbage.put("debris", 2.5);
        garbage.put("compactor", 3.0);
        garbage.put("cleanliness", 2.5);
        CATEGORY_KEYWORD_DICTIONARY.put(ComplaintCategory.GARBAGE_AND_CLEANLINESS, Collections.unmodifiableMap(garbage));

        // 3. Water Supply and Leakage
        Map<String, Double> water = new LinkedHashMap<>();
        water.put("pipe burst", 5.0);
        water.put("water line cracked", 4.5);
        water.put("drinking water line", 4.5);
        water.put("water pipeline", 4.5);
        water.put("pipeline", 4.0);
        water.put("drinking water", 4.5);
        water.put("water supply", 4.5);
        water.put("water leak", 4.5);
        water.put("water leakage", 4.5);
        water.put("pipe leak", 4.5);
        water.put("contaminated water", 4.5);
        water.put("dirty water", 4.0);
        water.put("muddy water", 4.0);
        water.put("low water pressure", 4.0);
        water.put("water pressure", 3.5);
        water.put("tap water", 4.0);
        water.put("valve leak", 4.0);
        water.put("isolation valve", 3.5);
        water.put("water tanker", 3.5);
        water.put("no water", 3.5);
        water.put("water pipe", 4.0);
        CATEGORY_KEYWORD_DICTIONARY.put(ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE, Collections.unmodifiableMap(water));

        // 4. Drainage and Sewage / Flooding
        Map<String, Double> drainage = new LinkedHashMap<>();
        drainage.put("open manhole", 5.0);
        drainage.put("sewage leak", 5.0);
        drainage.put("sewage overflow", 5.0);
        drainage.put("manhole cover missing", 5.0);
        drainage.put("manhole cover", 4.5);
        drainage.put("manhole", 4.0);
        drainage.put("clogged drain", 4.5);
        drainage.put("blocked drain", 4.5);
        drainage.put("storm drain", 4.0);
        drainage.put("sewer overflow", 4.5);
        drainage.put("sewage", 4.5);
        drainage.put("sewer", 4.0);
        drainage.put("drainage", 4.0);
        drainage.put("gutter", 4.0);
        drainage.put("waterlogging", 4.5);
        drainage.put("water logging", 4.5);
        drainage.put("flooding", 4.0);
        drainage.put("flood", 3.5);
        drainage.put("stagnant water", 3.5);
        drainage.put("drain", 3.0);
        CATEGORY_KEYWORD_DICTIONARY.put(ComplaintCategory.DRAINAGE_AND_SEWAGE, Collections.unmodifiableMap(drainage));

        // 5. Streetlights and Electricity
        Map<String, Double> electricity = new LinkedHashMap<>();
        electricity.put("live electric wire", 5.0);
        electricity.put("live wire", 5.0);
        electricity.put("dangling wire", 4.5);
        electricity.put("dangling power cable", 4.5);
        electricity.put("electric pole", 4.5);
        electricity.put("transformer", 4.5);
        electricity.put("sparking", 4.5);
        electricity.put("short circuit", 4.5);
        electricity.put("electrocution", 5.0);
        electricity.put("high voltage", 4.5);
        electricity.put("electric shock", 4.5);
        electricity.put("streetlight", 4.5);
        electricity.put("street light", 4.5);
        electricity.put("streetlights", 4.5);
        electricity.put("broken lamp", 3.5);
        electricity.put("blackout", 4.0);
        electricity.put("power outage", 4.0);
        electricity.put("power cable", 4.0);
        electricity.put("dark street", 3.5);
        electricity.put("electric wire", 4.0);
        electricity.put("electricity", 3.5);
        CATEGORY_KEYWORD_DICTIONARY.put(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY, Collections.unmodifiableMap(electricity));

        // 6. Public Health Hazards
        Map<String, Double> health = new LinkedHashMap<>();
        health.put("medical waste", 5.0);
        health.put("hospital waste", 5.0);
        health.put("mosquito breeding", 5.0);
        health.put("dengue outbreak", 5.0);
        health.put("dengue", 4.5);
        health.put("malaria", 4.5);
        health.put("dog bite", 4.5);
        health.put("rabies", 4.5);
        health.put("stray dog", 4.0);
        health.put("stray dogs", 4.0);
        health.put("toxic fumes", 4.5);
        health.put("chemical hazard", 4.5);
        health.put("epidemic", 4.5);
        health.put("fumigation", 4.0);
        health.put("food contamination", 4.0);
        health.put("health hazard", 4.0);
        health.put("sanitary hazard", 3.5);
        CATEGORY_KEYWORD_DICTIONARY.put(ComplaintCategory.PUBLIC_HEALTH_HAZARDS, Collections.unmodifiableMap(health));

        // 7. Other / Fallback
        Map<String, Double> other = new LinkedHashMap<>();
        other.put("paint dispute", 4.0);
        other.put("property dispute", 4.0);
        other.put("neighbor", 2.0);
        other.put("private", 2.0);
        CATEGORY_KEYWORD_DICTIONARY.put(ComplaintCategory.OTHER, Collections.unmodifiableMap(other));
    }

    public static Map<String, Double> getKeywordsForCategory(ComplaintCategory category) {
        return CATEGORY_KEYWORD_DICTIONARY.getOrDefault(category, Collections.emptyMap());
    }

    public static Map<ComplaintCategory, Map<String, Double>> getAllKeywords() {
        return Collections.unmodifiableMap(CATEGORY_KEYWORD_DICTIONARY);
    }

    public static ComplaintCategory resolveCategory(String nameOrAlias) {
        if (nameOrAlias == null || nameOrAlias.trim().isEmpty()) {
            return ComplaintCategory.OTHER;
        }
        String normalized = nameOrAlias.trim().toUpperCase();
        try {
            return ComplaintCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return ALIAS_MAP.getOrDefault(normalized, ComplaintCategory.OTHER);
        }
    }
}
