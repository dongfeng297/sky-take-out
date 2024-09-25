package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Delete("delete from orders where id = #{id}")
    void deleteById(Long id);


    @Select("select count(*) from orders where status=2")
    Integer get2();

    @Select("select count(*) from orders where status=3")
    Integer get3();

    @Select("select count(*) from orders where status=4")
    Integer get4();

    @Select("select count(*) from orders where status=#{status}")
    Integer countStatus(Integer status);

    @Select("select * from orders where status=#{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    @Select("select sum(amount) from orders where date(order_time) =#{date} and status=5")
    Double sum(LocalDate date);


    @Select("select count(*) from orders where date(order_time)=#{date}")
    Integer totalOrderCount(LocalDate date);

    @Select("select count(*) from orders where date(order_time)=#{date} and status=5 ")
    Integer validOrderCount(LocalDate date);

    @Select("select count(*) from orders where status=#{status} and date(order_time) =#{date}")
    Integer countByMap(Map map);
}
