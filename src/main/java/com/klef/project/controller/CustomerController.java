package com.klef.project.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.klef.project.entity.User;
import com.klef.project.service.CustomerService;

@RestController
@RequestMapping("/customer")
@CrossOrigin("*")
public class CustomerController
{
    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user)
    {
        String output = customerService.registerCustomer(user);

        return ResponseEntity.status(201).body(output);
    }

    @GetMapping("/viewallproducts")
    public ResponseEntity<?> viewAllProducts()
    {
        return ResponseEntity.ok(customerService.viewAllProducts());
    }

    @PostMapping("/addtocart")
    public ResponseEntity<?> addToCart(@RequestBody Map<String,Integer> data)
    {
        int userId = data.get("userId");
        int productId = data.get("productId");
        int quantity = data.get("quantity");

        String output = customerService.addToCart(userId, productId, quantity);

        return ResponseEntity.status(201).body(output);
    }

    @PostMapping("/viewcart")
    public ResponseEntity<?> viewCart(@RequestBody Map<String,Integer> data)
    {
        int userId = data.get("userId");

        return ResponseEntity.ok(customerService.viewCart(userId));
    }

    @PostMapping("/placeorder")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String,String> data)
    {
        int userId = Integer.parseInt(data.get("userId"));
        String deliveryAddress = data.get("deliveryAddress");

        String output = customerService.placeOrder(userId, deliveryAddress);

        return ResponseEntity.status(201).body(output);
    }

    @PostMapping("/viewmyorders")
    public ResponseEntity<?> viewMyOrders(@RequestBody Map<String,Integer> data)
    {
        int userId = data.get("userId");

        return ResponseEntity.ok(customerService.viewMyOrders(userId));
    }
    
    @PostMapping("/sendotp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String,String> data)
    {
        String email = data.get("email");
        String output = customerService.sendOtp(email);

        return ResponseEntity.ok(output);
    }
    
    @DeleteMapping("/removecartitem/{cartId}")
    public ResponseEntity<?> removeCartItem(@PathVariable int cartId)
    {
        return ResponseEntity.ok(customerService.removeCartItem(cartId));
    }

    @PostMapping("/verifyotp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String,String> data)
    {
        String email = data.get("email");
        String otp = data.get("otp");

        String output = customerService.verifyOtp(email, otp);

        return ResponseEntity.ok(output);
    }

    
    @PostMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,String> data)
    {
        String email = data.get("email");
        String newPassword = data.get("newPassword");

        String output = customerService.resetPassword(email, newPassword);

        return ResponseEntity.ok(output);
    }
    @PostMapping("/vieworderitems")
    public ResponseEntity<?> viewOrderItems(@RequestBody Map<String,Integer> data)
    {
        int orderId = data.get("orderId");
        return ResponseEntity.ok(customerService.viewOrderItems(orderId));
    }
    
    @PostMapping("/cancelorder")
    public ResponseEntity<?> cancelOrder(@RequestBody Map<String,Integer> data)
    {
        int orderId = data.get("orderId");

        return ResponseEntity.ok(customerService.cancelOrder(orderId));
    }
    
    @GetMapping("/getdeliverycharge")
    public ResponseEntity<?> getDeliveryCharge()
    {
        return ResponseEntity.ok(customerService.getDeliveryCharge());
    }
}