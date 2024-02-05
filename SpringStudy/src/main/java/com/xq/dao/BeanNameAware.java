package com.xq.dao;

public interface BeanNameAware extends Aware {
    //设置bean的id
    void setBeanName(String beanName);
}
