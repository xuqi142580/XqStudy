package com.xq.context;

import com.xq.annotion.*;
import com.xq.config.AppConfig;
import com.xq.pojo.BeanDefintion;
import com.xq.support.BeanDefintionReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class AnnotionContextApplication {
    //配置文件
    Class<AppConfig> appConfigClassList;
    //bean的名称
    Set<String> DEFAULT_BEANCLASSNAME = new HashSet<>();
    List<BeanDefintion> beanDefintionList = new ArrayList<BeanDefintion>();
    //已经加载完成的单例bean对象
    ConcurrentHashMap<String, Object> singletonBeanHashMap = new ConcurrentHashMap<>();
    //已经加载完成的原型bean对象
    ConcurrentHashMap<String, Object> normalBeanHashMap = new ConcurrentHashMap<>();

    public AnnotionContextApplication(Class<AppConfig> appConfigClass) {
        //初始化配置文件
        appConfigClassList = appConfigClass;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void refresh() throws Exception {
        //解析配置文件
        BeanDefintionReader beanDefintionReader = new BeanDefintionReader(appConfigClassList);
        DEFAULT_BEANCLASSNAME = beanDefintionReader.DEFAULT_BEANCLASSNAME;
        //设置bean的元配置信息
        setBeanDefintion();
        //初始化非懒加载的bean
        initNotLazyBean();
        //为已初始化的bean注入依赖
        dependencyInjection();
    }


    /****
     * 为已初始化的bean注入依赖
     * @throws Exception
     */
    public void dependencyInjection() throws Exception {
        //给单例池容器中的对象注入依赖
        doDependencyInjection(singletonBeanHashMap);
        //给非单例池容器中的对象注入依赖
        doDependencyInjection(normalBeanHashMap);
    }


    /****
     * 根据传入的数据源去注入依赖
     * @param resourceBeanHashMap
     * @throws Exception
     */
    public void doDependencyInjection(ConcurrentHashMap<String, Object> resourceBeanHashMap) throws Exception {
        Set<String> beanNameSet = resourceBeanHashMap.keySet();
        for (String beanName : beanNameSet) {
            //获取到实例对象
            Class<?> classNameByBeanName = (Class<?>) resourceBeanHashMap.get(beanName);
            //获取实例对象的所有属性
            Field[] fieldList = classNameByBeanName.getDeclaredFields();
            //遍历属性
            for (Field field : fieldList) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType().getComponentType();
                if (field.isAnnotationPresent(Autowired.class)) {
                    //如果含有Autowired注解 则说明为依赖
                    //默认按类型装配
                    if (singletonBeanHashMap.containsKey(fieldType)) {

                    }
                } else if (field.isAnnotationPresent(Resource.class)) {

                }
            }
        }
    }


    /*****
     * 初始化非懒加载的bean
     * @throws Exception
     */
    public void initNotLazyBean() throws Exception {
        for (BeanDefintion beanDefintion : beanDefintionList) {
            if (beanDefintion.isLazy()) {
                continue;
            }
            String beanNameClass = beanDefintion.getBeanNameClass();
            Object classInstance = Class.forName(beanNameClass).newInstance();
            String beanTypeName = beanDefintion.getBeanTypeName();
            String scope = beanDefintion.getScope();
            if (scope.equals("singleton")) {
                //如果是单例 则存入单例池
                singletonBeanHashMap.put(beanNameClass, classInstance);
                singletonBeanHashMap.put(beanTypeName, classInstance);
            } else {
                //非单例 则存入普通的bean容器中
                normalBeanHashMap.put(beanNameClass, classInstance);
                normalBeanHashMap.put(beanTypeName, classInstance);
            }
        }
    }


    /****
     * 设置bean的元配置信息
     * @throws ClassNotFoundException
     */
    public void setBeanDefintion() throws ClassNotFoundException {
        for (String beanClassName : DEFAULT_BEANCLASSNAME) {
            BeanDefintion beanDefintion = new BeanDefintion();
            Class<?> aClass = Class.forName(beanClassName);
            if (aClass.isAnnotationPresent(Component.class)) {
                Component component = aClass.getAnnotation(Component.class);
                beanDefintion.setBeanNameClass(component.value());
                if (aClass.isAnnotationPresent(Scope.class)) {
                    Scope scope = aClass.getAnnotation(Scope.class);
                    beanDefintion.setScope(scope.value());
                }
                if (aClass.isAnnotationPresent(Lazy.class)) {
                    Lazy lazy = aClass.getAnnotation(Lazy.class);
                    beanDefintion.setLazy(lazy.value());
                }
                //设置bean的类型
                String[] split = beanClassName.split("\\.");
                beanDefintion.setBeanTypeName(setBeanType(split[split.length - 1]));
                beanDefintionList.add(beanDefintion);
            }
        }
    }


    /****
     * 获取bean的类型
     * @param resourceFileName
     * @return
     */
    public String setBeanType(String resourceFileName) {
        //设置BeanType
        return String.valueOf(resourceFileName.charAt(0)).toLowerCase()
                + resourceFileName.substring(1);
    }

    /****
     * 根据bean 的类型去读取bean
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        return null;
    }
}
