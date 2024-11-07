package com.ace.mapper;

import com.ace.pojo.Administrator;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdministratorMapper {
    @Insert("INSERT INTO Administrator (username, password) VALUES (#{username}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "adminId")
    void insertAdministrator(Administrator administrator);

    @Select("SELECT * FROM Administrator WHERE username = #{username}")
    Administrator findByUsername(String username);
}
