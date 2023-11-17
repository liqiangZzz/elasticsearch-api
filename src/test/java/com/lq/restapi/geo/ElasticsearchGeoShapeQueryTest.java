package com.lq.restapi.geo;

import cn.hutool.json.JSONUtil;
import com.lq.entity.GeoPointLocation;
import com.lq.entity.GeoShapeLocation;
import com.lq.entity.Location;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.*;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.index.query.GeoShapeQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.restapi.geo
 * @className ElasticsearchGeoShapeQueryTest
 * @description: geo_shape  查询
 * @author: liqiang
 * @create: 2023-11-17 11:34
 **/
@SpringBootTest
public class ElasticsearchGeoShapeQueryTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    private final static String INDEX_NAME = "geo_shape";

    /**
     * 构造geo_bounding_box请求
     * 矩形查询
     *
     * @throws IOException
     */
    @Test
    void testGeoShapeQuery() throws IOException {
        List<com.lq.entity.GeoShape> resultList = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        // point 点
        /*PointBuilder pointBuilder = new PointBuilder(13.400544, 52.530286);
        GeoShapeQueryBuilder shapeQueryBuilder = QueryBuilders.geoShapeQuery("location", pointBuilder).relation(ShapeRelation.CONTAINS);*/

        //envelope 矩形
        Coordinate coordinate = new Coordinate(13, 53);
        Coordinate coordinate2 = new Coordinate(14, 52);
        EnvelopeBuilder pointBuilder = new EnvelopeBuilder(coordinate, coordinate2);
        GeoShapeQueryBuilder shapeQueryBuilder = QueryBuilders.geoShapeQuery("location", pointBuilder).relation(ShapeRelation.WITHIN);

        //Circle 圆
       /* CircleBuilder circleBuilder = new CircleBuilder();
        circleBuilder.center( 13.400544, 52.530286);
        DistanceUnit.Distance distance = new DistanceUnit.Distance(3, DistanceUnit.KILOMETERS);
        circleBuilder.radius(distance);
        GeoShapeQueryBuilder shapeQueryBuilder = QueryBuilders.geoShapeQuery("location", circleBuilder).relation(ShapeRelation.WITHIN);*/

        //Polygon 多边形
        /*CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        List<Location> boundaryPoints = new ArrayList<>();
        boundaryPoints.add(new Location("-1","99"));
        boundaryPoints.add(new Location("-1","102"));
        boundaryPoints.add(new Location( "2","102"));
        boundaryPoints.add(new Location( "2","99"));
        boundaryPoints.add(new Location("-1","99"));

        for (Location location : boundaryPoints) {
            coordinatesBuilder.coordinate(Double.parseDouble(location.getLon()), Double.parseDouble(location.getLat()));
        }

        PolygonBuilder polygonBuilder = new PolygonBuilder(coordinatesBuilder);
        GeoShapeQueryBuilder shapeQueryBuilder = QueryBuilders.geoShapeQuery("location", polygonBuilder).relation(ShapeRelation.WITHIN);*/


        //多边形 MultiPolygon
       /* CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        List<Location> boundaryPoints = new ArrayList<>();
        boundaryPoints.add(new Location("0", "100"));
        boundaryPoints.add(new Location("0", "101"));
        boundaryPoints.add(new Location("1", "101"));
        boundaryPoints.add(new Location("1", "100"));
        boundaryPoints.add(new Location("0", "100"));

        for (Location location : boundaryPoints) {
            coordinatesBuilder.coordinate(Double.parseDouble(location.getLon()), Double.parseDouble(location.getLat()));
        }
        PolygonBuilder polygon1 = new PolygonBuilder(coordinatesBuilder);

        CoordinatesBuilder coordinatesBuilder2 = new CoordinatesBuilder();
        List<Location> boundaryPoints2 = new ArrayList<>();
        boundaryPoints2.add(new Location("0.2", "100.2"));
        boundaryPoints2.add(new Location("0.2", "100.8"));
        boundaryPoints2.add(new Location("0.8", "100.8"));
        boundaryPoints2.add(new Location("0.8", "100.2"));
        boundaryPoints2.add(new Location("0.2", "100.2"));

        for (Location location : boundaryPoints2) {
            coordinatesBuilder2.coordinate(Double.parseDouble(location.getLon()), Double.parseDouble(location.getLat()));
        }
        PolygonBuilder polygon2 = new PolygonBuilder(coordinatesBuilder2);

        MultiPolygonBuilder multiPolygonBuilder = new MultiPolygonBuilder();
        multiPolygonBuilder.polygon(polygon1);
        multiPolygonBuilder.polygon(polygon2);

        GeoShapeQueryBuilder shapeQueryBuilder = QueryBuilders.geoShapeQuery("location", multiPolygonBuilder).relation(ShapeRelation.INTERSECTS);*/

        searchSourceBuilder.query(shapeQueryBuilder);
        searchRequest.source(searchSourceBuilder);


        //执行查询，处理相应结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.toString());
        System.out.println(searchSourceBuilder.query());
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit searchHit : hits) {
                com.lq.entity.GeoShape geoShape = new com.lq.entity.GeoShape();
                geoShape.setId(searchHit.getId());   //文档_id
                geoShape.setIndex(searchHit.getIndex());   //索引名称
                geoShape.setScore(searchHit.getScore());   //文档得分
                Map<String, Object> dataMap = searchHit.getSourceAsMap();
                Object name = dataMap.get("name");
                geoShape.setName(name.toString());
                //获取location
                Object location = dataMap.get("location");
                if (location != null) {
                    geoShape.setLocation(JSONUtil.toBean(JSONUtil.toJsonStr(location), GeoShapeLocation.class));
                }
                resultList.add(geoShape);
            }
        }
        resultList.forEach(System.out::println);
    }


}
