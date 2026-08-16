package com.klef.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    /*
     * ADD STOCK
     *
     * Staff selects:
     * Product
     * Quantity
     * Expiry Date
     *
     * If same expiry already exists:
     *     increase that batch.
     *
     * If different expiry:
     *     create a new batch.
     */
    @Override
    @Transactional
    public String updateStock(
            int productId,
            int quantity,
            LocalDate expiryDate)
    {
        Product product = productRepository
                .findById(productId)
                .orElse(null);

        if(product == null)
        {
            return "Product Not Found";
        }

        if(quantity <= 0)
        {
            return "Quantity Must Be Greater Than Zero";
        }

        if(expiryDate == null)
        {
            return "Expiry Date Required";
        }

        if(expiryDate.isBefore(LocalDate.now()))
        {
            return "Expiry Date Cannot Be In The Past";
        }


        int oldStock = product.getQuantity();
        int newStock = oldStock + quantity;


        /*
         * Check whether a batch with the same expiry already exists.
         */
        List<StockHistory> sameExpiryBatches =
                stockHistoryRepository
                        .findByProductIdAndChangeTypeAndExpiryDate(
                                productId,
                                "ADD_STOCK",
                                expiryDate
                        );


        StockHistory batch;


        if(!sameExpiryBatches.isEmpty())
        {
            /*
             * Same expiry date.
             * Add the new quantity to existing batch.
             */
            batch = sameExpiryBatches.get(0);

            batch.setRemainingQuantity(
                    batch.getRemainingQuantity() + quantity
            );

            batch.setQuantityChanged(
                    batch.getQuantityChanged() + quantity
            );

            batch.setNewStock(newStock);

            batch.setRemarks(
                    "Additional stock added to existing expiry batch"
            );

            batch.setDateTime(LocalDateTime.now());

            stockHistoryRepository.save(batch);
        }
        else
        {
            /*
             * New expiry date.
             * Create a new batch.
             */
            batch = new StockHistory();

            batch.setProduct(product);
            batch.setChangeType("ADD_STOCK");

            batch.setQuantityChanged(quantity);
            batch.setOldStock(oldStock);
            batch.setNewStock(newStock);

            batch.setRemainingQuantity(quantity);

            batch.setExpiryDate(expiryDate);

            batch.setChangedBy("STAFF");

            batch.setRemarks(
                    "New stock batch added by staff"
            );

            batch.setDateTime(LocalDateTime.now());

            stockHistoryRepository.save(batch);
        }


        /*
         * Product table stores total stock.
         */
        product.setQuantity(newStock);

        /*
         * Keep Product expiryDate as latest/current expiry
         * for compatibility with your existing system.
         */
        product.setExpiryDate(expiryDate);

        productRepository.save(product);


        return "Stock Added Successfully. Current Stock: "
                + newStock;
    }


    @Override
    public List<Product> viewAllProducts()
    {
        return productRepository.findAll();
    }


    /*
     * OFFLINE SALE
     *
     * FEFO:
     * First Expiry First Out
     */
    @Override
    @Transactional
    public String recordOfflineSale(
            int productId,
            int soldQuantity)
    {
        Product product = productRepository
                .findById(productId)
                .orElse(null);

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


        /*
         * Get batches sorted by earliest expiry.
         */
        List<StockHistory> batches =
                stockHistoryRepository
                        .findByProductIdAndChangeTypeOrderByExpiryDateAsc(
                                productId,
                                "ADD_STOCK"
                        );


        /*
         * Remove expired/empty batches from usable list.
         */
        int remainingToSell = soldQuantity;


        for(StockHistory batch : batches)
        {
            if(remainingToSell <= 0)
            {
                break;
            }

            if(batch.getRemainingQuantity() <= 0)
            {
                continue;
            }

            if(batch.getExpiryDate() != null
                    && batch.getExpiryDate().isBefore(LocalDate.now()))
            {
                continue;
            }


            int available = batch.getRemainingQuantity();

            int used = Math.min(
                    available,
                    remainingToSell
            );


            batch.setRemainingQuantity(
                    available - used
            );

            stockHistoryRepository.save(batch);

            remainingToSell -= used;
        }


        /*
         * If there are not enough batch records,
         * create a legacy batch using Product expiry.
         *
         * This helps existing products created before
         * this new batch system.
         */
        if(remainingToSell > 0)
        {
            LocalDate expiry = product.getExpiryDate();

            if(expiry == null)
            {
                return "Stock Batch Information Not Available";
            }

            StockHistory legacyBatch = new StockHistory();

            legacyBatch.setProduct(product);
            legacyBatch.setChangeType("ADD_STOCK");
            legacyBatch.setQuantityChanged(remainingToSell);
            legacyBatch.setOldStock(product.getQuantity());
            legacyBatch.setNewStock(product.getQuantity());
            legacyBatch.setRemainingQuantity(0);
            legacyBatch.setExpiryDate(expiry);
            legacyBatch.setChangedBy("SYSTEM");
            legacyBatch.setRemarks(
                    "Legacy stock batch created automatically"
            );
            legacyBatch.setDateTime(LocalDateTime.now());

            stockHistoryRepository.save(legacyBatch);
        }


        int oldStock = product.getQuantity();
        int newStock = oldStock - soldQuantity;

        product.setQuantity(newStock);

        productRepository.save(product);


        /*
         * Sale history.
         */
        StockHistory history = new StockHistory();

        history.setProduct(product);
        history.setChangeType("OFFLINE_SALE");

        history.setQuantityChanged(-soldQuantity);

        history.setOldStock(oldStock);
        history.setNewStock(newStock);

        history.setRemainingQuantity(0);

        history.setChangedBy("STAFF");

        history.setRemarks(
                "Product sold offline in shop"
        );

        history.setDateTime(LocalDateTime.now());

        stockHistoryRepository.save(history);


        return "Offline Sale Recorded Successfully. Current Stock: "
                + newStock;
    }


    @Override
    public String confirmOrder(int orderId)
    {
        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

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
    @Transactional
    public String cancelOrder(
            int orderId,
            String reason)
    {
        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

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


        List<OrderItem> items =
                orderItemRepository.findByOrderId(orderId);


        for(OrderItem item : items)
        {
            Product product = item.getProduct();

            int oldStock = product.getQuantity();

            int newStock =
                    oldStock + item.getQuantity();


            /*
             * Restore stock.
             *
             * Since OrderItem currently doesn't store
             * the exact batch, restore to the product's
             * current expiry batch when possible.
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
                StockHistory batch = batches.get(0);

                batch.setRemainingQuantity(
                        batch.getRemainingQuantity()
                                + item.getQuantity()
                );

                stockHistoryRepository.save(batch);
            }
            else
            {
                StockHistory batch = new StockHistory();

                batch.setProduct(product);
                batch.setChangeType("ADD_STOCK");

                batch.setQuantityChanged(
                        item.getQuantity()
                );

                batch.setOldStock(oldStock);
                batch.setNewStock(newStock);

                batch.setRemainingQuantity(
                        item.getQuantity()
                );

                batch.setExpiryDate(expiry);

                batch.setChangedBy("STAFF");

                batch.setRemarks(
                        "Stock restored after order cancellation"
                );

                batch.setDateTime(
                        LocalDateTime.now()
                );

                stockHistoryRepository.save(batch);
            }


            product.setQuantity(newStock);

            productRepository.save(product);


            StockHistory history =
                    new StockHistory();

            history.setProduct(product);

            history.setChangeType(
                    "ORDER_CANCELLED"
            );

            history.setQuantityChanged(
                    item.getQuantity()
            );

            history.setOldStock(oldStock);
            history.setNewStock(newStock);

            history.setRemainingQuantity(0);

            history.setExpiryDate(expiry);

            history.setChangedBy("STAFF");

            history.setRemarks(
                    "Order cancelled by staff. Reason: "
                    + reason
            );

            history.setDateTime(
                    LocalDateTime.now()
            );

            stockHistoryRepository.save(history);
        }


        order.setStatus("CANCELLED");

        order.setCancelledBy("MANAGEMENT");

        order.setCancelReason(reason);

        orderRepository.save(order);


        return "Order Cancelled Successfully";
    }


    @Override
    public String setExpectedDeliveryDate(
            int orderId,
            LocalDate expectedDate)
    {
        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

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
    public List<StockHistory> viewProductStockBatches(int productId)
    {
        return stockHistoryRepository
                .findByProductIdAndChangeTypeOrderByExpiryDateAsc(
                        productId,
                        "ADD_STOCK"
                );
    }
}