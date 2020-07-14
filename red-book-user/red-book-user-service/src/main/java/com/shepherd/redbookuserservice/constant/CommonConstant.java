package com.shepherd.redbookuserservice.constant;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/24 16:07
 */
public interface CommonConstant {
    /**
     * 本机号码一键登录
     */
    Integer PHONE_LOCAL_LOGIN = 1;
    /**
     * 短信验证登录
     */
    Integer PHONE_MESSAGE_LOGIN = 2;
    /**
     * 账户密码登录
     */
    Integer USER_PASSWORD_LOGIN = 3;

    Integer FIRST_LOGIN = 1;
    Integer NOT_FIRST_LOGIN = 0;
    Integer DEL = 1;
    Integer NOT_DEL = 0;

}
