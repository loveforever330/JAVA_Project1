package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class ThreadPoolTests {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);
    //Spring的普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;


    //JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);


    //JDK执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    //Spring的定时线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    //注入AlphaService
    @Autowired
    private AlphaService alphaService;

    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //1.JDK的线程池
    @Test
    public void testExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello,ExecutorService");
            }
        };
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);

        }
        sleep(10000);
    }

    //2.JDK的定时任务线程池
    @Test
    public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("make sure Time ExecutorService");
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);
        sleep(30000);
    }

    //3.Spring的线程池
    @Test
    public void testThreadPoolTaskExecutor() {//比jdk的更加的灵活
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskExecutor");
            }
        };
        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }
        sleep(100000);
    }

    //4.Spring的定时任务的线程池
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskScheduler");
            }
        };
        Date startTime=new Date(System.currentTimeMillis()+10000);
        taskScheduler.scheduleAtFixedRate(task,startTime,1000);
        sleep(30000);
    }
    //5.Spring的普通线程池的简化
    @Test
    public void testThreadPoolTaskExecutorSimple(){
        for(int i=0;i<10;i++){
            alphaService.execute1();
        }

        sleep(10000);
    }

    //6.spring定时任务的线程池
    @Test
    public void testThreadPoolTaskScheduledSimple(){
        sleep(30000);
    }
}


