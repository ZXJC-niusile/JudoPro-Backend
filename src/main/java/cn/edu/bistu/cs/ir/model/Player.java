package cn.edu.bistu.cs.ir.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面向国际柔道联盟的模型类
 */
@Entity
@Table(name = "player")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    /**
     * 页面的唯一ID
     */
    @Id
    @Column(length = 64)
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private String age;

    /**
     * 照片 URL
     */
    private String image;

    /**
     * 地区
     */
    private String location;

    /**
     * 地区 Icon
     */
    @Column(name = "location_icon")
    private String locationIcon;

    /**
     * 公斤数
     */
    private String kg;

    /**
     * 照片（程序中使用，不存数据库）
     */
    @Transient
    private PhotoEntity photoEntity;

    /**
     * 照片 JSON（存储在数据库中）
     */
    @Column(name = "photos_json", columnDefinition = "TEXT")
    private String photosJson;
}
