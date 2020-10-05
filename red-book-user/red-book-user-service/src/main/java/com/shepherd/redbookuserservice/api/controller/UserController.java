package com.shepherd.redbookuserservice.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.shepherd.redbookuserservice.api.service.UserService;
import com.shepherd.redbookuserservice.api.vo.LoginVO;
import com.shepherd.redbookuserservice.api.vo.UserVO;
import com.shepherd.redbookuserservice.dto.UserDTO;
import com.shepherd.redbookuserservice.utils.UserBeanUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:45
 */
@RestController
@RequestMapping("/api/auth/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/VerificationCode/{phoneNumber}")
    @ApiOperation("获取手机验证码")
    public void getCode(@PathVariable("phoneNumber") String phoneNumber) {
        userService.getCode(phoneNumber);
    }

    @GetMapping("/status")
    @ApiOperation("查看登录状态")
    public UserVO status(HttpServletRequest request, HttpServletResponse response) {
        UserDTO userDTO = userService.status(request, response);
        return UserBeanUtils.copy(userDTO, UserVO.class);

    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public LoginVO login(@RequestBody UserVO userVO, HttpServletRequest request, HttpServletResponse response) {
        UserDTO userDTO = UserBeanUtils.copy(userVO, UserDTO.class);
        UserDTO userDTO1 = userService.login(userDTO, request, response);
        return UserBeanUtils.copy(userDTO1, LoginVO.class);
    }

    @PutMapping
    @ApiOperation("修改用户信息")
    public void updateUserInfo(@RequestBody UserVO userVO) {
        UserDTO userDTO = UserBeanUtils.copy(userVO, UserDTO.class);
        userService.update(userDTO);
    }

    @ApiOperation("退出登陆")
    @PutMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
    }

    @GetMapping
    @ApiOperation("获取用户列表")
    public List<UserDTO> getList() {
        return userService.getList();
    }


    @ApiOperation("找回密码")
    @PutMapping("/retrievePassword")
    public void retrievePassword(@RequestBody UserVO userVO) {
        UserDTO userDTO = UserBeanUtils.copy(userVO, UserDTO.class);
        userService.retrievePassword(userDTO);

    }


}
