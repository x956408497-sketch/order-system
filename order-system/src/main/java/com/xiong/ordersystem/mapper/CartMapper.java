package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper {

    @Select("""
            SELECT c.id, c.user_id AS userId, c.product_id AS productId, c.quantity,
                   c.create_time AS createTime, p.name AS productName,
                   p.image AS imageUrl, p.price, p.stock, p.status AS productStatus
            FROM cart c
            JOIN product p ON p.id = c.product_id
            WHERE c.user_id = #{userId}
            ORDER BY c.id DESC
            """)
    List<CartItem> findByUserId(Long userId);

    @Select("""
            SELECT c.id, c.user_id AS userId, c.product_id AS productId, c.quantity,
                   c.create_time AS createTime, p.name AS productName,
                   p.image AS imageUrl, p.price, p.stock, p.status AS productStatus
            FROM cart c
            JOIN product p ON p.id = c.product_id
            WHERE c.id = #{id} AND c.user_id = #{userId}
            """)
    CartItem findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, product_id AS productId, quantity FROM cart WHERE user_id = #{userId} AND product_id = #{productId}")
    CartItem findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM `user` WHERE id = #{userId}")
    int countUser(Long userId);

    @Insert("INSERT INTO cart (user_id, product_id, quantity) VALUES (#{userId}, #{productId}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem item);

    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(CartItem item);

    @Delete("DELETE FROM cart WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
