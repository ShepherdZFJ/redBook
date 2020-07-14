package com.shepherd.redbookuserservice.api.service;

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

    void login(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response);
}
