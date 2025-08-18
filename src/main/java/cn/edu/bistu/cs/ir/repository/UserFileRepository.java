package cn.edu.bistu.cs.ir.repository;

import cn.edu.bistu.cs.ir.entity.UserFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户文件数据访问接口
 */
@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    
    /**
     * 根据用户ID查询未删除的文件列表（分页）
     */
    Page<UserFile> findByUserIdAndIsDeletedFalseOrderByUploadTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID查询未删除的文件列表
     */
    List<UserFile> findByUserIdAndIsDeletedFalseOrderByUploadTimeDesc(Long userId);
    
    /**
     * 根据用户ID和文件类型查询未删除的文件列表（分页）
     */
    Page<UserFile> findByUserIdAndFileTypeAndIsDeletedFalseOrderByUploadTimeDesc(
            Long userId, String fileType, Pageable pageable);
    
    /**
     * 根据用户ID和文件ID查询未删除的文件
     */
    Optional<UserFile> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);
    
    /**
     * 根据文件URL查询未删除的文件
     */
    Optional<UserFile> findByFileUrlAndIsDeletedFalse(String fileUrl);
    
    /**
     * 根据用户ID和文件URL查询未删除的文件
     */
    Optional<UserFile> findByUserIdAndFileUrlAndIsDeletedFalse(Long userId, String fileUrl);
    
    /**
     * 统计用户未删除的文件数量
     */
    long countByUserIdAndIsDeletedFalse(Long userId);
    
    /**
     * 统计用户指定类型的未删除文件数量
     */
    long countByUserIdAndFileTypeAndIsDeletedFalse(Long userId, String fileType);
    
    /**
     * 计算用户未删除文件的总大小
     */
    @Query("SELECT COALESCE(SUM(uf.fileSize), 0) FROM UserFile uf WHERE uf.userId = :userId AND uf.isDeleted = false")
    long sumFileSizeByUserIdAndIsDeletedFalse(@Param("userId") Long userId);
    
    /**
     * 查询用户最近上传的文件
     */
    List<UserFile> findTop10ByUserIdAndIsDeletedFalseOrderByUploadTimeDesc(Long userId);
    
    /**
     * 查询用户最常下载的文件
     */
    List<UserFile> findTop10ByUserIdAndIsDeletedFalseOrderByDownloadCountDescUploadTimeDesc(Long userId);
    
    /**
     * 根据原始文件名模糊查询用户的文件
     */
    Page<UserFile> findByUserIdAndOriginalFilenameContainingIgnoreCaseAndIsDeletedFalseOrderByUploadTimeDesc(
            Long userId, String filename, Pageable pageable);
    
    /**
     * 软删除文件（更新删除状态和删除时间）
     */
    @Modifying
    @Query("UPDATE UserFile uf SET uf.isDeleted = true, uf.deleteTime = :deleteTime WHERE uf.id = :id AND uf.userId = :userId")
    int softDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId, @Param("deleteTime") LocalDateTime deleteTime);
    
    /**
     * 更新文件下载次数和最后访问时间
     */
    @Modifying
    @Query("UPDATE UserFile uf SET uf.downloadCount = uf.downloadCount + 1, uf.lastAccessTime = :accessTime WHERE uf.id = :id")
    int incrementDownloadCount(@Param("id") Long id, @Param("accessTime") LocalDateTime accessTime);
    
    /**
     * 查询指定时间之前上传的已删除文件（用于物理删除清理）
     */
    List<UserFile> findByIsDeletedTrueAndDeleteTimeBefore(LocalDateTime beforeTime);
    
    /**
     * 查询用户在指定时间范围内上传的文件
     */
    List<UserFile> findByUserIdAndUploadTimeBetweenAndIsDeletedFalseOrderByUploadTimeDesc(
            Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询系统中所有用户的文件统计信息
     */
    @Query("SELECT uf.userId, COUNT(uf), SUM(uf.fileSize), SUM(uf.downloadCount) " +
           "FROM UserFile uf WHERE uf.isDeleted = false GROUP BY uf.userId")
    List<Object[]> getUserFileStatistics();
    
    /**
     * 查询热门文件（按下载次数排序）
     */
    List<UserFile> findTop20ByIsDeletedFalseOrderByDownloadCountDescUploadTimeDesc();
    
    /**
     * 根据文件扩展名查询用户的文件
     */
    Page<UserFile> findByUserIdAndFileExtensionAndIsDeletedFalseOrderByUploadTimeDesc(
            Long userId, String extension, Pageable pageable);
    
    /**
     * 查询用户指定大小范围的文件
     */
    Page<UserFile> findByUserIdAndFileSizeBetweenAndIsDeletedFalseOrderByUploadTimeDesc(
            Long userId, Long minSize, Long maxSize, Pageable pageable);
}