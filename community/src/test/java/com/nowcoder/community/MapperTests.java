package com.nowcoder.community;
import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.entity.LoginTicket;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private MessageMapper messageMapper;
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
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket =loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc",0);
        loginTicket=loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }
    @Test
    public void testInsertDiscussPost(){
        DiscussPost post=new DiscussPost();
        post.setUserId(12);
        post.setTitle("zjk123");
        post.setContent("zzjzjjzjz");
        discussPostMapper.insertDiscussPost(post);
    }
    @Test
    public void testSelectDiscussPost(){
        DiscussPost post=new DiscussPost();
        post=discussPostMapper.selectDiscussPostById(2);

    }
    @Test
    public void testCommentMapper(){
        List<Comment> commentList=new ArrayList<>();
        commentList=commentMapper.selectCommentsByEntity(1,228,3,8);
        int id=commentMapper.selectCountByEntity(1,228);
    }
    @Test
    public void testInsertComment() {
        Comment comment = new Comment();
        comment.setUserId(123);
        comment.setEntityType(1);
        comment.setEntityId(101);
        comment.setTargetId(0);
        comment.setContent("测试评论");
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        int rows = commentMapper.insertComment( comment);
        assertEquals(1, rows);

        // 验证插入的记录是否正确
        List<Comment> comments = commentMapper.selectCommentsByEntity(1, 101, 0, 1);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("测试评论", comments.get(0).getContent());
    }
   @Test
    public void testSelectletters(){
        List<Message> list=messageMapper.selectConversations(111,0,10);
        for(Message message:list){
            System.out.println(message);
        }
        int count= messageMapper.selectConversationCount(111);
       System.out.println(count);
       list=messageMapper.selectLetters("111_112",0,0);
       for(Message message:list){
           System.out.println(message);
       }
       messageMapper.selectLetterCount("111_112");
       System.out.println(count);
   }

}
