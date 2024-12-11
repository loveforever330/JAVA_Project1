package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.recycler.Recycler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


@Controller
@RequestMapping( "/user")
public class UserController implements CommunityConstant {
    public static final Logger logger= LoggerFactory.getLogger(UserController.class);


    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu,key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;
    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){
        // 上传文件的名称
        String fileName=CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy=new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));//异步的传回来成功的


        //生成上传的凭证，让七牛云可以识别凭证
        Auth auth=Auth.create(accessKey,secretKey);
        String uploadToken=auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);


        return "/site/setting";
    }

    //更新头像的路径

    @RequestMapping (path="/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不可以为空");
        }

        String url=headerBucketUrl+"/"+fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJSONString(0);
    }


    //下面废弃掉
    @LoginRequired
    @RequestMapping(path="/upload",method=RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        String fileName=headerImage.getOriginalFilename();
        String suffix =fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }

        //生成随机的文件名称
        fileName=CommunityUtil.generateUUID()+suffix;
        //确定文件存放的路径
        File dest=new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw  new RuntimeException("上传的文件失败，服务器发生异常！",e);
        }
        //更新当前用户的头像与路径（web可以访问的路径）
        //http://localhost:8080/community/user/header/xxx.png
        User user=hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);

        //完成之后跳转到首页
        return "redirect:/index";
    }
    //废弃
    @RequestMapping(path="/header/{fileName}",method = RequestMethod.GET)
    public void  getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放的路径
        fileName =uploadPath+"/"+fileName;
        //文件的后缀
        String suffix= fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try (
                FileInputStream fis=new FileInputStream(fileName);
                OutputStream os=response.getOutputStream();
        ){
            //下面是读出数据的操作
            byte[] buffer=new byte[1024];
            int b=0;
            while ((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
           logger.error("读取头像失败"+e.getMessage());
        }
    }

//    @RequestMapping(path ="/updatePassword",method = RequestMethod.POST)
//    public String updatePassword(){
//        //取得到user对象
//        User user=hostHolder.getUser();
//        //得到User的对象才行,然后将密码更改，确保两次的密码不可以相同,最后再更新
//
//        userService.updatePassword(user.getId(),);
//        return "redirect:/site/login";
//    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
