package com.klef.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CustomerServiceImpl
        implements CustomerService
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

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        userRepository.save(user);

        return "Customer Registered Successfully";
    }


    @Override
    public List<Product> viewAllProducts()
    {
        return productRepository.findAll();
    }


    @Override
    public String addToCart(
            int userId,
            int productId,
            int quantity)
    {
        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);

        Product product =
                productRepository
                        .findById(productId)
                        .orElse(null);


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

        cart.setTotalPrice(
                product.getPrice() *
                quantity
        );


        cartRepository.save(cart);


        return "Product Added To Cart Successfully";
    }


    @Override
    public List<Cart> viewCart(int userId)
    {
        return cartRepository
                .findByUserId(userId);
    }


    /*
     * PLACE ORDER
     *
     * Uses FEFO:
     * earliest expiry first.
     */
    @Override
    @Transactional
    public String placeOrder(
            int userId,
            String deliveryAddress)
    {
        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);


        if(user == null)
        {
            return "User Not Found";
        }


        List<Cart> cartItems =
                cartRepository
                        .findByUserId(userId);


        if(cartItems.isEmpty())
        {
            return "Cart Is Empty";
        }


        if(deliveryAddress == null ||
                deliveryAddress.trim().isEmpty())
        {
            return "Delivery Address Required";
        }


        double subtotal = 0;


        /*
         * First check stock.
         */
        for(Cart c : cartItems)
        {
            Product product =
                    c.getProduct();


            if(product.getQuantity() <
                    c.getQuantity())
            {
                return "Insufficient Stock For "
                        + product.getName();
            }


            subtotal =
                    subtotal +
                    c.getTotalPrice();
        }


        double deliveryCharge = 50;


        if(!deliverySettingRepository
                .findAll()
                .isEmpty())
        {
            deliveryCharge =
                    deliverySettingRepository
                            .findAll()
                            .get(0)
                            .getDeliveryCharge();
        }


        double totalAmount =
                subtotal +
                deliveryCharge;


        Order order = new Order();

        order.setUser(user);

        order.setOrderDate(
                LocalDate.now()
        );

        order.setDeliveryAddress(
                deliveryAddress
        );

        order.setDeliveryCharge(
                deliveryCharge
        );

        order.setTotalAmount(
                totalAmount
        );

        order.setStatus(
                "PENDING"
        );


        orderRepository.save(order);


        for(Cart c : cartItems)
        {
            Product product =
                    c.getProduct();


            /*
             * Create order item.
             */
            OrderItem item =
                    new OrderItem();

            item.setOrder(order);

            item.setProduct(product);

            item.setQuantity(
                    c.getQuantity()
            );

            item.setPrice(
                    c.getTotalPrice()
            );


            orderItemRepository.save(item);


            /*
             * Reduce stock using FEFO.
             */
            consumeStockBatches(
                    product,
                    c.getQuantity(),
                    user.getName(),
                    "ONLINE_ORDER"
            );


            /*
             * Update total product stock.
             */
            int oldStock =
                    product.getQuantity();

            int newStock =
                    oldStock -
                    c.getQuantity();


            product.setQuantity(
                    newStock
            );


            productRepository.save(product);
        }


        cartRepository.deleteAll(
                cartItems
        );


        return "Order Placed Successfully";
    }


    /*
     * Consume stock from earliest-expiry batches.
     */
    private void consumeStockBatches(
            Product product,
            int quantity,
            String changedBy,
            String changeType)
    {
        int remaining =
                quantity;


        List<StockHistory> batches =
                stockHistoryRepository
                        .findByProductIdAndChangeTypeOrderByExpiryDateAsc(
                                product.getId(),
                                "ADD_STOCK"
                        );


        for(StockHistory batch : batches)
        {
            if(remaining <= 0)
            {
                break;
            }


            if(batch.getRemainingQuantity() <= 0)
            {
                continue;
            }


            /*
             * Do not sell expired stock.
             */
            if(batch.getExpiryDate() != null &&
                    batch.getExpiryDate()
                            .isBefore(
                                    LocalDate.now()
                            ))
            {
                continue;
            }


            int available =
                    batch.getRemainingQuantity();


            int used =
                    Math.min(
                            available,
                            remaining
                    );


            batch.setRemainingQuantity(
                    available - used
            );


            stockHistoryRepository.save(
                    batch
            );


            remaining =
                    remaining - used;
        }


        /*
         * Legacy products may not have batches.
         */
        if(remaining > 0)
        {
            LocalDate expiry =
                    product.getExpiryDate();


            if(expiry != null)
            {
                StockHistory legacyBatch =
                        new StockHistory();


                legacyBatch.setProduct(
                        product
                );

                legacyBatch.setChangeType(
                        "ADD_STOCK"
                );

                legacyBatch.setQuantityChanged(
                        remaining
                );

                legacyBatch.setOldStock(
                        product.getQuantity()
                );

                legacyBatch.setNewStock(
                        product.getQuantity()
                );

                legacyBatch.setRemainingQuantity(
                        0
                );

                legacyBatch.setExpiryDate(
                        expiry
                );

                legacyBatch.setChangedBy(
                        "SYSTEM"
                );

                legacyBatch.setRemarks(
                        "Legacy stock batch created automatically"
                );

                legacyBatch.setDateTime(
                        LocalDateTime.now()
                );


                stockHistoryRepository.save(
                        legacyBatch
                );
            }
        }


        /*
         * Sale history record.
         */
        StockHistory history =
                new StockHistory();


        history.setProduct(
                product
        );

        history.setChangeType(
                changeType
        );

        history.setQuantityChanged(
                -quantity
        );

        history.setOldStock(
                product.getQuantity()
        );

        history.setNewStock(
                product.getQuantity() -
                quantity
        );

        history.setRemainingQuantity(
                0
        );

        history.setChangedBy(
                changedBy
        );

        history.setRemarks(
                "Product ordered online"
        );

        history.setDateTime(
                LocalDateTime.now()
        );


        stockHistoryRepository.save(
                history
        );
    }


    @Override
    public List<Order> viewMyOrders(
            int userId)
    {
        return orderRepository
                .findByUserId(userId);
    }


    @Override
    public String sendOtp(
            String email)
    {
        User user =
                userRepository
                        .findByEmail(email);


        if(user == null)
        {
            return "Email Not Found";
        }


        String otp =
                String.valueOf(
                        (int)
                        (Math.random() * 900000)
                        + 100000
                );


        PasswordResetOtp oldOtp =
                otpRepository
                        .findByEmail(email);


        if(oldOtp != null)
        {
            otpRepository.delete(
                    oldOtp
            );
        }


        PasswordResetOtp resetOtp =
                new PasswordResetOtp();


        resetOtp.setEmail(email);

        resetOtp.setOtp(otp);

        resetOtp.setExpiryTime(
                LocalDateTime.now()
                        .plusMinutes(5)
        );


        otpRepository.save(
                resetOtp
        );


        System.out.println(
                "OTP for "
                + email
                + " is: "
                + otp
        );


        return "OTP Sent Successfully";
    }


    @Override
    public String removeCartItem(
            int cartId)
    {
        Cart cart =
                cartRepository
                        .findById(cartId)
                        .orElse(null);


        if(cart == null)
        {
            return "Cart Item Not Found";
        }


        cartRepository.delete(cart);


        return "Item Removed From Cart";
    }


    @Override
    public String verifyOtp(
            String email,
            String otp)
    {
        PasswordResetOtp resetOtp =
                otpRepository
                        .findByEmailAndOtp(
                                email,
                                otp
                        );


        if(resetOtp == null)
        {
            return "Invalid OTP";
        }


        if(resetOtp.getExpiryTime()
                .isBefore(
                        LocalDateTime.now()
                ))
        {
            return "OTP Expired";
        }


        return "OTP Verified Successfully";
    }


    @Override
    public String resetPassword(
            String email,
            String newPassword)
    {
        User user =
                userRepository
                        .findByEmail(email);


        if(user == null)
        {
            return "User Not Found";
        }


        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        userRepository.save(user);


        PasswordResetOtp otp =
                otpRepository
                        .findByEmail(email);


        if(otp != null)
        {
            otpRepository.delete(otp);
        }


        return "Password Reset Successfully";
    }


    /*
     * CUSTOMER CANCEL ORDER
     */
    @Override
    @Transactional
    public String cancelOrder(
            int orderId)
    {
        Order order =
                orderRepository
                        .findById(orderId)
                        .orElse(null);


        if(order == null)
        {
            return "Order Not Found";
        }


        if(!order.getStatus()
                .equalsIgnoreCase(
                        "PENDING"
                ))
        {
            return "Only Pending Orders Can Be Cancelled";
        }


        List<OrderItem> items =
                orderItemRepository
                        .findByOrderId(
                                orderId
                        );


        for(OrderItem item : items)
        {
            Product product =
                    item.getProduct();


            int oldStock =
                    product.getQuantity();


            int restoredStock =
                    oldStock +
                    item.getQuantity();


            /*
             * Restore to the current product expiry
             * batch.
             */
            LocalDate expiry =
                    product.getExpiryDate();


            List<StockHistory> batches =
                    stockHistoryRepository
                            .findByProductIdAndChangeTypeAndExpiryDate(
                                    product.getId(),
                                    "ADD_STOCK",
                                    expiry
                            );


            if(!batches.isEmpty())
            {
                StockHistory batch =
                        batches.get(0);


                batch.setRemainingQuantity(
                        batch.getRemainingQuantity()
                                + item.getQuantity()
                );


                stockHistoryRepository.save(
                        batch
                );
            }
            else
            {
                StockHistory batch =
                        new StockHistory();


                batch.setProduct(
                        product
                );

                batch.setChangeType(
                        "ADD_STOCK"
                );

                batch.setQuantityChanged(
                        item.getQuantity()
                );

                batch.setOldStock(
                        oldStock
                );

                batch.setNewStock(
                        restoredStock
                );

                batch.setRemainingQuantity(
                        item.getQuantity()
                );

                batch.setExpiryDate(
                        expiry
                );

                batch.setChangedBy(
                        order.getUser()
                                .getName()
                );

                batch.setRemarks(
                        "Stock restored after customer cancellation"
                );

                batch.setDateTime(
                        LocalDateTime.now()
                );


                stockHistoryRepository.save(
                        batch
                );
            }


            product.setQuantity(
                    restoredStock
            );


            productRepository.save(
                    product
            );


            StockHistory history =
                    new StockHistory();


            history.setProduct(
                    product
            );

            history.setChangeType(
                    "ORDER_CANCELLED"
            );

            history.setQuantityChanged(
                    item.getQuantity()
            );

            history.setOldStock(
                    oldStock
            );

            history.setNewStock(
                    restoredStock
            );

            history.setRemainingQuantity(
                    0
            );

            history.setExpiryDate(
                    expiry
            );

            history.setChangedBy(
                    order.getUser()
                            .getName()
            );

            history.setRemarks(
                    "Order cancelled by customer, stock restored"
            );

            history.setDateTime(
                    LocalDateTime.now()
            );


            stockHistoryRepository.save(
                    history
            );
        }


        order.setStatus(
                "CANCELLED"
        );

        order.setCancelledBy(
                "CUSTOMER"
        );

        order.setCancelReason(
                "Cancelled by customer"
        );


        orderRepository.save(
                order
        );


        return "Order Cancelled Successfully";
    }


    @Override
    public List<OrderItem> viewOrderItems(
            int orderId)
    {
        return orderItemRepository
                .findByOrderId(
                        orderId
                );
    }


    @Override
    public double getDeliveryCharge()
    {
        if(deliverySettingRepository
                .findAll()
                .isEmpty())
        {
            return 50;
        }


        return deliverySettingRepository
                .findAll()
                .get(0)
                .getDeliveryCharge();
    }
    @Override
    public List<StockHistory> viewProductStockBatches(
            int productId)
    {
        return stockHistoryRepository
                .findByProductIdAndChangeTypeOrderByExpiryDateAsc(
                        productId,
                        "ADD_STOCK"
                );
    }
}