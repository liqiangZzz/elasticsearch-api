package com.lq.transport;

import com.lq.util.ElasticsearchClientUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @program: elasticsearch-api
 * @pageName com.lq
 * @className ElasticsearchQueryTest
 * @description:
 * @author: liqiang
 * @create: 2023-09-27 17:08
 **/
@SpringBootTest
public class ElasticsearchQueryTest {

    private static final String INDEX = "my_product";

    private static final String DOC = "_doc";

    @Test
    void testQueryAllDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits().value);
            System.out.println("MaxScore: " + hits.getMaxScore());

            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                //输出每条查询的结果信息
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testQueryAllFilterDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());

            //查询字段过滤
            String[] excludes = {"create_time"};
            String[] includes = {"name", "type", "price", "tags", "create_time"};
            searchSourceBuilder.fetchSource(includes, excludes);

            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits());
            System.out.println("MaxScore: " + hits.getMaxScore());

            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                //输出每条查询的结果信息
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testQueryBoolDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // term 精准匹配
            // boolQuery.must(QueryBuilders.termQuery("name.keyword","小米手机"));
            //boolQuery.mustNot(QueryBuilders.termQuery("name.keyword","红米耳机"));

            //match 小米手机 会被分词
            boolQuery.must(QueryBuilders.matchQuery("name", "小米手机"));
            //因为会分词，强制过滤  红米耳机
            boolQuery.mustNot(QueryBuilders.matchQuery("name.keyword", "红米耳机"));

            //should  可能包含
            boolQuery.should(QueryBuilders.matchQuery("price", 5000));

            //使用filter 过滤数据
            boolQuery.filter(QueryBuilders.matchQuery("name","手机"));

            searchSourceBuilder.query(boolQuery);
            searchRequest.source(searchSourceBuilder);
            System.out.println();
            System.out.println(searchSourceBuilder.query());
            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();
            System.out.println();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits());
            System.out.println("MaxScore: " + hits.getMaxScore());

            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                //输出每条查询的结果信息
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testQueryRangeDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");

            rangeQuery.gte("2000");
            rangeQuery.lte("5000");
            searchSourceBuilder.query(rangeQuery);
            searchRequest.source(searchSourceBuilder);
            System.out.println();
            System.out.println(searchSourceBuilder.query());
            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();
            System.out.println();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits());
            System.out.println("MaxScore: " + hits.getMaxScore());

            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                //输出每条查询的结果信息
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testQueryFuzzinessDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("name", "暗示米").fuzziness(Fuzziness.TWO);

            searchSourceBuilder.query(fuzzyQueryBuilder);
            searchRequest.source(searchSourceBuilder);
            System.out.println();
            System.out.println(searchSourceBuilder.query());
            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();
            System.out.println();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits());
            System.out.println("MaxScore: " + hits.getMaxScore());

            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                //输出每条查询的结果信息
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testQueryHighlightDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "小米");

            searchSourceBuilder.query(matchQueryBuilder);

            // 构建查询方式：高亮查询
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<font color='red'>");//设置标签前缀
            highlightBuilder.postTags("</font>");//设置标签后缀
            highlightBuilder.field("name");//设置高亮字段
            searchSourceBuilder.highlighter(highlightBuilder);
            // 设置请求体
            searchRequest.source(searchSourceBuilder);
            System.out.println();
            System.out.println(searchSourceBuilder.query());
            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();
            System.out.println();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits());
            System.out.println("MaxScore: " + hits.getMaxScore());

            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                //输出每条查询的结果信息
                System.out.println(hit.getSourceAsString());
                //打印高亮结果
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                System.out.println(highlightFields);
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testQueryAggDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("type_groupby").field("type.keyword");
            searchSourceBuilder.aggregation(aggregationBuilder);

            // 设置请求体
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();

            System.out.println();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits());
            System.out.println("MaxScore: " + hits.getMaxScore());

            Iterator<Map.Entry<String, Aggregation>> iterator = searchResponse.getAggregations().asMap().entrySet().stream().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Aggregation> next = iterator.next();
                System.out.println(next.getKey() + "--->" + next.getValue());
            }

            System.out.println();
            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testQueryAggsDocument() throws ExecutionException, InterruptedException {
        try {
            TransportClient client = ElasticsearchClientUtil.getConnection();

            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("type_groupby").field("type.keyword");
            MaxAggregationBuilder aggregationBuilder2 = AggregationBuilders.max("price_max").field("price");

            aggregationBuilder.subAggregation(aggregationBuilder2);

            searchSourceBuilder.aggregation(aggregationBuilder);

            // 设置请求体
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest).get();
            // 查询匹配
            SearchHits hits = searchResponse.getHits();

            System.out.println();
            System.out.println("took: " + searchResponse.getTook());
            System.out.println("timeout: " + searchResponse.isTimedOut());
            System.out.println("totalShards: " + searchResponse.getTotalShards());
            System.out.println("successfulShards: " + searchResponse.getSuccessfulShards());
            System.out.println("skippedShards:" + searchResponse.getSkippedShards());
            System.out.println("failedShards: " + searchResponse.getFailedShards());
            System.out.println("total: " + hits.getTotalHits());
            System.out.println("MaxScore: " + hits.getMaxScore());

            Iterator<Map.Entry<String, Aggregation>> iterator = searchResponse.getAggregations().asMap().entrySet().stream().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Aggregation> next = iterator.next();
                System.out.println(next.getKey() + "--->" + next.getValue());
            }

            System.out.println();
            System.out.println("hits ========>>");
            for (SearchHit hit : hits) {
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("<<========");
            // 关闭客户端连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}