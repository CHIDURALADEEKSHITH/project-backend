package com.klef.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "category_table")
public class Category
{
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private int id;

 private String categoryName;

 public int getId() {
     return id;
 }

 public void setId(int id) {
     this.id = id;
 }

 public String getCategoryName() {
     return categoryName;
 }

 public void setCategoryName(String categoryName) {
     this.categoryName = categoryName;
 }
}
