package com.nowcoder.comunity.community.service;

import com.nowcoder.comunity.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {
    @Autowired
    @Qualifier("MyBatis")
    private AlphaDao alphaDao;


    //下面这里将会用于判断是否在构造器之前还是之后调用了这个init方法
    public  AlphaService(){
        System.out.println("初始化AlphaService");
    }
    @PostConstruct//构造之后进行初始化
    public  void init(){
        System.out.println("Init构造器构造Bean之后");
    }
    @PreDestroy//在销毁对象之前去调用
    public void destory(){
        System.out.println("Destory销毁Bean之前");
    }

    public String find(){
        return alphaDao.select();
    }
}
