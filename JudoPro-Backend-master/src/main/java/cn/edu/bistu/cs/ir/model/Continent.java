package cn.edu.bistu.cs.ir.model;

/**
 * 大洲枚举类
 */
public enum Continent {
    ASIA("亚洲", "Asia"),
    EUROPE("欧洲", "Europe"),
    AFRICA("非洲", "Africa"),
    NORTH_AMERICA("北美洲", "North America"),
    SOUTH_AMERICA("南美洲", "South America"),
    OCEANIA("大洋洲", "Oceania");

    private final String chineseName;
    private final String englishName;

    Continent(String chineseName, String englishName) {
        this.chineseName = chineseName;
        this.englishName = englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    /**
     * 根据中文名称获取大洲
     * @param chineseName 中文名称
     * @return 大洲，如果不存在则返回null
     */
    public static Continent getByChineseName(String chineseName) {
        if (chineseName == null) {
            return null;
        }

        for (Continent continent : values()) {
            if (continent.chineseName.equals(chineseName)) {
                return continent;
            }
        }

        return null;
    }

    /**
     * 根据英文名称获取大洲
     * @param englishName 英文名称
     * @return 大洲，如果不存在则返回null
     */
    public static Continent getByEnglishName(String englishName) {
        if (englishName == null) {
            return null;
        }

        for (Continent continent : values()) {
            if (continent.englishName.equalsIgnoreCase(englishName)) {
                return continent;
            }
        }

        return null;
    }
} 