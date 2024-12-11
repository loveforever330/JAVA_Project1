package com.nowcoder.community.service;

import com.mysql.cj.util.StringUtils;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.elasticsearch.client.security.GrantApiKeyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;


    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private  String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id){

        //return userMapper.selectById(id);
        User user =getCache(id);
        if(user==null){
            user=initCache(id);

        }
        return user;
    }


    //用户注册
    public Map<String,Object> register(User user) throws IllegalAccessException {
        Map<String,Object> map=new HashMap<>();

        //对空值做一个处理
        if(user==null){
            throw new IllegalAccessException("参数不能够为空");
        }
        if(StringUtils.isNullOrEmpty(user.getUsername())){
            map.put("usernameMsg","账号不能够为空！");//将对应的消息Msg存入到map中去
            return  map;
        }
        if(StringUtils.isNullOrEmpty(user.getPassword())){
            map.put("passwordMsg","密码不能够为空！");
            return  map;
        }
        if(StringUtils.isNullOrEmpty(user.getEmail())){
            map.put("emailMsg","邮箱不能够为空！");
            return  map;
        }

        //验证账号
        User u=userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","该账号已存在!");
            return  map;
        }
        //验证邮箱
        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","该邮箱已经被注册");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);//类型
        user.setStatus(0);//状态
        user.setActivationCode(CommunityUtil.generateUUID());//激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context=new Context() ;//发送邮件
        context.setVariable("email",user.getEmail());
        //http://loaclhost:8080/community/activation/101/code
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    //下面的是激活码的内容
    public int activation(int userId,String code){
        User user=userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.     updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }
        else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map=new HashMap<>();

        //空值处理
        if(StringUtils.isNullOrEmpty(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isNullOrEmpty(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        //验证账号
        User user=userMapper.selectByName(username);
        if(user== null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        //验证状态
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未能激活！");
            return map;
        }
        //验证密码
        password=CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }
        //如果以上都ok的话，下面将生成登录凭证
        //这个表有点类似与session
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
       // loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey= RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return  map;
    }

    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket,1);//凭证更改为1，表示无效
        String redisKey=RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket=(LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);


    }

    public LoginTicket findLoginTicket(String ticket){
        String sanitizedTicket = ticket.replace("+", " ");
        System.out.println("UserService: "+sanitizedTicket);
        //return  loginTicketMapper.selectByTicket(sanitizedTicket);
        String redisKey=RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket)  redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId,String headerUrl){
       int rows= userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;

    }

    public int updatePassword(int userId,String password){
        return userMapper.updatePassword(userId,password);
    }



    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //1.优先从缓存当中去取值，
    private  User getCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);

    }

    //2.找不到时初始化缓存的数据
    private User initCache(int userId){
        User user=userMapper.selectById(userId);
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;

    }
    //3.数据变更的时候清除缓存数据

    private  void clearCache(int userId){
        String rediskey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(rediskey);
    }

    public Collection<?extends GrantedAuthority> getAuthorities(int userId){
        User user=this.findUserById(userId);


        List<GrantedAuthority> list=new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return  list;
    }


}
