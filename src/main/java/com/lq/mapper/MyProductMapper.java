package com.lq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lq.entity.MyProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.mapper
 * @className MyProductMapper
 * @description:
 * @author: liqiang
 * @create: 2023-09-27 10:27
 **/
@Mapper
public interface MyProductMapper extends BaseMapper<MyProduct> {
}
