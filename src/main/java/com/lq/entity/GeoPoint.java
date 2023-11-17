package com.lq.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.entity
 * @className GeoPoint
 * @description: geo_point 类型
 * @author: liqiang
 * @create: 2023-11-17 11:31
 **/
@Data
public class GeoPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private String index;

    private String id;

    private Float score;

    private String name;

    /**
     * 位置
     */
    private GeoPointLocation location;
}
