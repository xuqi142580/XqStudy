package com.xq.service.impl;


import com.xq.annotion.Autowired;
import com.xq.annotion.Component;
import com.xq.annotion.Resource;
import com.xq.annotion.Scope;
import com.xq.dao.BeanNameAware;
import com.xq.dao.impl.UserDaoImpl;
import com.xq.service.UserServiceRepository;

@Scope("prop")
@Component("userService")
public class UserServiceImpl implements UserServiceRepository, BeanNameAware {

    public UserServiceImpl() {
    }

    public UserServiceImpl(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    @Autowired
    private UserDaoImpl userDao;


    @Override
    public void test() {
        userDao.test();
        System.out.println("这是一个测试依赖注入的方法！！！！！！！！！！！！！！！！！！！！！！！！");
    }

    @Override
    public void setBeanName(String beanName) {
        System.out.println(beanName);
    }
}
