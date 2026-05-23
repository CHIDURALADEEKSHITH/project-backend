package com.klef.project.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_table")
public class Cart
{
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private int id;

 @ManyToOne
 @JoinColumn(name = "user_id")
 private User user;

 @ManyToOne
 @JoinColumn(name = "product_id")
 private Product product;

 private int quantity;

 private double totalPrice;

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

 public Product getProduct() {
     return product;
 }

 public void setProduct(Product product) {
     this.product = product;
 }

 public int getQuantity() {
     return quantity;
 }

 public void setQuantity(int quantity) {
     this.quantity = quantity;
 }

 public double getTotalPrice() {
     return totalPrice;
 }

 public void setTotalPrice(double totalPrice) {
     this.totalPrice = totalPrice;
 }
}