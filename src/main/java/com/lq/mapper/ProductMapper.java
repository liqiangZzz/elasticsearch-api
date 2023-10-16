package com.lq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lq.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.mapper
 * @className ProductMapper
 * @description:
 * @author: liqiang
 * @create: 2023-09-27 10:27
 **/
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
