package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.aspectj.lang.annotation.AfterReturning;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Stack;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTests {
    @Autowired
    private AlphaService alphaService;

    @Test
    public void testsave1(){
        Object obj=alphaService.save1();
        System.out.println(obj);
    }
    
}
