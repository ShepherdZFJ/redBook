package com.shepherd.redbookuserservice.api.service;

import com.shepherd.redbookuserservice.dto.UserDTO;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:47
 */
public interface UserService {
    void getCode(String phoneNumber);
    void login(UserDTO userDTO);
}
