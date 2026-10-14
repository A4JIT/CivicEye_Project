package com.civiceye.ai.ai.rules;

import java.util.*;

public class RiskIndicators {

    // Critical Hazard Markers: automatic CRITICAL threshold trigger
    private static final Map<String, Integer> CRITICAL_MARKERS = new LinkedHashMap<>();

    // High Risk Markers: significant hazard / disruption
    private static final Map<String, Integer> HIGH_RISK_MARKERS = new LinkedHashMap<>();

    // Medium Risk Markers: standard civic operational defects
    private static final Map<String, Integer> MEDIUM_RISK_MARKERS = new LinkedHashMap<>();

    // Low Risk Markers: cosmetic or non-hazardous civic issues
    private static final Map<String, Integer> LOW_RISK_MARKERS = new LinkedHashMap<>();

    // Contextual Vulnerability Multipliers (e.g., high pedestrian / vulnerable populations)
    private static final Map<String, Integer> VULNERABILITY_ZONES = new LinkedHashMap<>();

    // Urgency Modifiers
    private static final Map<String, Integer> URGENCY_MODIFIERS = new LinkedHashMap<>();

    static {
        // Critical Markers (weights 40 - 55)
        CRITICAL_MARKERS.put("collapsed bridge", 50);
        CRITICAL_MARKERS.put("bridge collapse", 50);
        CRITICAL_MARKERS.put("live electric wire", 50);
        CRITICAL_MARKERS.put("live wire", 48);
        CRITICAL_MARKERS.put("open manhole", 45);
        CRITICAL_MARKERS.put("manhole cover missing", 45);
        CRITICAL_MARKERS.put("electrocution", 50);
        CRITICAL_MARKERS.put("high voltage", 42);
        CRITICAL_MARKERS.put("pipeline explosion", 50);
        CRITICAL_MARKERS.put("deep sinkhole", 45);
        CRITICAL_MARKERS.put("road cave-in", 45);
        CRITICAL_MARKERS.put("toxic gas", 48);
        CRITICAL_MARKERS.put("gas leak", 48);

        // High Risk Markers (weights 25 - 35)
        HIGH_RISK_MARKERS.put("accident", 35);
        HIGH_RISK_MARKERS.put("two-wheeler accident", 35);
        HIGH_RISK_MARKERS.put("sewage leak", 32);
        HIGH_RISK_MARKERS.put("sewage overflow", 32);
        HIGH_RISK_MARKERS.put("pipe burst", 30);
        HIGH_RISK_MARKERS.put("contaminated water", 32);
        HIGH_RISK_MARKERS.put("severe flooding", 34);
        HIGH_RISK_MARKERS.put("waterlogging", 28);
        HIGH_RISK_MARKERS.put("sparking transformer", 34);
        HIGH_RISK_MARKERS.put("sparking", 28);
        HIGH_RISK_MARKERS.put("short circuit", 30);
        HIGH_RISK_MARKERS.put("dengue outbreak", 32);
        HIGH_RISK_MARKERS.put("dengue", 26);
        HIGH_RISK_MARKERS.put("dog bite", 28);
        HIGH_RISK_MARKERS.put("medical waste", 32);
        HIGH_RISK_MARKERS.put("falling tree", 30);

        // Medium Risk Markers (weights 15 - 22)
        MEDIUM_RISK_MARKERS.put("deep pothole", 22);
        MEDIUM_RISK_MARKERS.put("pothole", 18);
        MEDIUM_RISK_MARKERS.put("garbage piling", 20);
        MEDIUM_RISK_MARKERS.put("overflowing bin", 18);
        MEDIUM_RISK_MARKERS.put("overflowing container", 18);
        MEDIUM_RISK_MARKERS.put("dark street", 18);
        MEDIUM_RISK_MARKERS.put("broken streetlight", 16);
        MEDIUM_RISK_MARKERS.put("streetlight not working", 16);
        MEDIUM_RISK_MARKERS.put("low water pressure", 15);
        MEDIUM_RISK_MARKERS.put("stench", 15);
        MEDIUM_RISK_MARKERS.put("foul odor", 15);
        MEDIUM_RISK_MARKERS.put("traffic jam", 18);

        // Low Risk Markers (weights 5 - 12)
        LOW_RISK_MARKERS.put("minor litter", 8);
        LOW_RISK_MARKERS.put("litter", 8);
        LOW_RISK_MARKERS.put("dim light", 8);
        LOW_RISK_MARKERS.put("faded paint", 6);
        LOW_RISK_MARKERS.put("poster removal", 6);
        LOW_RISK_MARKERS.put("garden waste", 8);
        LOW_RISK_MARKERS.put("dusty road", 8);
        LOW_RISK_MARKERS.put("boundary wall paint", 5);

        // Vulnerability Zone Modifiers (+15 to +25 score if present near critical facilities)
        VULNERABILITY_ZONES.put("hospital", 20);
        VULNERABILITY_ZONES.put("school zone", 20);
        VULNERABILITY_ZONES.put("school", 18);
        VULNERABILITY_ZONES.put("kindergarten", 20);
        VULNERABILITY_ZONES.put("daycare", 18);
        VULNERABILITY_ZONES.put("metro station", 15);
        VULNERABILITY_ZONES.put("bus stop", 15);
        VULNERABILITY_ZONES.put("market square", 12);
        VULNERABILITY_ZONES.put("main road", 12);
        VULNERABILITY_ZONES.put("highway", 15);

        // Urgency Terms (+10 to +20)
        URGENCY_MODIFIERS.put("fatal", 25);
        URGENCY_MODIFIERS.put("deadly", 25);
        URGENCY_MODIFIERS.put("emergency", 20);
        URGENCY_MODIFIERS.put("immediate", 15);
        URGENCY_MODIFIERS.put("urgent", 15);
        URGENCY_MODIFIERS.put("danger", 15);
        URGENCY_MODIFIERS.put("hazardous", 15);
        URGENCY_MODIFIERS.put("severe", 12);
    }

    public static Map<String, Integer> getCriticalMarkers() {
        return Collections.unmodifiableMap(CRITICAL_MARKERS);
    }

    public static Map<String, Integer> getHighRiskMarkers() {
        return Collections.unmodifiableMap(HIGH_RISK_MARKERS);
    }

    public static Map<String, Integer> getMediumRiskMarkers() {
        return Collections.unmodifiableMap(MEDIUM_RISK_MARKERS);
    }

    public static Map<String, Integer> getLowRiskMarkers() {
        return Collections.unmodifiableMap(LOW_RISK_MARKERS);
    }

    public static Map<String, Integer> getVulnerabilityZones() {
        return Collections.unmodifiableMap(VULNERABILITY_ZONES);
    }

    public static Map<String, Integer> getUrgencyModifiers() {
        return Collections.unmodifiableMap(URGENCY_MODIFIERS);
    }
}
