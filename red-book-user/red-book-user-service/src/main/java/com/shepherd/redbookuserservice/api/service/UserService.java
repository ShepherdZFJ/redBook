package com.shepherd.redbookuserservice.api.service;

import com.alibaba.fastjson.JSONObject;
import com.shepherd.redbookuserservice.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:47
 */
public interface UserService {
    void getCode(String phoneNumber);

    UserDTO login(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response);

    UserDTO update(UserDTO userDTO);

    UserDTO status(HttpServletRequest request, HttpServletResponse response);

}
