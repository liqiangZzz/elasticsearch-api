package com.lq.service;

import com.lq.entity.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.service
 * @className Goods
 * @description:
 * @author: liqiang
 * @create: 2023-10-12 15:59
 **/
public interface GoodsService extends ElasticsearchRepository<Goods, Long> {
}
