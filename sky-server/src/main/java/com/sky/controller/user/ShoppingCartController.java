package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api(tags = "c端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车:{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }
    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查询购物车");
        List<ShoppingCart> list= shoppingCartService.list();
        return Result.success(list);
    }
    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clean(){
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }
}
