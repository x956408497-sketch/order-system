package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("""
            INSERT INTO orders (order_no, user_id, receiver_name, phone, detail_address,
                                total_amount, pay_status, order_status, remark)
            VALUES (#{orderNo}, #{userId}, #{receiverName}, #{phone}, #{detailAddress},
                    #{totalAmount}, #{payStatus}, #{orderStatus}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("""
            SELECT id, order_no AS orderNo, user_id AS userId, receiver_name AS receiverName,
                   phone, detail_address AS detailAddress, total_amount AS totalAmount,
                   pay_status AS payStatus, order_status AS orderStatus, remark,
                   create_time AS createTime, update_time AS updateTime
            FROM orders
            WHERE user_id = #{userId}
            ORDER BY id DESC
            """)
    List<Order> findByUserId(Long userId);

    @Select("""
            SELECT id, order_no AS orderNo, user_id AS userId, receiver_name AS receiverName,
                   phone, detail_address AS detailAddress, total_amount AS totalAmount,
                   pay_status AS payStatus, order_status AS orderStatus, remark,
                   create_time AS createTime, update_time AS updateTime
            FROM orders
            WHERE id = #{id} AND user_id = #{userId}
            """)
    Order findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            UPDATE orders
            SET order_status = #{orderStatus}
            WHERE id = #{id} AND user_id = #{userId} AND order_status = #{expectedStatus}
            """)
    int updateStatus(@Param("id") Long id,
                     @Param("userId") Long userId,
                     @Param("expectedStatus") String expectedStatus,
                     @Param("orderStatus") String orderStatus);

    @Update("""
            UPDATE orders
            SET pay_status = 1, order_status = 'PAID'
            WHERE id = #{id} AND user_id = #{userId}
              AND pay_status = 0 AND order_status = 'PENDING_PAY'
              AND create_time > DATE_SUB(NOW(), INTERVAL 10 MINUTE)
            """)
    int mockPay(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            SELECT id, order_no AS orderNo, user_id AS userId, receiver_name AS receiverName,
                   phone, detail_address AS detailAddress, total_amount AS totalAmount,
                   pay_status AS payStatus, order_status AS orderStatus, remark,
                   create_time AS createTime, update_time AS updateTime
            FROM orders
            WHERE order_status = 'PENDING_PAY'
              AND pay_status = 0
              AND create_time <= DATE_SUB(NOW(), INTERVAL 10 MINUTE)
            ORDER BY id ASC
            """)
    List<Order> findExpiredPendingPay();

    @Select("""
            SELECT id, order_no AS orderNo, user_id AS userId, receiver_name AS receiverName,
                   phone, detail_address AS detailAddress, total_amount AS totalAmount,
                   pay_status AS payStatus, order_status AS orderStatus, remark,
                   create_time AS createTime, update_time AS updateTime
            FROM orders
            ORDER BY id DESC
            """)
    List<Order> findAll();

    @Select("""
            SELECT id, order_no AS orderNo, user_id AS userId, receiver_name AS receiverName,
                   phone, detail_address AS detailAddress, total_amount AS totalAmount,
                   pay_status AS payStatus, order_status AS orderStatus, remark,
                   create_time AS createTime, update_time AS updateTime
            FROM orders
            WHERE id = #{id}
            """)
    Order findById(Long id);

    @Update("""
            UPDATE orders
            SET order_status = 'CANCELLED'
            WHERE id = #{id} AND order_status = 'PENDING_PAY' AND pay_status = 0
              AND create_time <= DATE_SUB(NOW(), INTERVAL 10 MINUTE)
            """)
    int cancelExpired(@Param("id") Long id);

    @Update("""
            UPDATE orders
            SET order_status = #{nextStatus}
            WHERE id = #{id} AND order_status = #{expectedStatus}
            """)
    int updateAdminStatus(@Param("id") Long id,
                          @Param("expectedStatus") String expectedStatus,
                          @Param("nextStatus") String nextStatus);

    @Update("""
            UPDATE orders
            SET order_status = 'COMPLETED'
            WHERE id = #{id} AND user_id = #{userId} AND order_status = 'SHIPPED'
            """)
    int confirmReceipt(@Param("id") Long id, @Param("userId") Long userId);
}
