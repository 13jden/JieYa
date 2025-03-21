package com.example.wx.controller;


import com.example.common.common.Result;
import com.example.common.redis.RedisComponent;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CheckCodeController {

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private Producer captchaProducer;

    @RequestMapping("/checkcode")
    public Result checkcode() throws IOException {
        // 生成验证码
        String code = captchaProducer.createText();
        BufferedImage captchaImage = captchaProducer.createImage(code);

        // 将验证码图像转换为 Base64 字符串
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(captchaImage, "png", byteArrayOutputStream);
        byte[] captchaBytes = byteArrayOutputStream.toByteArray();
        String captchaBase64 = Base64.getEncoder().encodeToString(captchaBytes);

        // 将验证码存入 Redis
        String checkCodeKey = redisComponent.saveCheckCode(code);
        Map<String,String> result = new HashMap<>();
        result.put("checkCode","data:image/png;base64,"+captchaBase64);
        result.put("checkCodeKey",checkCodeKey);
        // 返回 Base64 字符串
        return Result.success(result);
    }

}
