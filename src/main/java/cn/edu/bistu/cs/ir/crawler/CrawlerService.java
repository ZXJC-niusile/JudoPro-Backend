package cn.edu.bistu.cs.ir.crawler;

import cn.edu.bistu.cs.ir.config.Config;
import cn.edu.bistu.cs.ir.index.IdxService;
import cn.edu.bistu.cs.ir.index.LucenePipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.scheduler.PriorityScheduler;

import javax.annotation.PostConstruct;

import static us.codecraft.webmagic.Spider.Status.Stopped;

/**
 * 面向爬虫的服务类
 * @author zhaxijiancuo
 */
@Component
public class CrawlerService{

    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private final Config config;

    private final IdxService idxService;

    public CrawlerService(@Autowired Config config,
                          @Autowired IdxService idxService){
        this.config = config;
        this.idxService = idxService;
    }

    private Spider spider = null;


    /**
     * 启动面向国际柔道联合会（IJF）的赛事数据爬虫
     */
    public void startIjfCrawler() {
        String startPage = "https://www.ijf.org/judoka";

        if(this.spider != null){
            if(!Stopped.equals(this.spider.getStatus())){
                // 如果spider成员不为空，并且状态不是 Stopped，则不可以启动新的爬虫
                log.error("当前有正在运行的爬虫对象，不可以创建新的爬虫");
                return;
            }
        }
        Site site = Site
                .me()
                .setRetryTimes(config.getRetryTimes())
                .setSleepTime(config.getSleepTime())
                .setUserAgent(config.getAgent());
        this.spider = Spider.create(new IjfCrawler(site));
        spider.addPipeline(new LucenePipeline(idxService));
        spider.addPipeline(new JsonFilePipeline(config.getCrawler()));
        spider.setScheduler(new PriorityScheduler());
        spider.thread(1);
        spider.addUrl(startPage);
        spider.runAsync();
        log.info("面向国际柔道联合会（IJF）的爬虫任务已异步启动");
    }

    @PostConstruct
    public void init(){
        if(config.isStartCrawler()){
            log.info("系统配置 [startCrawler] 为 true，自动启动 IJF 爬虫任务");
            startIjfCrawler();
        }
    }
    /**
     * 兼容旧版命名方法
     * @deprecated 请使用 {@link #startIjfCrawler()}
     */
    @Deprecated
    public void startCnBlogCrawler(String blogger) {
        startIjfCrawler();
    }
}
