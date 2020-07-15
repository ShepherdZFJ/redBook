package com.shepherd.redbookuserservice.api.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author fjzheng
 * @version 1.0
 * @date 2020/7/8 23:08
 */
@Data
public class UserVO {
    private String userNo;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer sex;
    private String headPhoto;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthday;
    private Integer is_delete;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String VerificationCode;
    private Integer type;
    private Integer firstLogin;
    private Integer count;
    private String ticket;
    private String token;
}

