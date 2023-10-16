package com.lq.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.entity
 * @className Goods
 * @description:
 * @author: liqiang
 * @create: 2023-10-09 13:59
 **/
@Data
@Document(indexName = "goods")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品编号
     */
    @Id
    @Field(type = FieldType.Long,index = false)
    private Long id;

    /**
     * 商品标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 商品价格
     */
    @Field(type = FieldType.Double)
    private BigDecimal price;

    /**
     * 商品库存
     */
    @Field(type = FieldType.Integer)
    private Integer stock;

    /**
     * 商品销售数量
     */
    @Field(type = FieldType.Integer)
    private Integer saleNum;

    /**
     * 商品分类
     */
    @Field(type = FieldType.Keyword)
    private String categoryName;

    /**
     * 商品品牌
     */
    @Field(type = FieldType.Keyword)
    private String brandName;

    /**
     * 上下架状态
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 说明书
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String spec;

    /**
     * 商品创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
