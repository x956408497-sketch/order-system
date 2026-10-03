package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.Address;
import com.xiong.ordersystem.entity.CartItem;
import com.xiong.ordersystem.entity.Order;
import com.xiong.ordersystem.entity.OrderItem;
import com.xiong.ordersystem.mapper.AddressMapper;
import com.xiong.ordersystem.mapper.CartMapper;
import com.xiong.ordersystem.mapper.OrderItemMapper;
import com.xiong.ordersystem.mapper.OrderMapper;
import com.xiong.ordersystem.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private static final String PENDING_PAY = "PENDING_PAY";
    private static final String CANCELLED = "CANCELLED";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final AddressMapper addressMapper;
    private final ProductMapper productMapper;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        CartMapper cartMapper,
                        AddressMapper addressMapper,
                        ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.addressMapper = addressMapper;
        this.productMapper = productMapper;
    }

    @Transactional
    public Order create(Long userId, Long addressId, String remark) {
        validateUser(userId);
        Address address = addressMapper.findByIdAndUserId(addressId, userId);
        if (address == null) {
            throw new IllegalArgumentException("收货地址不存在");
        }
        List<CartItem> cartItems = cartMapper.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("购物车为空，不能创建订单");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductStatus() == null || cartItem.getProductStatus() != 1) {
                throw new IllegalArgumentException("商品已下架：" + cartItem.getProductName());
            }
            if (productMapper.decreaseStock(cartItem.getProductId(), cartItem.getQuantity()) != 1) {
                throw new IllegalStateException("库存不足：" + cartItem.getProductName());
            }
            totalAmount = totalAmount.add(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setReceiverName(address.getReceiverName());
        order.setPhone(address.getPhone());
        order.setDetailAddress(address.getDetailAddress());
        order.setTotalAmount(totalAmount);
        order.setPayStatus(0);
        order.setOrderStatus(PENDING_PAY);
        order.setRemark(remark == null ? "" : remark.trim());
        orderMapper.insert(order);

        for (CartItem cartItem : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(cartItem.getProductId());
            item.setProductName(cartItem.getProductName());
            item.setProductImage(cartItem.getImageUrl());
            item.setPrice(cartItem.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemMapper.insert(item);
        }
        cartMapper.deleteByUserId(userId);
        return findById(userId, order.getId());
    }

    public List<Order> findByUserId(Long userId) {
        validateUser(userId);
        List<Order> orders = orderMapper.findByUserId(userId);
        for (Order order : orders) {
            order.setItems(orderItemMapper.findByOrderId(order.getId()));
        }
        return orders;
    }

    public Order findById(Long userId, Long orderId) {
        validateUser(userId);
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        order.setItems(orderItemMapper.findByOrderId(orderId));
        return order;
    }

    @Transactional
    public Order mockPay(Long userId, Long orderId) {
        Order order = findById(userId, orderId);
        if (!PENDING_PAY.equals(order.getOrderStatus()) || order.getPayStatus() != null && order.getPayStatus() != 0) {
            throw new IllegalStateException("只有待支付订单可以模拟支付");
        }
        if (orderMapper.mockPay(orderId, userId) != 1) {
            throw new IllegalStateException("订单已超时或状态已变化，请刷新订单");
        }
        return findById(userId, orderId);
    }

    @Transactional
    public Order confirmReceipt(Long userId, Long orderId) {
        findById(userId, orderId);
        if (orderMapper.confirmReceipt(orderId, userId) != 1) {
            throw new IllegalStateException("只有已发货订单可以确认收货");
        }
        return findById(userId, orderId);
    }

    @Transactional
    public Order cancel(Long userId, Long orderId) {
        Order order = findById(userId, orderId);
        if (!PENDING_PAY.equals(order.getOrderStatus())) {
            throw new IllegalStateException("只有待支付订单可以取消");
        }
        if (orderMapper.updateStatus(orderId, userId, PENDING_PAY, CANCELLED) != 1) {
            throw new IllegalStateException("订单状态已发生变化");
        }
        for (OrderItem item : order.getItems()) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
        return findById(userId, orderId);
    }

    private void validateUser(Long userId) {
        if (userId == null || userId <= 0 || addressMapper.countUser(userId) == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "OD" + time + random;
    }
}
