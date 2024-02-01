package com.xq.api;

import com.xq.config.AppConfig;
import com.xq.context.AnnotionContextApplication;
import com.xq.dao.impl.UserDaoImpl;
import com.xq.service.impl.UserServiceImpl;

public class Application {

    public static void main(String[] args) throws Exception {
        AnnotionContextApplication annotionContextApplication = new AnnotionContextApplication(AppConfig.class);

//        UserDaoImpl userDao = (UserDaoImpl) annotionContextApplication.getBean("userDao");
//        userDao.test();
        UserServiceImpl userService = (UserServiceImpl) annotionContextApplication.getBean("userService");
        userService.test();

    }
}
