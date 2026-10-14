package com.civiceye.ai.ai;

import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.complaint.Priority;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentRecommendationService Unit Tests")
class DepartmentRecommendationServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private KeywordCategoryClassifier classifier;
    private SeverityRuleEngine severityEngine;
    private PriorityRuleEngine priorityEngine;
    private DepartmentRecommendationService departmentService;

    private Department roadDept;
    private Department wasteDept;
    private Department waterDept;
    private Department elecDept;
    private Department healthDept;

    @BeforeEach
    void setUp() {
        classifier = new KeywordCategoryClassifier();
        severityEngine = new SeverityRuleEngine();
        priorityEngine = new PriorityRuleEngine(severityEngine);
        departmentService = new DepartmentRecommendationService(departmentRepository, classifier, priorityEngine);

        roadDept = Department.builder().id(1L).code("ROAD_WORKS").name("Public Works & Roads").build();
        wasteDept = Department.builder().id(2L).code("SOLID_WASTE").name("Sanitation & Waste Management").build();
        waterDept = Department.builder().id(3L).code("WATER_SUPPLY").name("Water Supply & Sewerage").build();
        elecDept = Department.builder().id(4L).code("ELECTRICITY").name("Electricity & Street Lighting").build();
        healthDept = Department.builder().id(5L).code("HEALTH").name("Public Health & Vector Control").build();
    }

    @Test
    @DisplayName("Resolves department codes accurately for all complaint categories and aliases")
    void testDepartmentCodeResolution() {
        assertEquals("ROAD_WORKS", departmentService.resolveDepartmentCode(ComplaintCategory.ROADS_AND_FOOTPATHS));
        assertEquals("ROAD_WORKS", departmentService.resolveDepartmentCode("ROADS_INFRASTRUCTURE"));

        assertEquals("SOLID_WASTE", departmentService.resolveDepartmentCode(ComplaintCategory.GARBAGE_AND_CLEANLINESS));
        assertEquals("SOLID_WASTE", departmentService.resolveDepartmentCode("SANITATION_WASTE"));

        assertEquals("WATER_SUPPLY", departmentService.resolveDepartmentCode(ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE));
        assertEquals("WATER_SUPPLY", departmentService.resolveDepartmentCode("WATER_SUPPLY"));

        assertEquals("WATER_SUPPLY", departmentService.resolveDepartmentCode(ComplaintCategory.DRAINAGE_AND_SEWAGE));
        assertEquals("WATER_SUPPLY", departmentService.resolveDepartmentCode("DRAINAGE_FLOODING"));

        assertEquals("ELECTRICITY", departmentService.resolveDepartmentCode(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY));
        assertEquals("ELECTRICITY", departmentService.resolveDepartmentCode("ELECTRICITY_POWER"));

        assertEquals("HEALTH", departmentService.resolveDepartmentCode(ComplaintCategory.PUBLIC_HEALTH_HAZARDS));
        assertEquals("HEALTH", departmentService.resolveDepartmentCode("PUBLIC_HEALTH_SAFETY"));

        assertEquals("ROAD_WORKS", departmentService.resolveDepartmentCode((ComplaintCategory) null));
    }

    @Test
    @DisplayName("Recommends Department entity from repository based on category")
    void testRecommendDepartmentEntity() {
        when(departmentRepository.findByCode("ROAD_WORKS")).thenReturn(Optional.of(roadDept));
        when(departmentRepository.findByCode("ELECTRICITY")).thenReturn(Optional.of(elecDept));

        Optional<Department> roadResult = departmentService.recommendDepartment(ComplaintCategory.ROADS_AND_FOOTPATHS);
        assertTrue(roadResult.isPresent());
        assertEquals("ROAD_WORKS", roadResult.get().getCode());

        Optional<Department> elecResult = departmentService.recommendDepartment(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY);
        assertTrue(elecResult.isPresent());
        assertEquals("ELECTRICITY", elecResult.get().getCode());
    }

    @Test
    @DisplayName("Recommends department based on raw title and description")
    void testRecommendDepartmentFromText() {
        when(departmentRepository.findByCode("SOLID_WASTE")).thenReturn(Optional.of(wasteDept));

        Optional<Department> result = departmentService.recommendDepartment(
                "Garbage piling outside community gate",
                "Overflowing trash bin creating foul stench"
        );

        assertTrue(result.isPresent());
        assertEquals("SOLID_WASTE", result.get().getCode());
    }

    @Test
    @DisplayName("Builds comprehensive AiRecommendation payload with category, priority, department and explanation")
    void testBuildFullRecommendation() {
        when(departmentRepository.findByCode("ROAD_WORKS")).thenReturn(Optional.of(roadDept));

        AiRecommendation rec = departmentService.buildFullRecommendation(
                "Collapsed Bridge span on Highway",
                "Major bridge collapse creating extreme risk for vehicles",
                "KM 14 Highway"
        );

        assertNotNull(rec);
        assertEquals(ComplaintCategory.ROADS_AND_FOOTPATHS.name(), rec.getSuggestedCategory());
        assertEquals(Priority.CRITICAL.name(), rec.getSuggestedPriority());
        assertNotNull(rec.getSuggestedDepartment());
        assertEquals("ROAD_WORKS", rec.getSuggestedDepartment().getCode());
        assertTrue(rec.getConfidenceScore() >= 0.85);
        assertFalse(rec.getIsOverridden());
        assertTrue(rec.getExplanation().contains("ROADS_AND_FOOTPATHS"));
        assertTrue(rec.getExplanation().contains("CRITICAL"));
    }
}
