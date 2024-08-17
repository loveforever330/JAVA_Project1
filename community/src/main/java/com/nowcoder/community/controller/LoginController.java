package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @RequestMapping(path="/register",method= RequestMethod.GET)//注册的
    public String getRegisterPage(){
        return"/site/register";
    }
    @RequestMapping(path = "/login",method = RequestMethod.GET)//登录的页面的服务
    public String getLoginPage(){return "/site/login";}

    @RequestMapping(path="/register",method=RequestMethod.POST)//注册的内容
    public String register(Model model, User user) throws IllegalAccessException {
        Map<String,Object> map=userService.register(user);
        if(map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));//注册的用户信息
            model.addAttribute("passwordMsg",map.get("passwordMsg"));//注册的用户密码的信息
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }

    }


    @RequestMapping(path = "/activation/{userId}/{code}",method=RequestMethod.GET)//这里是把内容给到model携带，然后返回给到前端
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
      int result=userService.activation(userId,code);
      if(result==ACTIVATION_SUCCESS){
        model.addAttribute("msg","激活成功，您的账号已经可以使用了！");
        model.addAttribute("target","/login");

      }
      else if(result==ACTIVATION_REPEAT){
        model.addAttribute("msg","无效操作，该账号已经激活过了");
        model.addAttribute("target","/index");
      }
      else {
          model.addAttribute("msg","激活失败，您的激活码不正确");
          model.addAttribute("target","/index");
      }
      return "/site/operate-result";
      }

}
