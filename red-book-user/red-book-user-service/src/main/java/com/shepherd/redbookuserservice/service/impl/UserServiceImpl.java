package com.shepherd.redbookuserservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.tools.json.JSONUtil;
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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import cn.hutool.http.HttpStatus;



/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:49
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public static final String TICKET = "ticket";

    public static final String TOKEN = "token";

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
    public UserDTO login(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.equals(userDTO.getType(), CommonConstant.PHONE_LOCAL_LOGIN)) {
            return loginByLocal(userDTO, request, response );
        }
        return null;

    }

    @Override
    public UserDTO update(UserDTO userDTO) {
        User user = UserBeanUtils.copy(userDTO, User.class);
        int i = userDAO.updateById(user);
        return UserBeanUtils.copy(user, UserDTO.class);
    }

    @Override
    public UserDTO status(HttpServletRequest request, HttpServletResponse response) {
        String ticket = getTokenOrTicket(request, TICKET);
        String token = getTokenOrTicket(request, TOKEN);
        if ( (StringUtils.isBlank(ticket) && StringUtils.isBlank(token))) {
            log.info("ticket和token都为空，未登录的操作");
            response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
            return null;
        }
        if (StringUtils.isNotBlank(ticket)) {
            String value = stringRedisTemplate.opsForValue().get(ticket);
            if (StringUtils.isNotBlank(value)) {
                token = value;
            }
        }
        String userInfo = stringRedisTemplate.opsForValue().get(token);
        if (StringUtils.isBlank(userInfo)) {
            response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
            return null;
        }
        UserDTO userDTO = JSONObject.parseObject(userInfo, UserDTO.class);
        stringRedisTemplate.expire(token , 2, TimeUnit.HOURS);
        return userDTO;


    }


    private UserDTO loginByLocal(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response){
        //判断手机号是否登录过
        UserDTO userDTO1 = findUserByPhoneNumber(userDTO.getPhone());
        if (userDTO1 == null) {
            userDTO.setCount(1);
            userDTO.setLastLoginTime(new Date());
            int insert = userDAO.insert(UserBeanUtils.copy(userDTO, User.class));
            userDTO.setFirstLogin(CommonConstant.FIRST_LOGIN);

        } else {
            userDTO1.setCount(userDTO1.getCount()+1);
            userDTO1.setLastLoginTime(new Date());
            userDAO.updateById(UserBeanUtils.copy(userDTO1, User.class));
            userDTO = userDTO1;
            userDTO.setFirstLogin(CommonConstant.NOT_FIRST_LOGIN);
        }
        String ticket = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(ticket, token, 20, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(token, JSON.toJSONString(userDTO),2,TimeUnit.HOURS);
        //种植cookie
        CasProperties casProperties = cookBaseSessionUtils.getCasProperties();
        request.setAttribute(cookBaseSessionUtils.getCasProperties().getCookieName(), token);
        cookBaseSessionUtils.onNewSession(request, response);
        userDTO.setTicket(ticket);
        userDTO.setToken(token);
        return userDTO;

    }

    private void loginByPhoneAndCode(UserDTO userDTO){

    }

    private void loginByUserAndPassword(UserDTO userDTO){

    }

    private String getTokenOrTicket(HttpServletRequest request,String key) {
        String token = request.getHeader(key);
        if (!(StringUtils.isNotBlank(token))) {
            token =  request.getParameter(key);
        }
        if (!StringUtils.isNotBlank(token)) {
            token = request.getHeader(cookBaseSessionUtils.getCasProperties().getCookieName());
        }
        if ("token".equals(key) && !StringUtils.isNotBlank(token)) {
            if (null != request.getCookies() && request.getCookies().length > 0) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookBaseSessionUtils.getCasProperties().getCookieName().equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (StringUtils.isEmpty(token)) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            String[] tokens = parameterMap.get(key);
            token = tokens == null || tokens.length == 0 ? null : tokens[0];
        }
        return token;

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
