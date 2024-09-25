package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> searchByDishIds(List<Long> ids);

    void insert(List<SetmealDish> setmealDishes);

    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> getBySetmealId(Long id);

    @Delete("delete from setmeal_dish where id=#{id}")
    void delete(Long id);

    @Insert("insert into setmeal_dish(setmeal_id,dish_id,name,price,copies) values(#{setmealId},#{dishId},#{name},#{price},#{copies})")
    void insertBySetmealId(SetmealDish setmealDish);

//    @Select("select id from setmeal_dish where dish_id=#{id}")
//    Long searchByDishId(Long id);
}
