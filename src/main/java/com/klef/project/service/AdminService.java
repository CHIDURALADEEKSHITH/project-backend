package com.klef.project.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.klef.project.entity.Category;
import com.klef.project.entity.Product;
import com.klef.project.entity.StockHistory;
import com.klef.project.entity.User;
import com.klef.project.entity.Order;

public interface AdminService 
{
    public String addStaff(User staff);
    public List<User> viewAllStaff();
    public String deleteStaff(int id);

    public String addCategory(Category category);
    public List<Category> viewAllCategories();

    public String addProduct(Product product, MultipartFile image);
    public List<Product> viewAllProducts();
    public String deleteProduct(int id);

    public List<Order> viewAllOrders();
    public List<StockHistory> viewStockHistory();
  
    public String confirmOrder(int orderId);
    public String cancelOrder(int orderId, String reason);
    public String setExpectedDeliveryDate(int orderId, LocalDate expectedDate);
    public String updateDiscount(int productId, double discountPercentage);
    
    public String updateDeliveryCharge(double deliveryCharge);
    public double getDeliveryCharge();
}