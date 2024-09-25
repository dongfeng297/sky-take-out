package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Transactional
    @Override
    public void save(DishDTO dishDTO) {


        //新增到菜品表
        Dish dish = new Dish();
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());

        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        //新增到口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dish.getId());
        }
        dishFlavorMapper.insert(flavors);
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> pages = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(pages.getTotal(),pages.getResult());
    }

    @Transactional
    @Override
    public void delete(List<Long> ids) {
//        //是否被套餐关联
//        for (Long id : ids) {
//            if (setmealDishMapper.searchByDishId(id) != null) {
//               throw  new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
//            }
//        }
        List<Long> list = setmealDishMapper.searchByDishIds(ids);
        if(list!=null && list.size()>0){
            throw  new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //是否在起售中
        for (Long id : ids) {
            if(dishMapper.selectStatusById(id)==1){
                throw  new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //删除菜品
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除口味
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    @Override
    public DishVO getById(Long id) {
        //查询菜品
        Dish dish = dishMapper.getById(id);
        //查询口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        //封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setUpdateTime(LocalDateTime.now());

        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品
        dishMapper.update(dish);
        //删除口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //插入口味
            //前端传来的味道的数据中没有dishId，需要手动设置
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDTO.getId());
        }

        dishFlavorMapper.insert(flavors);

    }

    @Override
    public List<Dish> list(Long categoryId) {
        log.info("根据分类id查询菜品:{}",categoryId);
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        List<Dish> list = dishMapper.list(dish);
        return list;
    }

    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        dishMapper.updateStatus(status,id);
    }


}
