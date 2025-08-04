package cn.edu.bistu.cs.ir.utils;

import java.util.List;

/**
 * 分页响应对象
 * @param <T> 数据类型
 */
public class PageResponse<T> {
    
    /**
     * 当前页数据
     */
    private List<T> data;
    
    /**
     * 分页信息
     */
    private PageInfo pageInfo;
    
    public PageResponse(List<T> data, PageInfo pageInfo) {
        this.data = data;
        this.pageInfo = pageInfo;
    }
    
    public static <T> PageResponse<T> of(List<T> data, int pageNo, int pageSize, long total) {
        PageInfo pageInfo = new PageInfo(pageNo, pageSize, total);
        return new PageResponse<>(data, pageInfo);
    }
    
    public List<T> getData() {
        return data;
    }
    
    public void setData(List<T> data) {
        this.data = data;
    }
    
    public PageInfo getPageInfo() {
        return pageInfo;
    }
    
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
    
    /**
     * 分页信息内部类
     */
    public static class PageInfo {
        /**
         * 当前页码
         */
        private int pageNo;
        
        /**
         * 每页大小
         */
        private int pageSize;
        
        /**
         * 总记录数
         */
        private long total;
        
        /**
         * 总页数
         */
        private int totalPages;
        
        /**
         * 是否有上一页
         */
        private boolean hasPrevious;
        
        /**
         * 是否有下一页
         */
        private boolean hasNext;
        
        public PageInfo(int pageNo, int pageSize, long total) {
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            this.total = total;
            this.totalPages = (int) Math.ceil((double) total / pageSize);
            this.hasPrevious = pageNo > 1;
            this.hasNext = pageNo < totalPages;
        }
        
        // Getters and Setters
        public int getPageNo() {
            return pageNo;
        }
        
        public void setPageNo(int pageNo) {
            this.pageNo = pageNo;
        }
        
        public int getPageSize() {
            return pageSize;
        }
        
        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
        
        public long getTotal() {
            return total;
        }
        
        public void setTotal(long total) {
            this.total = total;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public boolean isHasPrevious() {
            return hasPrevious;
        }
        
        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
        
        public boolean isHasNext() {
            return hasNext;
        }
        
        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }
    }
} 