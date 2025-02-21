package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.dao.AlphaDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

    private  ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
    @Test
    public  void testApplicationContext(){
        System.out.println(applicationContext);
        AlphaDao alphaDao=applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());
        alphaDao=applicationContext.getBean("MyBatis",AlphaDao.class);
        System.out.println(alphaDao.select());
    }
    @Test
    public void Test_BeanManagement(){
        AlphaService alphaService=applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
    }
    @Test
    public void TestBeanConfig(){
            System.out.println(new Date());
            SimpleDateFormat simpleDateFormat=applicationContext.getBean(SimpleDateFormat.class);
            System.out.println("\n"+simpleDateFormat.format(new Date()));
        }
    @Autowired
    @Qualifier("MyBatis")//这里是原本的写了@Repository的更改了名字的内容
    private  AlphaDao alphaDao;
    @Autowired
    private AlphaService alphaService;
    @Autowired
    private  SimpleDateFormat simpleDateFormat;
    @Test
    public  void TestDi(){//采用这种方式即可将结果直接注入对应的实例对象中
        System.out.println(alphaDao);
        System.out.println(alphaService);
        System.out.println(simpleDateFormat);
    }
    }
