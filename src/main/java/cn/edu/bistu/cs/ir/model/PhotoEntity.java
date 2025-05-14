package cn.edu.bistu.cs.ir.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 照片实体模型类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEntity {
    /**
     * 聚光灯下
     */
    private List<Photo> underTheSpotlights;

    /**
     * 赛事照片
     */
    private List<Photo> photos;
}
