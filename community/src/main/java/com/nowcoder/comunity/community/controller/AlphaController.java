package com.nowcoder.comunity.community.controller;

import com.nowcoder.comunity.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;
    @RequestMapping("/hello")
    @ResponseBody
    public String show(){
        return  "MYX love ZJK.";
    }

    @RequestMapping("/MoMo")
    @ResponseBody
    public String use_service(){
        return alphaService.find();
    }
}
