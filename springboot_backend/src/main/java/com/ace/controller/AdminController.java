package com.ace.controller;


import com.ace.pojo.Administrator;
import com.ace.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;


    @PostMapping("/register")
    public Administrator register(@RequestBody Administrator administrator) {
        return adminService.register(administrator);

    }

    @PostMapping("/login")
    public String login(@RequestBody Administrator administrator) {

        return adminService.verify(administrator);
    }
}