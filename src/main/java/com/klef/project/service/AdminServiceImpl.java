package com.klef.project.service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.klef.project.entity.Category;
import com.klef.project.entity.DeliverySetting;
import com.klef.project.entity.Product;
import com.klef.project.entity.StockHistory;
import com.klef.project.entity.User;
import com.klef.project.entity.Order;
import com.klef.project.entity.OrderItem;
import com.klef.project.repository.CategoryRepository;
import com.klef.project.repository.DeliverySettingRepository;
import com.klef.project.repository.OrderItemRepository;
import com.klef.project.repository.OrderRepository;
import com.klef.project.repository.ProductRepository;
import com.klef.project.repository.StockHistoryRepository;
import com.klef.project.repository.UserRepository;

@Service
public class AdminServiceImpl implements AdminService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private StockHistoryRepository stockHistoryRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private DeliverySettingRepository deliverySettingRepository;

    @Override
    public String addStaff(User staff) 
    {
        staff.setRole("STAFF");
        userRepository.save(staff);
        return "Staff Added Successfully";
    }

    @Override
    public List<User> viewAllStaff() 
    {
        return userRepository.findAll()
                .stream()
                .filter(u -> "STAFF".equals(u.getRole()))
                .toList();
    }

    @Override
    public String deleteStaff(int id) 
    {
        userRepository.deleteById(id);
        return "Staff Deleted Successfully";
    }

    @Override
    public String addCategory(Category category) 
    {
        categoryRepository.save(category);
        return "Category Added Successfully";
    }

    @Override
    public List<Category> viewAllCategories() 
    {
        return categoryRepository.findAll();
    }

    @Override
    public String addProduct(Product product, MultipartFile image) 
    {
        try 
        {
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/uploads/";
            File folder = new File(uploadDir);

            if(!folder.exists()) 
            {
                folder.mkdirs();
            }

            String fileName = image.getOriginalFilename();
            File file = new File(uploadDir + fileName);
            image.transferTo(file);

            product.setImagePath("/uploads/" + fileName);
            productRepository.save(product);

            return "Product Added Successfully";
        } 
        catch(Exception e) 
        {
            return "Product Upload Failed: " + e.getMessage();
        }
    }

    @Override
    public List<Product> viewAllProducts() 
    {
        return productRepository.findAll();
    }

    @Override
    public String deleteProduct(int id) 
    {
        productRepository.deleteById(id);
        return "Product Deleted Successfully";
    }

    @Override
    public List<Order> viewAllOrders() 
    {
        return orderRepository.findAll();
    }
    @Override
    public List<StockHistory> viewStockHistory()
    {
        return stockHistoryRepository.findAll();
    }

    @Override
    public String confirmOrder(int orderId)
    {
        Order order = orderRepository.findById(orderId).orElse(null);

        if(order == null)
        {
            return "Order Not Found";
        }

        if(!order.getStatus().equalsIgnoreCase("PENDING"))
        {
            return "Only Pending Orders Can Be Confirmed";
        }

        order.setStatus("CONFIRMED");
        orderRepository.save(order);

        return "Order Confirmed Successfully";
    }

    @Override
    public String cancelOrder(int orderId, String reason)
    {
        Order order = orderRepository.findById(orderId).orElse(null);

        if(order == null)
        {
            return "Order Not Found";
        }

        if(order.getStatus().equalsIgnoreCase("CANCELLED"))
        {
            return "Order Already Cancelled";
        }

        if(order.getStatus().equalsIgnoreCase("DELIVERED"))
        {
            return "Delivered Order Cannot Be Cancelled";
        }

        if(reason == null || reason.trim().isEmpty())
        {
            return "Cancel Reason Required";
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        for(OrderItem item : items)
        {
            Product product = item.getProduct();

            int oldStock = product.getQuantity();
            int newStock = oldStock + item.getQuantity();

            product.setQuantity(newStock);
            productRepository.save(product);

            StockHistory history = new StockHistory();
            history.setProduct(product);
            history.setChangeType("ORDER_CANCELLED");
            history.setQuantityChanged(item.getQuantity());
            history.setOldStock(oldStock);
            history.setNewStock(newStock);
            history.setChangedBy("ADMIN");
            history.setRemarks("Order cancelled by admin. Reason: " + reason);
            history.setDateTime(LocalDateTime.now());

            stockHistoryRepository.save(history);
        }

        order.setStatus("CANCELLED");
        order.setCancelledBy("MANAGEMENT");
        order.setCancelReason(reason);

        orderRepository.save(order);

        return "Order Cancelled Successfully";
    }
    @Override
    public String setExpectedDeliveryDate(int orderId, LocalDate expectedDate)
    {
        Order order = orderRepository.findById(orderId).orElse(null);

        if(order == null)
        {
            return "Order Not Found";
        }

        if(order.getStatus().equalsIgnoreCase("CANCELLED"))
        {
            return "Cannot set delivery date for cancelled order";
        }

        order.setExpectedDeliveryDate(expectedDate);
        orderRepository.save(order);

        return "Expected Delivery Date Updated";
    }
    
    @Override
    public String updateDiscount(int productId, double discountPercentage)
    {
        Product product = productRepository.findById(productId).orElse(null);

        if(product == null)
        {
            return "Product Not Found";
        }

        if(discountPercentage < 0 || discountPercentage > 100)
        {
            return "Discount must be between 0 and 100";
        }

        product.setDiscountPercentage(discountPercentage);
        productRepository.save(product);

        return "Discount Updated Successfully";
    }
    
    @Override
    public String updateDeliveryCharge(double deliveryCharge)
    {
        if(deliveryCharge < 0)
        {
            return "Delivery charge cannot be negative";
        }

        DeliverySetting setting;

        if(deliverySettingRepository.findAll().isEmpty())
        {
            setting = new DeliverySetting();
        }
        else
        {
            setting = deliverySettingRepository.findAll().get(0);
        }

        setting.setDeliveryCharge(deliveryCharge);
        deliverySettingRepository.save(setting);

        return "Delivery Charge Updated Successfully";
    }

    @Override
    public double getDeliveryCharge()
    {
        if(deliverySettingRepository.findAll().isEmpty())
        {
            return 50;
        }

        return deliverySettingRepository.findAll().get(0).getDeliveryCharge();
    }
}