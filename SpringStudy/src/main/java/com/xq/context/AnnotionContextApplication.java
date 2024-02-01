package com.xq.context;

import com.xq.annotion.*;
import com.xq.config.AppConfig;
import com.xq.pojo.BeanDefintion;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class AnnotionContextApplication {

    private final String SUFFIX = ".class";
    private String DEFAULT_BEAN_TYPE = "singleton";
    private HashMap<String, BeanDefintion> BeanDefintionHashMap = new HashMap<String, BeanDefintion>();
    private ConcurrentHashMap<String, Object> singletonHashMap = new ConcurrentHashMap<String, Object>();

    public AnnotionContextApplication(Class<AppConfig> appConfigClass) {

        try {
            ComponentScan annotation = appConfigClass.getDeclaredAnnotation(ComponentScan.class);
            Assert.notNull(annotation, "注解为空");
            //获取当前类加载器
            ClassLoader classLoader = this.getClass().getClassLoader();
            //获取当前输出路径
            String outPutPath = classLoader.getResource("").getPath().substring(1);
            //获取配置的包
            String[] packageList = annotation.value();
            //校验配置包
            Assert.notEmpty(packageList, "the package resource Data is null!");
            //遍历扫描包
            for (String packageUrl : packageList) {
                //单个扫描包
                scanByPackageUrl(packageUrl, classLoader, outPutPath);
            }
            //通过元信息注册bean
            registerBean();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /****
     * 扫描包路径
     * @param packageUrl
     * @param classLoader
     * @param outPutPath
     * @throws Exception
     */
    public void scanByPackageUrl(String packageUrl, ClassLoader classLoader, String outPutPath) throws Exception {
        //解析包路径
        packageUrl = packageUrl.replaceAll("\\.", "/");
        URL url = classLoader.getResource(packageUrl);
        //获取包路径里的文件
        File resourceFile = new File(url.getFile());
        //判断资源是否为一个文件夹
        if (resourceFile.isDirectory()) {
            //获取文件夹中的所有文件
            File[] resourceFileList = resourceFile.listFiles();
            //遍历文件
            for (File resultFile : resourceFileList) {
                String resultFilePath = resultFile.getPath();
                if (resultFilePath.endsWith(SUFFIX)) {
                    //解析文件名称赋值为目标bean的名字
                    String resultFileName = resultFile.getName().replaceAll(SUFFIX, "");
                    resultFileName = String.valueOf(resultFileName.charAt(0)).toLowerCase() + resultFileName.substring(1);
                    //解析文件路径
                    resultFilePath = resultFilePath.substring(outPutPath.length(), resultFilePath.length() - SUFFIX.length());
                    resultFilePath = resultFilePath.replaceAll("\\\\", ".");
                    //获取bean对象
                    Class<?> className = Class.forName(resultFilePath);
                    //是否含有Compoent的注解
                    if (className.isAnnotationPresent(Component.class)) {
                        String beanName = className.getDeclaredAnnotation(Component.class).value();
                        if (!StringUtils.isEmpty(beanName)) {
                            beanName = resultFileName;
                        }
                        //是否含有scope注解 标识为单例 原型等
                        if (className.isAnnotationPresent(Scope.class)) {
                            DEFAULT_BEAN_TYPE = className.getDeclaredAnnotation(Scope.class).value();
                        }
                        //解析是否含有依赖注入
                        Field[] resourceFieldList = className.getDeclaredFields();
                        for (Field resourceField : resourceFieldList) {
                            if (resourceField.isAnnotationPresent(Autowired.class) || resourceField.isAnnotationPresent(Resource.class)) {
                                //如果含有Autowired、Resource注解说明是依赖
                                //首先按类型注入
                                String beanType = resourceField.getType().toString();
                                beanType = String.valueOf(beanType.charAt(0)).toLowerCase() + beanType.substring(1);
                                //
                                BeanDefintion beanDefintion = BeanDefintionHashMap.get(beanType);
                                Object bean = null;
                                if (ObjectUtils.isEmpty(beanDefintion) && resourceField.isAnnotationPresent(Resource.class)) {
                                    //其次按名称注入
                                    String name = resourceField.getName();
                                    bean = getBean(name);
                                }
                                String scope = beanDefintion.getScope();
                                if (scope.equals("singleton")) {
                                    //如果是单例
                                    bean = singletonHashMap.get(beanType);
                                } else {
                                    bean = createBeanByBeanDefintion(beanDefintion.getClassName());
                                }
                                System.out.println(bean);
                                resourceField.setAccessible(true);
                                className.getConstructor(Object.class).newInstance(bean);
                            }
                        }
                        //注册bean 的元配置信息
                        BeanDefintion beanDefintion = new BeanDefintion(className, DEFAULT_BEAN_TYPE);
                        BeanDefintionHashMap.put(beanName, beanDefintion);
                    }
                } else {
                    //如果是文件夹 递归解析文件夹
                    recursionAnaFile(resultFile, outPutPath);
                }
            }
        }

    }

    /*****
     * 注册bean对象
     * @throws Exception
     */
    public void registerBean() throws Exception {
        Set<String> beanNameList = BeanDefintionHashMap.keySet();
        for (String beanName : beanNameList) {
            BeanDefintion beanDefintion = BeanDefintionHashMap.get(beanName);
            String scope = beanDefintion.getScope();
            Class<?> className = beanDefintion.getClassName();
            if (scope.equals("singleton")) {
                //单例
                singletonHashMap.put(beanName, className.newInstance());
            } else {
                //原型
                createBeanByBeanDefintion(className);
            }
        }
    }


    /****
     * 重新创建bean
     * @param className
     * @return
     * @throws Exception
     */
    public Object createBeanByBeanDefintion(Class<?> className) throws Exception {
        return className.getConstructor().newInstance();
    }

    /****
     * 递归解析文件夹
     * @param resourceFile
     * @param outPutPath
     * @throws Exception
     */
    public void recursionAnaFile(File resourceFile, String outPutPath) throws Exception {
        if (resourceFile.isDirectory()) {
            File[] resourceFileList = resourceFile.listFiles();
            for (File resultFile : resourceFileList) {
                String resultFilePath = resultFile.getPath();
                if (resultFilePath.endsWith(SUFFIX)) {
                    //解析文件名称赋值为目标bean的名字
                    String resultFileName = resultFile.getName().replaceAll(SUFFIX, "");
                    resultFilePath = resultFilePath.substring(outPutPath.length(), resultFilePath.length() - SUFFIX.length());
                    resultFilePath = resultFilePath.replaceAll("\\\\", ".");
                    Class<?> className = Class.forName(resultFilePath);
                    //是否含有Compoent的注解
                    if (className.isAnnotationPresent(Component.class)) {
                        String beanName = className.getDeclaredAnnotation(Component.class).value();
                        if (StringUtils.isEmpty(beanName)) {
                            beanName = resultFileName;
                        }
                        //是否含有scope注解 标识为单例 原型等
                        if (className.isAnnotationPresent(Component.class)) {
                            DEFAULT_BEAN_TYPE = className.getDeclaredAnnotation(Scope.class).value();
                        }
                        //解析是否含有依赖注入
                        Field[] resourceFieldList = className.getDeclaredFields();
                        for (Field resourceField : resourceFieldList) {
                            if (resourceField.isAnnotationPresent(Autowired.class) || resourceField.isAnnotationPresent(Resource.class)) {
                                //如果含有Autowired、Resource注解说明是依赖
                                //首先按类型注入
                                String beanType = resourceField.getType().toString();
                                beanType = String.valueOf(beanType.charAt(0)).toLowerCase() + beanType.substring(1);
                                //
                                BeanDefintion beanDefintion = BeanDefintionHashMap.get(beanType);
                                Object bean = null;
                                if (ObjectUtils.isEmpty(beanDefintion) && resourceField.isAnnotationPresent(Resource.class)) {
                                    //其次按名称注入
                                    String name = resourceField.getName();
                                    bean = getBean(name);
                                } else {
                                    String scope = beanDefintion.getScope();
                                    if (scope.equals("singleton")) {
                                        //如果是单例
                                        bean = singletonHashMap.get(beanType);
                                    } else {
                                        bean = createBeanByBeanDefintion(beanDefintion.getClassName());
                                    }
                                }
                                Class<?> aClass = bean.getClass();
                                resourceField.setAccessible(true);
                                System.out.println(bean);
                                className.getConstructor(aClass).newInstance(bean);
                            }
                        }
                        //注册bean 的元配置信息
                        BeanDefintion beanDefintion = new BeanDefintion(className, DEFAULT_BEAN_TYPE);
                        BeanDefintionHashMap.put(beanName, beanDefintion);

                    }
                } else {
                    recursionAnaFile(resultFile, outPutPath);
                }
            }
        }
    }


    /****
     * 根据bean 的类型去读取bean
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) throws Exception {
        if (BeanDefintionHashMap.containsKey(beanName)) {
            BeanDefintion beanDefintion = BeanDefintionHashMap.get(beanName);
            String scope = beanDefintion.getScope();
            Class<?> className = beanDefintion.getClassName();
            if (scope.equals("singleton")) {
                //说明是单例
                return singletonHashMap.get(beanName);
            } else {
                return createBeanByBeanDefintion(className);
            }
        }
        return null;
    }
}
