package com.lq.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.entity
 * @className Product
 * @description: mybatis 映射实体
 * @author: liqiang
 * @create: 2023-09-26 18:04
 **/
@Data
@TableName("product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @TableField("title")
    private String title;

    @TableField("price")
    private BigDecimal price;

    @TableField("stock")
    private Integer stock;

    @TableField("sale_num")
    private Integer saleNum;

    @TableField("category_name")
    private String categoryName;

    @TableField("brand_name")
    private String brandName;

    @TableField("status")
    private Integer status;

    @TableField("spec")
    private String spec;

    @TableField("create_time")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
