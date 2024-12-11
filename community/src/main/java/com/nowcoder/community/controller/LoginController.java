package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private HostHolder hostHolder;
    @Value("${server.servlet.context-path")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)//注册的
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)//登录的页面的服务
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)//注册的内容
    public String register(Model model, User user) throws IllegalAccessException {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));//注册的用户信息
            model.addAttribute("passwordMsg", map.get("passwordMsg"));//注册的用户密码的信息
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }

    }


    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)//这里是把内容给到model携带，然后返回给到前端
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以使用了！");
            model.addAttribute("target", "/login");

        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活过了");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您的激活码不正确");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) throws UnsupportedEncodingException {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session
        //session.setAttribute("kaptcha",text);
        //验证码的归属
        String encodedUUID = CommunityUtil.generateUUID();
        String kaptchaOwner= URLEncoder.encode(encodedUUID, "UTF-8");
        kaptchaOwner = kaptchaOwner.replaceAll("\\+", "");
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        System.out.println("kaptchaOwner:"+kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);//这里的get被调用
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);
        //将图片传输给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme, Model model,
                        HttpSession session, HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) throws UnsupportedEncodingException {
        //String kaptcha=(String) session.getAttribute("kaptcha");
        //检查验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }
        //检查账号密码。交给业务层去做处理
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            //下面是去设置cookie
            String ticketValue = map.get("ticket").toString();

// 对 ticketValue 进行 URL 编码
            String encodedTicketValue = URLEncoder.encode(ticketValue, StandardCharsets.UTF_8.toString());
            //System.out.println(encodedTicketValue);
// 使用编码后的值创建 Cookie
            Cookie cookie = new Cookie("ticket", encodedTicketValue);
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        String sanitizedTicket = ticket.replace("+", " ");

        // 调用服务层的登出方法
        userService.logout(sanitizedTicket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";//重定向默认get请求
    }
}
