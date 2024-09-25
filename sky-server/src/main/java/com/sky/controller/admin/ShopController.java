package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShop")
@Slf4j
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺状态为：{}", status);
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        return Result.success();

    }



    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        return Result.success(status);
    }
}
