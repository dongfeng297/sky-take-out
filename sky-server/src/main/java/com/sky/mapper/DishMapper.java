package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select status from dish where id=#{id}")
    Integer selectStatusById(Long id);

    @Delete("delete from dish where id=#{id}")
    void deleteById(Long id);

    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    //增强泛用性
    List<Dish> list(Dish dish);



    void update(Dish dish);

    @Update("update dish set status=#{status} where id=#{id}")
    void updateStatus(Integer status, Long id);
    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
