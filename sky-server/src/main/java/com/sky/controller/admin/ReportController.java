package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Slf4j
@Api(tags = "报表相关接口")
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @ApiOperation("营业额统计")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                                       LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                                LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        UserReportVO userReportVO = reportService.userStatistics(begin, end);
        return Result.success(userReportVO);
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> statistics(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                                LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        OrderReportVO  orderReportVO= reportService.orderstatistics(begin, end);
        return Result.success(orderReportVO);
    }

    @GetMapping("/top10")
    @ApiOperation("销量排名")
    public Result<SalesTop10ReportVO> top10(@DateTimeFormat(pattern = "yyyy-MM-dd")
                                            LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(begin, end);
        return Result.success(salesTop10ReportVO);
    }
}
