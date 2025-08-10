package cn.edu.bistu.cs.ir.index;

import cn.edu.bistu.cs.ir.config.Config;
import cn.edu.bistu.cs.ir.model.AgeGroup;
import cn.edu.bistu.cs.ir.model.Continent;
import cn.edu.bistu.cs.ir.model.CountryContinentMapping;
import cn.edu.bistu.cs.ir.model.WeightClass;
import cn.edu.bistu.cs.ir.model.Player;
import cn.edu.bistu.cs.ir.service.SearchCriteria;
import cn.edu.bistu.cs.ir.utils.StringUtil;
import cn.edu.bistu.cs.ir.utils.PageResponse;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 面向<a href="https://lucene.apache.org/">Lucene</a>
 * 索引读、写的服务类
 * @author zhaxijiancuo
 */
@Component
public class IdxService implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(IdxService.class);

    private static final Class<? extends Analyzer> DEFAULT_ANALYZER = StandardAnalyzer.class;

    private IndexWriter writer;

    public IdxService(@Autowired Config config) throws Exception {
        Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();
        Directory index;
        try {
            index = FSDirectory.open(Paths.get(config.getIdx()));
            IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
            writer = new IndexWriter(index, writerConfig);
            log.info("索引初始化完成，索引目录为:[{}]", config.getIdx());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("无法初始化索引，请检查提供的索引目录是否可用:[{}]", config.getIdx());
            writer = null;
        }
    }

    public boolean addDocument(String idFld, String id, Document doc){
        if(writer==null||doc==null){
            log.error("Writer对象或文档对象为空，无法添加文档到索引中");
            return false;
        }
        if(StringUtil.isEmpty(idFld)||StringUtil.isEmpty(id)){
            log.error("ID字段名或ID字段值为空，无法添加文档到索引中");
            return false;
        }
        try {
            writer.updateDocument(new Term(idFld, id), doc);
            writer.commit();
            log.info("成功将ID为[{}]的柔道家信息加入索引", id);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("构建索引失败");
            return false;
        }
    }

    /**
     * 根据关键词对索引内容进行检索，并将检索结果返回
     * @param kw 待检索的关键词
     * @return 检索得到的文档列表
     */
    public List<Document> queryByKw(String kw) throws Exception{
        //打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();
        QueryParser parser = new QueryParser("NAME", analyzer);
        Query query = parser.parse(kw);
        TopDocs docs =searcher.search(query, 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<Document> results = new ArrayList<>();
        for (ScoreDoc doc : hits) {
            results.add(searcher.doc(doc.doc));
        }
        return results;
    }

    /**
     * 根据关键词对索引内容进行分页检索
     * @param kw 待检索的关键词
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页检索结果，包含当前页数据和总记录数
     */
    public PageResult queryByKwWithPaging(String kw, int pageNo, int pageSize) throws Exception {
        // 参数验证
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        
        // 打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();
        QueryParser parser = new QueryParser("NAME", analyzer);
        Query query = parser.parse(kw);
        
        // 先获取总记录数
        TopDocs totalDocs = searcher.search(query, Integer.MAX_VALUE);
        long total = totalDocs.totalHits.value;
        
        // 计算分页参数
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        
        // 如果起始索引超出范围，返回空结果
        if (fromIndex >= total) {
            return new PageResult(new ArrayList<>(), total);
        }
        
        // 在Lucene层面进行分页查询
        TopDocs docs = searcher.search(query, toIndex);
        ScoreDoc[] hits = docs.scoreDocs;
        
        List<Document> results = new ArrayList<>();
        // 只取当前页的数据
        for (int i = fromIndex; i < Math.min(hits.length, toIndex); i++) {
            results.add(searcher.doc(hits[i].doc));
        }
        
        return new PageResult(results, total);
    }

    /**
     * 分页查询结果封装类
     */
    public static class PageResult {
        private final List<Document> documents;
        private final long total;
        
        public PageResult(List<Document> documents, long total) {
            this.documents = documents;
            this.total = total;
        }
        
        public List<Document> getDocuments() {
            return documents;
        }
        
        public long getTotal() {
            return total;
        }
    }

    /**
     * 根据年龄组别进行分页检索
     * @param ageGroup 年龄组别
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页检索结果
     */
    public PageResult queryByAgeGroup(AgeGroup ageGroup, int pageNo, int pageSize) throws Exception {
        if (ageGroup == null) {
            throw new IllegalArgumentException("年龄组别不能为空");
        }
        
        // 参数验证
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        
        // 打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // 构建年龄范围查询
        Query query = IntPoint.newRangeQuery("AGE_NUM", ageGroup.getMinAge(), ageGroup.getMaxAge());
        
        // 先获取总记录数
        TopDocs totalDocs = searcher.search(query, Integer.MAX_VALUE);
        long total = totalDocs.totalHits.value;
        
        // 计算分页参数
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        
        // 如果起始索引超出范围，返回空结果
        if (fromIndex >= total) {
            return new PageResult(new ArrayList<>(), total);
        }
        
        // 在Lucene层面进行分页查询
        TopDocs docs = searcher.search(query, toIndex);
        ScoreDoc[] hits = docs.scoreDocs;
        
        List<Document> results = new ArrayList<>();
        // 只取当前页的数据
        for (int i = fromIndex; i < Math.min(hits.length, toIndex); i++) {
            results.add(searcher.doc(hits[i].doc));
        }
        
        return new PageResult(results, total);
    }

    /**
     * 根据体重级别进行分页检索
     * @param weightClass 体重级别
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页检索结果
     */
    public PageResult queryByWeightClass(WeightClass weightClass, int pageNo, int pageSize) throws Exception {
        if (weightClass == null) {
            throw new IllegalArgumentException("体重级别不能为空");
        }
        
        // 参数验证
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        
        // 打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // 构建体重级别查询
        Query query = new TermQuery(new Term("KG", weightClass.getCode()));
        
        // 先获取总记录数
        TopDocs totalDocs = searcher.search(query, Integer.MAX_VALUE);
        long total = totalDocs.totalHits.value;
        
        // 计算分页参数
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        
        // 如果起始索引超出范围，返回空结果
        if (fromIndex >= total) {
            return new PageResult(new ArrayList<>(), total);
        }
        
        // 在Lucene层面进行分页查询
        TopDocs docs = searcher.search(query, toIndex);
        ScoreDoc[] hits = docs.scoreDocs;
        
        List<Document> results = new ArrayList<>();
        // 只取当前页的数据
        for (int i = fromIndex; i < Math.min(hits.length, toIndex); i++) {
            results.add(searcher.doc(hits[i].doc));
        }
        
        return new PageResult(results, total);
    }

    /**
     * 根据大洲进行分页检索
     * @param continent 大洲
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页检索结果
     */
    public PageResult queryByContinent(Continent continent, int pageNo, int pageSize) throws Exception {
        if (continent == null) {
            throw new IllegalArgumentException("大洲不能为空");
        }
        
        // 参数验证
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        
        // 打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // 获取该大洲的所有国家
        List<String> countries = CountryContinentMapping.getCountriesByContinent(continent);
        if (countries.isEmpty()) {
            return new PageResult(new ArrayList<>(), 0);
        }
        
        // 构建多国家查询（OR查询）
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String country : countries) {
            booleanQuery.add(new TermQuery(new Term("LOCATION", country)), BooleanClause.Occur.SHOULD);
        }
        Query query = booleanQuery.build();
        
        // 先获取总记录数
        TopDocs totalDocs = searcher.search(query, Integer.MAX_VALUE);
        long total = totalDocs.totalHits.value;
        
        // 计算分页参数
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        
        // 如果起始索引超出范围，返回空结果
        if (fromIndex >= total) {
            return new PageResult(new ArrayList<>(), total);
        }
        
        // 在Lucene层面进行分页查询
        TopDocs docs = searcher.search(query, toIndex);
        ScoreDoc[] hits = docs.scoreDocs;
        
        List<Document> results = new ArrayList<>();
        // 只取当前页的数据
        for (int i = fromIndex; i < Math.min(hits.length, toIndex); i++) {
            results.add(searcher.doc(hits[i].doc));
        }
        
        return new PageResult(results, total);
    }

    /**
     * 根据国家进行分页检索
     * @param country 国家名称
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页检索结果
     */
    public PageResult queryByCountry(String country, int pageNo, int pageSize) throws Exception {
        if (StringUtil.isEmpty(country)) {
            throw new IllegalArgumentException("国家名称不能为空");
        }
        
        // 参数验证
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        
        // 打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // 构建国家查询
        Query query = new TermQuery(new Term("LOCATION", country));
        
        // 先获取总记录数
        TopDocs totalDocs = searcher.search(query, Integer.MAX_VALUE);
        long total = totalDocs.totalHits.value;
        
        // 计算分页参数
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        
        // 如果起始索引超出范围，返回空结果
        if (fromIndex >= total) {
            return new PageResult(new ArrayList<>(), total);
        }
        
        // 在Lucene层面进行分页查询
        TopDocs docs = searcher.search(query, toIndex);
        ScoreDoc[] hits = docs.scoreDocs;
        
        List<Document> results = new ArrayList<>();
        // 只取当前页的数据
        for (int i = fromIndex; i < Math.min(hits.length, toIndex); i++) {
            results.add(searcher.doc(hits[i].doc));
        }
        
        return new PageResult(results, total);
    }

    /**
     * 根据大洲和国家进行分页检索
     * @param continent 大洲
     * @param country 国家名称
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页检索结果
     */
    public PageResult queryByContinentAndCountry(Continent continent, String country, int pageNo, int pageSize) throws Exception {
        if (continent == null) {
            throw new IllegalArgumentException("大洲不能为空");
        }
        if (StringUtil.isEmpty(country)) {
            throw new IllegalArgumentException("国家名称不能为空");
        }
        
        // 验证国家是否属于指定大洲
        if (!CountryContinentMapping.isCountryInContinent(country, continent)) {
            return new PageResult(new ArrayList<>(), 0);
        }
        
        // 直接调用国家查询方法
        return queryByCountry(country, pageNo, pageSize);
    }

    /**
     * 组合条件检索 - 支持多个条件同时查询
     * @param criteria 检索条件对象
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页检索结果
     */
    public PageResult queryByCombinedCriteria(SearchCriteria criteria, int pageNo, int pageSize) throws Exception {
        if (criteria == null || !criteria.hasAnyCriteria()) {
            throw new IllegalArgumentException("检索条件不能为空");
        }
        
        // 参数验证
        if (pageNo < 1) pageNo = 1;
        if (pageSize < 1) pageSize = 10;
        
        // 打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // 构建组合查询
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        
        // 关键词查询
        if (criteria.hasKeyword()) {
            try {
                Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();
                QueryParser parser = new QueryParser("NAME", analyzer);
                Query keywordQuery = parser.parse(criteria.getKeyword());
                booleanQueryBuilder.add(keywordQuery, BooleanClause.Occur.MUST);
            } catch (Exception e) {
                log.warn("关键词查询解析失败: {}", criteria.getKeyword());
            }
        }
        
        // 年龄组别查询
        if (criteria.hasAgeGroup()) {
            Query ageQuery = IntPoint.newRangeQuery("AGE_NUM", 
                criteria.getAgeGroup().getMinAge(), 
                criteria.getAgeGroup().getMaxAge());
            booleanQueryBuilder.add(ageQuery, BooleanClause.Occur.MUST);
        }
        
        // 年龄范围查询
        if (criteria.hasAgeRange()) {
            int minAge = criteria.getMinAge() != null ? criteria.getMinAge() : 0;
            int maxAge = criteria.getMaxAge() != null ? criteria.getMaxAge() : Integer.MAX_VALUE;
            Query ageRangeQuery = IntPoint.newRangeQuery("AGE_NUM", minAge, maxAge);
            booleanQueryBuilder.add(ageRangeQuery, BooleanClause.Occur.MUST);
        }
        
        // 体重级别查询
        if (criteria.hasWeightClass()) {
            Query weightQuery = new TermQuery(new Term("KG", criteria.getWeightClass().getCode()));
            booleanQueryBuilder.add(weightQuery, BooleanClause.Occur.MUST);
        }
        
        // 体重范围查询
        if (criteria.hasWeightRange()) {
            // 体重范围查询需要解析KG字段中的数值
            // 由于KG字段存储的是体重级别代码，我们需要特殊处理
            // 这里我们使用通配符查询来匹配体重范围
            double minWeight = criteria.getMinWeight() != null ? criteria.getMinWeight() : 0.0;
            double maxWeight = criteria.getMaxWeight() != null ? criteria.getMaxWeight() : Double.MAX_VALUE;
            
            // 构建体重范围查询
            BooleanQuery.Builder weightRangeQueryBuilder = new BooleanQuery.Builder();
            
            // 根据体重范围匹配对应的体重级别
            if (minWeight <= 60 && maxWeight >= 60) {
                weightRangeQueryBuilder.add(new TermQuery(new Term("KG", "-60")), BooleanClause.Occur.SHOULD);
            }
            if (minWeight <= 66 && maxWeight >= 66) {
                weightRangeQueryBuilder.add(new TermQuery(new Term("KG", "-66")), BooleanClause.Occur.SHOULD);
            }
            if (minWeight <= 73 && maxWeight >= 73) {
                weightRangeQueryBuilder.add(new TermQuery(new Term("KG", "-73")), BooleanClause.Occur.SHOULD);
            }
            if (minWeight <= 81 && maxWeight >= 81) {
                weightRangeQueryBuilder.add(new TermQuery(new Term("KG", "-81")), BooleanClause.Occur.SHOULD);
            }
            if (minWeight <= 90 && maxWeight >= 90) {
                weightRangeQueryBuilder.add(new TermQuery(new Term("KG", "-90")), BooleanClause.Occur.SHOULD);
            }
            if (minWeight <= 100 && maxWeight >= 100) {
                weightRangeQueryBuilder.add(new TermQuery(new Term("KG", "-100")), BooleanClause.Occur.SHOULD);
            }
            if (maxWeight >= 100) {
                weightRangeQueryBuilder.add(new TermQuery(new Term("KG", "+100")), BooleanClause.Occur.SHOULD);
            }
            
            BooleanQuery weightRangeQuery = weightRangeQueryBuilder.build();
            if (weightRangeQuery.clauses().size() > 0) {
                booleanQueryBuilder.add(weightRangeQuery, BooleanClause.Occur.MUST);
            }
        }
        
        // 大洲查询
        if (criteria.hasContinent()) {
            // 获取该大洲下的所有国家（包括明确列出的国家和others）
            List<String> countries = CountryContinentMapping.getCountriesByContinentWithOthers(criteria.getContinent());
            if (!countries.isEmpty()) {
                BooleanQuery.Builder countryQueryBuilder = new BooleanQuery.Builder();
                for (String country : countries) {
                    countryQueryBuilder.add(new TermQuery(new Term("LOCATION", country)), BooleanClause.Occur.SHOULD);
                }
                booleanQueryBuilder.add(countryQueryBuilder.build(), BooleanClause.Occur.MUST);
            }
        }
        
        // 国家查询
        if (criteria.hasCountry()) {
            Query countryQuery = new TermQuery(new Term("LOCATION", criteria.getCountry()));
            booleanQueryBuilder.add(countryQuery, BooleanClause.Occur.MUST);
        }
        
        Query combinedQuery = booleanQueryBuilder.build();
        
        // 先获取总记录数
        TopDocs totalDocs = searcher.search(combinedQuery, Integer.MAX_VALUE);
        long total = totalDocs.totalHits.value;
        
        // 计算分页参数
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        
        // 如果起始索引超出范围，返回空结果
        if (fromIndex >= total) {
            return new PageResult(new ArrayList<>(), total);
        }
        
        // 在Lucene层面进行分页查询
        TopDocs docs = searcher.search(combinedQuery, toIndex);
        ScoreDoc[] hits = docs.scoreDocs;
        
        List<Document> results = new ArrayList<>();
        // 只取当前页的数据
        for (int i = fromIndex; i < Math.min(hits.length, toIndex); i++) {
            results.add(searcher.doc(hits[i].doc));
        }
        
        return new PageResult(results, total);
    }

    /**
     * 模糊匹配检索 - 支持模糊查询和相似度匹配
     * @param fuzzyKeyword 模糊关键词
     * @param similarity 相似度阈值 (0.0-1.0)
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageResponse<Player> fuzzySearch(String fuzzyKeyword, Double similarity, int page, int size) {
        try {
            IndexReader reader = DirectoryReader.open(writer);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            // 构建模糊查询
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
            
            // 1. 模糊查询 (FuzzyQuery) - 支持拼写错误和相似词
            FuzzyQuery nameFuzzyQuery = new FuzzyQuery(new Term("name", fuzzyKeyword.toLowerCase()), 2);
            FuzzyQuery countryFuzzyQuery = new FuzzyQuery(new Term("country", fuzzyKeyword.toLowerCase()), 2);
            
            // 2. 通配符查询 (WildcardQuery) - 支持*和?通配符
            WildcardQuery nameWildcardQuery = new WildcardQuery(new Term("name", "*" + fuzzyKeyword.toLowerCase() + "*"));
            WildcardQuery countryWildcardQuery = new WildcardQuery(new Term("country", "*" + fuzzyKeyword.toLowerCase() + "*"));
            
            // 3. 前缀查询 (PrefixQuery) - 支持前缀匹配
            PrefixQuery namePrefixQuery = new PrefixQuery(new Term("name", fuzzyKeyword.toLowerCase()));
            PrefixQuery countryPrefixQuery = new PrefixQuery(new Term("country", fuzzyKeyword.toLowerCase()));
            
            // 组合查询
            queryBuilder.add(nameFuzzyQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(countryFuzzyQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(nameWildcardQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(countryWildcardQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(namePrefixQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(countryPrefixQuery, BooleanClause.Occur.SHOULD);
            
            // 4. 如果提供了相似度阈值，调整查询权重
            if (similarity != null && similarity > 0.0) {
                // 使用相似度作为权重调整因子
                float boost = similarity.floatValue();
                BoostQuery boostedNameQuery = new BoostQuery(nameFuzzyQuery, boost);
                BoostQuery boostedCountryQuery = new BoostQuery(countryFuzzyQuery, boost);
                
                queryBuilder.add(boostedNameQuery, BooleanClause.Occur.SHOULD);
                queryBuilder.add(boostedCountryQuery, BooleanClause.Occur.SHOULD);
            }
            
            BooleanQuery query = queryBuilder.build();
            
            // 执行搜索
            TopDocs topDocs = searcher.search(query, page * size);
            
            // 分页处理
            int start = page * size;
            int end = Math.min(start + size, topDocs.scoreDocs.length);
            
            List<Player> players = new ArrayList<>();
            for (int i = start; i < end; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                Player player = documentToPlayer(doc);
                players.add(player);
            }
            
            reader.close();
            
            return PageResponse.of(players, page, size, topDocs.totalHits.value);
            
        } catch (Exception e) {
            log.error("模糊匹配检索失败", e);
            throw new RuntimeException("模糊匹配检索失败: " + e.getMessage());
        }
    }
    
    /**
     * 高级搜索 - 多字段组合检索
     * 支持复杂的布尔逻辑组合，包括AND、OR、NOT操作
     * @param criteria 检索条件
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageResponse<Player> advancedSearch(SearchCriteria criteria, int page, int size) {
        try {
            IndexReader reader = DirectoryReader.open(writer);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            BooleanQuery.Builder mainQueryBuilder = new BooleanQuery.Builder();
            
            // 1. 关键词检索 (精确匹配 + 模糊匹配)
            if (criteria.hasKeyword()) {
                BooleanQuery.Builder keywordQueryBuilder = new BooleanQuery.Builder();
                
                // 精确匹配
                TermQuery nameQuery = new TermQuery(new Term("name", criteria.getKeyword().toLowerCase()));
                TermQuery countryQuery = new TermQuery(new Term("country", criteria.getKeyword().toLowerCase()));
                
                // 短语查询 - 提高精确度
                PhraseQuery namePhraseQuery = new PhraseQuery.Builder()
                    .add(new Term("name", criteria.getKeyword().toLowerCase()))
                    .build();
                
                keywordQueryBuilder.add(nameQuery, BooleanClause.Occur.SHOULD);
                keywordQueryBuilder.add(countryQuery, BooleanClause.Occur.SHOULD);
                keywordQueryBuilder.add(namePhraseQuery, BooleanClause.Occur.SHOULD);
                
                mainQueryBuilder.add(keywordQueryBuilder.build(), BooleanClause.Occur.MUST);
            }
            
            // 2. 模糊关键词检索
            if (criteria.hasFuzzyKeyword()) {
                BooleanQuery.Builder fuzzyQueryBuilder = new BooleanQuery.Builder();
                
                // 模糊查询
                FuzzyQuery nameFuzzyQuery = new FuzzyQuery(new Term("name", criteria.getFuzzyKeyword().toLowerCase()), 2);
                FuzzyQuery countryFuzzyQuery = new FuzzyQuery(new Term("country", criteria.getFuzzyKeyword().toLowerCase()), 2);
                
                // 通配符查询
                WildcardQuery nameWildcardQuery = new WildcardQuery(new Term("name", "*" + criteria.getFuzzyKeyword().toLowerCase() + "*"));
                WildcardQuery countryWildcardQuery = new WildcardQuery(new Term("country", "*" + criteria.getFuzzyKeyword().toLowerCase() + "*"));
                
                fuzzyQueryBuilder.add(nameFuzzyQuery, BooleanClause.Occur.SHOULD);
                fuzzyQueryBuilder.add(countryFuzzyQuery, BooleanClause.Occur.SHOULD);
                fuzzyQueryBuilder.add(nameWildcardQuery, BooleanClause.Occur.SHOULD);
                fuzzyQueryBuilder.add(countryWildcardQuery, BooleanClause.Occur.SHOULD);
                
                mainQueryBuilder.add(fuzzyQueryBuilder.build(), BooleanClause.Occur.MUST);
            }
            
            // 3. 年龄组别检索
            if (criteria.hasAgeGroup()) {
                TermQuery ageGroupQuery = new TermQuery(new Term("ageGroup", criteria.getAgeGroup().name()));
                mainQueryBuilder.add(ageGroupQuery, BooleanClause.Occur.MUST);
            }
            
            // 4. 年龄范围检索
            if (criteria.hasAgeRange()) {
                Integer minAge = criteria.getMinAge();
                Integer maxAge = criteria.getMaxAge();
                
                if (minAge == null) minAge = 0;
                if (maxAge == null) maxAge = 150;
                
                // 使用IntPoint进行范围查询
                Query ageRangeQuery = IntPoint.newRangeQuery("AGE_NUM", minAge, maxAge);
                mainQueryBuilder.add(ageRangeQuery, BooleanClause.Occur.MUST);
            }
            
            // 5. 体重级别检索
            if (criteria.hasWeightClass()) {
                TermQuery weightClassQuery = new TermQuery(new Term("weightClass", criteria.getWeightClass().name()));
                mainQueryBuilder.add(weightClassQuery, BooleanClause.Occur.MUST);
            }
            
            // 6. 体重范围检索
            if (criteria.hasWeightRange()) {
                Double minWeight = criteria.getMinWeight();
                Double maxWeight = criteria.getMaxWeight();
                
                if (minWeight == null) minWeight = 0.0;
                if (maxWeight == null) maxWeight = 500.0;
                
                // 使用DoublePoint进行范围查询
                Query weightRangeQuery = DoublePoint.newRangeQuery("WEIGHT", minWeight, maxWeight);
                mainQueryBuilder.add(weightRangeQuery, BooleanClause.Occur.MUST);
            }
            
            // 7. 大洲检索
            if (criteria.hasContinent()) {
                BooleanQuery.Builder continentQueryBuilder = new BooleanQuery.Builder();
                
                // 精确匹配大洲
                TermQuery continentQuery = new TermQuery(new Term("continent", criteria.getContinent().name()));
                continentQueryBuilder.add(continentQuery, BooleanClause.Occur.SHOULD);
                
                // 获取该大洲的所有国家（包括others）
                List<String> allCountries = CountryContinentMapping.getCountriesByContinentWithOthers(criteria.getContinent());
                for (String country : allCountries) {
                    TermQuery countryQuery = new TermQuery(new Term("country", country));
                    continentQueryBuilder.add(countryQuery, BooleanClause.Occur.SHOULD);
                }
                
                mainQueryBuilder.add(continentQueryBuilder.build(), BooleanClause.Occur.MUST);
            }
            
            // 8. 国家检索
            if (criteria.hasCountry()) {
                TermQuery countryQuery = new TermQuery(new Term("country", criteria.getCountry()));
                mainQueryBuilder.add(countryQuery, BooleanClause.Occur.MUST);
            }
            
            BooleanQuery mainQuery = mainQueryBuilder.build();
            
            // 执行搜索
            TopDocs topDocs = searcher.search(mainQuery, page * size);
            
            // 分页处理
            int start = page * size;
            int end = Math.min(start + size, topDocs.scoreDocs.length);
            
            List<Player> players = new ArrayList<>();
            for (int i = start; i < end; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                Player player = documentToPlayer(doc);
                players.add(player);
            }
            
            reader.close();
            
            return PageResponse.of(players, page, size, topDocs.totalHits.value);
            
        } catch (Exception e) {
            log.error("高级搜索失败", e);
            throw new RuntimeException("高级搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 智能搜索 - 结合精确匹配和模糊匹配的智能检索
     * 优先返回精确匹配结果，然后返回模糊匹配结果
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageResponse<Player> smartSearch(String keyword, int page, int size) {
        try {
            IndexReader reader = DirectoryReader.open(writer);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
            
            // 1. 精确匹配 (高权重)
            TermQuery exactNameQuery = new TermQuery(new Term("name", keyword.toLowerCase()));
            TermQuery exactCountryQuery = new TermQuery(new Term("country", keyword.toLowerCase()));
            
            BoostQuery boostedExactNameQuery = new BoostQuery(exactNameQuery, 3.0f);
            BoostQuery boostedExactCountryQuery = new BoostQuery(exactCountryQuery, 2.0f);
            
            queryBuilder.add(boostedExactNameQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(boostedExactCountryQuery, BooleanClause.Occur.SHOULD);
            
            // 2. 短语匹配 (中权重)
            PhraseQuery namePhraseQuery = new PhraseQuery.Builder()
                .add(new Term("name", keyword.toLowerCase()))
                .build();
            
            BoostQuery boostedPhraseQuery = new BoostQuery(namePhraseQuery, 2.5f);
            queryBuilder.add(boostedPhraseQuery, BooleanClause.Occur.SHOULD);
            
            // 3. 前缀匹配 (中权重)
            PrefixQuery namePrefixQuery = new PrefixQuery(new Term("name", keyword.toLowerCase()));
            PrefixQuery countryPrefixQuery = new PrefixQuery(new Term("country", keyword.toLowerCase()));
            
            BoostQuery boostedNamePrefixQuery = new BoostQuery(namePrefixQuery, 2.0f);
            BoostQuery boostedCountryPrefixQuery = new BoostQuery(countryPrefixQuery, 1.5f);
            
            queryBuilder.add(boostedNamePrefixQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(boostedCountryPrefixQuery, BooleanClause.Occur.SHOULD);
            
            // 4. 模糊匹配 (低权重)
            FuzzyQuery nameFuzzyQuery = new FuzzyQuery(new Term("name", keyword.toLowerCase()), 2);
            FuzzyQuery countryFuzzyQuery = new FuzzyQuery(new Term("country", keyword.toLowerCase()), 2);
            
            BoostQuery boostedNameFuzzyQuery = new BoostQuery(nameFuzzyQuery, 1.0f);
            BoostQuery boostedCountryFuzzyQuery = new BoostQuery(countryFuzzyQuery, 0.8f);
            
            queryBuilder.add(boostedNameFuzzyQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(boostedCountryFuzzyQuery, BooleanClause.Occur.SHOULD);
            
            // 5. 通配符匹配 (最低权重)
            WildcardQuery nameWildcardQuery = new WildcardQuery(new Term("name", "*" + keyword.toLowerCase() + "*"));
            WildcardQuery countryWildcardQuery = new WildcardQuery(new Term("country", "*" + keyword.toLowerCase() + "*"));
            
            BoostQuery boostedNameWildcardQuery = new BoostQuery(nameWildcardQuery, 0.5f);
            BoostQuery boostedCountryWildcardQuery = new BoostQuery(countryWildcardQuery, 0.3f);
            
            queryBuilder.add(boostedNameWildcardQuery, BooleanClause.Occur.SHOULD);
            queryBuilder.add(boostedCountryWildcardQuery, BooleanClause.Occur.SHOULD);
            
            BooleanQuery query = queryBuilder.build();
            
            // 执行搜索
            TopDocs topDocs = searcher.search(query, page * size);
            
            // 分页处理
            int start = page * size;
            int end = Math.min(start + size, topDocs.scoreDocs.length);
            
            List<Player> players = new ArrayList<>();
            for (int i = start; i < end; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                Player player = documentToPlayer(doc);
                players.add(player);
            }
            
            reader.close();
            
            return PageResponse.of(players, page, size, topDocs.totalHits.value);
            
        } catch (Exception e) {
            log.error("智能搜索失败", e);
            throw new RuntimeException("智能搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Document转换为Player对象
     * @param doc Lucene文档
     * @return Player对象
     */
    private Player documentToPlayer(Document doc) {
        Player player = new Player();
        player.setId(doc.get("ID"));
        player.setName(doc.get("NAME"));
        player.setLocation(doc.get("COUNTRY"));
        player.setAge(doc.get("AGE"));
        player.setKg(doc.get("WEIGHT"));
        return player;
    }

    @Override
    public void destroy(){
        if(this.writer==null){
            return;
        }
        try {
            log.info("索引关闭");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("尝试关闭索引失败");
        }
    }
}
