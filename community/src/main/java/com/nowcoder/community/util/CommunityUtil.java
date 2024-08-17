package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CommunityUtil {
    //生成随机的字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-"," ");

    }
    //生成MD5加密
    //hello->abc123def456
    // hello+3e4a9o->adwdawfwq
    //防止黑客

    public  static  String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());

    }


}
