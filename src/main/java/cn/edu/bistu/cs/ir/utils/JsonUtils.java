package cn.edu.bistu.cs.ir.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON工具类，提供单例的ObjectMapper实例
 * 避免重复创建ObjectMapper的性能问题
 * 
 * @author zhaxijiancuo
 */
public class JsonUtils {
    
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    
    /**
     * 单例的ObjectMapper实例
     * ObjectMapper是线程安全的，可以安全地在多线程环境中使用
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * 将对象序列化为JSON字符串
     * 
     * @param obj 要序列化的对象
     * @return JSON字符串，如果序列化失败返回null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象序列化为JSON失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 将JSON字符串反序列化为指定类型的对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象，如果反序列化失败返回null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取ObjectMapper实例
     * 如果需要更复杂的配置，可以直接使用此方法获取实例
     * 
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}