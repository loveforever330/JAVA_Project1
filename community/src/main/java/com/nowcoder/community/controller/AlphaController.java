package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String show() {
        return "MYX love ZJK.";
    }

    @RequestMapping("/MoMo")
    @ResponseBody
    public String use_service() {
        return alphaService.find();
    }

    @RequestMapping("/http")

    public void http(HttpServletRequest request, HttpServletResponse response) {
        //这里的是请求行
        //处理请求，获取请求数据,获取请求方式
        System.out.println(request.getMethod());
        //处理请求，获取请求路径
        System.out.println(request.getServletPath());
        //这里的是若干行的请求头
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println("name；" + name + " value:" + value);
        }
        //下面这里的是请求体
        System.out.println(request.getParameter("code"));

        /**
         * 下面的是返回响应的数据
         */
        response.setContentType("text/html;charset=utf-8");
        try
                (PrintWriter writer = response.getWriter();) {//绘制一行文本内容
            writer.write("<h>GENCO love MoMo</h>");

        } catch (IOException e) {
            e.printStackTrace();

        }

    }
    //GET请求的对应操作

    //student?current=1&&limit=20
    //？后面的往往将是对应的参数
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    @ResponseBody
    public  String  getStudents(@RequestParam(name="current",required = false,defaultValue = "1") int current,
                                @RequestParam(name="limit",required = false,defaultValue = "10") int limit
                                ){
        System.out.println(limit);
        System.out.println(current);
        return "great";
    }

    // /student/123
    @RequestMapping
            (path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "enter great number";
    }
    //post 请求
    //保存
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    //响应html数据
    //动态的传数据
    @RequestMapping(path="/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView  mav=new ModelAndView();
        mav.addObject("name","张金柯");
        mav.addObject("age",20);
        mav.setViewName("/demo/view");
        return  mav;
    }
    //另外的一种方式做响应
    @RequestMapping(path = "/school",method = RequestMethod.GET)
    public String GetSchool(Model model){
        model.addAttribute("name","BJFU");
        model.addAttribute("age",90);
        return  "/demo/view";
    }
    //响应json的异步请求
    //java的对象 转换为 js对象，使用json字符串


    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object>getEmp(){
        Map<String,Object> emp=new HashMap<>();
        emp.put("name" ,"张三");
        emp.put("age",23);
        emp.put("salary",8000);
        return  emp;

    }

    //多个json对象的结果
    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getEmps(){
            List<Map<String,Object>> list=new ArrayList<>();
            Map<String,Object> emp=new HashMap<>();
            emp.put("name","张金柯");
            emp.put("age",20);

            list.add(emp);

            emp=new HashMap<>();
            emp.put("name","莫钰娴");
            emp.put("age",21);

            list.add(emp);

            return list;
    }
    //cookie示例
    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) throws UnsupportedEncodingException {
        String uuid = CommunityUtil.generateUUID();
        String encodedUUID = URLEncoder.encode(uuid, "UTF-8");//不能够有空格所以需要处理一下编码
        //创建cookie
        Cookie cookie=new Cookie("code",encodedUUID);
        //设置cookie的范围
        cookie.setPath("/community/alpha");
        //设置cookie的生存时间
        cookie.setMaxAge(60*10);//十分钟
        //发送cookie
        response.addCookie(cookie);

        return "set cookie";

    }

    @RequestMapping(path = "/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return  "get Cookie";
    }
    //session示例

    @RequestMapping(path="/session/set",method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        return "set session";
    }
    @RequestMapping(path="/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }
}
