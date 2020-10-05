package com.shepherd.redbookuserservice.service.impl;

import cn.hutool.core.util.IdUtil;
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
import com.shepherd.redbookuserservice.utils.MD5Utils;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.http.HttpStatus;
import org.springframework.util.CollectionUtils;


/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:49
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String TICKET = "ticket";

    private static final String TOKEN = "token";
    private static final String VERIFICATION = "red-book-code-";

    @Resource
    private UserDAO userDAO;

    @Resource
    private CookieBaseSessionUtils cookieBaseSessionUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${aliyun-sms.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun-sms.accessSecret}")
    private String accessSecret;

    @Value("${aliyun-sms.signName}")
    private String signName;

    @Value("${aliyun-sms.templateCode}")
    private String templateCode;
    @Value("${aliyun-sms.expireTime}")
    private Long expireTime;

    @Override
    public String getCode(String phoneNumber) {
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
        request.putQueryParameter("TemplateParam", "{code:" + code + "}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("send message result: " + response.getData());
            stringRedisTemplate.opsForValue().set(VERIFICATION + phoneNumber, code, expireTime, TimeUnit.MINUTES);
            return code;

        } catch (ServerException e) {
            log.error("send message error: ", e);
            throw new BusinessException(ErrorCodeEnum.SEND_MESSAGE_ERROR.getCode(), ErrorCodeEnum.SEND_MESSAGE_ERROR.getMessage());
        } catch (ClientException e) {
            log.error("send message error: ", e);
            throw new BusinessException(ErrorCodeEnum.SEND_MESSAGE_ERROR.getCode(), ErrorCodeEnum.SEND_MESSAGE_ERROR.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO login(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.equals(userDTO.getType(), CommonConstant.PHONE_LOCAL_LOGIN)) {
            return loginByLocal(userDTO, request, response);
        } else if (Objects.equals(userDTO.getType(), CommonConstant.PHONE_MESSAGE_LOGIN)) {
            return loginByPhoneAndCode(userDTO, request, response);
        } else if (Objects.equals(userDTO.getType(), CommonConstant.USER_PASSWORD_LOGIN)) {
            return loginByUserAndPassword(userDTO, request, response);
        }
        return null;

    }

    @Override
    public void update(UserDTO userDTO) {
        if (userDTO.getPassword() != null) {
            String salt = IdUtil.objectId();
            userDTO.setSalt(salt);
            String password = MD5Utils.encrypt(userDTO.getPassword() + salt);
            userDTO.setPassword(password);
        }
        int i = userDAO.updateById(userDTO);
    }

    @Override
    public UserDTO status(HttpServletRequest request, HttpServletResponse response) {
        String ticket = getTokenOrTicket(request, TICKET);
        String token = getTokenOrTicket(request, TOKEN);
        if (StringUtils.isBlank(ticket) && StringUtils.isBlank(token)) {
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
        stringRedisTemplate.expire(token, 2, TimeUnit.HOURS);
        return userDTO;


    }

    @Override
    public List<UserDTO> getList() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", CommonConstant.NOT_DEL);
        List<User> users = userDAO.selectList(queryWrapper);
        List<UserDTO> userDTOS = users.stream().map(user -> toUserDTO(user)).collect(Collectors.toList());
        return userDTOS;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String ticket = getTokenOrTicket(request, TICKET);
        String token = getTokenOrTicket(request, TOKEN);
        if (StringUtils.isNotBlank(ticket)) {
            String value = stringRedisTemplate.opsForValue().get(ticket);
            if (StringUtils.isNotBlank(value)) {
                token = value;
            }
        }
        String userInfo = stringRedisTemplate.opsForValue().get(token);
        if (StringUtils.isNotBlank(userInfo)) {
            log.info("删除token值为{}", token);
            //在redis中删除token
            stringRedisTemplate.delete(token);

            //清除cookie
            Cookie cookie = new Cookie(cookieBaseSessionUtils.getCasProperties().getCookieName(), null);
            cookie.setHttpOnly(true);
            cookie.setPath(request.getContextPath() + "/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);


        }


    }

    @Override
    public void retrievePassword(UserDTO userDTO) {
        Boolean flag = checkVerificationCode(userDTO.getPhone(), userDTO.getCode());
        if (!flag) {
            throw new BusinessException(ErrorCodeEnum.VERIFICATION_CODE_ERROR.getCode(), ErrorCodeEnum.VERIFICATION_CODE_ERROR.getMessage());
        }
        UserDTO userDTO1 = findUserByPhoneNumber(userDTO.getPhone());
        if (userDTO1 == null) {
            throw new BusinessException(ErrorCodeEnum.PHONE_NOT_REGISTER.getCode(), ErrorCodeEnum.PHONE_NOT_REGISTER.getMessage());
        }
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(userDTO1.getId());
        userDTO2.setPassword(userDTO1.getPassword());
        update(userDTO);
    }


    private UserDTO loginByLocal(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        //判断手机号是否登录过
        UserDTO userDTO1 = findUserByPhoneNumber(userDTO.getPhone());
        if (userDTO1 == null) {
            userDTO.setCount(1);
            userDTO.setLastLoginTime(new Date());
            userDTO.setIsDelete(CommonConstant.NOT_DEL);
            int insert = userDAO.insert(userDTO);
            userDTO.setFirstLogin(CommonConstant.FIRST_LOGIN);

        } else {
            userDTO1.setCount(userDTO1.getCount() + 1);
            userDTO1.setLastLoginTime(new Date());
            userDTO1.setUpdateTime(new Date());
            userDAO.updateById(userDTO1);
            userDTO = userDTO1;
            userDTO.setFirstLogin(CommonConstant.NOT_FIRST_LOGIN);
        }
        String ticket = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(ticket, token, 20, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(token, JSON.toJSONString(userDTO), 2, TimeUnit.HOURS);
        //种植cookie
        CasProperties casProperties = cookieBaseSessionUtils.getCasProperties();
        request.setAttribute(cookieBaseSessionUtils.getCasProperties().getCookieName(), token);
        cookieBaseSessionUtils.onNewSession(request, response);
        userDTO.setTicket(ticket);
        userDTO.setToken(token);
        return userDTO;

    }

    private UserDTO loginByPhoneAndCode(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        Boolean flag = checkVerificationCode(userDTO.getPhone(), userDTO.getCode());
        if (flag) {
            UserDTO userDTO1 = loginByLocal(userDTO, request, response);
            return userDTO1;
        } else {
            throw new BusinessException(ErrorCodeEnum.VERIFICATION_CODE_ERROR.getCode(), ErrorCodeEnum.VERIFICATION_CODE_ERROR.getMessage());
        }

    }

    private UserDTO loginByUserAndPassword(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_no", userDTO.getUserNo());
        queryWrapper.eq("is_delete", CommonConstant.NOT_DEL);
        User user = userDAO.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.USER_NO_NOT_EXIST_ERROR.getCode(), ErrorCodeEnum.USER_NO_NOT_EXIST_ERROR.getMessage());
        } else {
            String salt = user.getSalt();
            String password = MD5Utils.encrypt(userDTO.getPassword() + salt);
            if (!Objects.equals(password, user.getPassword())) {
                throw new BusinessException(ErrorCodeEnum.PASSWORD_ERROR.getCode(), ErrorCodeEnum.PASSWORD_ERROR.getMessage());
            } else {
                user.setCount(user.getCount() + 1);
                user.setLastLoginTime(new Date());
                user.setUpdateTime(new Date());
                userDAO.updateById(user);
                String ticket = UUID.randomUUID().toString();
                String token = UUID.randomUUID().toString();
                stringRedisTemplate.opsForValue().set(ticket, token, 20, TimeUnit.MINUTES);
                stringRedisTemplate.opsForValue().set(token, JSON.toJSONString(userDTO), 2, TimeUnit.HOURS);
                //种植cookie
                CasProperties casProperties = cookieBaseSessionUtils.getCasProperties();
                request.setAttribute(cookieBaseSessionUtils.getCasProperties().getCookieName(), token);
                cookieBaseSessionUtils.onNewSession(request, response);

                UserDTO userDTO1 = toUserDTO(user);
                userDTO1.setTicket(ticket);
                userDTO1.setToken(token);
                return userDTO1;
            }
        }
    }

    private String getTokenOrTicket(HttpServletRequest request, String key) {
        String token = request.getHeader(key);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(key);
        }
        if (StringUtils.isBlank(token)) {
            token = request.getHeader(cookieBaseSessionUtils.getCasProperties().getCookieName());
        }
        if ("token".equals(key) && StringUtils.isBlank(token)) {
            if (!CollectionUtils.isEmpty(Arrays.asList(request.getCookies()))) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookieBaseSessionUtils.getCasProperties().getCookieName().equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (StringUtils.isBlank(token)) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            String[] tokens = parameterMap.get(key);
            token = tokens == null || tokens.length == 0 ? null : tokens[0];
        }
        return token;

    }


    public UserDTO findUserByPhoneNumber(String phoneNumber) {
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

    private UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDTO = UserBeanUtils.copy(user, UserDTO.class);
        if (userDTO.getCount() > 1) {
            userDTO.setFirstLogin(CommonConstant.NOT_FIRST_LOGIN);
        } else {
            userDTO.setFirstLogin(CommonConstant.FIRST_LOGIN);
        }
        return userDTO;
    }

    private Boolean checkVerificationCode(String phoneNumber, String code) {
        Boolean flag = false;
        String value = stringRedisTemplate.opsForValue().get(VERIFICATION + phoneNumber);
        if (Objects.equals(code, value)) {
            flag = true;
        }
        return flag;

    }

}
