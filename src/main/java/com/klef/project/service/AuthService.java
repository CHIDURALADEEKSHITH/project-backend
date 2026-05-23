package com.klef.project.service;

import com.klef.project.entity.User;

public interface AuthService 
{
	public User login(String phone, String password);
}