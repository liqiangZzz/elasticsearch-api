package com.lq.common.utils;

import org.springframework.context.MessageSource;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.common.utils
 * @className MessageUtils
 * @description:
 * @author: liqiang
 * @create: 2023-10-13 14:24
 **/
public class MessageUtils {

    private static MessageSource messageSource;

    /**
     * 根据消息键和参数 获取消息
     * 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return
     */
    public static String message(String code, Object... args) {
        if (messageSource == null) {
            messageSource = SpringUtils.getBean(MessageSource.class);
        }
        return messageSource.getMessage(code, args, null);
    }
}
