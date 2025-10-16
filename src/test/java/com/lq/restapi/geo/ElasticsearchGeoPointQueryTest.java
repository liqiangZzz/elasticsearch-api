package com.lq.restapi.geo;

import cn.hutool.json.JSONUtil;
import com.lq.entity.GeoPointLocation;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.restapi.geo
 * @className ElasticsearchGeoPointQueryTest
 * @description: geo_point  查询
 * @author: liqiang
 * @create: 2023-11-17 11:34
 **/
@SpringBootTest
public class ElasticsearchGeoPointQueryTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    private final static String INDEX_NAME = "geo_point";

    /**
     * 构造geo_bounding_box请求
     * 矩形查询
     *
     * @throws IOException
     */
    @Test
    void testGeoBoundingBoxQuery() throws IOException {
        List<com.lq.entity.GeoPoint> resultList = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        // 构造左上点坐标
        GeoPoint topLeft = new GeoPoint(40.187328D, 116.498353D);
        // 构造右下点坐标
        GeoPoint bottomRight = new GeoPoint(40.084509D, 116.610461D);
        GeoBoundingBoxQueryBuilder geoBoundingBoxQueryBuilder = QueryBuilders.geoBoundingBoxQuery("location");
        geoBoundingBoxQueryBuilder.setCorners(topLeft, bottomRight);
        searchSourceBuilder.query(geoBoundingBoxQueryBuilder);

        searchRequest.source(searchSourceBuilder);

        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit searchHit : hits) {
                com.lq.entity.GeoPoint geoPoint = new com.lq.entity.GeoPoint();
                geoPoint.setId(searchHit.getId());   //文档_id
                geoPoint.setIndex(searchHit.getIndex());   //索引名称
                geoPoint.setScore(searchHit.getScore());   //文档得分
                Map<String, Object> dataMap = searchHit.getSourceAsMap();
                Object name = dataMap.get("name");
                geoPoint.setName(name.toString());
                //获取location
                Object location = dataMap.get("location");
                if (location != null) {
                    geoPoint.setLocation(JSONUtil.toBean(JSONUtil.toJsonStr(location), GeoPointLocation.class));
                }
                resultList.add(geoPoint);
            }
        }

        resultList.forEach(System.out::println);
    }

    /**
     * 半径查询
     * geo_distance
     *
     * @throws IOException
     */
    @Test
    void testGeoDistanceQuery() throws IOException {
        List<com.lq.entity.GeoPoint> resultList = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        // /假设目标距离坐标
        GeoPoint sourcePoint = new GeoPoint(40.174697D, 116.5864D);

        GeoDistanceQueryBuilder geoDistanceQueryBuilder = QueryBuilders.geoDistanceQuery("location");
        geoDistanceQueryBuilder.distance("3", DistanceUnit.KILOMETERS).point(sourcePoint);
        searchSourceBuilder.query(geoDistanceQueryBuilder);

        searchRequest.source(searchSourceBuilder);

        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit searchHit : hits) {
                com.lq.entity.GeoPoint geoPoint = new com.lq.entity.GeoPoint();
                geoPoint.setId(searchHit.getId());   //文档_id
                geoPoint.setIndex(searchHit.getIndex());   //索引名称
                geoPoint.setScore(searchHit.getScore());   //文档得分
                Map<String, Object> dataMap = searchHit.getSourceAsMap();
                Object name = dataMap.get("name");
                geoPoint.setName(name.toString());
                //获取location
                Object location = dataMap.get("location");
                if (location != null) {
                    geoPoint.setLocation(JSONUtil.toBean(JSONUtil.toJsonStr(location), GeoPointLocation.class));
                }
                resultList.add(geoPoint);
            }
        }
        resultList.forEach(System.out::println);
    }

    /**
     * geo_distance
     * 通过半径查询后 进行排序
     *
     * @throws IOException
     */
    @Test
    void testGeoDistanceSortQuery() throws IOException {
        List<com.lq.entity.GeoPoint> resultList = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        //假设目标距离坐标
        GeoPoint sourcePoint = new GeoPoint(40.174697D, 116.610461D);

        GeoDistanceQueryBuilder geoDistanceQueryBuilder = QueryBuilders.geoDistanceQuery("location");
        geoDistanceQueryBuilder.distance("3", DistanceUnit.KILOMETERS).point(sourcePoint);
        searchSourceBuilder.query(geoDistanceQueryBuilder);
        // 设置排序方式
        GeoDistanceSortBuilder geoSort = new GeoDistanceSortBuilder("location", 40.174697, 116.610461);
        geoSort.order(SortOrder.ASC);
        searchSourceBuilder.sort(geoSort);

        searchRequest.source(searchSourceBuilder);
        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit searchHit : hits) {
                com.lq.entity.GeoPoint geoPoint = new com.lq.entity.GeoPoint();
                geoPoint.setId(searchHit.getId());   //文档_id
                geoPoint.setIndex(searchHit.getIndex());   //索引名称
                geoPoint.setScore(searchHit.getScore());   //文档得分
                Map<String, Object> dataMap = searchHit.getSourceAsMap();
                Object name = dataMap.get("name");
                geoPoint.setName(name.toString());
                //获取location
                Object location = dataMap.get("location");
                if (location != null) {
                    geoPoint.setLocation(JSONUtil.toBean(JSONUtil.toJsonStr(location), GeoPointLocation.class));
                }
                resultList.add(geoPoint);
            }
        }
        resultList.forEach(System.out::println);
    }

    /**
     * geo_polygon 多边形查询，最少三个坐标
     *
     * @throws IOException
     */
    @Test
    void testGeoPolygonQuery() throws IOException {
        List<com.lq.entity.GeoPoint> resultList = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        List<GeoPoint> sourcePointList = new ArrayList<>();

        GeoPoint sourcePoint1 = new GeoPoint(31.085262, 121.531257);
        GeoPoint sourcePoint2 = new GeoPoint(40.163716, 116.630950);
        GeoPoint sourcePoint3 = new GeoPoint(40.174697, 116.516422);
        GeoPoint sourcePoint4 = new GeoPoint(31.086252, 121.530632);

        sourcePointList.add(sourcePoint1);
        sourcePointList.add(sourcePoint2);
        sourcePointList.add(sourcePoint3);
        sourcePointList.add(sourcePoint4);

        GeoPolygonQueryBuilder geoPolygonQueryBuilder = QueryBuilders.geoPolygonQuery("location", sourcePointList);
        searchSourceBuilder.query(geoPolygonQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit searchHit : hits) {
                com.lq.entity.GeoPoint geoPoint = new com.lq.entity.GeoPoint();
                geoPoint.setId(searchHit.getId());   //文档_id
                geoPoint.setIndex(searchHit.getIndex());   //索引名称
                geoPoint.setScore(searchHit.getScore());   //文档得分
                Map<String, Object> dataMap = searchHit.getSourceAsMap();
                Object name = dataMap.get("name");
                geoPoint.setName(name.toString());
                //获取location
                Object location = dataMap.get("location");
                if (location != null) {
                    geoPoint.setLocation(JSONUtil.toBean(JSONUtil.toJsonStr(location), GeoPointLocation.class));
                }
                resultList.add(geoPoint);
            }
        }
        resultList.forEach(System.out::println);
    }


}
