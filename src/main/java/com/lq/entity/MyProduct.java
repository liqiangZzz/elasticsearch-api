package com.lq.entity;

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
@TableName("my_product")
public class MyProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("price")
    private BigDecimal price;

    @TableField("tags")
    private String tags;

    @TableField("create_time")
    private LocalDateTime createTime;
}
