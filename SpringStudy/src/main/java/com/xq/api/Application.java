package com.xq.api;

import com.xq.config.AppConfig;
import com.xq.context.AnnotionContextApplication;
import com.xq.dao.UserDaoRepository;
import com.xq.service.UserServiceRepository;

public class Application {

    public static void main(String[] args) throws Exception {
        AnnotionContextApplication annotionContextApplication = new AnnotionContextApplication(AppConfig.class);
        UserDaoRepository userDao = (UserDaoRepository) annotionContextApplication.getBean("userDao");
        UserServiceRepository userService = (UserServiceRepository) annotionContextApplication.getBean("userService");
        userDao.test();
        userService.test();
    }
}
