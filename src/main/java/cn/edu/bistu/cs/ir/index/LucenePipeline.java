package cn.edu.bistu.cs.ir.index;

import cn.edu.bistu.cs.ir.crawler.IjfCrawler;
import cn.edu.bistu.cs.ir.model.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * 基于Lucene的WebMagic Pipeline,
 * 用于将抓取的数据写入本地的Lucene索引
 *
 * @author ruoyuchen
 */
public class LucenePipeline implements Pipeline {

    private static final Logger log = LoggerFactory.getLogger(LucenePipeline.class);

    private final IdxService idxService;

    public LucenePipeline(IdxService idxService) {
        log.info("初始化LucenePipeline模块");
        this.idxService = idxService;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Player player = resultItems.get(IjfCrawler.RESULT_ITEM_KEY);
        if (player == null) {
            log.error("无法从爬取的结果中提取到Player对象");
            return;
        }
        String id = player.getId();
        Document doc = toDoc(player);
        boolean result = idxService.addDocument("ID", id, doc);
        if (!result) {
            log.error("无法将ID为[{}]的柔道家信息写入索引", id);
        } else {
            log.info("成功将ID为[{}]的柔道家信息写入索引", id);
        }
    }

    private Document toDoc(Player player) {
        Document document = new Document();
        // 页面ID
        document.add(new StringField("ID", player.getId(), Field.Store.YES));
        // 姓名
        document.add(new TextField("NAME", player.getName(), Field.Store.YES));
        // 年龄
        document.add(new TextField("AGE", player.getAge(), Field.Store.YES));
        // 照片 URL
        document.add(new TextField("IMAGE", player.getImage(), Field.Store.YES));
        // 地区
        document.add(new TextField("LOCATION", player.getLocation(), Field.Store.YES));
        // 地区 Icon
        document.add(new TextField("LOCATION_ICON", player.getLocationIcon(), Field.Store.YES));
        // 公斤数
        document.add(new TextField("KG", player.getKg(), Field.Store.YES));

        // 照片
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(player.getPhotoEntity());
            document.add(new TextField("PHOTOS", json, Field.Store.YES));
        } catch (JsonProcessingException e) {
            log.error("序列化为 JSON 失败");
        }


        return document;
    }
}
