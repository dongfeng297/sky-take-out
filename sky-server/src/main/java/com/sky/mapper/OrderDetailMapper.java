package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetailList);

    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    @Select("select od.name,sum(od.number) number from order_detail od " +
            "join orders o on od.order_id = o.id where status=5 and " +
            "date(order_time) between #{begin} and #{end} group by od.name order by number desc limit 0,10")
    List<GoodsSalesDTO> getTop10(LocalDate begin, LocalDate end);
}
