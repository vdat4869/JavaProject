package com.uth.confms.user.controller;

import com.uth.confms.user.dto.RegisterRequest;
import com.uth.confms.user.entity.User;
import com.uth.confms.user.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping("/me")
    public String me(Authentication authentication) {
        return "Hello " + authentication.getName();
    }
}
