package cn.edu.bistu.cs.ir.crawler;


import cn.edu.bistu.cs.ir.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

/**
 * 面向国际柔道联盟（<a href="https://www.ijf.org/">国际柔道联盟</a>）的爬虫
 */
public class IjfCrawler implements PageProcessor {

    private final Site site;

    private static final Logger log = LoggerFactory.getLogger(IjfCrawler.class);

    public static final String RESULT_ITEM_KEY = "BLOG_INFO";

    private static final String JUDOKA = "https://www.ijf.org/judoka";


    public IjfCrawler(Site site) {
        this.site = site;
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("uuuu-MM-dd HH:mm");

    @Override
    public void process(Page page) {
        // url里存储的是请求页面的URL地址
        String url = page.getRequest().getUrl();

        if (Objects.equals(JUDOKA, url)) {
            log.info("解析地区代码页[{}]", url);
            List<String> locations = page.getHtml().xpath("//div[@class='component--filters']//select[@name='nation']/option/@value").all();
            log.info("获取地区代码共[{}]个", locations.size());

            for (String location : locations) {
                String req = String.format("https://www.ijf.org/judoka?nation=%s&gender=both&category=all", location);

                Request lowPriorityRequest = new Request(req);
                lowPriorityRequest.setPriority(5);

                page.addTargetRequest(lowPriorityRequest);
            }

            page.setSkip(true);
            System.out.println(locations);
        } else if (url.startsWith(JUDOKA + "?nation")) {
            log.info("解析柔道家列表页[{}]", url);
            List<String> judokas = page.getHtml().xpath("//div[@class='page-content']//div[@class='results-section']//a/@href").all();
            log.info("获取柔道家网页共[{}]个", judokas.size());

            for (String judoka : judokas) {
                String req = String.format("https://www.ijf.org/judoka/%s", judoka.replace("/judoka/", ""));

                Request highPriorityRequest = new Request(req);
                highPriorityRequest.setPriority(1);

                page.addTargetRequest(highPriorityRequest);
            }

            page.setSkip(true);
        } else if (url.startsWith(JUDOKA + "/")) {
            log.info("解析柔道家详情页[{}]", url);

            String name = page.getHtml().xpath("//div[@class='athlete-title-hero']/text()").get();
            String age = page.getHtml().xpath("//div[@class='age-info']/text()").get();
            if (age != null) {
                age = age.replaceAll("[^0-9]", "");
            }
            String image = page.getHtml().xpath("//div[@class='pic-big']/@style").get().replace("background-image: url(", "").replace(");", "");
            String location = page.getHtml().xpath("//div[@class='location']/text()").get();
            String locationIco = page.getHtml().xpath("//div[@class='location']//img[@class='country-ico']/@src").get();
            String kg = page.getHtml().xpath("//div[@class='kg']/text()").get();

            Player player = new Player(url.replace("https://www.ijf.org/judoka/", ""),
                    name == null ? "未获取到名字" : name.trim(),
                    age == null ? "未获取到年龄" : age.trim(),
                    image.isEmpty() ? "未提供照片" : image.trim(),
                    location == null ? "未提供地区" : location.trim(),
                    locationIco == null ? "未提供地区Icon" : locationIco.trim(),
                    kg == null ? "未提供公斤数" : kg.trim());

            System.out.println("解析出的柔道家信息 = " + player);

            page.putField(RESULT_ITEM_KEY, player);
        } else {
            log.warn("暂不支持的URL地址:[{}]", url);
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
