package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShop")
@Slf4j
@Api(tags = "店铺相关接口")
@RequestMapping("/user/shop")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        return Result.success(shopStatus);
    }

}
