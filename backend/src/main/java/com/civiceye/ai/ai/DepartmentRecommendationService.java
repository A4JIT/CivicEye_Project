package com.civiceye.ai.ai;

import com.civiceye.ai.ai.rules.CategoryKeywords;
import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DepartmentRecommendationService {

    private final DepartmentRepository departmentRepository;
    private final KeywordCategoryClassifier keywordCategoryClassifier;
    private final PriorityRuleEngine priorityRuleEngine;

    private static final Map<ComplaintCategory, String> CATEGORY_TO_DEPT_CODE_MAP = new EnumMap<>(ComplaintCategory.class);

    static {
        CATEGORY_TO_DEPT_CODE_MAP.put(ComplaintCategory.ROADS_AND_FOOTPATHS, "ROAD_WORKS");
        CATEGORY_TO_DEPT_CODE_MAP.put(ComplaintCategory.GARBAGE_AND_CLEANLINESS, "SOLID_WASTE");
        CATEGORY_TO_DEPT_CODE_MAP.put(ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE, "WATER_SUPPLY");
        CATEGORY_TO_DEPT_CODE_MAP.put(ComplaintCategory.DRAINAGE_AND_SEWAGE, "WATER_SUPPLY");
        CATEGORY_TO_DEPT_CODE_MAP.put(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY, "ELECTRICITY");
        CATEGORY_TO_DEPT_CODE_MAP.put(ComplaintCategory.PUBLIC_HEALTH_HAZARDS, "HEALTH");
        CATEGORY_TO_DEPT_CODE_MAP.put(ComplaintCategory.OTHER, "ROAD_WORKS");
    }

    @Autowired
    public DepartmentRecommendationService(
            DepartmentRepository departmentRepository,
            KeywordCategoryClassifier keywordCategoryClassifier,
            PriorityRuleEngine priorityRuleEngine) {
        this.departmentRepository = departmentRepository;
        this.keywordCategoryClassifier = keywordCategoryClassifier;
        this.priorityRuleEngine = priorityRuleEngine;
    }

    public String resolveDepartmentCode(ComplaintCategory category) {
        if (category == null) return "ROAD_WORKS";
        return CATEGORY_TO_DEPT_CODE_MAP.getOrDefault(category, "ROAD_WORKS");
    }

    public String resolveDepartmentCode(String categoryNameOrAlias) {
        ComplaintCategory category = CategoryKeywords.resolveCategory(categoryNameOrAlias);
        return resolveDepartmentCode(category);
    }

    public Optional<Department> recommendDepartment(ComplaintCategory category) {
        String code = resolveDepartmentCode(category);
        if (departmentRepository != null) {
            return departmentRepository.findByCode(code);
        }
        return Optional.empty();
    }

    public Optional<Department> recommendDepartment(String title, String description) {
        AiRecommendation categoryRec = keywordCategoryClassifier.classify(title, description);
        ComplaintCategory category = CategoryKeywords.resolveCategory(categoryRec.getSuggestedCategory());
        return recommendDepartment(category);
    }

    public AiRecommendation buildFullRecommendation(String title, String description, String location) {
        AiRecommendation categoryRec = keywordCategoryClassifier.classify(title, description);
        ComplaintCategory category = CategoryKeywords.resolveCategory(categoryRec.getSuggestedCategory());

        PriorityRuleEngine.PriorityRecommendation priorityRec = priorityRuleEngine.evaluatePriority(title, description, location);

        Optional<Department> deptOpt = recommendDepartment(category);

        String combinedExplanation = String.format("%s | Priority: %s (%s)",
                categoryRec.getExplanation(),
                priorityRec.getPriority().name(),
                priorityRec.getExplanation());

        AiRecommendation rec = new AiRecommendation();
        rec.setSuggestedCategory(category.name());
        rec.setSuggestedPriority(priorityRec.getPriority().name());
        deptOpt.ifPresent(rec::setSuggestedDepartment);
        rec.setConfidenceScore(categoryRec.getConfidenceScore());
        rec.setExplanation(combinedExplanation);
        rec.setIsOverridden(false);
        return rec;
    }
}
