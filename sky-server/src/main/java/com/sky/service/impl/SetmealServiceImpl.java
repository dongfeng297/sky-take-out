package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Transactional
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //新增套餐
        setmealMapper.insert(setmeal);

        //获取生成的套餐id
        Long setmealId = setmeal.getId();
        //遍历套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });


        //新增套餐菜品关系
        setmealDishMapper.insert(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<Setmeal> pages =setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(pages.getTotal(),pages.getResult());
    }

    @Override
    public void delete(List<Long> ids) {
        setmealMapper.delete(ids);
    }

    @Override
    public SetmealVO getById(Long id) {
        //查询套餐基本信息
        Setmeal setmeal= setmealMapper.getById(id);
        //查询套餐菜品关系
        List<SetmealDish> setmealDishes= setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void updateWithDish(SetmealDTO setmealDTO) {

        //修改套餐信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        //修改套餐菜品关系

        setmealDTO.getSetmealDishes().forEach(setmealDish -> {
            setmealDishMapper.delete(setmealDTO.getId());
            setmealDishMapper.insertBySetmealId(setmealDish);

        });

    }

    @Override
    public void updateStatus(Integer status, Long id) {
        setmealMapper.updateStatus(status,id);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
