package com.snailmann.security.core.validate.code.controller;

import com.snailmann.security.core.validate.code.ValidateCodeGenerator;
import com.snailmann.security.core.validate.code.entity.ImageCode;
import com.snailmann.security.core.validate.code.entity.ValidateCode;
import com.snailmann.security.core.validate.code.sms.SmsCodeSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
public class ValidateCodeController {

    public static final String SESSION_KEY = "SESSION_KEY_IMAGE_CODE";
    //Session操作工具类
    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    @Autowired
    @Qualifier("imageCodeGenerator")
    private ValidateCodeGenerator imageCodeGenerator;

    @Autowired
    @Qualifier("smsCodeGenerator")
    private ValidateCodeGenerator smsCodeGenerator;

    @Autowired
    private SmsCodeSender smsCodeSender;


    /**
     * 图形验证码验证
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/code/image")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ImageCode imageCode = (ImageCode) imageCodeGenerator.generate(new ServletWebRequest(request));
        //将SESSION_KEY与imageCode放进Session
        sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY, imageCode);
        //然后将图片放入response
        ImageIO.write(imageCode.getImage(),"JPEG",response.getOutputStream());

    }


    /**
     * 短信验证
     * @param request
     * @param response
     */
    @GetMapping("/code/sms")
    public void createSmsCode(HttpServletRequest request,HttpServletResponse response) throws ServletRequestBindingException {

        ValidateCode smsCode = smsCodeGenerator.generate(new ServletWebRequest(request)); //生成smsCode
        sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY, smsCode); //放入Session
        String mobile = ServletRequestUtils.getRequiredStringParameter(request,"mobile");
        smsCodeSender.send(mobile,smsCode.getCode()); //发送验证码
    }


}