package com.shepherd.redbookuserservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fjzheng
 * @version 1.0
 * @date 2020/6/15 23:07
 */
@Data
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
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



}
