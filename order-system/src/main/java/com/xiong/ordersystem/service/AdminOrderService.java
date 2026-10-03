package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.Order;
import com.xiong.ordersystem.entity.OrderItem;
import com.xiong.ordersystem.mapper.OrderItemMapper;
import com.xiong.ordersystem.mapper.OrderMapper;
import com.xiong.ordersystem.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminOrderService {

    private static final String PAID = "PAID";
    private static final String TO_SHIP = "TO_SHIP";
    private static final String SHIPPED = "SHIPPED";
    private static final String COMPLETED = "COMPLETED";
    private static final String CANCELLED = "CANCELLED";
    private static final String PENDING_PAY = "PENDING_PAY";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    public AdminOrderService(OrderMapper orderMapper,
                             OrderItemMapper orderItemMapper,
                             ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    public List<Order> findAll() {
        List<Order> orders = orderMapper.findAll();
        for (Order order : orders) {
            order.setItems(orderItemMapper.findByOrderId(order.getId()));
        }
        return orders;
    }

    public Order findById(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        order.setItems(orderItemMapper.findByOrderId(id));
        return order;
    }

    public Order markToShip(Long id) {
        return changeStatus(id, PAID, TO_SHIP);
    }

    public Order ship(Long id) {
        return changeStatus(id, TO_SHIP, SHIPPED);
    }

    public Order complete(Long id) {
        return changeStatus(id, SHIPPED, COMPLETED);
    }

    @Transactional
    public Order cancel(Long id) {
        Order order = findById(id);
        if (!PENDING_PAY.equals(order.getOrderStatus()) && !PAID.equals(order.getOrderStatus())) {
            throw new IllegalStateException("只有待支付或已支付订单可以取消");
        }
        if (orderMapper.updateAdminStatus(id, order.getOrderStatus(), CANCELLED) != 1) {
            throw new IllegalStateException("订单状态已发生变化");
        }
        for (OrderItem item : order.getItems()) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
        return findById(id);
    }

    private Order changeStatus(Long id, String expectedStatus, String nextStatus) {
        findById(id);
        if (orderMapper.updateAdminStatus(id, expectedStatus, nextStatus) != 1) {
            throw new IllegalStateException("订单当前状态不是" + expectedStatus + "，不能变更为" + nextStatus);
        }
        return findById(id);
    }
}
