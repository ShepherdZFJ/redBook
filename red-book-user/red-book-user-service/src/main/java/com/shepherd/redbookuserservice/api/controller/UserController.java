package com.shepherd.redbookuserservice.api.controller;

import com.shepherd.redbookuserservice.api.service.UserService;
import com.shepherd.redbookuserservice.api.vo.UserVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:45
 */
@RestController
@RequestMapping("/api/sso/auth")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/VerificationCode/{phoneNumber}")
    @ApiOperation("获取手机验证码")
    public void getCode(@PathVariable("phoneNumber") String phoneNumber){
        userService.getCode(phoneNumber);
    }

    @GetMapping("/login")
    @ApiOperation("用户登录")
    public void login(@RequestBody UserVO userVO){



    }

}
