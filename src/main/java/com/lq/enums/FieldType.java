package com.lq.enums;
/**
 * @program: elasticsearch-api
 * @pageName com.lq.enums
 * @className FieldType
 * @description:
 * @author: liqiang
 * @create: 2023-10-12 16:46
 **/
public enum FieldType {
    /**
     * text
     */
    TEXT("text"),

    KEYWORD("keyword"),

    Long("long"),

    INTEGER("integer"),

    DOUBLE("double"),

    DATE("date"),

    /**
     * 单条数据
     */
    OBJECT("object"),

    /**
     * 嵌套数组
     */
    NESTED("nested"),
    ;

    FieldType(String type){
        this.type = type;
    }

    private final String type;

    public String getType() {
        return type;
    }
}
