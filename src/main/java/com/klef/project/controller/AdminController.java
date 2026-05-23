package com.klef.project.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.klef.project.entity.Category;
import com.klef.project.entity.Product;
import com.klef.project.entity.User;
import com.klef.project.service.AdminService;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController
{
    @Autowired
    private AdminService adminService;

    @PostMapping("/addstaff")
    public ResponseEntity<?> addStaff(@RequestBody User user)
    {
        String output = adminService.addStaff(user);

        return ResponseEntity.status(201).body(output);
    }

    @GetMapping("/viewallstaff")
    public ResponseEntity<?> viewAllStaff()
    {
        return ResponseEntity.ok(adminService.viewAllStaff());
    }

    @DeleteMapping("/deletestaff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable int id)
    {
        String output = adminService.deleteStaff(id);

        return ResponseEntity.ok(output);
    }

    @PostMapping("/addcategory")
    public ResponseEntity<?> addCategory(@RequestBody Category category)
    {
        String output = adminService.addCategory(category);

        return ResponseEntity.status(201).body(output);
    }

    @GetMapping("/viewallcategories")
    public ResponseEntity<?> viewAllCategories()
    {
        return ResponseEntity.ok(adminService.viewAllCategories());
    }

    @PostMapping("/addproduct")
    public ResponseEntity<?> addProduct(
            @RequestPart Product product,
            @RequestPart MultipartFile image)
    {
        String output = adminService.addProduct(product, image);

        return ResponseEntity.status(201).body(output);
    }

    @GetMapping("/viewallproducts")
    public ResponseEntity<?> viewAllProducts()
    {
        return ResponseEntity.ok(adminService.viewAllProducts());
    }

    @DeleteMapping("/deleteproduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id)
    {
        String output = adminService.deleteProduct(id);

        return ResponseEntity.ok(output);
    }

    @GetMapping("/viewallorders")
    public ResponseEntity<?> viewAllOrders()
    {
        return ResponseEntity.ok(adminService.viewAllOrders());
    }
    
    @GetMapping("/viewstockhistory")
    public ResponseEntity<?> viewStockHistory()
    {
        return ResponseEntity.ok(adminService.viewStockHistory());
    }
    
    @PostMapping("/confirmorder")
    public ResponseEntity<?> confirmOrder(@RequestBody Map<String,Integer> data)
    {
        int orderId = data.get("orderId");
        return ResponseEntity.ok(adminService.confirmOrder(orderId));
    }

    @PostMapping("/cancelorder")
    public ResponseEntity<?> cancelOrder(@RequestBody Map<String,String> data)
    {
        int orderId = Integer.parseInt(data.get("orderId"));
        String reason = data.get("reason");

        return ResponseEntity.ok(adminService.cancelOrder(orderId, reason));
    }
}