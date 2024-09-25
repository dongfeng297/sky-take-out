package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin/order")
@Api(tags = "订单管理相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditonSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult pageResult=orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("统计订单信息")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO=orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getById(Long id){
        OrderVO orderVO=orderService.getById(id);
        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @ApiOperation("确认订单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.cancelOrders(ordersCancelDTO);
        return Result.success();
    }
    @PutMapping("delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }

}
