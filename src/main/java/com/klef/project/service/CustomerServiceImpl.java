package com.klef.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.klef.project.entity.Cart;
import com.klef.project.entity.Order;
import com.klef.project.entity.OrderItem;
import com.klef.project.entity.PasswordResetOtp;
import com.klef.project.entity.Product;
import com.klef.project.entity.StockHistory;
import com.klef.project.entity.User;
import com.klef.project.repository.CartRepository;
import com.klef.project.repository.DeliverySettingRepository;
import com.klef.project.repository.OrderItemRepository;
import com.klef.project.repository.OrderRepository;
import com.klef.project.repository.PasswordResetOtpRepository;
import com.klef.project.repository.ProductRepository;
import com.klef.project.repository.StockHistoryRepository;
import com.klef.project.repository.UserRepository;

@Service
public class CustomerServiceImpl implements CustomerService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordResetOtpRepository otpRepository;
    
    @Autowired
    private StockHistoryRepository stockHistoryRepository;
    
    @Autowired
    private DeliverySettingRepository deliverySettingRepository;
    
    

    @Override
    public String registerCustomer(User user)
    {
        if(userRepository.existsByEmail(user.getEmail()))
        {
            return "Email Already Exists";
        }

        user.setRole("CUSTOMER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return "Customer Registered Successfully";
    }

    @Override
    public List<Product> viewAllProducts()
    {
        return productRepository.findAll();
    }

    @Override
    public String addToCart(int userId, int productId, int quantity)
    {
        User user = userRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if(user == null)
        {
            return "User Not Found";
        }

        if(product == null)
        {
            return "Product Not Found";
        }

        if(quantity <= 0)
        {
            return "Quantity Must Be Greater Than Zero";
        }

        if(product.getQuantity() < quantity)
        {
            return "Insufficient Stock";
        }

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(quantity);
        cart.setTotalPrice(product.getPrice() * quantity);

        cartRepository.save(cart);

        return "Product Added To Cart Successfully";
    }

    @Override
    public List<Cart> viewCart(int userId)
    {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public String placeOrder(int userId, String deliveryAddress)
    {
        User user = userRepository.findById(userId).orElse(null);

        if(user == null)
        {
            return "User Not Found";
        }

        List<Cart> cartItems = cartRepository.findByUserId(userId);

        if(cartItems.isEmpty())
        {
            return "Cart Is Empty";
        }

        if(deliveryAddress == null || deliveryAddress.trim().isEmpty())
        {
            return "Delivery Address Required";
        }

        double subtotal = 0;

        for(Cart c : cartItems)
        {
            Product product = c.getProduct();

            if(product.getQuantity() < c.getQuantity())
            {
                return "Insufficient Stock For " + product.getName();
            }

            subtotal = subtotal + c.getTotalPrice();
        }

        double deliveryCharge = 50;

        if(!deliverySettingRepository.findAll().isEmpty())
        {
            deliveryCharge = deliverySettingRepository.findAll().get(0).getDeliveryCharge();
        }

        double totalAmount = subtotal + deliveryCharge;

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDate.now());
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryCharge(deliveryCharge);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");

        orderRepository.save(order);

        for(Cart c : cartItems)
        {
            Product product = c.getProduct();

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(c.getQuantity());
            item.setPrice(c.getTotalPrice());

            orderItemRepository.save(item);

            int oldStock = product.getQuantity();
            int newStock = oldStock - c.getQuantity();

            product.setQuantity(newStock);
            productRepository.save(product);

            StockHistory history = new StockHistory();
            history.setProduct(product);
            history.setChangeType("ONLINE_ORDER");
            history.setQuantityChanged(-c.getQuantity());
            history.setOldStock(oldStock);
            history.setNewStock(newStock);
            history.setChangedBy(user.getName());
            history.setRemarks("Product ordered online");
            history.setDateTime(LocalDateTime.now());

            stockHistoryRepository.save(history);
        }

        cartRepository.deleteAll(cartItems);

        return "Order Placed Successfully";
    }
    
    @Override
    public List<Order> viewMyOrders(int userId)
    {
        return orderRepository.findByUserId(userId);
    }	

    @Override
    public String sendOtp(String email)
    {
        User user = userRepository.findByEmail(email);

        if(user == null)
        {
            return "Email Not Found";
        }

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        PasswordResetOtp oldOtp = otpRepository.findByEmail(email);

        if(oldOtp != null)
        {
            otpRepository.delete(oldOtp);
        }

        PasswordResetOtp resetOtp = new PasswordResetOtp();
        resetOtp.setEmail(email);
        resetOtp.setOtp(otp);
        resetOtp.setExpiryTime(java.time.LocalDateTime.now().plusMinutes(5));

        otpRepository.save(resetOtp);

        System.out.println("OTP for " + email + " is: " + otp);

        return "OTP Sent Successfully";
    }
    
    @Override
    public String removeCartItem(int cartId)
    {
        Cart cart = cartRepository.findById(cartId).orElse(null);

        if(cart == null)
        {
            return "Cart Item Not Found";
        }

        cartRepository.delete(cart);

        return "Item Removed From Cart";
    }

    @Override
    public String verifyOtp(String email, String otp)
    {
        PasswordResetOtp resetOtp = otpRepository.findByEmailAndOtp(email, otp);

        if(resetOtp == null)
        {
            return "Invalid OTP";
        }

        if(resetOtp.getExpiryTime().isBefore(java.time.LocalDateTime.now()))
        {
            return "OTP Expired";
        }

        return "OTP Verified Successfully";
    }

    @Override
    public String resetPassword(String email, String newPassword)
    {
        User user = userRepository.findByEmail(email);

        if(user == null)
        {
            return "User Not Found";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        PasswordResetOtp otp = otpRepository.findByEmail(email);

        if(otp != null)
        {
            otpRepository.delete(otp);
        }

        return "Password Reset Successfully";
    }
    
    
    @Override
    public String cancelOrder(int orderId)
    {
        Order order = orderRepository.findById(orderId).orElse(null);

        if(order == null)
        {
            return "Order Not Found";
        }

        if(!order.getStatus().equalsIgnoreCase("PENDING"))
        {
            return "Only Pending Orders Can Be Cancelled";
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        for(OrderItem item : items)
        {
            Product product = item.getProduct();

            int oldStock = product.getQuantity();
            int restoredStock = oldStock + item.getQuantity();

            product.setQuantity(restoredStock);

            productRepository.save(product);

            StockHistory history = new StockHistory();

            history.setProduct(product);
            history.setChangeType("ORDER_CANCELLED");
            history.setQuantityChanged(item.getQuantity());
            history.setOldStock(oldStock);
            history.setNewStock(restoredStock);
            history.setChangedBy(order.getUser().getName());
            history.setRemarks("Order cancelled by customer, stock restored");
            history.setDateTime(LocalDateTime.now());

            stockHistoryRepository.save(history);
        }

        order.setStatus("CANCELLED");
        order.setCancelledBy("CUSTOMER");
        order.setCancelReason("Cancelled by customer");

        orderRepository.save(order);

        return "Order Cancelled Successfully";
    }

    @Override
    public List<OrderItem> viewOrderItems(int orderId)
    {
        return orderItemRepository.findByOrderId(orderId);
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