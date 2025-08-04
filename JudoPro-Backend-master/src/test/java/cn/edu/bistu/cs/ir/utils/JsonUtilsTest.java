package cn.edu.bistu.cs.ir.utils;

import cn.edu.bistu.cs.ir.model.Photo;
import cn.edu.bistu.cs.ir.model.PhotoEntity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonUtils工具类的测试类
 */
public class JsonUtilsTest {

    @Test
    public void testToJson() {
        // 测试基本对象序列化
        Photo photo = new Photo("测试照片", "https://example.com/photo.jpg");
        String json = JsonUtils.toJson(photo);
        assertNotNull(json);
        assertTrue(json.contains("测试照片"));
        assertTrue(json.contains("https://example.com/photo.jpg"));
    }

    @Test
    public void testToJsonWithNull() {
        // 测试null对象序列化
        String json = JsonUtils.toJson(null);
        assertNull(json);
    }

    @Test
    public void testFromJson() {
        // 测试JSON反序列化
        String json = "{\"title\":\"测试照片\",\"url\":\"https://example.com/photo.jpg\"}";
        Photo photo = JsonUtils.fromJson(json, Photo.class);
        assertNotNull(photo);
        assertEquals("测试照片", photo.getTitle());
        assertEquals("https://example.com/photo.jpg", photo.getUrl());
    }

    @Test
    public void testFromJsonWithNull() {
        // 测试null JSON反序列化
        Photo photo = JsonUtils.fromJson(null, Photo.class);
        assertNull(photo);
    }

    @Test
    public void testComplexObject() {
        // 测试复杂对象序列化
        List<Photo> photos = Arrays.asList(
            new Photo("照片1", "https://example.com/photo1.jpg"),
            new Photo("照片2", "https://example.com/photo2.jpg")
        );
        List<Photo> spotlights = Arrays.asList(
            new Photo("聚光灯1", "https://example.com/spotlight1.jpg")
        );
        
        PhotoEntity photoEntity = new PhotoEntity(spotlights, photos);
        String json = JsonUtils.toJson(photoEntity);
        assertNotNull(json);
        
        // 反序列化验证
        PhotoEntity deserialized = JsonUtils.fromJson(json, PhotoEntity.class);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.getUnderTheSpotlights().size());
        assertEquals(2, deserialized.getPhotos().size());
    }

    @Test
    public void testGetObjectMapper() {
        // 测试获取ObjectMapper实例
        assertNotNull(JsonUtils.getObjectMapper());
        // 验证是同一个实例
        assertSame(JsonUtils.getObjectMapper(), JsonUtils.getObjectMapper());
    }
} 