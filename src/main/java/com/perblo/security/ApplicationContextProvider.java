package com.perblo.security;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

//@Component("applicationContextProvider")
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext ctx = null;

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public static Object getBean(String beanName) {
        return ctx.getBean(beanName);
    }

    @Override
    public void setApplicationContext(ApplicationContext ac)
            throws BeansException {
        ctx = ac;
    }
}
