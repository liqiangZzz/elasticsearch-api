package com.lq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @description:
 * @author: liqiang
 * @create: 2023-09-26 18:04
 **/
@Data
@TableName("product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
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
