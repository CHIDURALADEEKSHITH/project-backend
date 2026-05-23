package com.klef.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.klef.project.entity.User;
import com.klef.project.repository.UserRepository;

@Service
public class AuthServiceImpl implements AuthService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User login(String phone, String password)
    {
        User user = userRepository.findByPhone(phone);

        if(user == null)
        {
            return null;
        }

        boolean plainPasswordMatch = password.equals(user.getPassword());

        boolean encodedPasswordMatch =
                passwordEncoder.matches(password, user.getPassword());

        if(!plainPasswordMatch && !encodedPasswordMatch)
        {
            return null;
        }

        return user;
    }
}