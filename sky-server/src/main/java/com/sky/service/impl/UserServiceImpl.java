package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    public User login(UserLoginDTO userLoginDTO) {

        //获取openid
        String openid = getOpenid(userLoginDTO.getCode());
        // 根据openid是否为空，如果为空，则表示登录失败
        if (openid == null) {
            throw new LoginFailedException("登录失败");
        }

        // 判断当前用户是否为新用户,如果是新用户，自动完成注册
        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            User newUser = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            user= userMapper.insert(newUser);

        }
        // 返回用户信息
        return user;
    }

    private String getOpenid(String code) {

        //doGet 的作用是向指定的微信接口服务发送HTTP GET请求，并附带参数，然后返回服务器响应的内容
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");
        //注意微信的接口中，返回的json数据中，openid是json对象中的属性，不是json字符串中的属性，所以不能用JSONObject.parseObject()方法解析
        String json = HttpClientUtil.doGet("http://api.weixin.qq.com/sns/jscode2session", paramMap);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        return openid;
    }
}
