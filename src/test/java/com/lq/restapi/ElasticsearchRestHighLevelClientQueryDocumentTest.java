package com.lq.restapi;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.lq.entity.Product;
import org.apache.commons.lang.ArrayUtils;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @program: elasticsearch-api
 * @pageName com.lq
 * @className ElasticsearchRestHighLevelClientQueryDocumentTest
 * @description: Elasticsearch Rest高级客户端
 * @author: liqiang
 * @create: 2023-10-09 15:07
 **/
@SpringBootTest
class ElasticsearchRestHighLevelClientQueryDocumentTest {


    @Autowired
    private  RestHighLevelClient restHighLevelClient ;

    private final static String INDEX_NAME = "product";


    /**
     * 执行es查询
     *
     * @param indexName
     * @param beanClass
     * @param list
     * @param searchSourceBuilder
     * @param <T>
     * @throws IOException
     */
    private <T> void queryEsData(String indexName, Class<T> beanClass, List<T> list, SearchSourceBuilder searchSourceBuilder) throws IOException {
        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);

        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                // 将 JSON 转换成对象
                //Product userInfo = JSON.parseObject(hit.getSourceAsString(), Product.class);
                // 将 JSON 转换成对象
                T bean = JSON.parseObject(hit.getSourceAsString(), beanClass);
                list.add(bean);
            }
        }

        System.out.println(searchSourceBuilder.query());
        list.forEach(System.out::println);
    }

    /**
     * term查询：不会分析查询条件，只有当词条和查询字符串完全匹配时才匹配，也就是精确查找，比如数字，日期，布尔值或 not_analyzed 的字符串(未经分析的文本数据类型)
     * terms查询：terms 跟 term 有点类似，但 terms 允许指定多个匹配条件。 如果某个字段指定了多个值，那么文档需要一起去 做匹配
     */


    /**
     * 精准查询
     *
     * @throws IOException
     */
    @Test
    void testTermQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 构建查询条件（注意：termQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("brandName", "华为"));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 多个查询内容在一个字段中进行查询
     *
     * @throws IOException
     */
    @Test
    void testTermsQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 构建查询条件（注意：termQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] args = {"华为", "Apple"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("brandName", args));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 全文查询会分析查询条件，先将查询条件进行分词，然后查询，求并集。
     * term和match的区别是：match是经过analyer的，也就是说，文档首先被分析器给处理了。根据不同的分析器，分析的结果也稍显不同，然后再根据分词结果进行匹配。
     * term则不经过分词，它是直接去倒排索引中查找了精确的值了。
     */

    /**
     * 匹配查询符合条件的所有数据，并设置分页
     *
     * @throws IOException
     */
    @Test
    void testMatchAllQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询条件
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 设置分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(2);

        List<String> orderList = Arrays.asList("-price", "-saleNum");
        // 设置排序
        for (String order : orderList) {
            // -开头代表：倒序
            boolean flag = order.startsWith("-");
            SortOrder sort = flag ? SortOrder.DESC : SortOrder.ASC;
            order = flag ? order.substring(1) : order;
            searchSourceBuilder.sort(order, sort);
        }
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 匹配查询数据
     *
     * @throws IOException
     */
    @Test
    void testMatchQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("brandName", "华为"));

        List<String> orderList = Arrays.asList("price", "saleNum");
        // 设置排序
        for (String order : orderList) {
            // -开头代表：倒序
            boolean flag = order.startsWith("-");
            SortOrder sort = flag ? SortOrder.DESC : SortOrder.ASC;
            order = flag ? order.substring(1) : order;
            searchSourceBuilder.sort(order, sort);
        }
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 词语匹配查询
     *
     * @throws IOException
     */
    @Test
    void testMatchPhraseQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询条件
        searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("title", "256GB"));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 前缀查询，根据短语中最后一个词组做前缀匹配
     *
     * @throws IOException
     */
    @Test
    void testMatchPhrasePrefixQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询条件
        searchSourceBuilder.query(QueryBuilders.matchBoolPrefixQuery("title", "华为"));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 内容在多字段中进行查询
     *
     * @throws IOException
     */
    @Test
    void testMatchMultiQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询条件

        String[] fields = {"title", "categoryName"};

        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("手机", fields));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 通配符查询(wildcard)：会对查询条件进行分词。还可以使用通配符 ?（任意单个字符） 和 * （0个或多个字符）
     * <p>
     * *：表示多个字符（0个或多个字符）
     * ?：表示单个字符
     */
    @Test
    void testWildcardQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.wildcardQuery("brandName", "*为"));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    @Test
    void testFuzzyQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //fuzziness允许的编辑距离，写错两个字符，设置可以编辑两步  Apple bpale
        searchSourceBuilder.query(QueryBuilders.fuzzyQuery("brandName", "bpale").fuzziness(Fuzziness.TWO));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * 通过正则 匹配某个字段的值
     *
     * @throws IOException
     */
    @Test
    void testRegexpQuery() throws IOException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.regexpQuery("brandName", "[^\u4E00-\u9FA5]+"));
        //执行查询
        this.queryEsData(INDEX_NAME, Product.class, list, searchSourceBuilder);
    }

    /**
     * boolQuery 查询
     * 高亮展示标题搜索字段
     * 设置出参返回字段
     *
     * @throws IOException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    @Test
    void testBoolQuery() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<Product> list = new ArrayList<>();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //匹配
        boolQuery.must(QueryBuilders.matchQuery("brandName", "华为"));
        //过滤
        boolQuery.mustNot(QueryBuilders.matchQuery("title", "mate60"));
        //should  可能包含
        boolQuery.should(QueryBuilders.matchQuery("price", 2999)).minimumShouldMatch(1);
        //使用filter 过滤数据
        boolQuery.filter(QueryBuilders.matchQuery("categoryName", "手表"));

        searchSourceBuilder.query(boolQuery);

        // 甚至返回字段
        // 如果查询的属性很少，那就使用includes，而excludes设置为空数组
        // 如果排序的属性很少，那就使用excludes，而includes设置为空数组
        String[] includes = {};
        String[] excludes = {};
        searchSourceBuilder.fetchSource(includes, excludes);

        //执行查询
        this.queryEsHighlightData(INDEX_NAME, Product.class, list, searchSourceBuilder, new String[]{"categoryName", "brandName"});
        //this.queryEsHighlightData(INDEX_NAME, Product.class, list, searchSourceBuilder, new String[]{});
    }


    /**
     * 执行es高亮查询
     *
     * @param indexName
     * @param beanClass
     * @param list
     * @param searchSourceBuilder
     * @param <T>
     * @throws IOException
     */
    private <T> void queryEsHighlightData(String indexName, Class<T> beanClass, List<T> list, SearchSourceBuilder searchSourceBuilder, String[] highFields) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));

        // 高亮设置
        // 设置高亮三要素:  field: 你的高亮字段 , preTags ：前缀    , postTags：后缀
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮查询字段
        if (ArrayUtils.isNotEmpty(highFields)) {
            for (String field : highFields) {
                highlightBuilder.field(field);
            }
        }
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");

        //如果要多个字段高亮,这项要为false
        highlightBuilder.requireFieldMatch(false);
        searchSourceBuilder.highlighter(highlightBuilder);

        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchSourceBuilder.query());
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                // 将 JSON 转换成对象
                //Product userInfo = JSON.parseObject(hit.getSourceAsString(), Product.class);
                // 将 JSON 转换成对象
                T bean = JSON.parseObject(hit.getSourceAsString(), beanClass);
                System.out.println(hit.getSourceAsMap());
                System.out.println(hit.getHighlightFields());
                //打印高亮结果
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (MapUtil.isNotEmpty(highlightFields)) {
                    for (Map.Entry<String, HighlightField> next : highlightFields.entrySet()) {
                        HighlightField highlightField = next.getValue();
                        String highlightFieldName = next.getKey();

                        // 替换掉原来的数据
                        Text[] fragments = highlightField.getFragments();
                        if (fragments != null && fragments.length > 0) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Text fragment : fragments) {
                                stringBuilder.append(fragment);
                            }
                            // 获取method对象，其中包含方法名称和参数列表
                            highlightFieldName = highlightFieldName.substring(0, 1).toUpperCase() + highlightFieldName.substring(1);
                            Method setName = beanClass.getMethod("set" + highlightFieldName, String.class);
                            // 执行method，bean为实例对象，后面是方法参数列表；setName 没有返回值
                            setName.invoke(bean, stringBuilder.toString());
                        }
                    }
                }
                list.add(bean);
            }
        }
        System.out.println(searchSourceBuilder.query());
        list.forEach(System.out::println);
    }


    /**
     * 获取所有商品价格的指标
     * 聚合查询 : 聚合查询一定是【先查出结果】，然后对【结果使用聚合函数】做处理.
     *
     * @throws IOException
     */
    @Test
    void testMetricQuery() throws IOException {
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        StatsAggregationBuilder aggregationBuilder = AggregationBuilders.stats("priceBucket").field("price");
        searchSourceBuilder.aggregation(aggregationBuilder);

        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.toString());
        System.out.println(searchSourceBuilder.query());
        Aggregations aggregations = searchResponse.getAggregations();

        Map<String, Aggregation> asMap = aggregations.getAsMap();

        ParsedStats parsedStats = (ParsedStats) asMap.get("priceBucket");

        System.out.println("max -->" + parsedStats.getMax());
        System.out.println("min -->" + parsedStats.getMin());
        System.out.println("sum -->" + parsedStats.getSum());
        System.out.println("avg -->" + parsedStats.getAvg());
    }


    /**
     * 用于集合查询 聚合查询一定是【先查出结果】，然后对【结果使用聚合函数】做处理.
     * Bucket 分桶聚合分析 : 对查询出的数据进行分组group by，再在组上进行游标聚合
     *
     * @throws IOException
     */
    @Test
    void testBucketQuery() throws IOException {
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("brandNameBucket").field("brandName.keyword");
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.toString());
        System.out.println(searchSourceBuilder.query());
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedStringTerms parsedStringTerms = aggregations.get("brandNameBucket"); // 分组结果数据
        for (Terms.Bucket bucket : parsedStringTerms.getBuckets()) {
            System.out.println(bucket.getKeyAsString() + "====" + bucket.getDocCount());
        }
    }


    /**
     * 先分组之后 然后再计算指标
     *
     * @throws IOException
     */
    @Test
    void testSubBucketQuery() throws IOException {
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("brandBucket").field("brandName");
        StatsAggregationBuilder statsAggregationBuilder = AggregationBuilders.stats("priceBucket").field("price");
        termsAggregationBuilder.subAggregation(statsAggregationBuilder);
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.toString());
        Aggregations aggregations = searchResponse.getAggregations();
        // 分组结果数据
        ParsedStringTerms parsedStringTerms = aggregations.get("brandBucket");
        for (Terms.Bucket bucket : parsedStringTerms.getBuckets()) {
            // 获取聚合后的 组内字段平均值,注意返回值不是Aggregation对象,而是指定的 priceBucket 对象
            ParsedStats parsedStats = bucket.getAggregations().get("priceBucket");
            System.out.println(bucket.getKeyAsString() + "====" + bucket.getDocCount());
            System.out.println("max -->" + parsedStats.getMax());
            System.out.println("min -->" + parsedStats.getMin());
            System.out.println("sum -->" + parsedStats.getSum());
            System.out.println("avg -->" + parsedStats.getAvg());
        }
    }

    /**
     * 综合聚合查询
     * 先分组->计算指标->再分组
     *
     * @throws IOException
     */
    @Test
    void testSubSubAgg() throws IOException {
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        TermsAggregationBuilder categoryBucket = AggregationBuilders.terms("categoryBucket").field("categoryName");
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("brandBucket").field("brandName");

        ExtendedStatsAggregationBuilder extendedStatsAggregationBuilder = AggregationBuilders.extendedStats("categoryStatsBucket").field("price");
        ExtendedStatsAggregationBuilder aggregationBuilder = AggregationBuilders.extendedStats("brandStatsBucket").field("price");

        categoryBucket.subAggregation(extendedStatsAggregationBuilder);
        categoryBucket.subAggregation(termsAggregationBuilder);
        termsAggregationBuilder.subAggregation(aggregationBuilder);
        searchSourceBuilder.aggregation(categoryBucket);

        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(searchSourceBuilder);
        // 执行查询，然后处理响应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.toString());

        System.out.println(searchSourceBuilder.query());
        //获取总记录数
        System.out.println("totalHits = " + searchResponse.getHits().getTotalHits());
        // 获取聚合信息
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedStringTerms categoryNameAgg = aggregations.get("categoryBucket");

        //获取值返回
        for (Terms.Bucket bucket : categoryNameAgg.getBuckets()) {
            // 获取聚合后的分类名称
            String categoryName = bucket.getKeyAsString();
            // 获取聚合命中的文档数量
            long docCount = bucket.getDocCount();
            // 获取聚合后的分类的平均价格,注意返回值不是Aggregation对象,而是指定的   categoryStatsBucket 对象
            ParsedExtendedStats extendedStats = bucket.getAggregations().get("categoryStatsBucket");

            System.out.println(categoryName + "====== max -->" + extendedStats.getMax() + "======数量:" + docCount);
            System.out.println(categoryName + "====== min -->" + extendedStats.getMin() + "======数量:" + docCount);
            System.out.println(categoryName + "====== sum -->" + extendedStats.getSum() + "======数量:" + docCount);
            System.out.println(categoryName + "====== avg -->" + extendedStats.getAvg() + "======数量:" + docCount);

            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brandBucket");
            for (Terms.Bucket brandeNameAggBucket : brandNameAgg.getBuckets()) {
                // 获取聚合后的品牌名称
                ParsedExtendedStats brandStatsBucket = (ParsedExtendedStats) brandeNameAggBucket.getAggregations().asMap().get("brandStatsBucket");
                System.out.println("  品牌-->" + brandeNameAggBucket.getKeyAsString() + "最大价格-->" + brandStatsBucket.getMax() + "最小价格-->" + brandStatsBucket.getMin());
            }
        }
    }

    /**
     * 根据查询条件滚动查询
     * 可以用来解决深度分页查询问题
     */
    @Test
    void testScrollQuery() {

        // 假设用户想获取第2页数据，其中每页1条
        int pageNo = 2;
        int pageSize = 2;

        // 定义请求对象
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        // 构建查询条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        searchRequest.source(builder.query(QueryBuilders.matchAllQuery()).size(pageSize));
        //滚动Id
        String scrollId = null;

        //发送请求到ES
        SearchResponse scrollResponse = null;

        //设置游标Id 存货时间
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(2));

        //记录所有游标Id
        ArrayList<String> scrollIds = new ArrayList<>();

        for (int i = 0; i < pageNo; i++) {
            try {
                //首次检索
                if (i == 0) {
                    //记录游标Id
                    searchRequest.scroll(scroll);
                    //首次查询需要指定索引名和查询条件
                    SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                    //下一次搜索要用到的该游标Id
                    scrollId = response.getScrollId();
                    //非首次检索
                } else {
                    // 不需要在使用其他条件，也不需要指定索引名称，只需要使用执行游标id存活时间和上次游标id即可，毕竟信息都在上次游标id里面
                    SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
                    searchScrollRequest.scroll(scroll);
                    scrollResponse = restHighLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
                    // 下一次搜索要用到该游标id
                    scrollId = scrollResponse.getScrollId();
                }
                scrollIds.add(scrollId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //清楚游标Id
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.scrollIds(scrollIds);

        try {
            restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //处理相应结果
        System.out.println("滚动查询返回数据：");
        if (scrollResponse == null) {
            return;
        }

        SearchHits hits = scrollResponse.getHits();
        for (SearchHit hit : hits) {
            // 将 JSON 转换成对象
            Product Product = JSON.parseObject(hit.getSourceAsString(), Product.class);
            // 输出查询信息
            System.out.println(Product.toString());
        }
    }

    /**
     * 不支持向前搜索,每次只能向后搜索1页数据
     * @throws IOException
     */
    @Test
    void testSearchAfterQuery() throws IOException {

        int pageSize = 2;
        String indexName = "product";
        String price = "price";
        String id = "_id";
        // 定义请求对象
        SearchRequest searchRequest = new SearchRequest(indexName);
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchRequest.source(searchSourceBuilder.query(QueryBuilders.matchAllQuery()).sort(price, SortOrder.ASC).sort(id, SortOrder.ASC));
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.toString());
        System.out.println(searchSourceBuilder.query());

        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            // 将 JSON 转换成对象
            Product product = JSON.parseObject(hit.getSourceAsString(), Product.class);
            // 输出查询信息
            System.out.println(product.toString());
        }

        SearchRequest searchRequest2 = new SearchRequest(indexName);
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
        searchRequest2.source(searchSourceBuilder2.query(QueryBuilders.matchAllQuery()).size(pageSize).sort(price, SortOrder.ASC).sort(id, SortOrder.ASC).searchAfter(new Object[]{999, "4"}));
        SearchResponse searchResponse2 = restHighLevelClient.search(searchRequest2, RequestOptions.DEFAULT);
        System.out.println(searchResponse2.toString());
        System.out.println(searchSourceBuilder2.query());

        SearchHits hits2 = searchResponse2.getHits();
        for (SearchHit hit : hits2) {
            // 将 JSON 转换成对象
            Product product = JSON.parseObject(hit.getSourceAsString(), Product.class);
            // 输出查询信息
            System.out.println(product.toString());
        }
    }

}
