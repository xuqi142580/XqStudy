package com.xq.pojo;

public class BeanDefintion {


    public BeanDefintion(Class className, String scope) {
        this.className = className;
        this.scope = scope;
    }



    public Class getClassName() {
        return className;
    }

    public void setClassName(Class className) {
        this.className = className;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    private Class className;
    private String scope;

}
