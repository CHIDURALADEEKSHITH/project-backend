package com.klef.project.entity;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "product_table")
public class Product
{
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private int id;

 private String name;
 private String brand;
 private double price;
 private int quantity;

 @Column(length = 500)
 private String description;

 private String imagePath;

 @ManyToOne
 @JoinColumn(name = "category_id")
 private Category category;
 
 private LocalDate manufactureDate;
 private LocalDate expiryDate;

 public int getId() {
     return id;
 }

 public void setId(int id) {
     this.id = id;
 }

 public String getName() {
     return name;
 }

 public void setName(String name) {
     this.name = name;
 }

 public String getBrand() {
     return brand;
 }

 public void setBrand(String brand) {
     this.brand = brand;
 }

 public double getPrice() {
     return price;
 }

 public void setPrice(double price) {
     this.price = price;
 }

 public int getQuantity() {
     return quantity;
 }

 public void setQuantity(int quantity) {
     this.quantity = quantity;
 }

 public String getDescription() {
     return description;
 }

 public void setDescription(String description) {
     this.description = description;
 }

 public String getImagePath() {
     return imagePath;
 }

 public void setImagePath(String imagePath) {
     this.imagePath = imagePath;
 }

 public Category getCategory() {
     return category;
 }

 public void setCategory(Category category) {
     this.category = category;
 }

 public LocalDate getManufactureDate() {
	return manufactureDate;
 }

 public void setManufactureDate(LocalDate manufactureDate) {
	this.manufactureDate = manufactureDate;
 }

 public LocalDate getExpiryDate() {
	return expiryDate;
 }

 public void setExpiryDate(LocalDate expiryDate) {
	this.expiryDate = expiryDate;
 }
}
