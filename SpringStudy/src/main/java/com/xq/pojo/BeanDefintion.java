package com.xq.pojo;

public class BeanDefintion {


    public String getBeanNameClass() {
        return beanNameClass;
    }

    public void setBeanNameClass(String beanNameClass) {
        this.beanNameClass = beanNameClass;
    }

    public String getBeanTypeName() {
        return beanTypeName;
    }

    public void setBeanTypeName(String beanTypeName) {
        this.beanTypeName = beanTypeName;
    }

    public boolean isLazy() {
        return isLazy;
    }

    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    private String beanNameClass;
    private String beanTypeName;
    private boolean isLazy;
    private String scope;

}
