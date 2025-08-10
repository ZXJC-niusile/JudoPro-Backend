# 工程说明

本工程在 [ZXJC-niusile/ir_ijf](https://github.com/ZXJC-niusile/ir_ijf) 和 [ir_demo](https://github.com/ruoyu-chen/ir_demo) 的基础上实现了对 [IJF](https://www.ijf.org/) 的适配

本项目是一个基于Spring Boot + Lucene + WebMagic的柔道运动员信息检索系统，实现了从国际柔道联盟官网爬取运动员数据、建立Lucene索引、提供多种检索方式等功能。
**仅用于学习和研究使用！**

# 软件包说明

开发语言：Java 11  
第三方库依赖：  
（1）Lucene(8.11.1，用于构建索引和执行检索)  
（2）WebMagic(0.7.6，用于编写爬虫)  
（3）HanLP（portable-1.8.3，用于索引和检索过程中的分词）