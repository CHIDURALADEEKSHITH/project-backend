
package com.klef.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.project.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>
{
    public User findByEmailAndPassword(String email,String password);

    public User findByEmail(String email);

    public boolean existsByEmail(String email);
    
    User findByPhone(String phone);
    
}