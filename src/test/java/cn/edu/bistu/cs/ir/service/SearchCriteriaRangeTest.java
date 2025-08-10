package cn.edu.bistu.cs.ir.service;

import cn.edu.bistu.cs.ir.model.AgeGroup;
import cn.edu.bistu.cs.ir.model.Continent;
import cn.edu.bistu.cs.ir.model.WeightClass;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchCriteria年龄范围和体重范围检索测试
 * 
 * @author zhaxijiancuo
 */
public class SearchCriteriaRangeTest {

    @Test
    public void testAgeRangeBuilder() {
        // 测试年龄范围构建器
        SearchCriteria criteria = SearchCriteria.builder()
                .minAge(20)
                .maxAge(30)
                .build();
        
        assertEquals(Integer.valueOf(20), criteria.getMinAge());
        assertEquals(Integer.valueOf(30), criteria.getMaxAge());
        assertTrue(criteria.hasAgeRange());
    }

    @Test
    public void testAgeRangeBuilderMethod() {
        // 测试年龄范围构建器方法
        SearchCriteria criteria = SearchCriteria.builder()
                .ageRange(25, 35)
                .build();
        
        assertEquals(Integer.valueOf(25), criteria.getMinAge());
        assertEquals(Integer.valueOf(35), criteria.getMaxAge());
        assertTrue(criteria.hasAgeRange());
    }

    @Test
    public void testWeightRangeBuilder() {
        // 测试体重范围构建器
        SearchCriteria criteria = SearchCriteria.builder()
                .minWeight(60.0)
                .maxWeight(80.0)
                .build();
        
        assertEquals(Double.valueOf(60.0), criteria.getMinWeight());
        assertEquals(Double.valueOf(80.0), criteria.getMaxWeight());
        assertTrue(criteria.hasWeightRange());
    }

    @Test
    public void testWeightRangeBuilderMethod() {
        // 测试体重范围构建器方法
        SearchCriteria criteria = SearchCriteria.builder()
                .weightRange(70.0, 90.0)
                .build();
        
        assertEquals(Double.valueOf(70.0), criteria.getMinWeight());
        assertEquals(Double.valueOf(90.0), criteria.getMaxWeight());
        assertTrue(criteria.hasWeightRange());
    }

    @Test
    public void testCombinedRangeCriteria() {
        // 测试组合范围检索条件
        SearchCriteria criteria = SearchCriteria.builder()
                .keyword("张")
                .ageRange(20, 30)
                .weightRange(60.0, 80.0)
                .continent(Continent.ASIA)
                .build();
        
        assertTrue(criteria.hasAnyCriteria());
        assertTrue(criteria.hasKeyword());
        assertTrue(criteria.hasAgeRange());
        assertTrue(criteria.hasWeightRange());
        assertTrue(criteria.hasContinent());
        
        assertEquals("张", criteria.getKeyword());
        assertEquals(Integer.valueOf(20), criteria.getMinAge());
        assertEquals(Integer.valueOf(30), criteria.getMaxAge());
        assertEquals(Double.valueOf(60.0), criteria.getMinWeight());
        assertEquals(Double.valueOf(80.0), criteria.getMaxWeight());
        assertEquals(Continent.ASIA, criteria.getContinent());
    }

    @Test
    public void testAgeRangeValidation() {
        // 测试年龄范围验证 - 正常情况
        SearchCriteria criteria = SearchCriteria.builder()
                .ageRange(20, 30)
                .build();
        
        assertNull(criteria.validateAgeRange());
        
        // 测试年龄范围验证 - 最小年龄大于最大年龄
        criteria = SearchCriteria.builder()
                .ageRange(30, 20)
                .build();
        
        assertEquals("最小年龄不能大于最大年龄", criteria.validateAgeRange());
        
        // 测试年龄范围验证 - 最小年龄超出范围
        criteria = SearchCriteria.builder()
                .minAge(-1)
                .build();
        
        assertEquals("最小年龄必须在0-150之间", criteria.validateAgeRange());
        
        // 测试年龄范围验证 - 最大年龄超出范围
        criteria = SearchCriteria.builder()
                .maxAge(151)
                .build();
        
        assertEquals("最大年龄必须在0-150之间", criteria.validateAgeRange());
    }

    @Test
    public void testWeightRangeValidation() {
        // 测试体重范围验证 - 正常情况
        SearchCriteria criteria = SearchCriteria.builder()
                .weightRange(60.0, 80.0)
                .build();
        
        assertNull(criteria.validateWeightRange());
        
        // 测试体重范围验证 - 最小体重大于最大体重
        criteria = SearchCriteria.builder()
                .weightRange(80.0, 60.0)
                .build();
        
        assertEquals("最小体重不能大于最大体重", criteria.validateWeightRange());
        
        // 测试体重范围验证 - 最小体重超出范围
        criteria = SearchCriteria.builder()
                .minWeight(-1.0)
                .build();
        
        assertEquals("最小体重必须在0-500公斤之间", criteria.validateWeightRange());
        
        // 测试体重范围验证 - 最大体重超出范围
        criteria = SearchCriteria.builder()
                .maxWeight(501.0)
                .build();
        
        assertEquals("最大体重必须在0-500公斤之间", criteria.validateWeightRange());
    }

    @Test
    public void testPartialRangeCriteria() {
        // 测试部分范围条件
        SearchCriteria criteria = SearchCriteria.builder()
                .minAge(25)
                .build();
        
        assertTrue(criteria.hasAgeRange());
        assertEquals(Integer.valueOf(25), criteria.getMinAge());
        assertNull(criteria.getMaxAge());
        
        criteria = SearchCriteria.builder()
                .maxAge(35)
                .build();
        
        assertTrue(criteria.hasAgeRange());
        assertNull(criteria.getMinAge());
        assertEquals(Integer.valueOf(35), criteria.getMaxAge());
        
        criteria = SearchCriteria.builder()
                .minWeight(70.0)
                .build();
        
        assertTrue(criteria.hasWeightRange());
        assertEquals(Double.valueOf(70.0), criteria.getMinWeight());
        assertNull(criteria.getMaxWeight());
        
        criteria = SearchCriteria.builder()
                .maxWeight(90.0)
                .build();
        
        assertTrue(criteria.hasWeightRange());
        assertNull(criteria.getMinWeight());
        assertEquals(Double.valueOf(90.0), criteria.getMaxWeight());
    }

    @Test
    public void testRangeWithOtherCriteria() {
        // 测试范围条件与其他条件的组合
        SearchCriteria criteria = SearchCriteria.builder()
                .ageGroup(AgeGroup.SENIOR)
                .ageRange(25, 35)
                .weightClass(WeightClass.LIGHTWEIGHT)
                .weightRange(70.0, 80.0)
                .continent(Continent.EUROPE)
                .country("德国")
                .build();
        
        assertTrue(criteria.hasAnyCriteria());
        assertTrue(criteria.hasAgeGroup());
        assertTrue(criteria.hasAgeRange());
        assertTrue(criteria.hasWeightClass());
        assertTrue(criteria.hasWeightRange());
        assertTrue(criteria.hasContinent());
        assertTrue(criteria.hasCountry());
        
        assertEquals(AgeGroup.SENIOR, criteria.getAgeGroup());
        assertEquals(Integer.valueOf(25), criteria.getMinAge());
        assertEquals(Integer.valueOf(35), criteria.getMaxAge());
        assertEquals(WeightClass.LIGHTWEIGHT, criteria.getWeightClass());
        assertEquals(Double.valueOf(70.0), criteria.getMinWeight());
        assertEquals(Double.valueOf(80.0), criteria.getMaxWeight());
        assertEquals(Continent.EUROPE, criteria.getContinent());
        assertEquals("德国", criteria.getCountry());
    }

    @Test
    public void testToStringWithRanges() {
        // 测试toString方法包含范围信息
        SearchCriteria criteria = SearchCriteria.builder()
                .keyword("张")
                .ageRange(20, 30)
                .weightRange(60.0, 80.0)
                .build();
        
        String result = criteria.toString();
        assertTrue(result.contains("张"));
        assertTrue(result.contains("minAge=20"));
        assertTrue(result.contains("maxAge=30"));
        assertTrue(result.contains("minWeight=60.0"));
        assertTrue(result.contains("maxWeight=80.0"));
    }
}