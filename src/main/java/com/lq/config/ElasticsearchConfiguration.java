package com.lq.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.config
 * @className ElasticsearchConfiguration
 * @description: Elasticsearch 配置信息
 * @author: liqiang
 * @create: 2023-10-09 11:12
 **/
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchConfiguration {

    /**
     * es host ip 地址（集群）
     */
    private String hosts;
    /**
     * es用户名
     */
    private String userName;
    /**
     * es密码
     */
    private String password;
    /**
     * es 请求方式
     */
    private String scheme;
    /**
     * es集群名称
     */
    private String clusterName;
    /**
     * es 连接超时时间
     */
    private int connectTimeOut;
    /**
     * es socket 连接超时时间
     */
    private int socketTimeOut;
    /**
     * es 请求超时时间
     */
    private int connectionRequestTimeOut;
    /**
     * es 最大连接数
     */
    private int maxConnectNum;
    /**
     * es 每个路由的最大连接数
     */
    private int maxConnectNumPerRoute;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 拆分地址
//        List<HttpHost> hostLists = new ArrayList<>();
//        String[] hostList = hosts.split(",");
//        for (String adder : hostList) {
//            String host = adder.split(":")[0];
//            String port = adder.split(":")[1];
//            hostLists.add(new HttpHost(host, Integer.parseInt(port), scheme));
//        }
//        // 转换成 HttpHost 数组
//        HttpHost[] httpHost = hostLists.toArray(new HttpHost[]{});

        // 此处为单节点es
        String host = hosts.split(":")[0];
        String port = hosts.split(":")[1];
        HttpHost httpHost = new HttpHost(host, Integer.parseInt(port));

        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);

        //region 设置请求头，避免每个请求都必须指定
        Header[] defaultHeaders = new Header[]{
                new BasicHeader("header", "application/json")
        };

        builder.setDefaultHeaders(defaultHeaders);

        //region 设置每次节点发生故障时收到通知的侦听器。内部嗅探到故障时被启用
        builder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                super.onFailure(node);
            }
        });

        // 设置用户名、密码
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

        // 连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeOut);
            requestConfigBuilder.setSocketTimeout(socketTimeOut);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
            return requestConfigBuilder;
        });

        // 连接数配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(maxConnectNum);
            httpClientBuilder.setMaxConnPerRoute(maxConnectNumPerRoute);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            return httpClientBuilder;
        });

        return new RestHighLevelClient(builder);
    }
}
