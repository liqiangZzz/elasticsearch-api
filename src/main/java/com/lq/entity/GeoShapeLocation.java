
package com.lq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.entity
 * @className Location
 * @description:
 * @author: liqiang
 * @create: 2023-11-17 11:31
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoShapeLocation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;

    private List<Object> coordinates;


}
