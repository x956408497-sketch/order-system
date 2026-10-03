package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.Address;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressMapper {

    @Select("""
            SELECT id, user_id AS userId, receiver_name AS receiverName,
                   phone, detail_address AS detailAddress, is_default AS isDefault,
                   create_time AS createTime
            FROM address
            WHERE user_id = #{userId}
            ORDER BY is_default DESC, id DESC
            """)
    List<Address> findByUserId(Long userId);

    @Select("""
            SELECT id, user_id AS userId, receiver_name AS receiverName,
                   phone, detail_address AS detailAddress, is_default AS isDefault,
                   create_time AS createTime
            FROM address
            WHERE id = #{id} AND user_id = #{userId}
            """)
    Address findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM `user` WHERE id = #{userId}")
    int countUser(Long userId);

    @Insert("""
            INSERT INTO address (user_id, receiver_name, phone, detail_address, is_default)
            VALUES (#{userId}, #{receiverName}, #{phone}, #{detailAddress}, #{isDefault})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    @Update("""
            UPDATE address
            SET receiver_name = #{receiverName}, phone = #{phone}, detail_address = #{detailAddress}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int update(Address address);

    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefault(Long userId);

    @Update("UPDATE address SET is_default = 1 WHERE id = #{id} AND user_id = #{userId}")
    int setDefault(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM address WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
