package com.klef.project.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "orders_table")
public class Order
{
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private int id;

 @ManyToOne
 @JoinColumn(name = "user_id")
 private User user;

 private LocalDate orderDate;

 private double totalAmount;

 private String status;
 @Column(length = 500)
 private String deliveryAddress;
 
 @Column(length = 500)
 private String cancelReason;
 
 private String cancelledBy;

 public int getId() {
     return id;
 }

 public void setId(int id) {
     this.id = id;
 }

 public User getUser() {
     return user;
 }

 public void setUser(User user) {
     this.user = user;
 }

 public LocalDate getOrderDate() {
     return orderDate;
 }

 public void setOrderDate(LocalDate orderDate) {
     this.orderDate = orderDate;
 }

 public double getTotalAmount() {
     return totalAmount;
 }

 public void setTotalAmount(double totalAmount) {
     this.totalAmount = totalAmount;
 }

 public String getStatus() {
     return status;
 }

 public void setStatus(String status) {
     this.status = status;
 }
 public String getDeliveryAddress() {
    return deliveryAddress;
 }
public void setDeliveryAddress(String deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
 }

public String getCancelReason() {
	return cancelReason;
}

public void setCancelReason(String cancelReason) {
	this.cancelReason = cancelReason;
}

public String getCancelledBy() {
	return cancelledBy;
}

public void setCancelledBy(String cancelledBy) {
	this.cancelledBy = cancelledBy;
}
}