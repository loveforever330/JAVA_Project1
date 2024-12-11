package com.nowcoder.community.config;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration

public class KaptchaConfig {
    @Bean
    public Producer kaptchaProducer(){
        Properties properties=new Properties();
        properties.setProperty("kapacha.image.width","100");
        properties.setProperty("kapacha.image.width","40");
        properties.setProperty("kapacha.textproducer.font.size","32");
        properties.setProperty("kapacha.textproducer.font.color","0.0.0");
        properties.setProperty("kapacha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kapacha.textproducer.char.length","4");
        properties.setProperty("kapacha.noise.impl","om.google.code.kaptcha.impl.NoNoise");
        DefaultKaptcha kaptcha=new DefaultKaptcha();
        Config config=new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
