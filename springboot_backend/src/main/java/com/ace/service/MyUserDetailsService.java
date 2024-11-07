package com.ace.service;

import org.springframework.security.core.userdetails.User;
import com.ace.mapper.AdministratorMapper;
import com.ace.pojo.Administrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private AdministratorMapper administratorMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Administrator administrator = administratorMapper.findByUsername(username);
        if (administrator == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return User
                .withUsername(administrator.getUsername())
                .password(administrator.getPassword())
                .authorities("USER")
                .build();
    }
}