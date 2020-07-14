package com.shepherd.redbookuserservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shepherd.redbookuserservice.api.service.UserService;
import com.shepherd.redbookuserservice.config.CasProperties;
import com.shepherd.redbookuserservice.constant.CommonConstant;
import com.shepherd.redbookuserservice.constant.ErrorCodeEnum;
import com.shepherd.redbookuserservice.dao.UserDAO;
import com.shepherd.redbookuserservice.dto.UserDTO;
import com.shepherd.redbookuserservice.entity.User;
import com.shepherd.redbookuserservice.exception.BusinessException;
import com.shepherd.redbookuserservice.utils.CookieBaseSessionUtils;
import com.shepherd.redbookuserservice.utils.UserBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:49
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDAO userDAO;

    @Resource
    private CookieBaseSessionUtils cookBaseSessionUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void login(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.equals(userDTO.getType(), CommonConstant.PHONE_LOCAL_LOGIN)) {
            loginByLocal(userDTO, request, response );
        }

    }


    private void loginByLocal(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response){
        UserDTO userDTO1 = findUserByPhoneNumber(userDTO.getPhone());
        if (userDTO1 == null) {
            userDTO.setCount(1);
            userDTO.setLastLoginTime(new Date());
            int insert = userDAO.insert(UserBeanUtils.copy(userDTO, User.class));
            String ticket = UUID.randomUUID().toString();
            String token = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set(ticket, token, 20, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set(token, JSON.toJSONString(userDTO),2,TimeUnit.HOURS);
            //种植cookie
            CasProperties casProperties = cookBaseSessionUtils.getCasProperties();
            request.setAttribute(cookBaseSessionUtils.getCasProperties().getCookieName(), token);
            cookBaseSessionUtils.onNewSession(request, response);

        }


    }

    private void loginByPhoneAndCode(UserDTO userDTO){

    }

    private void loginByUserAndPassword(UserDTO userDTO){

    }


    public UserDTO findUserByPhoneNumber(String phoneNumber){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", CommonConstant.NOT_DEL);
        queryWrapper.eq("phone", phoneNumber);
        User user = userDAO.selectOne(queryWrapper);
        if (user != null) {
            UserDTO userDTO = UserBeanUtils.copy(user, UserDTO.class);
            return userDTO;
        }
        return null;
    }

}
