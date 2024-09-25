package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.websocket.WebSocketServer;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    private Orders orders;



    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种异常（地址簿为空，购物车为空）
            //判断地址簿是否为空,返回的只有无或一条地址

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }


        //判断购物车是否为空
        Long userId = BaseContext.getCurrentId();

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(userId);
        if(shoppingCartList==null || shoppingCartList.size()==0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        //向order表中插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        this.orders = orders;
            //注意在此处order会得到自增后生成的主键id
        orderMapper.insert(orders);


        //向order_detail表中插入多条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车
        shoppingCartMapper.clean(userId);
        //封装并返回VO

        return new OrderSubmitVO(orders.getId(),orders.getNumber(),orders.getAmount(),orders.getOrderTime());
    }


    /**
     * 订单支付 来自吾浴西风
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
/*        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
*/
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, this.orders.getId());

        //通过websocket推送客户端消息
        HashMap map = new HashMap();
        map.put("type",1);      //1 表示来单提醒 2表示用户催单
        map.put("orderId",this.orders.getId());
        map.put("content","订单号："+this.orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        //通过websocket推送客户端消息
        HashMap map = new HashMap();
        map.put("type",1);      //1 表示来单提醒 2表示用户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号："+outTradeNo);
        webSocketServer.sendToAllClient(JSON.toJSONString(map));


    }

    @Override
    public PageResult pagerQuery(int pageNum, int pageSize, Integer status) {
        // 设置分页
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        // 分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);

    }

    @Override
    public OrderVO getById(Long id) {
        Orders orders=  orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    @Override
    public void cancel(Long id) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void repetition(Long id) {

        //查询用户id
        Long userId = BaseContext.getCurrentId();
        //根据订单id查询订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //将订单详情转为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(item -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item, shoppingCart, "id");

            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());


    }

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;

    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        orders.setStatus(Orders.CONFIRMED);
        orders.setId(ordersConfirmDTO.getId());
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

//        //支付状态
//        Integer payStatus = ordersDB.getPayStatus();
//        if (payStatus == Orders.PAID) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
//        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public void cancelOrders(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

//        //支付状态
//        Integer payStatus = ordersDB.getPayStatus();
//        if (payStatus == 1) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
//        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = new Orders();
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setId(id);
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = new Orders();
        orders.setStatus(Orders.COMPLETED);
        orders.setId(id);
        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);
        //校验订单是否存在
        if (ordersDB == null ) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        HashMap map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + ordersDB.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

    }
}
