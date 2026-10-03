package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.AppUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AppUserMapper {
    @Select("SELECT id, openid, nickname, avatar, phone, create_time AS createTime FROM `user` WHERE openid = #{openid}")
    AppUser findByOpenid(String openid);

    @Insert("INSERT INTO `user` (openid, nickname) VALUES (#{openid}, #{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppUser user);
}
