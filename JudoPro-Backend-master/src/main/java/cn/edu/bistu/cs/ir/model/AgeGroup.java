package cn.edu.bistu.cs.ir.model;

/**
 * 柔道选手年龄组别枚举
 */
public enum AgeGroup {
    CADET("青少年", 15, 17),
    JUNIOR("青年", 18, 20),
    SENIOR("成年", 21, 35),
    VETERAN("资深", 36, 100);

    private final String name;
    private final int minAge;
    private final int maxAge;

    AgeGroup(String name, int minAge, int maxAge) {
        this.name = name;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public String getName() {
        return name;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    /**
     * 根据年龄获取对应的年龄组别
     * @param age 年龄
     * @return 年龄组别，如果年龄小于最小年龄或为null则返回null
     */
    public static AgeGroup getByAge(Integer age) {
        if (age == null || age < CADET.minAge) {
            return null;
        }

        for (AgeGroup group : values()) {
            if (age >= group.minAge && age <= group.maxAge) {
                return group;
            }
        }

        return VETERAN; // 如果超过所有范围，返回资深组
    }

    /**
     * 检查年龄是否在当前组别范围内
     * @param age 年龄
     * @return 是否在范围内
     */
    public boolean isInRange(Integer age) {
        return age != null && age >= minAge && age <= maxAge;
    }
}
