package com.lq;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: elasticsearch-api
 * @pageName com.lq
 * @className ElasticsearchApiApplication
 * @description:
 * @author: liqiang
 * @create: 2023-09-26 17:50
 **/
@SpringBootApplication
@MapperScan(basePackages = "com.lq.mapper")
public class ElasticsearchApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchApiApplication.class);
    }
}
