package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    public static final Logger logger= LoggerFactory.getLogger(LoginTicketInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从Cookie中获取凭证
        String ticket= CookieUtil.getValue(request,"ticket");
        //GPT
        if (ticket != null) {
            ticket = URLDecoder.decode(ticket, "UTF-8"); // 对ticket进行解码,之前的问题在于没有解码
        }
        //String sanitizedTicket = ticket.replace("+", " ");
        //System.out.println("loginTicketInterceptor:  "+ticket);

        if (ticket != null) {
            logger.info("获取到的ticket: " + ticket);
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                logger.info("找到的用户: " + user.getUsername());
                //本次请求当中持有的用户对象
                hostHolder.setUser(user);
                //构建用户认证的结果，并存入到SecurityContext中，便于对Security进行授权
               Authentication authentication=new UsernamePasswordAuthenticationToken(user,user.getPassword(),
                       userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

            } else {
                logger.warn("无效的登录凭证或凭证已过期");
            }
        } else {
            logger.warn("未找到ticket的cookie");
        }


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user=hostHolder.getUser();
        if(user!=null&&modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
