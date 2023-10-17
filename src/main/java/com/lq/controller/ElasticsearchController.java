package com.lq.controller;

import com.lq.entity.Goods;
import com.lq.service.GoodsService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.controller
 * @className ElasticsearchController
 * @description:
 * @author: liqiang
 * @create: 2023-10-12 16:00
 **/
@RestController
@RequestMapping("/es")
public class ElasticsearchController {

    @Autowired
    private GoodsService goodsService;

    @RequestMapping("/create")
    public String create() {
        // 创建商品信息
        Goods goods = new Goods();
        goods.setId(5L);
        goods.setTitle("小米15 256GB 支持移动联通电信5G 双卡双待手机");
        goods.setPrice(new BigDecimal("3799.00"));
        goods.setStock(1000);
        goods.setSaleNum(1999);
        goods.setCategoryName("手机");
        goods.setBrandName("小米");
        goods.setStatus(0);
        goods.setCreateTime(new Date());
        goods.setSpec("机身内存:16G,网络:全网通5G");

        // 创建索引请求对象
        goodsService.save(goods);
        return null;
    }

}
