package com.klef.project.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.klef.project.entity.User;
import com.klef.project.security.JwtUtil;
import com.klef.project.service.AuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController
{
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user)
    {
        User u = authService.login(user.getPhone(), user.getPassword());

        if(u == null)
        {
            return ResponseEntity.status(401).body("Invalid Login Details");
        }

        String token = jwtUtil.generateToken(u.getPhone());

        return ResponseEntity.ok(Map.of(
            "token", token,
            "role", u.getRole(),
            "userId", u.getId()
        ));
    }
}