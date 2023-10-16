package com.lq.restapi;

import com.lq.util.ElasticsearchClientUtil;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @program: elasticsearch-api
 * @pageName com.lq
 * @className ElasticsearchRestHighLevelClientIndexTest
 * @description: Elasticsearch Rest高级客户端索引测试
 * @author: liqiang
 * @create: 2023-10-09 11:17
 **/
@SpringBootTest
class ElasticsearchRestHighLevelClientIndexTest {

    private final static String INDEX_NAME = "goods";

    /**
     * 启动时加载
     */
    private static RestHighLevelClient restHighLevelClient = null;

    static {
        restHighLevelClient = ElasticsearchClientUtil.getRestHighLevelClientConnection();
    }

    @Test
    void testClient() {
        System.out.println(restHighLevelClient);
    }

    /**
     * 创建索引库和映射表结构
     *
     * @throws IOException
     */
    @Test
    void testIndexCreate() throws IOException {
        // 1、创建 创建索引request 参数：索引名mess
        CreateIndexRequest indexRequest = new CreateIndexRequest(INDEX_NAME);
        // 2、设置索引的settings
        // 3、设置索引的mappings

        String mapping = "{\n" +
                "\n" +
                "\t\t\"properties\": {\n" +
                "\t\t  \"brandName\": {\n" +
                "\t\t\t\"type\": \"keyword\"\n" +
                "\t\t  },\n" +
                "\t\t  \"categoryName\": {\n" +
                "\t\t\t\"type\": \"keyword\"\n" +
                "\t\t  },\n" +
                "\t\t  \"createTime\": {\n" +
                "\t\t\t\"type\": \"date\",\n" +
                "\t\t\t\"format\": \"yyyy-MM-dd HH:mm:ss\"\n" +
                "\t\t  },\n" +
                "\t\t  \"id\": {\n" +
                "\t\t\t\"type\": \"long\"\n" +
                "\t\t  },\n" +
                "\t\t  \"price\": {\n" +
                "\t\t\t\"type\": \"double\"\n" +
                "\t\t  },\n" +
                "\t\t  \"saleNum\": {\n" +
                "\t\t\t\"type\": \"integer\"\n" +
                "\t\t  },\n" +
                "\t\t  \"status\": {\n" +
                "\t\t\t\"type\": \"integer\"\n" +
                "\t\t  },\n" +
                "\t\t  \"stock\": {\n" +
                "\t\t\t\"type\": \"integer\"\n" +
                "\t\t  },\n" +
                "\t\t  \"spec\": {\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\": \"ik_max_word\",\n" +
                "\t\t\t\"search_analyzer\": \"ik_smart\"\n" +
                "\t\t  },\n" +
                "\t\t  \"title\": {\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\": \"ik_max_word\",\n" +
                "\t\t\t\"search_analyzer\": \"ik_smart\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "  }";

        // 4、 设置索引的别名
        // 5、 发送请求
        // 5.1 同步方式发送请求
        IndicesClient indicesClient = restHighLevelClient.indices();
        indexRequest.mapping(mapping, XContentType.JSON);
        // 请求服务器
        CreateIndexResponse response = indicesClient.create(indexRequest, RequestOptions.DEFAULT);

        System.out.println(response.isAcknowledged());
    }

    /**
     * 获取表结构
     *
     * @throws IOException
     */
    @Test
    void testGetMapping() throws IOException {
        IndicesClient indicesClient = restHighLevelClient.indices();
        // 创建get请求
        GetIndexRequest request = new GetIndexRequest(INDEX_NAME);
        // 发送get请求
        GetIndexResponse response = indicesClient.get(request, RequestOptions.DEFAULT);
        Map<String, MappingMetadata> mappings = response.getMappings();
        Map<String, Object> sourceAsMap = mappings.get(INDEX_NAME).getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    /**
     * 输出索引库
     *
     * @throws IOException
     */
    @Test
    void testIndexDelete() throws IOException {
        IndicesClient indices = restHighLevelClient.indices();
        //创建delete 请求方式
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(INDEX_NAME);
        //发送delete 请求
        AcknowledgedResponse response = indices.delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    /**
     * 判断索引库是否存在
     *
     * @throws IOException
     */
    @Test
    void testIndexExists() throws IOException {
        IndicesClient indices = restHighLevelClient.indices();
        //创建get  请求方式
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX_NAME);
        // 判断索引库是否存在
        boolean exists = indices.exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
}
