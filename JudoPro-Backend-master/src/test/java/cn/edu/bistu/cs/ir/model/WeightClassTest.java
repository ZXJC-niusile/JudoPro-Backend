package cn.edu.bistu.cs.ir.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WeightClass测试类
 */
public class WeightClassTest {

    @Test
    public void testGetByWeight() {
        // 测试根据体重获取体重级别
        assertEquals(WeightClass.EXTRA_LIGHTWEIGHT, WeightClass.getByWeight(55));
        assertEquals(WeightClass.HALF_LIGHTWEIGHT, WeightClass.getByWeight(65));
        assertEquals(WeightClass.LIGHTWEIGHT, WeightClass.getByWeight(70));
        assertEquals(WeightClass.HALF_MIDDLEWEIGHT, WeightClass.getByWeight(80));
        assertEquals(WeightClass.MIDDLEWEIGHT, WeightClass.getByWeight(85));
        assertEquals(WeightClass.HALF_HEAVYWEIGHT, WeightClass.getByWeight(95));
        assertEquals(WeightClass.HEAVYWEIGHT, WeightClass.getByWeight(110));
    }

    @Test
    public void testGetByCode() {
        // 测试根据代码获取体重级别
        assertEquals(WeightClass.EXTRA_LIGHTWEIGHT, WeightClass.getByCode("-60"));
        assertEquals(WeightClass.HALF_LIGHTWEIGHT, WeightClass.getByCode("-66"));
        assertEquals(WeightClass.LIGHTWEIGHT, WeightClass.getByCode("-73"));
        assertEquals(WeightClass.HALF_MIDDLEWEIGHT, WeightClass.getByCode("-81"));
        assertEquals(WeightClass.MIDDLEWEIGHT, WeightClass.getByCode("-90"));
        assertEquals(WeightClass.HALF_HEAVYWEIGHT, WeightClass.getByCode("-100"));
        assertEquals(WeightClass.HEAVYWEIGHT, WeightClass.getByCode("+100"));
    }

    @Test
    public void testGetByCodeInvalid() {
        // 测试无效代码
        assertNull(WeightClass.getByCode("invalid"));
        assertNull(WeightClass.getByCode(null));
    }

    @Test
    public void testGetByWeightNull() {
        // 测试null体重
        assertNull(WeightClass.getByWeight(null));
    }

    @Test
    public void testIsInRange() {
        // 测试体重范围检查
        WeightClass lightWeight = WeightClass.LIGHTWEIGHT;
        assertTrue(lightWeight.isInRange(70));
        assertTrue(lightWeight.isInRange(66));
        assertFalse(lightWeight.isInRange(73));
        assertFalse(lightWeight.isInRange(65));
        assertFalse(lightWeight.isInRange(null));
    }

    @Test
    public void testGetCodeAndName() {
        // 测试获取代码和名称
        WeightClass lightWeight = WeightClass.LIGHTWEIGHT;
        assertEquals("-73", lightWeight.getCode());
        assertEquals("轻量级", lightWeight.getName());
    }
} 