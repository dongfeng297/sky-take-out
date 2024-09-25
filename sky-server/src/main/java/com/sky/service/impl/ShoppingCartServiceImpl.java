package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {

        //判断购物车中是否有该菜品或套餐
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
            //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list= shoppingCartMapper.get(shoppingCart);

        //如果有，数量+1
        if(list!=null && list.size()>0){
            ShoppingCart sc = list.get(0);
            sc.setNumber(sc.getNumber()+1);
            shoppingCartMapper.update(sc);
            return;
        }

        //如果没有，添加到购物车，数量默认为1
        Long dishId= shoppingCartDTO.getDishId();
        Long setmealId= shoppingCartDTO.getSetmealId();
        if(dishId!=null){
            //本次购物车添加的是菜品
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());


        }else{
            //本次购物车添加的是套餐
            Setmeal setmeal = setmealMapper.getById(setmealId);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());

        }
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(userId);

        return list;
    }

    @Override
    public void clean() {
        shoppingCartMapper.clean(BaseContext.getCurrentId());
    }


}
