package com.nowcoder.community;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public void  testSelectUser(){
        User user=userMapper.selectById(150);
        System.out.println(user);
    }

    @Test
    public  void testInsertUser(){
        User user=new User();
        user.setUsername("zhangjinke");
        user.setPassword("123456");
        user.setEmail("zhangjinke@qq.com");
        user.setSalt("abcd");
        user.setHeaderUrl("http://www.newcoder/com/120.png");
        user.setCreateTime(new Date());

        int rows=userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser(){
        int rows=userMapper.updateStatus(150,1);
        System.out.println(rows);
         rows=userMapper.updateHeader(150,"http://www.newcoder.com/102.png");
        System.out.println(rows);
    }

    @Test
    public void  testSelectPosts(){
        List<DiscussPost>  list=discussPostMapper.selectDiscussPosts(149,0,10);
        for(DiscussPost post:list){
            System.out.println(post);
        }
        int rows= discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
}
