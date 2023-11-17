package com.lq.restapi;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.lq.entity.Product;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: elasticsearch-api
 * @pageName com.lq
 * @className ElasticsearchRestHighLevelClientDocumentTest
 * @description: Elasticsearch Rest高级客户端文档测试
 * @author: liqiang
 * @create: 2023-10-09 14:02
 **/
@SpringBootTest
class ElasticsearchRestHighLevelClientDocumentTest {

    @Autowired
    private  RestHighLevelClient restHighLevelClient ;


    private final static String INDEX_NAME = "product";

    /**
     * 添加文档信息
     *
     * @throws IOException
     */
    @Test
    void testAddDocument() throws IOException {
        // 创建商品信息
        Product product = new Product();
        product.setId(1L);
        product.setTitle("Apple iPhone 14 Pro (A2639) 256GB 远峰蓝色 支持移动联通电信5G 双卡双待手机");
        product.setPrice(new BigDecimal("8799.00"));
        product.setStock(1000);
        product.setSaleNum(8989);
        product.setCategoryName("手机");
        product.setBrandName("Apple");
        product.setStatus(0);
        product.setCreateTime(LocalDateTime.now());
        product.setSpec("机身内存:16G,网络:全网通5G");

        // 将对象转为json
        String jsonString = JSON.toJSONString(product);
        // 创建索引请求对象
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME, "_doc").id(product.getId().toString()).source(jsonString, XContentType.JSON);
        //执行文档操作
        IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("创建状态：  " + response.status());
    }

    /**
     * 获取文档信息
     *
     * @throws IOException
     */
    @Test
    void testGetDocument() throws IOException {
        //创建获取请求对象
        GetRequest request = new GetRequest(INDEX_NAME, "_doc", "1");
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);

        Map<String, Object> sourceAsMap = response.getSourceAsMap();
        Product product = BeanUtil.toBean(sourceAsMap, Product.class);
        System.out.println(product);
    }

    /**
     * 更新文档信息
     *
     * @throws IOException
     */
    @Test
    void testUpdateDocument() throws IOException {
        // 创建商品信息
        Product product = new Product();
        product.setTitle("华为 mate60 256GB 支持移动联通电信5G 卫星通讯 双卡双待");
        product.setPrice(new BigDecimal("8999"));
        product.setId(2L);
        // 将对象转为json
        String jsonString = JSON.toJSONString(product);
        // 创建索引请求对象
        UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, "_doc", product.getId().toString());
        // 设置更新文档内容
        updateRequest.doc(jsonString, XContentType.JSON);
        // 执行更新文档
        UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

        System.out.println("执行状态：  " + response.status());
    }

    /**
     * 删除文档信息
     *
     * @throws IOException
     */
    @Test
    void testDeleteDocument() throws IOException {
        // 创建索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME, "_doc", "3");
        // 执行删除文档
        DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);

        System.out.println("执行状态：  " + response.status());
    }

    /**
     * 批量导出数据
     *
     * @throws IOException
     */
    @Test
    void testBulkDocument() throws IOException {
        // 准备数据
        Product product = new Product();
        product.setId(2L);
        product.setTitle("华为手表 支持移动通讯");
        product.setPrice(new BigDecimal("2999.00"));
        product.setStock(1000);
        product.setSaleNum(599);
        product.setCategoryName("手表");
        product.setBrandName("华为");
        product.setStatus(0);
        product.setCreateTime(LocalDateTime.now());
        product.setSpec("支持全网通5G");

        Product product2 = new Product();
        product2.setId(3L);
        product2.setTitle("TP-LINK Wifi 穿墙能力强");
        product2.setPrice(new BigDecimal("399.00"));
        product2.setStock(1000);
        product2.setSaleNum(599);
        product2.setCategoryName("Wifi");
        product2.setBrandName("TP-LINK");
        product2.setStatus(0);
        product2.setCreateTime(LocalDateTime.now());
        product2.setSpec("TP-LINK Wifi 穿墙能力强");

        Product product3 = new Product();
        product3.setId(4L);
        product3.setTitle("华为 Wifi 穿墙能力强");
        product3.setPrice(new BigDecimal("499.00"));
        product3.setStock(1000);
        product3.setSaleNum(399);
        product3.setCategoryName("Wifi");
        product3.setBrandName("华为");
        product3.setStatus(0);
        product3.setCreateTime(LocalDateTime.now());
        product3.setSpec("华为 Wifi 穿墙能力强");

        List<Product> productList = new ArrayList<>();
        productList.add(product);
        productList.add(product2);
        productList.add(product3);

        //2.bulk导入
        BulkRequest bulkRequest = new BulkRequest();

        //2.1 循环 productList，创建IndexRequest添加数据
        for (Product entity : productList) {
            //将goods对象转换为json字符串
            String data = JSON.toJSONString(entity);//map --> {}
            IndexRequest indexRequest = new IndexRequest(INDEX_NAME, "_doc");
            indexRequest.id(entity.getId().toString()).source(data, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        //执行批量请求
        BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        System.out.println("执行状态：  " + response.status());
    }


}
