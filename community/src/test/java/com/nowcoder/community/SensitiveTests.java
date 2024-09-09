package com.nowcoder.community;
import com.nowcoder.community.util.MailClient;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;


import org.thymeleaf.context.Context;
import javax.naming.NamingException;
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Test
    public void   testSensitiveFilter(){
        String message="嫖娼，开票，哈哈哈哈，这里可以吸毒。";
        String text=sensitiveFilter.filter(message);
        System.out.println(text);
    }
}
