package com.lq.common.exception;

import com.lq.common.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.exception
 * @className BaseException
 * @description:
 * @author: liqiang
 * @create: 2023-10-13 14:23
 **/
public class BaseException extends RuntimeException {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @Fields serialVersionUID
     */
    private static final long serialVersionUID = 4426416610311255445L;

    /**
     * 异常代码
     */
    private final String code;

    /**
     * 错误码对应的参数
     */
    private final Object[] args;

    /**
     * 错误消息
     */
    private final String defaultMessage;

    /**
     * 构造基础异常
     *
     * @param code           异常代码
     * @param defaultMessage 默认异常消息
     * @param cause          引起此异常的触发异常
     * @paramargs args           异常参数
     */
    public BaseException(String code, Object[] args, String defaultMessage, Throwable cause) {
        super(cause);
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;

    }

    /**
     * 构造基础异常
     *
     * @param code           异常代码
     * @param args           异常参数
     * @param defaultMessage 默认异常消息
     */
    public BaseException(String code, Object[] args, String defaultMessage) {
        this(code, args, defaultMessage, null);
    }

    /**
     * 构造基础异常
     *
     * @param defaultMessage 默认异常消息
     * @param cause          引起此异常的触发异常
     */
    public BaseException(String defaultMessage, Throwable cause) {
        this(null, null, defaultMessage, cause);
    }

    /**
     * 构造基础异常
     *
     * @param defaultMessage 默认异常消息
     */
    public BaseException(String defaultMessage) {
        this(null, null, defaultMessage, null);
    }

    /**
     * 构造基础异常
     *
     * @param code  异常代码
     * @param args  异常参数
     * @param cause 引起此异常的触发异常
     */
    public BaseException(String code, Object[] args, Throwable cause) {
        this(code, args, null, cause);
    }

    /**
     * 构造基础异常
     *
     * @param code 异常代码
     * @param args 异常参数
     */
    public BaseException(String code, Object[] args) {
        this(code, args, null, null);
    }

    @Override
    public String getMessage() {
        String message = null;
        try {
            if (!StringUtils.isEmpty(code)) {
                message = MessageUtils.message(code, args);
            }
        } catch (Exception e) {
            logger.warn("Can't convert message from properties", e);
            message = defaultMessage;
        }
        if (message == null) {
            message = defaultMessage;
        }
        return message;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return this.getClass() + "{" +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
