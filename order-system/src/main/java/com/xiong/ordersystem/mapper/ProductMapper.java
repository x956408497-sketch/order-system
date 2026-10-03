package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("""
            SELECT p.id, p.category_id AS categoryId, c.name AS categoryName,
                   p.name, p.image AS imageUrl, p.price, p.stock,
                   p.description, p.status, p.create_time AS createTime,
                   p.update_time AS updateTime
            FROM product p
            JOIN category c ON c.id = p.category_id
            WHERE p.status = 1
              AND (#{categoryId} IS NULL OR p.category_id = #{categoryId})
              AND (#{keyword} IS NULL OR #{keyword} = '' OR p.name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY p.id DESC
            """)
    List<Product> findPublished(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    @Select("""
            SELECT p.id, p.category_id AS categoryId, c.name AS categoryName,
                   p.name, p.image AS imageUrl, p.price, p.stock,
                   p.description, p.status, p.create_time AS createTime,
                   p.update_time AS updateTime
            FROM product p
            JOIN category c ON c.id = p.category_id
            WHERE p.id = #{id}
            """)
    Product findById(Long id);

    @Select("""
            SELECT p.id, p.category_id AS categoryId, c.name AS categoryName,
                   p.name, p.image AS imageUrl, p.price, p.stock,
                   p.description, p.status, p.create_time AS createTime,
                   p.update_time AS updateTime
            FROM product p
            JOIN category c ON c.id = p.category_id
            ORDER BY p.id DESC
            """)
    List<Product> findAll();

    @Insert("""
            INSERT INTO product (category_id, name, image, price, stock, description, status)
            VALUES (#{categoryId}, #{name}, #{imageUrl}, #{price}, #{stock}, #{description}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("""
            UPDATE product
            SET category_id = #{categoryId}, name = #{name}, image = #{imageUrl},
                price = #{price}, stock = #{stock}, description = #{description}, status = #{status}
            WHERE id = #{id}
            """)
    int update(Product product);

    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE product SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE product SET stock = #{stock} WHERE id = #{id}")
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);

    @Update("""
            UPDATE product
            SET stock = stock - #{quantity}
            WHERE id = #{id} AND status = 1 AND stock >= #{quantity}
            """)
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
