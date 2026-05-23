package com.klef.project.entity;


import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "password_reset_otp_table")
public class PasswordResetOtp
{
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private int id;

 private String email;

 private String otp;

 private LocalDateTime expiryTime;

 public int getId() {
     return id;
 }

 public void setId(int id) {
     this.id = id;
 }

 public String getEmail() {
     return email;
 }

 public void setEmail(String email) {
     this.email = email;
 }

 public String getOtp() {
     return otp;
 }

 public void setOtp(String otp) {
     this.otp = otp;
 }

 public LocalDateTime getExpiryTime() {
     return expiryTime;
 }

 public void setExpiryTime(LocalDateTime expiryTime) {
     this.expiryTime = expiryTime;
 }
}
