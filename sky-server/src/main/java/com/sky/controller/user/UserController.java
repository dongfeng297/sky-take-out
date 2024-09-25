package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sky.service.UserService;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/user/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信登录：{}", userLoginDTO);
        //微信登录
        User user = userService.login(userLoginDTO);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        //为微信用户生成jwt令牌
        String jwt = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        UserLoginVO userLoginVO = new UserLoginVO(user.getId(), user.getOpenid(), jwt);

        return Result.success(userLoginVO);
    }
}
