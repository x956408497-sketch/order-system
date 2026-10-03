package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminMapper {

    @Select("SELECT id, username, password, role FROM admin WHERE username = #{username}")
    Admin findByUsername(String username);

    @Select("SELECT id, username, password, role FROM admin WHERE id = #{id}")
    Admin findById(Long id);

    @Update("UPDATE admin SET username = #{username}, password = #{password} WHERE id = #{id}")
    int updateAccount(@Param("id") Long id, @Param("username") String username, @Param("password") String password);

    @Update("UPDATE admin SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
