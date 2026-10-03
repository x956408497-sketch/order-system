package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("""
            SELECT id, name, sort, create_time AS createTime
            FROM category
            ORDER BY sort ASC, id ASC
            """)
    List<Category> findAll();

    @Select("SELECT id, name, sort, create_time AS createTime FROM category WHERE id = #{id}")
    Category findById(Long id);

    @Select("SELECT id, name, sort, create_time AS createTime FROM category WHERE name = #{name}")
    Category findByName(String name);

    @Insert("INSERT INTO category (name, sort) VALUES (#{name}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name = #{name}, sort = #{sort} WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    int countProducts(Long categoryId);
}
