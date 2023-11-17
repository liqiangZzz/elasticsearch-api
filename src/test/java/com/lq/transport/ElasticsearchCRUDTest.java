package com.lq.transport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lq.entity.MyProduct;
import com.lq.mapper.MyProductMapper;
import com.lq.util.ElasticsearchClientUtil;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @program: elasticsearch-api
 * @pageName com.lq
 * @className ElasticsearchCRUDTest
 * @description: transport
 * @author: liqiang
 * @create: 2023-09-27 17:08
 **/
@SpringBootTest
class ElasticsearchCRUDTest {

    @Autowired
    private MyProductMapper myProductMapper;

    private static final String INDEX = "my_product";

    private static final String DOC = "_doc";

    @Test
    void testQuery() {
        System.out.println(myProductMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @Test
    void testEsClient() throws UnknownHostException {
        TransportClient client = ElasticsearchClientUtil.getConnection();
        System.out.println(client);
        // 释放资源
        client.close();
    }

    @Test
    void testIndex() throws IOException {
        // 获取 Client连接对象
        TransportClient client = ElasticsearchClientUtil.getConnection();
        List<MyProduct> myProductList = myProductMapper.selectList(new LambdaQueryWrapper<>());
        for (MyProduct product : myProductList) {
            IndexResponse indexResponse = client.prepareIndex(INDEX, DOC, product.getId().toString())
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("id", product.getId())
                            .field("name", product.getName())
                            .field("type", product.getType())
                            .field("price", product.getPrice())
                            .field("tags", product.getTags())
                            .field("create_time", product.getCreateTime())
                            .endObject()
                    )
                    .get();
            System.out.println(indexResponse.getResult());
        }
        client.close();
    }

    @Test
    void testCreate() throws IOException {
        // 获取 Client连接对象
        TransportClient client = ElasticsearchClientUtil.getConnection();
        IndexResponse indexResponse = client.prepareIndex(INDEX, DOC, String.valueOf(5))
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("id", 5)
                        .field("name", "苹果手机")
                        .field("type", "Phone")
                        .field("price", 8999)
                        .field("tags", "性能好，不卡顿")
                        .field("create_time", LocalDateTime.now())
                        .endObject()
                )
                .get();
        System.out.println(indexResponse.getResult());
        client.close();
    }

    @Test
    void testUpdate() throws IOException {
        // 获取 Client连接对象
        TransportClient client = ElasticsearchClientUtil.getConnection();
        //修改某一条数据
      /*  UpdateResponse updateResponse = client.prepareUpdate(INDEX, DOC, String.valueOf(1))
                .setDoc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "小米电视Pro")
                        .endObject()).get();*/

        UpdateResponse updateResponse = client.prepareUpdate(INDEX, DOC, String.valueOf(1))
                .setScript(new Script("ctx._source.price += 1")).get();
        System.out.println(updateResponse.getResult());
        client.close();
    }

    @Test
    void testUpsert() throws IOException, ExecutionException, InterruptedException {
        TransportClient client = ElasticsearchClientUtil.getConnection();

        IndexRequest indexRequest = new IndexRequest(INDEX, DOC, String.valueOf(4))
                .source(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "磨砂壳")
                        .field("type", "手机壳")
                        .field("price", 19)
                        .field("tags", "防滑，手感好")
                        .field("create_time", LocalDateTime.now())
                        .endObject());

        UpdateRequest updateRequest = new UpdateRequest(INDEX, DOC, String.valueOf(4))
                .doc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "苹果磨砂手机")
                        .endObject())
                .upsert(indexRequest);

        UpdateResponse updateResponse = client.update(updateRequest).get();

        System.out.println(updateResponse.getResult());
        client.close();
    }

    @Test
    void testDelete() throws IOException {
        // 获取 Client连接对象
        TransportClient client = ElasticsearchClientUtil.getConnection();
        DeleteResponse response = client.prepareDelete(INDEX, DOC, String.valueOf(5)).get();
        System.out.println(response.getResult());
        client.close();
    }

    @Test
    void testBulk() throws IOException {
        // 获取 Client连接对象
        TransportClient client = ElasticsearchClientUtil.getConnection();
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        IndexRequest indexRequest = new IndexRequest(INDEX, DOC, String.valueOf(6))
                .source(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("id", 6)
                        .field("name", "HP电脑")
                        .field("type", "电脑")
                        .field("price", 6999)
                        .field("tags", "性能好，反应快")
                        .field("create_time", LocalDateTime.now())
                        .endObject());

        IndexRequest indexRequest2 = new IndexRequest(INDEX, DOC, String.valueOf(7))
                .source(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("id", 7)
                        .field("name", "联想电脑")
                        .field("type", "电脑")
                        .field("price", 7999)
                        .field("tags", "性能好，容量大")
                        .field("create_time", LocalDateTime.now())
                        .endObject());

        bulkRequestBuilder.add(indexRequest);
        bulkRequestBuilder.add(indexRequest2);
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        bulkResponse.hasFailures();// process failures by iterating through each bulk response item
    }


}
