package cn.edu.bistu.cs.ir.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 年龄处理工具类
 */
public class AgeUtils {
    private static final Logger log = LoggerFactory.getLogger(AgeUtils.class);

    // 匹配年龄的正则表达式，支持多种格式
    private static final Pattern AGE_PATTERN = Pattern.compile("(\\d+)\\s*(?:years?|岁)?\\s*(?:old)?", Pattern.CASE_INSENSITIVE);

    /**
     * 从字符串中解析年龄
     * @param ageStr 年龄字符串
     * @return 解析后的年龄，如果解析失败返回null
     */
    public static Integer parseAge(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) {
            return null;
        }

        try {
            // 尝试直接解析数字
            if (ageStr.trim().matches("\\d+")) {
                return Integer.parseInt(ageStr.trim());
            }

            // 使用正则表达式匹配年龄
            Matcher matcher = AGE_PATTERN.matcher(ageStr);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }

            // log.warn("无法解析年龄字符串: {}", ageStr);
            return null;
        } catch (NumberFormatException e) {
            log.error("年龄解析失败: {}", ageStr, e);
            return null;
        }
    }

    /**
     * 获取年龄的模糊匹配范围
     * @param age 年龄
     * @param fuzzyRange 模糊范围（上下浮动值）
     * @return 年龄范围数组 [最小年龄, 最大年龄]
     */
    public static int[] getFuzzyAgeRange(int age, int fuzzyRange) {
        return new int[]{
            Math.max(0, age - fuzzyRange),
            age + fuzzyRange
        };
    }
}
