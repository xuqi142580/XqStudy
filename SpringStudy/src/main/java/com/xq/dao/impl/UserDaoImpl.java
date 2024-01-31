package com.xq.dao.impl;

import com.xq.annotion.Component;
import com.xq.annotion.Scope;
import com.xq.dao.UserDaoRepository;

@Scope("prope")
@Component("userDao")
public class UserDaoImpl implements UserDaoRepository {
    @Override
    public void test() {
        System.out.println("这是一个注解Bean的测试方法！");
    }
}
