package com.lq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.entity
 * @className GeoPointLocation
 * @description:
 * @author: liqiang
 * @create: 2023-11-17 11:31
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoPointLocation implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 纬度
     */
    private String lat;

    /**
     * 经度
     */
    private String lon;
}
