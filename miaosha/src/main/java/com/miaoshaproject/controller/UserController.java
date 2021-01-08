package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created on 2021/1/3.
 *
 * @author 小逸
 * @description
 * CrossOrigin 解决跨域问题
 */

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "getotp",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        // 将OTP验证码和用户手机号关联，放入缓存
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        //将OTP验证码通过短信通道发送给用户,省略
        System.out.println("telphone = " + telphone + " & otpCode = "+otpCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="gender")Integer gender,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 验证手机号和验证码
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!StringUtils.equals(otpCode, inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = userService.getUserById(id);
        userModel = null;
        userModel.getAge();
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        // 将核心领域模型用户对象转换为可供UI使用的viewobject
        UserVO userVO = converFromModel(userModel);

        // 返回通用对象
        return CommonReturnType.create(userVO);
    }

    private UserVO converFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
