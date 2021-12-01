package net.lianbian.controller;

import net.lianbian.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HelloController {
    @Autowired
    private IUserService userService;

    @GetMapping("/hello")
    public String hello() {
        List<Map<String, Object>> userMaps = userService.selectRelateList();
        return userMaps.toString();
    }
}