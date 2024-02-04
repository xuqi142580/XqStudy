package com.xq.support;

import com.xq.annotion.Component;
import com.xq.annotion.ComponentScan;
import com.xq.config.AppConfig;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeanDefintionReader {

    //配置的包数组
    String[] appConfigPackageList;
    //默认后缀为.class文件
    private final String DEFAULT_SUFFIX = ".class";

    //获取当前类加载器
    private final ClassLoader classLoader = this.getClass().getClassLoader();
    //获取当前输出路径
    private final String outPutPath = classLoader.getResource("").getPath().substring(1);
    //bean的名称
    public Set<String> DEFAULT_BEANCLASSNAME = new HashSet<>();

    /****
     * 解析配置文件
     * @param appConfigClassList
     * @return
     */
    public BeanDefintionReader(Class<AppConfig> appConfigClassList) throws ClassNotFoundException {
        //解析配置文件获取包名
        setAppConfigPackage(appConfigClassList);
        //解析配置文件中的包名
        scanPackageList();
    }


    /****
     * 解析配置文件获取配置的包名
     * @return
     */
    public void setAppConfigPackage(Class<AppConfig> appConfigClassList) {
        ComponentScan annotation = appConfigClassList.getAnnotation(ComponentScan.class);
        if (annotation != null) {
            appConfigPackageList = annotation.value();
        }
    }


    public void scanPackageList() throws ClassNotFoundException {
        //遍历配置包
        for (String appConfigPackage : appConfigPackageList) {
            //将配置包转为文件格式
            appConfigPackage = appConfigPackage.replaceAll("\\.", "/");
            //利用类加载器获取资源
            URL resource = classLoader.getResource(appConfigPackage);
            //资源文件
            File resourceFile = new File(resource.getFile());
            doScanPackage(resourceFile);
        }
    }


    /****
     * 扫描单个包
     */
    public void doScanPackage(File resourceFile) throws ClassNotFoundException {
        if (resourceFile.isDirectory()) {
            //解析文件夹下的文件
            for (File next_resourceFile : resourceFile.listFiles()) {
                //递归调用解析文件夹下的文件
                doScanPackage(next_resourceFile);
            }
        } else {
            //获取文件名称
            String resourceFileName = resourceFile.getName();
            //获取文件路径
            String resultFilePath = resourceFile.getPath();
            //解析文件路径
            resultFilePath = resultFilePath.substring(outPutPath.length(), resultFilePath.length() - DEFAULT_SUFFIX.length());
            resultFilePath = resultFilePath.replaceAll("\\\\", ".");
            //是否为.class文件
            //获取class对象
            //如果含有Component注解说明是一个bean
            if (isClassFile(resourceFileName)) {
                //存储bean的全类名路径
                DEFAULT_BEANCLASSNAME.add(resultFilePath);
            }
        }
    }




    /****
     * 是否为class文件
     * @param resourceFileName
     * @return
     */
    public boolean isClassFile(String resourceFileName) {
        return resourceFileName.endsWith(DEFAULT_SUFFIX);
    }
}
