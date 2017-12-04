package com.zjaxn.jobs.service.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class SpringUtils implements ApplicationContextAware {
    private static ApplicationContext atx;

    @Override
    public void setApplicationContext(ApplicationContext atx)
            throws BeansException {
        // TODO Auto-generated method stub
        this.atx = atx;
    }

    public static ApplicationContext getAtx() {
        return atx;
    }

    public static Object getBean(String beanName) {
        return getAtx().getBean(beanName);
    }

    public static  <T> T getBean(Class<T> requiredType){
        return atx.getBean(requiredType);
    }
}
