package com.klef.project.service;

import java.util.List;

import com.klef.project.entity.Cart;
import com.klef.project.entity.Order;
import com.klef.project.entity.OrderItem;
import com.klef.project.entity.Product;
import com.klef.project.entity.StockHistory;
import com.klef.project.entity.User;

public interface CustomerService
{
    public String registerCustomer(User user);

    public List<Product> viewAllProducts();

    public List<StockHistory> viewProductStockBatches(
            int productId
    );

    public String addToCart(
            int userId,
            int productId,
            int quantity
    );

    public List<Cart> viewCart(
            int userId
    );

    public String placeOrder(
            int userId,
            String deliveryAddress
    );

    public List<Order> viewMyOrders(
            int userId
    );

    public String removeCartItem(
            int cartId
    );

    public String sendOtp(
            String email
    );

    public String verifyOtp(
            String email,
            String otp
    );

    public String resetPassword(
            String email,
            String newPassword
    );

    public List<OrderItem> viewOrderItems(
            int orderId
    );

    public String cancelOrder(
            int orderId
    );

    public double getDeliveryCharge();
}