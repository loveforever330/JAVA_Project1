package com.nowcoder.community;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.logging.Logger;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTests {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(LoggerTests.class);//强制类型转换了
    @Test
    public void testLogger(){
        System.out.println(logger.getName());
        logger.info("debug log");
        logger.warning("warn log");
    }
}
