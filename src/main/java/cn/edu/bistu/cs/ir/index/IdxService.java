package cn.edu.bistu.cs.ir.index;

import cn.edu.bistu.cs.ir.config.Config;
import cn.edu.bistu.cs.ir.utils.StringUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
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
import java.util.Locale;

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
        if(writer == null || doc == null){
            log.error("Writer对象或文档对象为空，无法添加文档到索引中");
            return false;
        }
        if(StringUtil.isEmpty(idFld) || StringUtil.isEmpty(id)){
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

    public List<Document> queryByParams(String kw, String age, String kg, String location) throws Exception {
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();

        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        if (kw != null && !kw.trim().isEmpty()) {
            QueryParser parser = new QueryParser("NAME", analyzer);
            Query nameQuery = parser.parse(kw);
            builder.add(nameQuery, BooleanClause.Occur.MUST);
        }

        if (age != null && !age.trim().isEmpty()) {
            builder.add(new TermQuery(new Term("AGE", age)), BooleanClause.Occur.MUST);
        }

        if (kg != null && !kg.trim().isEmpty()) {
            builder.add(new TermQuery(new Term("KG", kg)), BooleanClause.Occur.MUST);
        }

        if (location != null && !location.trim().isEmpty()) {
            builder.add(new TermQuery(new Term("LOCATION", location.toLowerCase())), BooleanClause.Occur.MUST);
        }

        Query query = builder.build();
        TopDocs docs = searcher.search(query, 100); // 返回前100个结果
        List<Document> results = new ArrayList<>();
        for (ScoreDoc doc : docs.scoreDocs) {
            results.add(searcher.doc(doc.doc));
        }

        return results;
    }

    @Override
    public void destroy() {
        if(this.writer == null) return;
        try {
            log.info("索引关闭");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("尝试关闭索引失败");
        }
    }

    public List<Document> queryByKw(String kw) {

        return List.of();
    }
}
