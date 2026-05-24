package com.klef.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.klef.project.entity.Order;
import com.klef.project.entity.OrderItem;
import com.klef.project.entity.Product;
import com.klef.project.entity.StockHistory;
import com.klef.project.repository.OrderItemRepository;
import com.klef.project.repository.OrderRepository;
import com.klef.project.repository.ProductRepository;
import com.klef.project.repository.StockHistoryRepository;

@Service
public class StaffServiceImpl implements StaffService
{
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<Order> viewAllOrders()
    {
        return orderRepository.findAll();
    }

    @Override
    public String updateOrderStatus(int orderId, String status)
    {
        Order order = orderRepository.findById(orderId).orElse(null);

        if(order == null)
        {
            return "Order Not Found";
        }

        order.setStatus(status);
        orderRepository.save(order);

        return "Order Status Updated";
    }

    @Override
    public String updateStock(int productId, int quantity)
    {
        Product product = productRepository.findById(productId).orElse(null);

        if(product == null)
        {
            return "Product Not Found";
        }

        if(quantity <= 0)
        {
            return "Quantity Must Be Greater Than Zero";
        }

        int oldStock = product.getQuantity();
        int newStock = oldStock + quantity;

        product.setQuantity(newStock);
        productRepository.save(product);

        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setChangeType("ADD_STOCK");
        history.setQuantityChanged(quantity);
        history.setOldStock(oldStock);
        history.setNewStock(newStock);
        history.setChangedBy("STAFF");
        history.setRemarks("New stock added by staff");
        history.setDateTime(LocalDateTime.now());

        stockHistoryRepository.save(history);

        return "Stock Added Successfully. Current Stock: " + newStock;
    }

    @Override
    public List<Product> viewAllProducts()
    {
        return productRepository.findAll();
    }

    @Override
    public String recordOfflineSale(int productId, int soldQuantity)
    {
        Product product = productRepository.findById(productId).orElse(null);

        if(product == null)
        {
            return "Product Not Found";
        }

        if(soldQuantity <= 0)
        {
            return "Sold Quantity Must Be Greater Than Zero";
        }

        if(product.getQuantity() < soldQuantity)
        {
            return "Insufficient Stock";
        }

        int oldStock = product.getQuantity();
        int newStock = oldStock - soldQuantity;

        product.setQuantity(newStock);
        productRepository.save(product);

        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setChangeType("OFFLINE_SALE");
        history.setQuantityChanged(-soldQuantity);
        history.setOldStock(oldStock);
        history.setNewStock(newStock);
        history.setChangedBy("STAFF");
        history.setRemarks("Product sold offline in shop");
        history.setDateTime(LocalDateTime.now());

        stockHistoryRepository.save(history);

        return "Offline Sale Recorded Successfully. Current Stock: " + newStock;
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
            history.setChangedBy("STAFF");
            history.setRemarks("Order cancelled by staff. Reason: " + reason);
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
}