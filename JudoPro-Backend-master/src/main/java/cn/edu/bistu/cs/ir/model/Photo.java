package cn.edu.bistu.cs.ir.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 照片模型类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    /**
     * 图片标题
     */
    private String title;

    /**
     * 图片 URL
     */
    private String url;
}
