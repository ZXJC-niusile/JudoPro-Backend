package cn.edu.bistu.cs.ir.controller;

import cn.edu.bistu.cs.ir.IrDemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 组合条件检索API集成测试
 * 
 * @author chenruoyu
 */
@SpringBootTest(classes = IrDemoApplication.class)
@ActiveProfiles("test")
public class QueryControllerCombinedTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    public void testCombinedSearchWithKeywordAndAgeGroup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("keyword", "张")
                .param("ageGroup", "SENIOR")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("组合条件检索成功"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.pageNo").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    public void testCombinedSearchWithWeightClassAndContinent() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("weightClass", "-73")
                .param("continent", "ASIA")
                .param("pageNo", "1")
                .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("组合条件检索成功"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.pageNo").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(5));
    }

    @Test
    public void testCombinedSearchWithAllConditions() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("keyword", "李")
                .param("ageGroup", "JUNIOR")
                .param("weightClass", "-66")
                .param("continent", "EUROPE")
                .param("country", "德国")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("组合条件检索成功"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void testCombinedSearchWithNoConditions() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请至少提供一个检索条件"));
    }

    @Test
    public void testCombinedSearchWithInvalidAgeGroup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("ageGroup", "INVALID")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无效的年龄组别，支持：CADET(青少年), JUNIOR(青年), SENIOR(成年), VETERAN(资深)"));
    }

    @Test
    public void testCombinedSearchWithInvalidWeightClass() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("weightClass", "INVALID")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无效的体重级别，支持：-60, -66, -73, -81, -90, -100, +100"));
    }

    @Test
    public void testCombinedSearchWithInvalidContinent() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("continent", "INVALID")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无效的大洲，支持：ASIA(亚洲), EUROPE(欧洲), AFRICA(非洲), NORTH_AMERICA(北美洲), SOUTH_AMERICA(南美洲), OCEANIA(大洋洲)"));
    }

    @Test
    public void testCombinedSearchWithPagination() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/query/combined")
                .param("keyword", "王")
                .param("pageNo", "2")
                .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("组合条件检索成功"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.pageNo").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5));
    }
} 