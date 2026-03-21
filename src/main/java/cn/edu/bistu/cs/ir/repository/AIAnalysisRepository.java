package cn.edu.bistu.cs.ir.repository;

import cn.edu.bistu.cs.ir.entity.AIAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AI分析结果数据访问接口
 */
@Repository
public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {

    /**
     * 根据用户文件ID查找分析结果
     * @param userFileId 用户文件ID
     * @return 分析结果列表（可能有多次分析）
     */
    List<AIAnalysis> findByUserFileIdOrderByCreatedTimeDesc(Long userFileId);

    /**
     * 根据用户ID查找所有分析结果
     * @param userId 用户ID
     * @return 分析结果列表
     */
    List<AIAnalysis> findByUserIdOrderByCreatedTimeDesc(Long userId);

    /**
     * 根据用户文件ID查找最新的分析结果
     * @param userFileId 用户文件ID
     * @return 最新的分析结果
     */
    Optional<AIAnalysis> findFirstByUserFileIdOrderByCreatedTimeDesc(Long userFileId);

    /**
     * 根据用户ID和分析状态查找分析结果
     * @param userId 用户ID
     * @param status 分析状态
     * @return 分析结果列表
     */
    List<AIAnalysis> findByUserIdAndAnalysisStatusOrderByCreatedTimeDesc(Long userId, String status);

    /**
     * 根据用户文件ID和分析状态查找分析结果
     * @param userFileId 用户文件ID
     * @param status 分析状态
     * @return 分析结果列表
     */
    List<AIAnalysis> findByUserFileIdAndAnalysisStatusOrderByCreatedTimeDesc(Long userFileId, String status);

    /**
     * 根据媒体类型查找分析结果
     * @param mediaType 媒体类型（image/video）
     * @return 分析结果列表
     */
    List<AIAnalysis> findByMediaTypeOrderByCreatedTimeDesc(String mediaType);

    /**
     * 根据分析时间范围查找结果
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分析结果列表
     */
    @Query("SELECT a FROM AIAnalysis a WHERE a.analysisTime BETWEEN :startTime AND :endTime ORDER BY a.analysisTime DESC")
    List<AIAnalysis> findByAnalysisTimeBetween(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户的分析结果数量
     * @param userId 用户ID
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(a) FROM AIAnalysis a WHERE a.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户指定状态的分析结果数量
     * @param userId 用户ID
     * @param analysisStatus 分析状态
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(a) FROM AIAnalysis a WHERE a.userId = :userId AND a.analysisStatus = :analysisStatus")
    long countByUserIdAndAnalysisStatus(@Param("userId") Long userId, @Param("analysisStatus") String analysisStatus);

    /**
     * 统计各状态的分析结果数量
     * @param userId 用户ID
     * @return 按状态分组的统计结果
     */
    @Query("SELECT a.analysisStatus, COUNT(a) FROM AIAnalysis a WHERE a.userId = :userId GROUP BY a.analysisStatus")
    List<Object[]> countByUserIdGroupByStatus(@Param("userId") Long userId);

    /**
     * 查找成功的分析结果
     * @param userId 用户ID
     * @return 成功的分析结果列表
     */
    @Query("SELECT a FROM AIAnalysis a WHERE a.userId = :userId AND a.analysisStatus = 'completed' ORDER BY a.analysisTime DESC")
    List<AIAnalysis> findCompletedByUserId(@Param("userId") Long userId);

    /**
     * 查找失败的分析结果
     * @param userId 用户ID
     * @return 失败的分析结果列表
     */
    @Query("SELECT a FROM AIAnalysis a WHERE a.userId = :userId AND a.analysisStatus = 'failed' ORDER BY a.analysisTime DESC")
    List<AIAnalysis> findFailedByUserId(@Param("userId") Long userId);

    /**
     * 删除指定时间之前的分析结果（用于清理）
     * @param beforeTime 时间界限
     * @return 删除的记录数
     */
    @Query("DELETE FROM AIAnalysis a WHERE a.createdTime < :beforeTime")
    int deleteByCreatedTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);
}