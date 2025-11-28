# Elasticsearch API 项目文档

## 项目简介

本项目是一个基于 Spring Boot 的 Elasticsearch 集成示例项目，整合了三种主流的 Elasticsearch 客户端操作方式：

1. **Transport Client** - 传统的传输客户端
2. **Rest High Level Client** - 高级 REST 客户端
3. **Spring Data Elasticsearch** - Spring Data 对 Elasticsearch 的封装

项目提供了对地理空间类型（GeoPoint 和 GeoShape）的完整 CRUD 操作与查询功能，包含详细的测试用例。

## 技术栈

- Java 8
- Spring Boot 2.5.14
- Elasticsearch 7.12.1
- Maven 3.x
- MySQL 8.0.32 (用于部分数据源测试)
- MyBatis-Plus 3.4.0
- Lombok 1.18.24
- FastJSON 1.2.28
- Hutool 5.8.5

## 项目结构

```
src/
├── main/
│   ├── java/com/lq/
│   │   ├── common/          # 通用工具类和异常处理
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器层
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── mapper/          # 数据访问层(MyBatis)
│   │   ├── service/         # 服务层
│   │   ├── util/            # 工具类
│   │   └── ElasticsearchApiApplication.java  # 启动类
│   └── resources/
│       ├── mapper/          # MyBatis XML 映射文件
│       └── application.yml  # 配置文件
└── test/
    ├── restapi/             # Rest High Level Client 测试
    ├── springdataapi/       # Spring Data Elasticsearch 测试
    └── transport/           # Transport Client 测试
```

## 功能特性

### 1. 多种客户端实现

#### Transport Client
- 位于 [transport](src/test/java/com/lq/transport/) 包下
- 实现基础的增删改查操作
- 包含各种查询方式：全文搜索、范围查询、聚合查询等

#### Rest High Level Client
- 位于 [restapi](src/test/java/com/lq/restapi/) 包下
- 更现代化的 RESTful API 调用方式
- 支持索引管理、文档操作、复杂查询
- 包含地理空间查询(GeoPoint、GeoShape)

#### Spring Data Elasticsearch
- 位于 [springdataapi](src/test/java/com/lq/springdataapi/) 包下
- 基于 Spring Data 的 Repository 模式
- 提供更简洁的操作接口
- 自动映射索引结构

### 2. 地理空间功能

#### GeoPoint
- 点位置数据类型支持
- 支持矩形查询(geo_bounding_box)
- 支持半径查询(geo_distance)
- 支持多边形查询(geo_polygon)
- 支持距离排序

#### GeoShape
- 复杂几何形状支持
- 支持点、线、面、圆等多种几何类型
- 支持多种空间关系查询(WITHIN、CONTAINS、INTERSECTS)

### 3. 文档操作

- 单文档增删改查
- 批量操作(Bulk API)
- 更新部分字段
- 文档版本控制

### 4. 查询功能

- 全文搜索(match/multi_match)
- 精确匹配(term/terms)
- 范围查询(range)
- 布尔查询(bool)
- 聚合查询(aggregations)
- 高亮显示(highlight)
- 分页查询(from/size)
- 排序(sort)

## 配置说明

### 应用配置 (application.yml)

```yaml
# Elasticsearch 配置
elasticsearch:
  clusterName: single-node-cluster  # 集群名称
  userName: elastic                 # 用户名
  password: elastic                 # 密码
  hosts: 127.0.0.1:9200            # ES 地址(单节点)
  scheme: http                     # 协议
  connectTimeOut: 1000             # 连接超时(ms)
  socketTimeOut: 30000             # Socket超时(ms)
  connectionRequestTimeOut: 500    # 请求超时(ms)
  maxConnectNum: 100               # 最大连接数
  maxConnectNumPerRoute: 100       # 每路由最大连接数
```

### 数据库配置

```yaml
# MySQL 数据源配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root
    url: jdbc:mysql://127.0.0.1:3306/es_test?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
```

## 快速开始

### 环境准备

1. 安装 JDK 8+
2. 安装 Maven 3.x
3. 启动 Elasticsearch 7.12.1
4. 启动 MySQL 8.0+ 并创建数据库 `es_test`

### 项目启动

```bash
# 克隆项目
git clone <项目地址>

# 进入项目目录
cd elasticsearch-api

# 编译项目
mvn clean compile

# 运行测试
mvn test
```

### 核心测试类说明

#### Transport Client 测试
- [ElasticsearchCRUDTest](src/test/java/com/lq/transport/ElasticsearchCRUDTest.java) - 文档基本操作
- [ElasticsearchQueryTest](src/test/java/com/lq/transport/ElasticsearchQueryTest.java) - 各种查询操作

#### Rest High Level Client 测试
- [ElasticsearchRestHighLevelClientIndexTest](src/test/java/com/lq/restapi/ElasticsearchRestHighLevelClientIndexTest.java) - 索引操作
- [ElasticsearchRestHighLevelClientDocumentTest](src/test/java/com/lq/restapi/ElasticsearchRestHighLevelClientDocumentTest.java) - 文档操作
- [ElasticsearchGeoPointQueryTest](src/test/java/com/lq/restapi/geo/ElasticsearchGeoPointQueryTest.java) - GeoPoint 查询
- [ElasticsearchGeoShapeQueryTest](src/test/java/com/lq/restapi/geo/ElasticsearchGeoShapeQueryTest.java) - GeoShape 查询

#### Spring Data Elasticsearch 测试
- [ElasticsearchRestHighLevelDocumentTest](src/test/java/com/lq/springdataapi/ElasticsearchRestHighLevelDocumentTest.java) - 文档操作
- [ElasticsearchRestHighLevelQueryDocumentTest](src/test/java/com/lq/springdataapi/ElasticsearchRestHighLevelQueryDocumentTest.java) - 查询操作

## 使用示例

### 添加文档

```java
// 使用 Rest High Level Client
Goods goods = new Goods();
goods.setId(1L);
goods.setTitle("Apple iPhone 14 Pro");
goods.setPrice(new BigDecimal("8799.00"));

IndexRequest request = new IndexRequest("goods").id(goods.getId().toString())
    .source(JSON.toJSONString(goods), XContentType.JSON);
IndexResponse response = client.index(request, RequestOptions.DEFAULT);
```

### 查询文档

```java
// 使用 Spring Data Elasticsearch
@Autowired
private ElasticsearchRepository<Goods, Long> goodsRepository;

// 精确查询
List<Goods> goodsList = goodsRepository.findByBrandName("华为");
```

### 地理位置查询

```java
// GeoPoint 距离查询
GeoDistanceQueryBuilder queryBuilder = QueryBuilders
    .geoDistanceQuery("location")
    .distance("3km")
    .point(new GeoPoint(40.174697, 116.5864));

SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
sourceBuilder.query(queryBuilder);
```

## 注意事项

1. 本项目使用 Elasticsearch 7.12.1 版本，请确保服务端版本兼容
2. Transport Client 在 Elasticsearch 8.0 后已被废弃，仅作学习参考
3. 生产环境中建议使用 Rest High Level Client 或 Spring Data Elasticsearch
4. 地理位置查询需要在索引映射中正确配置 geo_point 或 geo_shape 类型
5. 批量操作时注意内存使用情况，合理设置批次大小

## 许可证

本项目仅供学习交流使用。