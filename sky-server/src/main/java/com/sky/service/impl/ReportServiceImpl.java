package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> timeList = new ArrayList<>();
        timeList.add(begin);
        //将所有时间段加到集合中
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            timeList.add(begin);
        }
        //将所有时间段拼接成字符串
        String dateString = StringUtils.join(timeList, ",");


        //统计每个时间段的营业额
        List<Double> turnoverlist = new ArrayList<>();
        for (LocalDate date : timeList) {
            Double sum=orderMapper.sum(date);
            if(sum==null) sum=0.0;
            turnoverlist.add(sum);

        }



        String turnoverString = StringUtils.join(turnoverlist, ",");

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(dateString);
        turnoverReportVO.setTurnoverList(turnoverString);
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //时间段
        ArrayList<LocalDate> timeList = new ArrayList<>();
        timeList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            timeList.add(begin);
        }
        String dateString = StringUtils.join(timeList,",");

        ArrayList<Integer> newUserList = new ArrayList<>();
        ArrayList<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : timeList) {
            //统计新增用户
            Integer newUser= userMapper.newUserStatistics(date);
            newUserList.add(newUser);
            //统计总用户
            Integer totalUser=userMapper.totalUserStatistics(date);
            totalUserList.add(totalUser);
        }

        String newUserString = StringUtils.join(newUserList,",");
        String totalUserString = StringUtils.join(totalUserList,",");

        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(dateString);
        userReportVO.setNewUserList(newUserString);
        userReportVO.setTotalUserList(totalUserString);
        return userReportVO;

    }

    @Override
    public OrderReportVO orderstatistics(LocalDate begin, LocalDate end) {
        //时间段
        ArrayList<LocalDate> timeList = new ArrayList<>();
        timeList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            timeList.add(begin);
        }
        String dateString = StringUtils.join(timeList,",");


        //Integer
        //订单总数
         Integer totalOrderCount=0;

        //有效订单数
         Integer validOrderCount=0;

        //订单完成率
         Double orderCompletionRate=0.0;

         //订单总数集合
        ArrayList<Integer> totallist = new ArrayList<>();
        // 有效订单数集合
         ArrayList<Integer> validlist = new ArrayList<>();

        for (LocalDate date : timeList) {
            Integer n1= orderMapper.totalOrderCount(date);
            totalOrderCount+=n1;
            totallist.add(n1);

            Integer n2= orderMapper.validOrderCount(date);
            validOrderCount+=n2;
            validlist.add(n2);
        }
        if(totalOrderCount!=0){
        orderCompletionRate= validOrderCount.doubleValue()/totalOrderCount;}

        //转为字符串
        String totalOrderCountString = StringUtils.join(totallist, ",");
        String validOrderCountString = StringUtils.join(validlist,",");

        OrderReportVO orderReportVO = new OrderReportVO(dateString, totalOrderCountString, validOrderCountString, totalOrderCount, validOrderCount, orderCompletionRate);

        return orderReportVO;



    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {

         List<GoodsSalesDTO>  list =orderDetailMapper.getTop10(begin,end);
        List<String> names = list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = list.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(nameList);
        salesTop10ReportVO.setNumberList(numberList);
        return salesTop10ReportVO;

    }
}
