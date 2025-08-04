package cn.edu.bistu.cs.ir.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Continent测试类
 */
public class ContinentTest {

    @Test
    public void testContinentValues() {
        // 测试大洲枚举值
        assertEquals("亚洲", Continent.ASIA.getChineseName());
        assertEquals("Asia", Continent.ASIA.getEnglishName());
        assertEquals("欧洲", Continent.EUROPE.getChineseName());
        assertEquals("Europe", Continent.EUROPE.getEnglishName());
    }

    @Test
    public void testGetByChineseName() {
        // 测试根据中文名称获取大洲
        assertEquals(Continent.ASIA, Continent.getByChineseName("亚洲"));
        assertEquals(Continent.EUROPE, Continent.getByChineseName("欧洲"));
        assertEquals(Continent.AFRICA, Continent.getByChineseName("非洲"));
        assertEquals(Continent.NORTH_AMERICA, Continent.getByChineseName("北美洲"));
        assertEquals(Continent.SOUTH_AMERICA, Continent.getByChineseName("南美洲"));
        assertEquals(Continent.OCEANIA, Continent.getByChineseName("大洋洲"));
    }

    @Test
    public void testGetByEnglishName() {
        // 测试根据英文名称获取大洲
        assertEquals(Continent.ASIA, Continent.getByEnglishName("Asia"));
        assertEquals(Continent.EUROPE, Continent.getByEnglishName("Europe"));
        assertEquals(Continent.AFRICA, Continent.getByEnglishName("Africa"));
        assertEquals(Continent.NORTH_AMERICA, Continent.getByEnglishName("North America"));
        assertEquals(Continent.SOUTH_AMERICA, Continent.getByEnglishName("South America"));
        assertEquals(Continent.OCEANIA, Continent.getByEnglishName("Oceania"));
    }

    @Test
    public void testGetByChineseNameInvalid() {
        // 测试无效的中文名称
        assertNull(Continent.getByChineseName("无效大洲"));
        assertNull(Continent.getByChineseName(null));
    }

    @Test
    public void testGetByEnglishNameInvalid() {
        // 测试无效的英文名称
        assertNull(Continent.getByEnglishName("Invalid Continent"));
        assertNull(Continent.getByEnglishName(null));
    }

    @Test
    public void testCaseInsensitive() {
        // 测试英文名称大小写不敏感
        assertEquals(Continent.ASIA, Continent.getByEnglishName("asia"));
        assertEquals(Continent.EUROPE, Continent.getByEnglishName("EUROPE"));
        assertEquals(Continent.AFRICA, Continent.getByEnglishName("Africa"));
    }
} 