package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    User insert(User user);

    @Select("select * from user where id=#{userId}")
    User getById(Long userId);

    @Select("select count(id) from user where date(create_time)=#{date}")
    Integer newUserStatistics(LocalDate date);

    @Select("select count(id) from user where date(create_time)<=#{date}")
    Integer totalUserStatistics(LocalDate date);
}
