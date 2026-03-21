package cn.edu.bistu.cs.ir.controller;

import cn.edu.bistu.cs.ir.index.IdxService;
import cn.edu.bistu.cs.ir.model.AgeGroup;
import cn.edu.bistu.cs.ir.model.Continent;
import cn.edu.bistu.cs.ir.model.CountryContinentMapping;
import cn.edu.bistu.cs.ir.model.Photo;
import cn.edu.bistu.cs.ir.model.PhotoEntity;
import cn.edu.bistu.cs.ir.model.Player;
import cn.edu.bistu.cs.ir.model.WeightClass;
import cn.edu.bistu.cs.ir.service.SearchCriteria;
import cn.edu.bistu.cs.ir.utils.PageResponse;
import cn.edu.bistu.cs.ir.utils.QueryResponse;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 面向检索服务接口的控制器类
 * Restful Web Services/Rest风格的Web服务
 *
 * @author zhaxijiancuo
 */
@RestController
@RequestMapping("/query")
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    private final IdxService idxService;

    /**
     * 将PhotoEntity转换为JSON字符串
     */
    private String convertPhotoEntityToJson(PhotoEntity photoEntity) {
        if (photoEntity == null) return "[]";

        try {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"underTheSpotlights\":[");

            if (photoEntity.getUnderTheSpotlights() != null) {
                for (int i = 0; i < photoEntity.getUnderTheSpotlights().size(); i++) {
                    Photo photo = photoEntity.getUnderTheSpotlights().get(i);
                    jsonBuilder.append("{\"title\":\"").append(photo.getTitle() != null ? photo.getTitle() : "")
                              .append("\",\"url\":\"").append(photo.getUrl() != null ? photo.getUrl() : "")
                              .append("\"}");
                    if (i < photoEntity.getUnderTheSpotlights().size() - 1) {
                        jsonBuilder.append(",");
                    }
                }
            }

            jsonBuilder.append("],\"photos\":[");

            if (photoEntity.getPhotos() != null) {
                for (int i = 0; i < photoEntity.getPhotos().size(); i++) {
                    Photo photo = photoEntity.getPhotos().get(i);
                    jsonBuilder.append("{\"title\":\"").append(photo.getTitle() != null ? photo.getTitle() : "")
                              .append("\",\"url\":\"").append(photo.getUrl() != null ? photo.getUrl() : "")
                              .append("\"}");
                    if (i < photoEntity.getPhotos().size() - 1) {
                        jsonBuilder.append(",");
                    }
                }
            }

            jsonBuilder.append("]}");
            return jsonBuilder.toString();
        } catch (Exception e) {
            log.error("转换PhotoEntity为JSON时出错", e);
            return "[]";
        }
    }

    public QueryController(@Autowired IdxService idxService) {
        this.idxService = idxService;
    }

    /**
     * 根据关键词对索引进行分页检索，
     * 根据页号和页面大小，
     * 返回指定页的数据记录
     *
     * @param kw       待检索的关键词
     * @param pageNo   页号，默认为1
     * @param pageSize 页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/kw", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByKw(@RequestParam(name = "kw") String kw,
                                                                      @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                                      @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10; // 限制最大页大小
            
            // 使用Lucene层面分页查询
            IdxService.PageResult pageResult = idxService.queryByKwWithPaging(kw, pageNo, pageSize);
            
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", doc.get("NAME"));
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", doc.get("KG"));
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());
            
            return QueryResponse.genSucc("检索成功", pageResponse);
        } catch (Exception e) {
            log.error("检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("检索过程中发生异常");
        }
    }

    /**
     * 根据年龄组别进行分页检索
     *
     * @param ageGroup 年龄组别（CADET-青少年, JUNIOR-青年, SENIOR-成年, VETERAN-资深）
     * @param pageNo   页号，默认为1
     * @param pageSize 页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/ageGroup", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByAgeGroup(@RequestParam(name = "ageGroup") String ageGroup,
                                                                           @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                                           @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        log.info("🎯 年龄组查询API开始 - ageGroup: {}, pageNo: {}, pageSize: {}", ageGroup, pageNo, pageSize);

        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            // 解析年龄组别
            AgeGroup group;
            try {
                group = AgeGroup.valueOf(ageGroup.toUpperCase());
            } catch (IllegalArgumentException e) {
                return QueryResponse.genErr("无效的年龄组别，支持：CADET(青少年), JUNIOR(青年), SENIOR(成年), VETERAN(资深)");
            }
            
            // 使用Lucene层面分页查询
            IdxService.PageResult pageResult = idxService.queryByAgeGroup(group, pageNo, pageSize);
            
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", doc.get("NAME"));
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", doc.get("KG"));
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());

            // 添加调试日志
            log.info("✅ 年龄组查询完成 - 组别: {}, 查询到结果数: {}, 总记录数: {}",
                ageGroup, results.size(), pageResult.getTotal());

            // 如果是CADET且无结果，特别提醒
            if ("CADET".equalsIgnoreCase(ageGroup) && results.isEmpty()) {
                log.warn("🚨 CADET年龄组查询返回空结果！建议检查数据源中是否有15-17岁的运动员");
            }

            return QueryResponse.genSucc("年龄组别检索成功", pageResponse);
        } catch (Exception e) {
            log.error("年龄组别检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("年龄组别检索过程中发生异常");
        }
    }

    /**
     * 根据体重级别进行分页检索
     *
     * @param weightClass 体重级别代码（-60, -66, -73, -81, -90, -100, +100）
     * @param pageNo      页号，默认为1
     * @param pageSize    页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/weightClass", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByWeightClass(@RequestParam(name = "weightClass") String weightClass,
                                                                              @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                                              @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        log.info("体重级别查询API - weightClass: {}, pageNo: {}, pageSize: {}", weightClass, pageNo, pageSize);

        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;

            // 解析体重级别
            WeightClass wc = WeightClass.getByCode(weightClass);
            log.info("解析体重级别: {} -> {}", weightClass, wc != null ? wc.getDisplayName() : "null");

            if (wc == null) {
                log.warn("体重级别参数无效: {}", weightClass);
                return QueryResponse.genErr("无效的体重级别，支持：-60, -66, -73, -81, -90, -100, +100");
            }

            log.info("开始Lucene体重级别查询 - 体重级别: {}", wc.getDisplayName());
            // 使用Lucene层面分页查询
            IdxService.PageResult pageResult = idxService.queryByWeightClass(wc, pageNo, pageSize);
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                String kg = doc.get("KG");
                String name = doc.get("NAME");

                log.info("找到运动员 - 姓名: {}, 体重级别: {}", name, kg);

                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", name);
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", kg);
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }

            log.info("Document转换完成 - 转换记录数: {}", results.size());
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());
            
            return QueryResponse.genSucc("体重级别检索成功", pageResponse);
        } catch (Exception e) {
            log.error("体重级别检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("体重级别检索过程中发生异常");
        }
    }

    /**
     * 获取所有年龄组别列表
     *
     * @return 年龄组别列表
     */
    @GetMapping(value = "/ageGroups", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<Map<String, Object>>> getAgeGroups() {
        try {
            List<Map<String, Object>> ageGroups = new ArrayList<>();
            for (AgeGroup group : AgeGroup.values()) {
                Map<String, Object> ageGroupMap = new HashMap<>();
                ageGroupMap.put("name", group.name());
                ageGroupMap.put("chineseName", group.getName());
                ageGroupMap.put("minAge", group.getMinAge());
                ageGroupMap.put("maxAge", group.getMaxAge());
                ageGroups.add(ageGroupMap);
            }
            return QueryResponse.genSucc("获取年龄组别列表成功", ageGroups);
        } catch (Exception e) {
            log.error("获取年龄组别列表失败:[{}]", e.getMessage());
            return QueryResponse.genErr("获取年龄组别列表失败");
        }
    }

    /**
     * 获取所有体重级别列表
     *
     * @return 体重级别列表
     */
    @GetMapping(value = "/weightClasses", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<Map<String, Object>>> getWeightClasses() {
        try {
            List<Map<String, Object>> weightClasses = new ArrayList<>();
            for (WeightClass wc : WeightClass.values()) {
                Map<String, Object> weightClassMap = new HashMap<>();
                weightClassMap.put("name", wc.name());
                weightClassMap.put("chineseName", wc.getName());
                weightClassMap.put("code", wc.getCode());
                weightClassMap.put("minWeight", wc.getMinWeight());
                weightClassMap.put("maxWeight", wc.getMaxWeight());
                weightClasses.add(weightClassMap);
            }
            return QueryResponse.genSucc("获取体重级别列表成功", weightClasses);
        } catch (Exception e) {
            log.error("获取体重级别列表失败:[{}]", e.getMessage());
            return QueryResponse.genErr("获取体重级别列表失败");
        }
    }

    /**
     * 获取所有大洲列表（包含中文名称）
     *
     * @return 大洲列表
     */
    @GetMapping(value = "/continents", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<Map<String, String>>> getContinents() {
        try {
            List<Map<String, String>> continents = new ArrayList<>();
            for (Continent continent : Continent.values()) {
                Map<String, String> continentMap = new HashMap<>();
                continentMap.put("name", continent.name());
                continentMap.put("chineseName", continent.getChineseName());
                continentMap.put("englishName", continent.getEnglishName());
                continents.add(continentMap);
            }
            return QueryResponse.genSucc("获取大洲列表成功", continents);
        } catch (Exception e) {
            log.error("获取大洲列表失败:[{}]", e.getMessage());
            return QueryResponse.genErr("获取大洲列表失败");
        }
    }

    /**
     * 根据大洲获取国家列表
     *
     * @param continent 大洲代码（ASIA, EUROPE, AFRICA, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA）
     * @return 国家列表
     */
    @GetMapping(value = "/countries", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<String>> getCountriesByContinent(@RequestParam(name = "continent") String continent) {
        try {
            // 解析大洲
            Continent cont;
            try {
                cont = Continent.valueOf(continent.toUpperCase());
            } catch (IllegalArgumentException e) {
                return QueryResponse.genErr("无效的大洲代码，支持：ASIA, EUROPE, AFRICA, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA");
            }
            
            List<String> countries = CountryContinentMapping.getCountriesByContinent(cont);
            return QueryResponse.genSucc("获取国家列表成功", countries);
        } catch (Exception e) {
            log.error("获取国家列表过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("获取国家列表过程中发生异常");
        }
    }

    /**
     * 根据大洲进行分页检索
     *
     * @param continent 大洲代码（ASIA, EUROPE, AFRICA, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA）
     * @param pageNo    页号，默认为1
     * @param pageSize  页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/continent", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByContinent(@RequestParam(name = "continent") String continent,
                                                                            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                                            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            // 解析大洲
            Continent cont;
            try {
                cont = Continent.valueOf(continent.toUpperCase());
            } catch (IllegalArgumentException e) {
                return QueryResponse.genErr("无效的大洲代码，支持：ASIA, EUROPE, AFRICA, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA");
            }
            
            // 使用Lucene层面分页查询
            IdxService.PageResult pageResult = idxService.queryByContinent(cont, pageNo, pageSize);
            
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", doc.get("NAME"));
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", doc.get("KG"));
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());
            
            return QueryResponse.genSucc("大洲检索成功", pageResponse);
        } catch (Exception e) {
            log.error("大洲检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("大洲检索过程中发生异常");
        }
    }

    /**
     * 根据国家进行分页检索
     *
     * @param country  国家名称
     * @param pageNo   页号，默认为1
     * @param pageSize 页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/country", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByCountry(@RequestParam(name = "country") String country,
                                                                          @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                                          @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        log.info("国家查询API - country: {}, pageNo: {}, pageSize: {}", country, pageNo, pageSize);

        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;

            log.info("开始Lucene国家查询 - 国家: {}", country);
            // 使用Lucene层面分页查询
            IdxService.PageResult pageResult = idxService.queryByCountry(country, pageNo, pageSize);
            log.info("Lucene查询完成 - 结果总数: {}", pageResult.getTotal());
            
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", doc.get("NAME"));
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", doc.get("KG"));
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());
            
            return QueryResponse.genSucc("国家检索成功", pageResponse);
        } catch (Exception e) {
            log.error("国家检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("国家检索过程中发生异常");
        }
    }

    /**
     * 根据年龄范围进行分页检索
     *
     * @param minAge   最小年龄（可选）：0-150
     * @param maxAge   最大年龄（可选）：0-150
     * @param pageNo   页号，默认为1
     * @param pageSize 页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/ageRange", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByAgeRange(
            @RequestParam(name = "minAge", required = false) Integer minAge,
            @RequestParam(name = "maxAge", required = false) Integer maxAge,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            // 检查是否提供了年龄范围参数
            if (minAge == null && maxAge == null) {
                return QueryResponse.genErr("请至少提供一个年龄范围参数（minAge或maxAge）");
            }
            
            // 构建检索条件
            SearchCriteria criteria = SearchCriteria.builder()
                    .minAge(minAge)
                    .maxAge(maxAge)
                    .build();
            
            // 验证年龄范围参数
            String ageRangeError = criteria.validateAgeRange();
            if (ageRangeError != null) {
                return QueryResponse.genErr(ageRangeError);
            }
            
            // 使用组合条件检索
            IdxService.PageResult pageResult = idxService.queryByCombinedCriteria(criteria, pageNo, pageSize);
            
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", doc.get("NAME"));
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", doc.get("KG"));
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());
            
            return QueryResponse.genSucc("年龄范围检索成功", pageResponse);
        } catch (Exception e) {
            log.error("年龄范围检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("年龄范围检索过程中发生异常");
        }
    }

    /**
     * 根据体重范围进行分页检索
     *
     * @param minWeight 最小体重（可选）：0-500公斤
     * @param maxWeight 最大体重（可选）：0-500公斤
     * @param pageNo    页号，默认为1
     * @param pageSize  页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/weightRange", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByWeightRange(
            @RequestParam(name = "minWeight", required = false) Double minWeight,
            @RequestParam(name = "maxWeight", required = false) Double maxWeight,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            // 检查是否提供了体重范围参数
            if (minWeight == null && maxWeight == null) {
                return QueryResponse.genErr("请至少提供一个体重范围参数（minWeight或maxWeight）");
            }
            
            // 构建检索条件
            SearchCriteria criteria = SearchCriteria.builder()
                    .minWeight(minWeight)
                    .maxWeight(maxWeight)
                    .build();
            
            // 验证体重范围参数
            String weightRangeError = criteria.validateWeightRange();
            if (weightRangeError != null) {
                return QueryResponse.genErr(weightRangeError);
            }
            
            // 使用组合条件检索
            IdxService.PageResult pageResult = idxService.queryByCombinedCriteria(criteria, pageNo, pageSize);
            
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", doc.get("NAME"));
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", doc.get("KG"));
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());
            
            return QueryResponse.genSucc("体重范围检索成功", pageResponse);
        } catch (Exception e) {
            log.error("体重范围检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("体重范围检索过程中发生异常");
        }
    }

    /**
     * 组合条件检索 - 支持多个条件同时查询
     * 可以组合：关键词、年龄组别、年龄范围、体重级别、体重范围、大洲、国家
     *
     * @param keyword     关键词（可选）
     * @param ageGroup    年龄组别（可选）：CADET-青少年, JUNIOR-青年, SENIOR-成年, VETERAN-资深
     * @param minAge      最小年龄（可选）：0-150
     * @param maxAge      最大年龄（可选）：0-150
     * @param weightClass 体重级别（可选）：-60, -66, -73, -81, -90, -100, +100
     * @param minWeight   最小体重（可选）：0-500公斤
     * @param maxWeight   最大体重（可选）：0-500公斤
     * @param continent   大洲（可选）：ASIA-亚洲, EUROPE-欧洲, AFRICA-非洲, NORTH_AMERICA-北美洲, SOUTH_AMERICA-南美洲, OCEANIA-大洋洲
     * @param country     国家（可选）
     * @param pageNo      页号，默认为1
     * @param pageSize    页的大小，默认为10
     * @return 检索得到的结果记录，包含分页信息
     */
    @GetMapping(value = "/combined", produces = "application/json;charset=UTF-8")
    public QueryResponse<PageResponse<Map<String, String>>> queryByCombinedCriteria(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "ageGroup", required = false) String ageGroup,
            @RequestParam(name = "minAge", required = false) Integer minAge,
            @RequestParam(name = "maxAge", required = false) Integer maxAge,
            @RequestParam(name = "weightClass", required = false) String weightClass,
            @RequestParam(name = "minWeight", required = false) Double minWeight,
            @RequestParam(name = "maxWeight", required = false) Double maxWeight,
            @RequestParam(name = "continent", required = false) String continent,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        log.info("组合查询API - keyword: {}, ageGroup: {}, weightClass: {}, country: {}, pageNo: {}, pageSize: {}",
                keyword, ageGroup, weightClass, country, pageNo, pageSize);

        try {
            // 参数验证
            if (pageNo < 1) pageNo = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            // 构建检索条件
            SearchCriteria.Builder criteriaBuilder = SearchCriteria.builder();
            
            // 设置关键词
            if (keyword != null && !keyword.trim().isEmpty()) {
                criteriaBuilder.keyword(keyword.trim());
            }
            
            // 设置年龄组别
            if (ageGroup != null && !ageGroup.trim().isEmpty()) {
                try {
                    AgeGroup group = AgeGroup.valueOf(ageGroup.toUpperCase());
                    criteriaBuilder.ageGroup(group);
                } catch (IllegalArgumentException e) {
                    return QueryResponse.genErr("无效的年龄组别，支持：CADET(青少年), JUNIOR(青年), SENIOR(成年), VETERAN(资深)");
                }
            }
            
            // 设置年龄范围
            if (minAge != null || maxAge != null) {
                criteriaBuilder.minAge(minAge).maxAge(maxAge);
            }
            
            // 设置体重级别
            if (weightClass != null && !weightClass.trim().isEmpty()) {
                WeightClass wc = WeightClass.getByCode(weightClass);
                if (wc == null) {
                    return QueryResponse.genErr("无效的体重级别，支持：-60, -66, -73, -81, -90, -100, +100");
                }
                criteriaBuilder.weightClass(wc);
            }
            
            // 设置体重范围
            if (minWeight != null || maxWeight != null) {
                criteriaBuilder.minWeight(minWeight).maxWeight(maxWeight);
            }
            
            // 设置大洲
            if (continent != null && !continent.trim().isEmpty()) {
                try {
                    Continent cont = Continent.valueOf(continent.toUpperCase());
                    criteriaBuilder.continent(cont);
                } catch (IllegalArgumentException e) {
                    return QueryResponse.genErr("无效的大洲，支持：ASIA(亚洲), EUROPE(欧洲), AFRICA(非洲), NORTH_AMERICA(北美洲), SOUTH_AMERICA(南美洲), OCEANIA(大洋洲)");
                }
            }
            
            // 设置国家
            if (country != null && !country.trim().isEmpty()) {
                criteriaBuilder.country(country.trim());
            }
            
            SearchCriteria criteria = criteriaBuilder.build();
            
            // 检查是否有任何检索条件
            if (!criteria.hasAnyCriteria()) {
                return QueryResponse.genErr("请至少提供一个检索条件");
            }
            
            // 验证年龄范围参数
            String ageRangeError = criteria.validateAgeRange();
            if (ageRangeError != null) {
                return QueryResponse.genErr(ageRangeError);
            }
            
            // 验证体重范围参数
            String weightRangeError = criteria.validateWeightRange();
            if (weightRangeError != null) {
                return QueryResponse.genErr(weightRangeError);
            }
            
            // 使用组合条件检索
            IdxService.PageResult pageResult = idxService.queryByCombinedCriteria(criteria, pageNo, pageSize);
            
            // 转换Document为Map
            List<Map<String, String>> results = new ArrayList<>();
            for (Document doc : pageResult.getDocuments()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("NAME", doc.get("NAME"));
                record.put("AGE", doc.get("AGE"));
                record.put("IMAGE", doc.get("IMAGE"));
                record.put("LOCATION", doc.get("LOCATION"));
                record.put("LOCATION_ICON", doc.get("LOCATION_ICON"));
                record.put("KG", doc.get("KG"));
                record.put("PHOTOS", doc.get("PHOTOS"));
                results.add(record);
            }
            
            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(results, pageNo, pageSize, pageResult.getTotal());
            
            return QueryResponse.genSucc("组合条件检索成功", pageResponse);
        } catch (Exception e) {
            log.error("组合条件检索过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("组合条件检索过程中发生异常");
        }
    }

    /**
     * 获取指定大洲的others国家列表
     *
     * @param continent 大洲
     * @return others国家列表
     */
    @GetMapping(value = "/others", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<String>> getOthersByContinent(@RequestParam(name = "continent") String continent) {
        try {
            // 解析大洲
            Continent cont;
            try {
                cont = Continent.valueOf(continent.toUpperCase());
            } catch (IllegalArgumentException e) {
                return QueryResponse.genErr("无效的大洲，支持：ASIA(亚洲), EUROPE(欧洲), AFRICA(非洲), NORTH_AMERICA(北美洲), SOUTH_AMERICA(南美洲), OCEANIA(大洋洲)");
            }
            
            List<String> others = CountryContinentMapping.getOthersByContinent(cont);
            return QueryResponse.genSucc("获取others国家列表成功", others);
        } catch (Exception e) {
            log.error("获取others国家列表过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("获取others国家列表过程中发生异常");
        }
    }

    /**
     * 动态添加国家到指定大洲的others集合
     *
     * @param country   国家名称
     * @param continent 大洲
     * @return 操作结果
     */
    @GetMapping(value = "/addOthers", produces = "application/json;charset=UTF-8")
    public QueryResponse<String> addCountryToOthers(@RequestParam(name = "country") String country,
                                                   @RequestParam(name = "continent") String continent) {
        try {
            // 参数验证
            if (country == null || country.trim().isEmpty()) {
                return QueryResponse.genErr("国家名称不能为空");
            }
            
            // 解析大洲
            Continent cont;
            try {
                cont = Continent.valueOf(continent.toUpperCase());
            } catch (IllegalArgumentException e) {
                return QueryResponse.genErr("无效的大洲，支持：ASIA(亚洲), EUROPE(欧洲), AFRICA(非洲), NORTH_AMERICA(北美洲), SOUTH_AMERICA(南美洲), OCEANIA(大洋洲)");
            }
            
            // 添加国家到others
            CountryContinentMapping.addCountryToOthers(country.trim(), cont);
            
            return QueryResponse.genSucc("成功添加国家到others集合", 
                String.format("国家[%s]已添加到[%s]大洲的others集合", country.trim(), cont.toString()));
        } catch (Exception e) {
            log.error("添加国家到others过程中发生异常:[{}]", e.getMessage());
            return QueryResponse.genErr("添加国家到others过程中发生异常");
        }
    }

    /**
     * 模糊匹配检索接口
     * 支持模糊查询、通配符查询、前缀查询等多种模糊匹配方式
     *
     * @param fuzzyKeyword 模糊关键词
     * @param similarity 相似度阈值 (0.0-1.0)，可选
     * @param page 页码，从1开始，默认1
     * @param size 每页大小，默认10
     * @return 分页检索结果
     */
    @GetMapping("/fuzzy")
    public QueryResponse<PageResponse<Map<String, String>>> fuzzySearch(
            @RequestParam String fuzzyKeyword,
            @RequestParam(required = false) Double similarity,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("🎯 QueryController模糊搜索接口 - 接收到请求: fuzzyKeyword='{}', similarity={}, page={}, size={}",
                 fuzzyKeyword, similarity, page, size);

        try {
            // 参数验证和详细日志
            if (fuzzyKeyword == null || fuzzyKeyword.trim().isEmpty()) {
                log.warn("⚠️ 模糊搜索关键词为空或空白");
                return QueryResponse.genErr("模糊关键词不能为空");
            }

            if (similarity != null && (similarity < 0.0 || similarity > 1.0)) {
                log.warn("⚠️ 相似度阈值参数异常: {}", similarity);
                return QueryResponse.genErr("相似度阈值必须在0.0-1.0之间");
            }

            if (page < 1) {
                log.warn("⚠️ 页码参数异常: {}，调整为1", page);
                page = 1;
            }

            if (size < 1 || size > 100) {
                log.warn("⚠️ 页大小参数异常: {}，调整为10", size);
                size = 10;
            }

            log.info("✅ 参数验证通过，准备调用IdxService进行模糊搜索");

            // 🔧 修复：直接传递原始页码，无需-1转换（IdxService内部已处理分页逻辑）
            PageResponse<Player> playerResult = idxService.fuzzySearch(fuzzyKeyword, similarity, page, size);

            log.info("📊 IdxService返回结果 - 实际返回{}条记录，总匹配数: {}",
                     playerResult.getContent().size(), playerResult.getTotalElements());

            // 转换Player为Map格式以保持一致性
            List<Map<String, String>> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Player player : playerResult.getContent()) {
                try {
                    Map<String, String> record = new HashMap<>();

                    // 🔧 添加空值检查，防止NPE
                    record.put("ID", player.getId() != null ? player.getId() : "");
                    record.put("NAME", player.getName() != null ? player.getName() : "");
                    record.put("AGE", player.getAge() != null ? player.getAge() : "");
                    record.put("IMAGE", player.getImage() != null ? player.getImage() : "");
                    record.put("LOCATION", player.getLocation() != null ? player.getLocation() : "");
                    record.put("LOCATION_ICON", player.getLocationIcon() != null ? player.getLocationIcon() : "");
                    record.put("KG", player.getKg() != null ? player.getKg() : "");

                    // 处理照片信息 - 增强错误处理
                    String photosJson = "[]";
                    if (player.getPhotoEntity() != null) {
                        try {
                            photosJson = convertPhotoEntityToJson(player.getPhotoEntity());
                            // 🔧 修复：计算PhotoEntity中的总照片数量
                            int totalPhotos = 0;
                            PhotoEntity photoEntity = player.getPhotoEntity();
                            if (photoEntity != null) {
                                int spotlightPhotos = (photoEntity.getUnderTheSpotlights() != null) ? photoEntity.getUnderTheSpotlights().size() : 0;
                                int eventPhotos = (photoEntity.getPhotos() != null) ? photoEntity.getPhotos().size() : 0;
                                totalPhotos = spotlightPhotos + eventPhotos;
                            }
                            log.debug("📸 成功处理运动员照片信息 - ID: {}, 聚光灯照片: {}, 赛事照片: {}, 总照片数: {}",
                                     player.getId(),
                                     (photoEntity.getUnderTheSpotlights() != null) ? photoEntity.getUnderTheSpotlights().size() : 0,
                                     (photoEntity.getPhotos() != null) ? photoEntity.getPhotos().size() : 0,
                                     totalPhotos);
                        } catch (Exception e) {
                            log.warn("⚠️ 转换照片信息失败 - ID: {}, 错误: {}", player.getId(), e.getMessage());
                            photosJson = "[]";
                        }
                    }
                    record.put("PHOTOS", photosJson);

                    results.add(record);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                    log.error("💥 转换Player记录失败 - Player: {}, 错误: {}", player, e.getMessage());
                    // 继续处理其他记录，不因单个失败影响整体
                }
            }

            log.info("🔄 数据转换完成 - 成功: {}, 失败: {}", successCount, failCount);

            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(
                results,
                page,
                size,
                playerResult.getTotalElements()
            );

            log.info("🏆 QueryController模糊搜索接口成功返回 - 当前页: {}, 页大小: {}, 返回记录数: {}, 总匹配数: {}",
                    page, size, results.size(), playerResult.getTotalElements());

            return QueryResponse.genSucc("模糊搜索成功", pageResponse);

        } catch (Exception e) {
            log.error("💥 QueryController模糊搜索接口异常 - 关键词: '{}', 错误: {}", fuzzyKeyword, e.getMessage(), e);
            return QueryResponse.genErr("模糊搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 高级搜索接口
     * 支持多字段组合检索，包括关键词、模糊关键词、年龄组别、年龄范围、体重级别、体重范围、大洲、国家等条件
     *
     * @param keyword 精确关键词，可选
     * @param fuzzyKeyword 模糊关键词，可选
     * @param similarity 相似度阈值，可选
     * @param ageGroup 年龄组别，可选
     * @param minAge 最小年龄，可选
     * @param maxAge 最大年龄，可选
     * @param weightClass 体重级别，可选
     * @param minWeight 最小体重，可选
     * @param maxWeight 最大体重，可选
     * @param continent 大洲，可选
     * @param country 国家，可选
     * @param page 页码，从1开始，默认1
     * @param size 每页大小，默认10
     * @return 分页检索结果
     */
    @GetMapping("/advanced")
    public QueryResponse<PageResponse<Map<String, String>>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fuzzyKeyword,
            @RequestParam(required = false) Double similarity,
            @RequestParam(required = false) AgeGroup ageGroup,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) WeightClass weightClass,
            @RequestParam(required = false) Double minWeight,
            @RequestParam(required = false) Double maxWeight,
            @RequestParam(required = false) Continent continent,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // 构建检索条件
            SearchCriteria criteria = SearchCriteria.builder()
                    .keyword(keyword)
                    .fuzzyKeyword(fuzzyKeyword)
                    .similarity(similarity)
                    .ageGroup(ageGroup)
                    .minAge(minAge)
                    .maxAge(maxAge)
                    .weightClass(weightClass)
                    .minWeight(minWeight)
                    .maxWeight(maxWeight)
                    .continent(continent)
                    .country(country)
                    .build();

            // 参数验证
            if (!criteria.hasAnyCriteria()) {
                return QueryResponse.genErr("请至少提供一个搜索条件");
            }

            String ageRangeError = criteria.validateAgeRange();
            if (ageRangeError != null) {
                return QueryResponse.genErr(ageRangeError);
            }

            String weightRangeError = criteria.validateWeightRange();
            if (weightRangeError != null) {
                return QueryResponse.genErr(weightRangeError);
            }

            String similarityError = criteria.validateSimilarity();
            if (similarityError != null) {
                return QueryResponse.genErr(similarityError);
            }

            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;

            // 执行高级搜索（转换为从0开始的页码）
            PageResponse<Player> playerResult = idxService.advancedSearch(criteria, page - 1, size);

            // 转换Player为Map格式以保持一致性
            List<Map<String, String>> results = new ArrayList<>();
            for (Player player : playerResult.getContent()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", player.getId());
                record.put("NAME", player.getName());
                record.put("AGE", player.getAge());
                record.put("IMAGE", player.getImage());
                record.put("LOCATION", player.getLocation());
                record.put("LOCATION_ICON", player.getLocationIcon() != null ? player.getLocationIcon() : "");
                record.put("KG", player.getKg());

                // 处理照片信息
                String photosJson = "[]";
                if (player.getPhotoEntity() != null) {
                    try {
                        photosJson = convertPhotoEntityToJson(player.getPhotoEntity());
                    } catch (Exception e) {
                        log.warn("转换照片信息失败: {}", e.getMessage());
                        photosJson = "[]";
                    }
                }
                record.put("PHOTOS", photosJson);
                results.add(record);
            }

            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(
                results,
                page,
                size,
                playerResult.getTotalElements()
            );

            return QueryResponse.genSucc("高级搜索成功", pageResponse);

        } catch (Exception e) {
            log.error("高级搜索失败", e);
            return QueryResponse.genErr("高级搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 智能搜索接口
     * 结合精确匹配和模糊匹配的智能检索，优先返回精确匹配结果
     *
     * @param keyword 搜索关键词
     * @param page 页码，从1开始，默认1
     * @param size 每页大小，默认10
     * @return 分页检索结果
     */
    @GetMapping("/smart")
    public QueryResponse<PageResponse<Map<String, String>>> smartSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // 参数验证
            if (keyword == null || keyword.trim().isEmpty()) {
                return QueryResponse.genErr("搜索关键词不能为空");
            }

            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;

            // 执行智能搜索（转换为从0开始的页码）
            PageResponse<Player> playerResult = idxService.smartSearch(keyword, page - 1, size);

            // 转换Player为Map格式以保持一致性
            List<Map<String, String>> results = new ArrayList<>();
            for (Player player : playerResult.getContent()) {
                Map<String, String> record = new HashMap<>();
                record.put("ID", player.getId());
                record.put("NAME", player.getName());
                record.put("AGE", player.getAge());
                record.put("IMAGE", player.getImage());
                record.put("LOCATION", player.getLocation());
                record.put("LOCATION_ICON", player.getLocationIcon() != null ? player.getLocationIcon() : "");
                record.put("KG", player.getKg());

                // 处理照片信息
                String photosJson = "[]";
                if (player.getPhotoEntity() != null) {
                    try {
                        photosJson = convertPhotoEntityToJson(player.getPhotoEntity());
                    } catch (Exception e) {
                        log.warn("转换照片信息失败: {}", e.getMessage());
                        photosJson = "[]";
                    }
                }
                record.put("PHOTOS", photosJson);
                results.add(record);
            }

            // 构建分页响应对象
            PageResponse<Map<String, String>> pageResponse = PageResponse.of(
                results,
                page,
                size,
                playerResult.getTotalElements()
            );

            return QueryResponse.genSucc("智能搜索成功", pageResponse);

        } catch (Exception e) {
            log.error("智能搜索失败", e);
            return QueryResponse.genErr("智能搜索失败：" + e.getMessage());
        }
    }

    /**
     * 🎯 新增：从workspace重建索引
     *
     * @return 重建结果
     */
    @PostMapping("/rebuild-index")
    public QueryResponse<Map<String, Object>> rebuildIndex() {
        try {
            log.info("=== 收到重建索引请求 ===");

            // 执行索引重建
            int rebuiltRecords = idxService.rebuildIndexFromWorkspace();

            Map<String, Object> result = new HashMap<>();
            result.put("rebuiltRecords", rebuiltRecords);
            result.put("message", rebuiltRecords > 0 ? "索引重建成功" : "索引重建失败");

            if (rebuiltRecords > 0) {
                log.info("索引重建完成，成功重建{}条记录", rebuiltRecords);
                return QueryResponse.genSucc("索引重建成功", result);
            } else {
                log.error("索引重建失败，返回记录数: {}", rebuiltRecords);
                return QueryResponse.genErr("索引重建失败，请检查日志");
            }

        } catch (Exception e) {
            log.error("重建索引API调用失败", e);
            return QueryResponse.genErr("重建索引失败：" + e.getMessage());
        }
    }
}
