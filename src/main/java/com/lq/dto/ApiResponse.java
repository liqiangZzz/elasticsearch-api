package com.lq.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.dto
 * @className ApiResponse
 * @description:
 * @author: liqiang
 * @create: 2023-10-12 13:11
 **/
@Data
@Accessors(chain = true)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1821035307580963056L;

    private boolean success;

    private int httpCode;

    private String message;

    private String tag;

    private T data;
}
