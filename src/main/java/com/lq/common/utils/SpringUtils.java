/**
 * Copyright (c) 2005-2012 https://github.com/zhangkaitao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.lq.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * @author Jie Wu <wujie18@centaline.com.cn>
 */
public final class SpringUtils implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext;

    /**
     * 初始化Spring Application Context
     */
    @Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		SpringUtils.applicationContext = applicationContext;
	}
    
    /**
	 * 获取applicationContext
	 * 
	 * @return applicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		return SpringUtils.applicationContext;
	}
	
	@Override
	public void destroy() throws Exception {
		SpringUtils.applicationContext=null;
	}
	
    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException {
        return (T) applicationContext.getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz
     * @return
     * @throws BeansException
     *
     */
    public static <T> T getBean(Class<T> clz) throws BeansException {
        Assert.notNull(clz,"不能为空");
        return (T) applicationContext.getBean(clz);
    }
    
    public static <T> T getBean(String name, Class<T> type){
    	Assert.hasText(name,"不能为空");
		Assert.notNull(type,"不能为空");
    	return applicationContext.getBean(name, type);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name) {
    	Assert.hasText(name,"不能为空");
        return applicationContext.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
    	Assert.hasText(name,"不能为空");
        return applicationContext.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
    	Assert.hasText(name,"不能为空");
        return applicationContext.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
    	Assert.hasText(name,"不能为空");
        return applicationContext.getAliases(name);
    }
}
