package cn.edu.bistu.cs.ir.model;

/**
 * 柔道体重级别枚举
 */
public enum WeightClass {
    EXTRA_LIGHTWEIGHT("-60", "超轻量级", 0, 60),
    HALF_LIGHTWEIGHT("-66", "半轻量级", 60, 66),
    LIGHTWEIGHT("-73", "轻量级", 66, 73),
    HALF_MIDDLEWEIGHT("-81", "半中量级", 73, 81),
    MIDDLEWEIGHT("-90", "中量级", 81, 90),
    HALF_HEAVYWEIGHT("-100", "半重量级", 90, 100),
    HEAVYWEIGHT("+100", "重量级", 100, Integer.MAX_VALUE);

    private final String code;
    private final String name;
    private final int minWeight;
    private final int maxWeight;

    WeightClass(String code, String name, int minWeight, int maxWeight) {
        this.code = code;
        this.name = name;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getMinWeight() {
        return minWeight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * 获取显示名称（用于日志和前端显示）
     * @return 体重级别的中文显示名称
     */
    public String getDisplayName() {
        return name;  // 直接返回name字段，它已经包含了中文显示名称
    }

    /**
     * 根据体重获取对应的体重级别
     * @param weight 体重（kg）
     * @return 体重级别，如果体重为null则返回null
     */
    public static WeightClass getByWeight(Integer weight) {
        if (weight == null) {
            return null;
        }

        for (WeightClass weightClass : values()) {
            if (weight >= weightClass.minWeight && weight < weightClass.maxWeight) {
                return weightClass;
            }
        }

        return HEAVYWEIGHT; // 如果超过所有范围，返回重量级
    }

    /**
     * 根据代码获取体重级别
     * @param code 体重级别代码
     * @return 体重级别，如果代码不存在则返回null
     */
    public static WeightClass getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (WeightClass weightClass : values()) {
            if (weightClass.code.equals(code)) {
                return weightClass;
            }
        }

        return null;
    }

    /**
     * 检查体重是否在当前级别范围内
     * @param weight 体重
     * @return 是否在范围内
     */
    public boolean isInRange(Integer weight) {
        return weight != null && weight >= minWeight && weight < maxWeight;
    }
} 