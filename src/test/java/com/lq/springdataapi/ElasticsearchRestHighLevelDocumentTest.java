package com.lq.springdataapi;

import cn.hutool.core.bean.BeanUtil;
import com.lq.entity.Goods;
import com.lq.util.ElasticsearchCRUDDocumentUtil;
import org.elasticsearch.action.index.IndexResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @program: elasticsearch-api
 * @pageName com.lq.dataapi
 * @className ElasticsearchRestHighLevelDocumentTest
 * @description:spring data Elasticsearch Rest 操作文档
 * @author: liqiang
 * @create: 2023-10-12 15:02
 **/
@SpringBootTest
public class ElasticsearchRestHighLevelDocumentTest {

    @Autowired
    private ElasticsearchCRUDDocumentUtil elasticsearchCRUDDocumentUtil;

    /**
     * 提供新增或修改的功能
     *
     * @throws Exception
     */
    @Test
    public void testIndex() throws Exception {
        // 创建商品信息
        Goods goods = new Goods();
        goods.setId(1L);
        goods.setTitle("Apple iPhone 14 Pro (A2639) 256GB 远峰蓝色 支持移动联通电信5G 双卡双待手机");
        goods.setPrice(new BigDecimal("8799.00"));
        goods.setStock(1000);
        goods.setSaleNum(8989);
        goods.setCategoryName("手机");
        goods.setBrandName("Apple");
        goods.setStatus(0);
        goods.setCreateTime(new Date());
        goods.setSpec("机身内存:16G,网络:全网通5G");

        IndexResponse indexRequest = elasticsearchCRUDDocumentUtil.index(goods);
        System.out.println("创建状态：  " + indexRequest.status());

    }

    @Test
    void testUpdateDocumentByMap() throws Exception {
        Goods goods = new Goods();
        goods.setId(1L);
        goods.setTitle("Apple iPhone 14 Pro (A2639) 256GB 远峰蓝色 支持移动联通电信5G 双卡双待手机");
        goods.setPrice(new BigDecimal("8799.00"));
        goods.setStock(1000);
        goods.setSaleNum(8989);
        goods.setCategoryName("手机");
        goods.setBrandName("Apple");
        goods.setStatus(0);
        goods.setCreateTime(new Date());
        goods.setSpec("机身内存:16G,网络:全网通5G");

        System.out.println(elasticsearchCRUDDocumentUtil.updateDoc("goods", goods.getId().toString(), goods));

        Map<String, Object> map = BeanUtil.beanToMap(goods);
        System.out.println(elasticsearchCRUDDocumentUtil.updateDoc("goods", goods.getId().toString(), map));
    }

    @Test
    void testDeleteDocument() throws Exception {
        System.out.println(elasticsearchCRUDDocumentUtil.deleteDoc("goods", "1"));
    }


    @Test
    void testBulkDocument() throws Exception {
        // 准备数据
        Goods goods = new Goods();
        goods.setId(2L);
        goods.setTitle("华为手表 支持移动通讯");
        goods.setPrice(new BigDecimal("2999.00"));
        goods.setStock(1000);
        goods.setSaleNum(599);
        goods.setCategoryName("手表");
        goods.setBrandName("华为");
        goods.setStatus(0);
        goods.setCreateTime(new Date());
        goods.setSpec("支持全网通5G");

        Goods goods2 = new Goods();
        goods2.setId(3L);
        goods2.setTitle("TP-LINK Wifi 穿墙能力强");
        goods2.setPrice(new BigDecimal("399.00"));
        goods2.setStock(1000);
        goods2.setSaleNum(599);
        goods2.setCategoryName("Wifi");
        goods2.setBrandName("TP-LINK");
        goods2.setStatus(0);
        goods2.setCreateTime(new Date());
        goods2.setSpec("TP-LINK Wifi 穿墙能力强");

        Goods goods3 = new Goods();
        goods3.setId(4L);
        goods3.setTitle("华为 Wifi 穿墙能力强");
        goods3.setPrice(new BigDecimal("499.00"));
        goods3.setStock(1000);
        goods3.setSaleNum(399);
        goods3.setCategoryName("Wifi");
        goods3.setBrandName("华为");
        goods3.setStatus(0);
        goods3.setCreateTime(new Date());
        goods3.setSpec("华为 Wifi 穿墙能力强");

        List<Goods> goodsList = new ArrayList<>();
        goodsList.add(goods);
        goodsList.add(goods2);
        goodsList.add(goods3);
        System.out.println(elasticsearchCRUDDocumentUtil.batchSaveOrUpdate(goodsList, true));
    }

    @Test
    public void testDeleteIndex() throws Exception {
        // elasticsearchUtil.createIndex(Goods.class);
        System.out.println(1232312312);
    }
}
