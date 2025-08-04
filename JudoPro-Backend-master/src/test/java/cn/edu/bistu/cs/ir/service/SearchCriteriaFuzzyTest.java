package cn.edu.bistu.cs.ir.service;

import cn.edu.bistu.cs.ir.model.AgeGroup;
import cn.edu.bistu.cs.ir.model.Continent;
import cn.edu.bistu.cs.ir.model.WeightClass;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchCriteria模糊匹配功能测试类
 */
public class SearchCriteriaFuzzyTest {

    @Test
    public void testFuzzyKeywordValidation() {
        // 测试有效的模糊关键词
        SearchCriteria criteria = SearchCriteria.builder()
                .fuzzyKeyword("zhang")
                .build();
        
        assertTrue(criteria.hasFuzzyKeyword());
        assertEquals("zhang", criteria.getFuzzyKeyword());
        
        // 测试空模糊关键词
        criteria = SearchCriteria.builder()
                .fuzzyKeyword("")
                .build();
        
        assertFalse(criteria.hasFuzzyKeyword());
        
        // 测试null模糊关键词
        criteria = SearchCriteria.builder()
                .fuzzyKeyword(null)
                .build();
        
        assertFalse(criteria.hasFuzzyKeyword());
    }

    @Test
    public void testSimilarityValidation() {
        // 测试有效的相似度
        SearchCriteria criteria = SearchCriteria.builder()
                .similarity(0.8)
                .build();
        
        assertEquals(0.8, criteria.getSimilarity());
        assertNull(criteria.validateSimilarity());
        
        // 测试边界值
        criteria = SearchCriteria.builder()
                .similarity(0.0)
                .build();
        
        assertNull(criteria.validateSimilarity());
        
        criteria = SearchCriteria.builder()
                .similarity(1.0)
                .build();
        
        assertNull(criteria.validateSimilarity());
        
        // 测试无效相似度
        criteria = SearchCriteria.builder()
                .similarity(-0.1)
                .build();
        
        assertNotNull(criteria.validateSimilarity());
        
        criteria = SearchCriteria.builder()
                .similarity(1.1)
                .build();
        
        assertNotNull(criteria.validateSimilarity());
    }

    @Test
    public void testCombinedFuzzySearch() {
        // 测试模糊关键词和相似度组合
        SearchCriteria criteria = SearchCriteria.builder()
                .fuzzyKeyword("zhang")
                .similarity(0.7)
                .ageGroup(AgeGroup.SENIOR)
                .weightClass(WeightClass.LIGHTWEIGHT)
                .continent(Continent.ASIA)
                .build();
        
        assertTrue(criteria.hasFuzzyKeyword());
        assertEquals(0.7, criteria.getSimilarity());
        assertTrue(criteria.hasAgeGroup());
        assertTrue(criteria.hasWeightClass());
        assertTrue(criteria.hasContinent());
        assertTrue(criteria.hasAnyCriteria());
        assertNull(criteria.validateSimilarity());
    }

    @Test
    public void testFuzzyKeywordWithOtherCriteria() {
        // 测试模糊关键词与其他条件的组合
        SearchCriteria criteria = SearchCriteria.builder()
                .fuzzyKeyword("li")
                .minAge(20)
                .maxAge(30)
                .minWeight(60.0)
                .maxWeight(80.0)
                .country("China")
                .build();
        
        assertTrue(criteria.hasFuzzyKeyword());
        assertTrue(criteria.hasAgeRange());
        assertTrue(criteria.hasWeightRange());
        assertTrue(criteria.hasCountry());
        assertTrue(criteria.hasAnyCriteria());
        
        assertEquals(20, criteria.getMinAge());
        assertEquals(30, criteria.getMaxAge());
        assertEquals(60.0, criteria.getMinWeight());
        assertEquals(80.0, criteria.getMaxWeight());
        assertEquals("China", criteria.getCountry());
    }

    @Test
    public void testBuilderPatternWithFuzzy() {
        // 测试构建器模式与模糊搜索
        SearchCriteria criteria = SearchCriteria.builder()
                .keyword("zhang")
                .fuzzyKeyword("zhang")
                .similarity(0.8)
                .ageGroup(AgeGroup.JUNIOR)
                .ageRange(15, 20)
                .weightClass(WeightClass.MIDDLEWEIGHT)
                .weightRange(70.0, 90.0)
                .continent(Continent.EUROPE)
                .country("Germany")
                .build();
        
        assertTrue(criteria.hasKeyword());
        assertTrue(criteria.hasFuzzyKeyword());
        assertEquals(0.8, criteria.getSimilarity());
        assertTrue(criteria.hasAgeGroup());
        assertTrue(criteria.hasAgeRange());
        assertTrue(criteria.hasWeightClass());
        assertTrue(criteria.hasWeightRange());
        assertTrue(criteria.hasContinent());
        assertTrue(criteria.hasCountry());
        assertTrue(criteria.hasAnyCriteria());
    }

    @Test
    public void testToStringWithFuzzy() {
        // 测试toString方法包含模糊搜索信息
        SearchCriteria criteria = SearchCriteria.builder()
                .fuzzyKeyword("test")
                .similarity(0.9)
                .build();
        
        String result = criteria.toString();
        assertTrue(result.contains("fuzzyKeyword='test'"));
        assertTrue(result.contains("similarity=0.9"));
    }
} 