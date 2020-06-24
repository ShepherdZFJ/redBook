package com.shepherd.redbookuserservice.constant;

import lombok.Getter;

/**
 * @author jfWu
 * @version 1.0
 * @date 2019/11/18 11:07
 */
@Getter
public enum ErrorCodeEnum {

    SEND_MESSAGE_ERROR("SEND MESSAGE ERROR", "发送短信失败，请稍后重试");
    private String code;
    private String message;

    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
