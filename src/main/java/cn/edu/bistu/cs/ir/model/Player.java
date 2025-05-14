package cn.edu.bistu.cs.ir.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面向国际柔道联盟的模型类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    /**
     * 页面的唯一ID
     */
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
    private String locationIcon;

    /**
     * 公斤数
     */
    private String kg;

    /**
     * 照片
     */
    private PhotoEntity photoEntity;
}
