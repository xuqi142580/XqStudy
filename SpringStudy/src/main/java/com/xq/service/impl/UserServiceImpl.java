package com.xq.service.impl;

import com.xq.annotion.Autowired;

import com.xq.annotion.Component;
import com.xq.annotion.Resource;
import com.xq.annotion.Scope;
import com.xq.dao.impl.UserDaoImpl;
import com.xq.service.UserServiceRepository;

@Scope
@Component("userService")
public class UserServiceImpl implements UserServiceRepository {

    public UserServiceImpl() {
    }

    public UserServiceImpl(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    @Resource
    private UserDaoImpl userDao;


    @Override
    public void test() {
        userDao.test();
        System.out.println("这是一个测试依赖注入的方法！！！！！！！！！！！！！！！！！！！！！！！！");
    }
}
