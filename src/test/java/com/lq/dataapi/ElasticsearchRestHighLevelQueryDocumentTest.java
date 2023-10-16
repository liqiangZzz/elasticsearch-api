package com.lq.dataapi;

import com.lq.entity.Goods;
import com.lq.util.ElasticsearchQueryDocumentUtil;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.dataapi
 * @className ElasticsearchRestHighLevelQueryDocumentTest
 * @description:
 * @author: liqiang
 * @create: 2023-10-13 11:28
 **/
@SpringBootTest
public class ElasticsearchRestHighLevelQueryDocumentTest {


    @Autowired
    private ElasticsearchQueryDocumentUtil elasticsearchQueryDocumentUtil;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 精准查询
     *
     * @throws IOException
     */
    @Test
    void testTermQuery() throws Exception {
        // 构建查询条件（注意：termQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("brandName", "华为"));
        //执行查询
        List<Goods> search = elasticsearchQueryDocumentUtil.search(searchSourceBuilder, Goods.class);
        search.forEach(System.out::println);
    }

    @Test
    void testTermsQuery() throws Exception {
        // 构建查询条件（注意：termQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] args = {"华为", "Apple"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("brandName", args));
        //执行查询
        List<Goods> search = elasticsearchQueryDocumentUtil.search(searchSourceBuilder, Goods.class);
        search.forEach(System.out::println);
        ;
    }

    /**
     * 匹配查询符合条件的所有数据，并设置分页
     *
     * @throws Exception
     */
    @Test
    void testMatchAllQuery() throws Exception {
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
        List<Goods> search = elasticsearchQueryDocumentUtil.search(searchSourceBuilder, Goods.class);
        search.forEach(System.out::println);
    }

    /**
     * 查询结果 高亮显示
     * @throws Exception
     */
    @Test
    void testBoolQuery() throws Exception {
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
        System.out.println(elasticsearchQueryDocumentUtil.searchHighlightData(searchSourceBuilder, Goods.class, new String[]{"brandName", "categoryName"}));
    }

    /**
     *
     * 查询并分页
     * @throws Exception
     */
    @Test
    void testBoolQuery2() throws Exception {
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
        System.out.println(elasticsearchQueryDocumentUtil.searchListData(Goods.class,"goods",searchSourceBuilder,1,0,null,null,new String[]{"categoryName","brandName"}));
    }



}
