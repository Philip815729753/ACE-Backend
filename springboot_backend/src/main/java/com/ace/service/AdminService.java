package com.ace.service;

import com.ace.mapper.AdministratorMapper;
import com.ace.pojo.Administrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdministratorMapper administratorMapper;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Administrator register(Administrator administrator) {
        administrator.setPassword(encoder.encode(administrator.getPassword()));
        administratorMapper.insertAdministrator(administrator);
        return administrator;
    }
    public String verify(Administrator administrator) {
        Authentication authentication =
                authManager.authenticate(new UsernamePasswordAuthenticationToken(administrator.getUsername(), administrator.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(administrator.getUsername());
        } else {
            return "fail";
        }
    }
}
