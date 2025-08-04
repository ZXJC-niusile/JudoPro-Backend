package cn.edu.bistu.cs.ir.model;

import java.util.*;

/**
 * 国家与大洲映射关系类
 * 支持为每个大洲添加"others"选项，包含未明确列出的国家
 */
public class CountryContinentMapping {
    
    private static final Map<String, Continent> COUNTRY_TO_CONTINENT = new HashMap<>();
    private static final Map<Continent, Set<String>> CONTINENT_TO_COUNTRIES = new HashMap<>();
    private static final Map<Continent, Set<String>> CONTINENT_TO_OTHERS = new HashMap<>();
    
    // 定义"others"标识符
    public static final String OTHERS_SUFFIX = "_others";
    
    static {
        // 初始化国家与大洲的映射关系
        initMapping();
    }
    
    /**
     * 初始化国家与大洲的映射关系
     */
    private static void initMapping() {
        // 亚洲国家
        addCountries(Continent.ASIA, Arrays.asList(
            "China", "Japan", "South Korea", "North Korea", "Mongolia",
            "Vietnam", "Laos", "Cambodia", "Thailand", "Myanmar",
            "Malaysia", "Singapore", "Indonesia", "Philippines", "Brunei",
            "East Timor", "India", "Pakistan", "Bangladesh", "Sri Lanka",
            "Nepal", "Bhutan", "Maldives", "Afghanistan", "Iran",
            "Iraq", "Kuwait", "Saudi Arabia", "Bahrain", "Qatar",
            "United Arab Emirates", "Oman", "Yemen", "Jordan", "Lebanon",
            "Syria", "Israel", "Palestine", "Cyprus", "Turkey",
            "Georgia", "Armenia", "Azerbaijan", "Kazakhstan", "Uzbekistan",
            "Turkmenistan", "Kyrgyzstan", "Tajikistan", "Russia", "Taiwan",
            "Hong Kong", "Macau"
        ));
        
        // 欧洲国家
        addCountries(Continent.EUROPE, Arrays.asList(
            "Albania", "Andorra", "Austria", "Belarus", "Belgium",
            "Bosnia and Herzegovina", "Bulgaria", "Croatia", "Czech Republic", "Denmark",
            "Estonia", "Finland", "France", "Germany", "Greece",
            "Hungary", "Iceland", "Ireland", "Italy", "Latvia",
            "Liechtenstein", "Lithuania", "Luxembourg", "Malta", "Moldova",
            "Monaco", "Montenegro", "Netherlands", "North Macedonia", "Norway",
            "Poland", "Portugal", "Romania", "San Marino", "Serbia",
            "Slovakia", "Slovenia", "Spain", "Sweden", "Switzerland",
            "Ukraine", "United Kingdom", "Vatican City", "Kosovo"
        ));
        
        // 非洲国家
        addCountries(Continent.AFRICA, Arrays.asList(
            "Algeria", "Angola", "Benin", "Botswana", "Burkina Faso",
            "Burundi", "Cameroon", "Cape Verde", "Central African Republic", "Chad",
            "Comoros", "Congo", "Democratic Republic of the Congo", "Djibouti", "Egypt",
            "Equatorial Guinea", "Eritrea", "Ethiopia", "Gabon", "Gambia",
            "Ghana", "Guinea", "Guinea-Bissau", "Ivory Coast", "Kenya",
            "Lesotho", "Liberia", "Libya", "Madagascar", "Malawi",
            "Mali", "Mauritania", "Mauritius", "Morocco", "Mozambique",
            "Namibia", "Niger", "Nigeria", "Rwanda", "Sao Tome and Principe",
            "Senegal", "Seychelles", "Sierra Leone", "Somalia", "South Africa",
            "South Sudan", "Sudan", "Swaziland", "Tanzania", "Togo",
            "Tunisia", "Uganda", "Zambia", "Zimbabwe"
        ));
        
        // 北美洲国家
        addCountries(Continent.NORTH_AMERICA, Arrays.asList(
            "Antigua and Barbuda", "Bahamas", "Barbados", "Belize", "Canada",
            "Costa Rica", "Cuba", "Dominica", "Dominican Republic", "El Salvador",
            "Grenada", "Guatemala", "Haiti", "Honduras", "Jamaica",
            "Mexico", "Nicaragua", "Panama", "Saint Kitts and Nevis", "Saint Lucia",
            "Saint Vincent and the Grenadines", "Trinidad and Tobago", "United States"
        ));
        
        // 南美洲国家
        addCountries(Continent.SOUTH_AMERICA, Arrays.asList(
            "Argentina", "Bolivia", "Brazil", "Chile", "Colombia",
            "Ecuador", "Guyana", "Paraguay", "Peru", "Suriname",
            "Uruguay", "Venezuela"
        ));
        
        // 大洋洲国家
        addCountries(Continent.OCEANIA, Arrays.asList(
            "Australia", "Fiji", "Kiribati", "Marshall Islands", "Micronesia",
            "Nauru", "New Zealand", "Palau", "Papua New Guinea", "Samoa",
            "Solomon Islands", "Tonga", "Tuvalu", "Vanuatu"
        ));
        
        // 初始化"others"集合
        initOthersSets();
    }
    
    /**
     * 添加国家到大洲的映射
     */
    private static void addCountries(Continent continent, List<String> countries) {
        Set<String> countrySet = CONTINENT_TO_COUNTRIES.computeIfAbsent(continent, k -> new HashSet<>());
        countrySet.addAll(countries);
        
        for (String country : countries) {
            COUNTRY_TO_CONTINENT.put(country.toLowerCase(), continent);
        }
    }
    
    /**
     * 初始化各大洲的"others"集合
     */
    private static void initOthersSets() {
        for (Continent continent : Continent.values()) {
            CONTINENT_TO_OTHERS.put(continent, new HashSet<>());
        }
    }
    
    /**
     * 根据国家名称获取所属大洲
     * @param country 国家名称
     * @return 所属大洲，如果未找到则返回null
     */
    public static Continent getContinentByCountry(String country) {
        if (country == null) {
            return null;
        }
        return COUNTRY_TO_CONTINENT.get(country.toLowerCase());
    }
    
    /**
     * 根据大洲获取该大洲的所有国家（包括明确列出的国家）
     * @param continent 大洲
     * @return 国家列表，如果大洲不存在则返回空列表
     */
    public static List<String> getCountriesByContinent(Continent continent) {
        if (continent == null) {
            return new ArrayList<>();
        }
        Set<String> countries = CONTINENT_TO_COUNTRIES.get(continent);
        return countries != null ? new ArrayList<>(countries) : new ArrayList<>();
    }
    
    /**
     * 根据大洲获取该大洲的所有国家（包括明确列出的国家和others）
     * @param continent 大洲
     * @return 国家列表，如果大洲不存在则返回空列表
     */
    public static List<String> getCountriesByContinentWithOthers(Continent continent) {
        if (continent == null) {
            return new ArrayList<>();
        }
        
        List<String> allCountries = new ArrayList<>();
        
        // 添加明确列出的国家
        Set<String> countries = CONTINENT_TO_COUNTRIES.get(continent);
        if (countries != null) {
            allCountries.addAll(countries);
        }
        
        // 添加others中的国家
        Set<String> others = CONTINENT_TO_OTHERS.get(continent);
        if (others != null) {
            allCountries.addAll(others);
        }
        
        return allCountries;
    }
    
    /**
     * 获取指定大洲的"others"国家列表
     * @param continent 大洲
     * @return others国家列表
     */
    public static List<String> getOthersByContinent(Continent continent) {
        if (continent == null) {
            return new ArrayList<>();
        }
        Set<String> others = CONTINENT_TO_OTHERS.get(continent);
        return others != null ? new ArrayList<>(others) : new ArrayList<>();
    }
    
    /**
     * 动态添加国家到指定大洲的"others"集合
     * @param country 国家名称
     * @param continent 大洲
     */
    public static void addCountryToOthers(String country, Continent continent) {
        if (country == null || continent == null) {
            return;
        }
        
        // 检查国家是否已经在明确列表中
        if (isCountryInMainList(country, continent)) {
            return; // 如果已经在主列表中，不需要添加到others
        }
        
        // 添加到others集合
        Set<String> others = CONTINENT_TO_OTHERS.computeIfAbsent(continent, k -> new HashSet<>());
        others.add(country);
        
        // 更新国家到大洲的映射
        COUNTRY_TO_CONTINENT.put(country.toLowerCase(), continent);
    }
    
    /**
     * 检查国家是否在指定大洲的主列表中
     * @param country 国家名称
     * @param continent 大洲
     * @return 是否在主列表中
     */
    public static boolean isCountryInMainList(String country, Continent continent) {
        if (country == null || continent == null) {
            return false;
        }
        Set<String> countries = CONTINENT_TO_COUNTRIES.get(continent);
        return countries != null && countries.contains(country);
    }
    
    /**
     * 检查国家是否在指定大洲的others列表中
     * @param country 国家名称
     * @param continent 大洲
     * @return 是否在others列表中
     */
    public static boolean isCountryInOthers(String country, Continent continent) {
        if (country == null || continent == null) {
            return false;
        }
        Set<String> others = CONTINENT_TO_OTHERS.get(continent);
        return others != null && others.contains(country);
    }
    
    /**
     * 获取所有大洲
     * @return 大洲列表
     */
    public static List<Continent> getAllContinents() {
        return Arrays.asList(Continent.values());
    }
    
    /**
     * 检查国家是否属于指定大洲（包括主列表和others）
     * @param country 国家名称
     * @param continent 大洲
     * @return 是否属于该大洲
     */
    public static boolean isCountryInContinent(String country, Continent continent) {
        if (country == null || continent == null) {
            return false;
        }
        Continent countryContinent = getContinentByCountry(country);
        return continent.equals(countryContinent);
    }
} 