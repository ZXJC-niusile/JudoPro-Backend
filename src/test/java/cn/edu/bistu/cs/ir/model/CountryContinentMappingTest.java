package cn.edu.bistu.cs.ir.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * CountryContinentMapping单元测试
 * 测试others功能
 * 
 * @author zhaxijiancuo
 */
public class CountryContinentMappingTest {

    @Test
    public void testGetCountriesByContinent() {
        // 测试获取亚洲国家列表
        List<String> asiaCountries = CountryContinentMapping.getCountriesByContinent(Continent.ASIA);
        assertNotNull(asiaCountries);
        assertFalse(asiaCountries.isEmpty());
        assertTrue(asiaCountries.contains("China"));
        assertTrue(asiaCountries.contains("Japan"));
        assertTrue(asiaCountries.contains("South Korea"));
    }

    @Test
    public void testGetOthersByContinent() {
        // 测试获取亚洲others列表（初始应该为空）
        List<String> asiaOthers = CountryContinentMapping.getOthersByContinent(Continent.ASIA);
        assertNotNull(asiaOthers);
        assertTrue(asiaOthers.isEmpty());
    }

    @Test
    public void testAddCountryToOthers() {
        // 测试添加国家到others
        String testCountry = "TestCountry";
        Continent testContinent = Continent.ASIA;
        
        // 添加前检查
        assertFalse(CountryContinentMapping.isCountryInOthers(testCountry, testContinent));
        
        // 添加国家到others
        CountryContinentMapping.addCountryToOthers(testCountry, testContinent);
        
        // 添加后检查
        assertTrue(CountryContinentMapping.isCountryInOthers(testCountry, testContinent));
        assertTrue(CountryContinentMapping.isCountryInContinent(testCountry, testContinent));
        
        // 检查others列表
        List<String> others = CountryContinentMapping.getOthersByContinent(testContinent);
        assertTrue(others.contains(testCountry));
    }

    @Test
    public void testAddCountryToOthersDuplicate() {
        // 测试重复添加国家到others
        String testCountry = "DuplicateCountry";
        Continent testContinent = Continent.EUROPE;
        
        // 第一次添加
        CountryContinentMapping.addCountryToOthers(testCountry, testContinent);
        List<String> others1 = CountryContinentMapping.getOthersByContinent(testContinent);
        int size1 = others1.size();
        
        // 第二次添加（应该不会重复）
        CountryContinentMapping.addCountryToOthers(testCountry, testContinent);
        List<String> others2 = CountryContinentMapping.getOthersByContinent(testContinent);
        int size2 = others2.size();
        
        // 大小应该相同
        assertEquals(size1, size2);
    }

    @Test
    public void testAddCountryToOthersMainList() {
        // 测试添加已经在主列表中的国家到others（应该不会添加）
        String mainCountry = "China";
        Continent testContinent = Continent.ASIA;
        
        // 检查国家在主列表中
        assertTrue(CountryContinentMapping.isCountryInMainList(mainCountry, testContinent));
        
        // 尝试添加到others
        CountryContinentMapping.addCountryToOthers(mainCountry, testContinent);
        
        // 检查国家不在others中
        assertFalse(CountryContinentMapping.isCountryInOthers(mainCountry, testContinent));
    }

    @Test
    public void testGetCountriesByContinentWithOthers() {
        // 测试获取包含others的国家列表
        Continent testContinent = Continent.AFRICA;
        
        // 添加一个测试国家到others
        String testCountry = "TestAfricanCountry";
        CountryContinentMapping.addCountryToOthers(testCountry, testContinent);
        
        // 获取包含others的完整列表
        List<String> allCountries = CountryContinentMapping.getCountriesByContinentWithOthers(testContinent);
        
        // 检查包含主列表中的国家
        assertTrue(allCountries.contains("Egypt"));
        assertTrue(allCountries.contains("South Africa"));
        
        // 检查包含others中的国家
        assertTrue(allCountries.contains(testCountry));
    }

    @Test
    public void testGetContinentByCountry() {
        // 测试根据国家获取大洲
        assertEquals(Continent.ASIA, CountryContinentMapping.getContinentByCountry("China"));
        assertEquals(Continent.EUROPE, CountryContinentMapping.getContinentByCountry("Germany"));
        assertEquals(Continent.AFRICA, CountryContinentMapping.getContinentByCountry("Egypt"));
        assertEquals(Continent.NORTH_AMERICA, CountryContinentMapping.getContinentByCountry("United States"));
        assertEquals(Continent.SOUTH_AMERICA, CountryContinentMapping.getContinentByCountry("Brazil"));
        assertEquals(Continent.OCEANIA, CountryContinentMapping.getContinentByCountry("Australia"));
    }

    @Test
    public void testGetContinentByCountryWithOthers() {
        // 测试根据others中的国家获取大洲
        String testCountry = "TestCountryForContinent";
        Continent testContinent = Continent.SOUTH_AMERICA;
        
        // 添加国家到others
        CountryContinentMapping.addCountryToOthers(testCountry, testContinent);
        
        // 检查能正确获取大洲
        assertEquals(testContinent, CountryContinentMapping.getContinentByCountry(testCountry));
    }

    @Test
    public void testIsCountryInContinent() {
        // 测试国家是否属于指定大洲
        assertTrue(CountryContinentMapping.isCountryInContinent("China", Continent.ASIA));
        assertTrue(CountryContinentMapping.isCountryInContinent("Germany", Continent.EUROPE));
        assertFalse(CountryContinentMapping.isCountryInContinent("China", Continent.EUROPE));
        assertFalse(CountryContinentMapping.isCountryInContinent("NonExistentCountry", Continent.ASIA));
    }

    @Test
    public void testIsCountryInContinentWithOthers() {
        // 测试others中的国家是否属于指定大洲
        String testCountry = "TestCountryForContinentCheck";
        Continent testContinent = Continent.NORTH_AMERICA;
        
        // 添加国家到others
        CountryContinentMapping.addCountryToOthers(testCountry, testContinent);
        
        // 检查国家属于该大洲
        assertTrue(CountryContinentMapping.isCountryInContinent(testCountry, testContinent));
        assertFalse(CountryContinentMapping.isCountryInContinent(testCountry, Continent.ASIA));
    }

    @Test
    public void testGetAllContinents() {
        // 测试获取所有大洲
        List<Continent> continents = CountryContinentMapping.getAllContinents();
        assertNotNull(continents);
        assertEquals(6, continents.size()); // 应该有6个大洲
        assertTrue(continents.contains(Continent.ASIA));
        assertTrue(continents.contains(Continent.EUROPE));
        assertTrue(continents.contains(Continent.AFRICA));
        assertTrue(continents.contains(Continent.NORTH_AMERICA));
        assertTrue(continents.contains(Continent.SOUTH_AMERICA));
        assertTrue(continents.contains(Continent.OCEANIA));
    }

    @Test
    public void testNullParameters() {
        // 测试空参数处理
        assertNull(CountryContinentMapping.getContinentByCountry(null));
        assertTrue(CountryContinentMapping.getCountriesByContinent(null).isEmpty());
        assertTrue(CountryContinentMapping.getOthersByContinent(null).isEmpty());
        assertTrue(CountryContinentMapping.getCountriesByContinentWithOthers(null).isEmpty());
        assertFalse(CountryContinentMapping.isCountryInContinent(null, Continent.ASIA));
        assertFalse(CountryContinentMapping.isCountryInContinent("China", null));
        assertFalse(CountryContinentMapping.isCountryInMainList(null, Continent.ASIA));
        assertFalse(CountryContinentMapping.isCountryInOthers(null, Continent.ASIA));
    }
}