package com.shepherd.redbookuserservice.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shepherd.redbookuserservice.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/6/16 19:26
 */
@Repository
@Mapper
public interface UserDAO extends BaseMapper<User> {
}
