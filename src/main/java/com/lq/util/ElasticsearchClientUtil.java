package com.lq.util;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.util
 * @className ElasticsearchClientUtil
 * @description:链接配置信息
 * @author: liqiang
 * @create: 2023-09-27 14:51
 **/
public class ElasticsearchClientUtil {

    private ElasticsearchClientUtil() {
    }

    /**
     * Transport 链接信息
     * @return
     * @throws UnknownHostException
     */
    public static TransportClient getConnection() throws UnknownHostException {
        // 创建Client连接对象(指定集群名称)
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
        TransportAddress transportAddress = new TransportAddress(InetAddress.getByName("localhost"), 9300);
        return new PreBuiltTransportClient(settings).addTransportAddress(transportAddress);
    }


}
