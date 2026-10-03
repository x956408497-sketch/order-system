package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.Order;
import com.xiong.ordersystem.entity.OrderItem;
import com.xiong.ordersystem.mapper.OrderItemMapper;
import com.xiong.ordersystem.mapper.OrderMapper;
import com.xiong.ordersystem.mapper.ProductMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderTimeoutService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    public OrderTimeoutService(OrderMapper orderMapper,
                               OrderItemMapper orderItemMapper,
                               ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void cancelExpiredOrders() {
        List<Order> orders = orderMapper.findExpiredPendingPay();
        for (Order order : orders) {
            cancelOne(order);
        }
    }

    private void cancelOne(Order order) {
        if (orderMapper.cancelExpired(order.getId()) != 1) {
            return;
        }
        for (OrderItem item : orderItemMapper.findByOrderId(order.getId())) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
    }
}
