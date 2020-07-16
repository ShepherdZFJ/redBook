package com.shepherd.redbookuserservice.dto;

import com.shepherd.redbookuserservice.entity.User;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author fjzheng
 * @version 1.0
 * @date 2020/7/8 23:11
 */
@Data
public class UserDTO extends User {
    private String code;
    private Integer type;
    private Integer firstLogin;
    private String ticket;
    private String token;
}
