package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Insert("""
            INSERT INTO order_item (order_id, product_id, product_name, product_image, price, quantity, subtotal)
            VALUES (#{orderId}, #{productId}, #{productName}, #{productImage}, #{price}, #{quantity}, #{subtotal})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem item);

    @Select("""
            SELECT id, order_id AS orderId, product_id AS productId, product_name AS productName,
                   product_image AS productImage, price, quantity, subtotal
            FROM order_item
            WHERE order_id = #{orderId}
            ORDER BY id ASC
            """)
    List<OrderItem> findByOrderId(Long orderId);
}
