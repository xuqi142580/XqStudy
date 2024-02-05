package com.xq.context;

import com.xq.annotion.*;
import com.xq.config.AppConfig;
import com.xq.dao.BeanNameAware;
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
    //按bean的类型与元配置信息的映射
    ConcurrentHashMap<Object, BeanDefintion> beanTypeBeanDefintion = new ConcurrentHashMap<>();
    //按bean的名称与元配置信息的映射
    ConcurrentHashMap<Object, BeanDefintion> beanNameBeanDefintion = new ConcurrentHashMap<>();

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
            Object resultClass = resourceBeanHashMap.get(beanName);
            Class<?> classNameByBeanName = resultClass.getClass();
            //注入依赖属性
            setFiledAttrByAutowired(resultClass, classNameByBeanName);
        }
    }


    /****
     * 对bean的字段进行赋值
     * @param resultClass
     * @param classNameByBeanName
     * @throws Exception
     */
    public void setFiledAttrByAutowired(Object resultClass, Class<?> classNameByBeanName) throws Exception {
        //获取实例对象的所有属性
        Field[] fieldList = classNameByBeanName.getDeclaredFields();
        //遍历属性
        for (Field field : fieldList) {
            Object object = null;
            String fieldName = field.getName();
            String fieldType = setBeanType(field.getType().getSimpleName());
            if (field.isAnnotationPresent(Autowired.class)) {
                object = getBean(fieldType);
                if (field.isAnnotationPresent(Resource.class) && object == null) {
                    object = getBean(fieldName);
                }
            }
            field.setAccessible(true);
            field.set(resultClass, object);
            field.setAccessible(false);
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
            String beanClassName = beanDefintion.getBeanClassName();
            Object classInstance = Class.forName(beanClassName).getConstructor().newInstance();
            String beanTypeName = beanDefintion.getBeanTypeName();
            String scope = beanDefintion.getScope();
            if ("singleton".equals(scope)) {
                //如果是单例 则存入单例池
                singletonBeanHashMap.put(beanNameClass, classInstance);
                singletonBeanHashMap.put(beanTypeName, classInstance);
            } else {
                //非单例 则每次使用则创建一次
                createBean(beanNameClass);
            }
        }
    }


    /****
     * 重新创建bean并进行初始化
     * @param beanName
     * @return
     */
    public Object createBean(String beanName) throws Exception {
        Object object = null;
        BeanDefintion beanDefintion = null;
        //尝试按类型去创建bean
        if (beanTypeBeanDefintion.containsKey(beanName)) {
            beanDefintion = beanTypeBeanDefintion.get(beanName);
        } else if (beanDefintion == null) {
            //尝试按名称去创建bean
            beanDefintion = beanNameBeanDefintion.get(beanName);
        }
        String beanClassName = beanDefintion.getBeanClassName();
        //返回已经创建的bean实例对象
        object = Class.forName(beanClassName).getConstructor().newInstance();
        //获取class对象
        Class<?> aClass = object.getClass();
        //为bean的字段注入属性
        setFiledAttrByAutowired(object, aClass);
        //是否实现了BeanNameAware接口
        if (object instanceof BeanNameAware) {
            ((BeanNameAware) object).setBeanName("这是一个测试Aware回调的方法");
        }
        return object;
    }


    /****
     * 设置bean的元配置信息
     * @throws ClassNotFoundException
     */
    public void setBeanDefintion() throws ClassNotFoundException {
        for (String beanClassName : DEFAULT_BEANCLASSNAME) {
            //初始化一个bean的元配置信息实体
            BeanDefintion beanDefintion = new BeanDefintion();
            //设置bean的类型
            String[] split = beanClassName.split("\\.");
            String beanType = setBeanType(split[split.length - 1]);
            beanDefintion.setBeanTypeName(beanType);
            beanDefintion.setBeanClassName(beanClassName);
            //实例化bean的对象
            Class<?> aClass = Class.forName(beanClassName);
            if (aClass.isInterface()) {
                //如果是接口则执行下一个循环
                continue;
            }
            if (aClass.isAnnotationPresent(Component.class)) {
                Component component = aClass.getAnnotation(Component.class);
                beanClassName = component.value();
                beanDefintion.setBeanNameClass(beanClassName);
                if (aClass.isAnnotationPresent(Scope.class)) {
                    Scope scope = aClass.getAnnotation(Scope.class);
                    beanDefintion.setScope(scope.value());
                }
                if (aClass.isAnnotationPresent(Lazy.class)) {
                    //是否为懒加载
                    Lazy lazy = aClass.getAnnotation(Lazy.class);
                    beanDefintion.setLazy(lazy.value());
                }
                //存入元配置信息集
                beanDefintionList.add(beanDefintion);
                //设置bean的类型和bean的元配置信息映射
                beanTypeBeanDefintion.put(beanType, beanDefintion);
                //设置bean的名称和bean的元配置信息映射
                beanNameBeanDefintion.put(beanClassName, beanDefintion);
            }
        }
    }


    /****
     * 首字母转小写
     * @param resourceFileName
     * @return
     */
    public String setBeanType(String resourceFileName) {
        return String.valueOf(resourceFileName.charAt(0)).toLowerCase()
                + resourceFileName.substring(1);
    }

    /****
     * 根据bean 的类型去读取bean
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) throws Exception {
        Object object = null;
        if (singletonBeanHashMap.containsKey(beanName)) {
            object = singletonBeanHashMap.get(beanName);
        } else {
            object = createBean(beanName);
        }
        return object;
    }
}
