package com.shepherd.redbookuserservice.dao;

import com.shepherd.redbookuserservice.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:29
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class UserDAOTest {
    @Resource
    private UserDAO userDAO;

    @Test
    public void test(){
        User user = userDAO.selectById(3L);
        log.info("user: "+user);

    }


}