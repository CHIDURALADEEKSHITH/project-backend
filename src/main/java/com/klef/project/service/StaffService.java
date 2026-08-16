package com.klef.project.service;

import java.time.LocalDate;
import java.util.List;

import com.klef.project.entity.Order;
import com.klef.project.entity.Product;
import com.klef.project.entity.StockHistory;

public interface StaffService
{
    public List<Order> viewAllOrders();

    public String updateOrderStatus(int orderId, String status);

    public String updateStock(
            int productId,
            int quantity,
            LocalDate expiryDate
    );

    public List<Product> viewAllProducts();

    public String recordOfflineSale(
            int productId,
            int soldQuantity
    );

    public String confirmOrder(int orderId);

    public String cancelOrder(
            int orderId,
            String reason
    );

    public String setExpectedDeliveryDate(
            int orderId,
            LocalDate expectedDate
    );
    public List<StockHistory> viewProductStockBatches(int productId);
}