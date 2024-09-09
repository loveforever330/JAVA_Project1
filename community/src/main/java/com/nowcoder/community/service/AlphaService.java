package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
public class AlphaService {
    @Autowired
    @Qualifier("MyBatis")
    private AlphaDao alphaDao;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;
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


    //REQUIRED： //支持当前事务，外部事务，调用者的事务，如果不存在则创建新事务
    //REQUIRES_NEW//创建一个新的事务，并暂停当前事务（外部事务）
    //NESTED//如果存在外部事务，则嵌套在外部事务中去执行（独立提交和回滚），否则就会和REQUIRED一样
    //调用不同的事务的时候会有交叉的情况这里是怎么去解决

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)//声明采用读已提交
    public  Object save1(){
        //新增用户

        User user=new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

    //新增帖子
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("zjk");

        return "ok";

    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                User user=new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl(" http://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post=new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("新人报道");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("zjk");

                return "ok";


            }
        });
    }
}
