# 国际柔道联盟爬虫接口文档

##  接口功能说明

该接口通过爬虫从 [国际柔道联盟官网](https://www.ijf.org/judoka) 获取全球柔道运动员信息，包括基本资料、国家、年龄、体重级别、照片等详细信息。

---

## 输出字段结构（爬虫数据模型）

```json
{
  "id": "12345",
  "name": "Teddy Riner",
  "age": "34",
  "image": "https://cdn.ijf.org/photos/athletes/123456.jpg",
  "location": "France",
  "locationIco": "https://cdn.ijf.org/images/flags/fra.png",
  "kg": "+100 kg",
  "photoEntity": {
    "underTheSpotlights": [
      {
        "title": "IJF World Tour",
        "link": "https://cdn.ijf.org/photos/highlight1.jpg"
      },
      {
        "title": "Tokyo Grand Slam",
        "link": "https://cdn.ijf.org/photos/highlight2.jpg"
      }
    ],
    "photos": [
      {
        "title": "World Judo Championships",
        "link": "https://cdn.ijf.org/photos/match1.jpg"
      },
      {
        "title": "Paris Grand Slam",
        "link": "https://cdn.ijf.org/photos/match2.jpg"
      }
    ]
  }
}
