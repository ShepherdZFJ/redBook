package com.shepherd.redbookuserservice.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.shepherd.redbookuserservice.api.service.UserService;
import com.shepherd.redbookuserservice.constant.ErrorCodeEnum;
import com.shepherd.redbookuserservice.exception.BusinessException;
import com.shepherd.redbookuserservice.utils.SendSmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:49
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Value("${aliyun-sms.accessKeyId}")
    private  String accessKeyId;

    @Value("${aliyun-sms.accessSecret}")
    private  String accessSecret;

    @Value("${aliyun-sms.signName}")
    private  String signName;

    @Value("${aliyun-sms.templateCode}")
    private  String templateCode;
    @Override
    public void getCode(String phoneNumber) {
        String code = RandomStringUtils.randomNumeric(6);
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        CommonRequest request1 = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("PhoneNumbers", phoneNumber);
        request.putQueryParameter("TemplateParam", "{code:"+ code +"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("send message result: "+response.getData());

        } catch (ServerException e) {
            log.error("send message error: ", e);
            throw new BusinessException(ErrorCodeEnum.SEND_MESSAGE_ERROR.getCode(), ErrorCodeEnum.SEND_MESSAGE_ERROR.getMessage());
        } catch (ClientException e) {
            log.error("send message error: ", e);
        }
    }
}
