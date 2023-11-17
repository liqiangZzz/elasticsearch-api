package com.lq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.entity
 * @className Location
 * @description:
 * @author: liqiang
 * @create: 2023-11-17 15:59
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location implements Serializable {
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
