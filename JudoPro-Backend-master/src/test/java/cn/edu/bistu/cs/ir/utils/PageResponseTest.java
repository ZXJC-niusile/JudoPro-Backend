package cn.edu.bistu.cs.ir.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResponse测试类
 */
public class PageResponseTest {

    @Test
    public void testPageResponseCreation() {
        List<String> data = Arrays.asList("item1", "item2", "item3");
        PageResponse<String> response = PageResponse.of(data, 1, 10, 25);
        
        assertNotNull(response);
        assertEquals(data, response.getData());
        assertNotNull(response.getPageInfo());
        assertEquals(1, response.getPageInfo().getPageNo());
        assertEquals(10, response.getPageInfo().getPageSize());
        assertEquals(25, response.getPageInfo().getTotal());
        assertEquals(3, response.getPageInfo().getTotalPages());
    }

    @Test
    public void testPageInfoCalculations() {
        // 测试分页信息计算
        PageResponse<String> response = PageResponse.of(Arrays.asList("item1"), 2, 5, 12);
        PageResponse.PageInfo pageInfo = response.getPageInfo();
        
        assertEquals(2, pageInfo.getPageNo());
        assertEquals(5, pageInfo.getPageSize());
        assertEquals(12, pageInfo.getTotal());
        assertEquals(3, pageInfo.getTotalPages()); // ceil(12/5) = 3
        assertTrue(pageInfo.isHasPrevious()); // 第2页有上一页
        assertTrue(pageInfo.isHasNext()); // 第2页有下一页
    }

    @Test
    public void testFirstPage() {
        PageResponse<String> response = PageResponse.of(Arrays.asList("item1"), 1, 10, 5);
        PageResponse.PageInfo pageInfo = response.getPageInfo();
        
        assertFalse(pageInfo.isHasPrevious()); // 第1页没有上一页
        assertFalse(pageInfo.isHasNext()); // 只有1页，没有下一页
    }

    @Test
    public void testLastPage() {
        PageResponse<String> response = PageResponse.of(Arrays.asList("item1"), 3, 5, 12);
        PageResponse.PageInfo pageInfo = response.getPageInfo();
        
        assertTrue(pageInfo.isHasPrevious()); // 第3页有上一页
        assertFalse(pageInfo.isHasNext()); // 第3页没有下一页
    }

    @Test
    public void testEmptyResult() {
        PageResponse<String> response = PageResponse.of(Arrays.asList(), 1, 10, 0);
        PageResponse.PageInfo pageInfo = response.getPageInfo();
        
        assertEquals(0, pageInfo.getTotal());
        assertEquals(0, pageInfo.getTotalPages());
        assertFalse(pageInfo.isHasPrevious());
        assertFalse(pageInfo.isHasNext());
    }
} 