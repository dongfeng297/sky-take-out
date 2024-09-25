package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    //每隔一分钟触发
    @Scheduled(cron="0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("定时取消订单{}", LocalDateTime.now());

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        for (Orders orders : ordersList) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason("订单超时,已自动取消");
            orderMapper.update(orders);
        }
    }
    @Scheduled(cron="0 0 1 * * ? ")
    public void processDeliveryStatus(){
        log.info("定时修改订单状态{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        for (Orders orders : ordersList) {
            orders.setStatus(Orders.COMPLETED);

            orderMapper.update(orders);
        }
    }
}
