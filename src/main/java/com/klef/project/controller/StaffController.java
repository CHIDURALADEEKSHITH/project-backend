package com.klef.project.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.klef.project.service.StaffService;

@RestController
@RequestMapping("/staff")
@CrossOrigin("*")
public class StaffController
{
    @Autowired
    private StaffService staffService;


    @GetMapping("/viewallorders")
    public ResponseEntity<?> viewAllOrders()
    {
        return ResponseEntity.ok(
                staffService.viewAllOrders()
        );
    }


    @PutMapping("/updateorderstatus")
    public ResponseEntity<?> updateOrderStatus(
            @RequestBody Map<String, String> data)
    {
        int orderId =
                Integer.parseInt(data.get("orderId"));

        String status =
                data.get("status");

        String output =
                staffService.updateOrderStatus(
                        orderId,
                        status
                );

        return ResponseEntity.ok(output);
    }


    /*
     * ADD STOCK
     */
    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(
            @RequestBody Map<String, String> data)
    {
        int productId =
                Integer.parseInt(
                        data.get("productId")
                );

        int quantity =
                Integer.parseInt(
                        data.get("quantity")
                );

        LocalDate expiryDate =
                LocalDate.parse(
                        data.get("expiryDate")
                );


        String output =
                staffService.updateStock(
                        productId,
                        quantity,
                        expiryDate
                );


        return ResponseEntity.ok(output);
    }


    @GetMapping("/viewallproducts")
    public ResponseEntity<?> viewAllProducts()
    {
        return ResponseEntity.ok(
                staffService.viewAllProducts()
        );
    }


    /*
     * OFFLINE SALE
     */
    @PostMapping("/offline-sale")
    public ResponseEntity<?> recordOfflineSale(
            @RequestBody Map<String, Integer> data)
    {
        int productId =
                data.get("productId");

        int soldQuantity =
                data.get("soldQuantity");


        String output =
                staffService.recordOfflineSale(
                        productId,
                        soldQuantity
                );


        return ResponseEntity.ok(output);
    }


    @PostMapping("/confirmorder")
    public ResponseEntity<?> confirmOrder(
            @RequestBody Map<String, Integer> data)
    {
        int orderId =
                data.get("orderId");

        return ResponseEntity.ok(
                staffService.confirmOrder(orderId)
        );
    }


    @PostMapping("/cancelorder")
    public ResponseEntity<?> cancelOrder(
            @RequestBody Map<String, String> data)
    {
        int orderId =
                Integer.parseInt(
                        data.get("orderId")
                );

        String reason =
                data.get("reason");


        return ResponseEntity.ok(
                staffService.cancelOrder(
                        orderId,
                        reason
                )
        );
    }


    @PostMapping("/setdeliverydate")
    public ResponseEntity<?> setExpectedDeliveryDate(
            @RequestBody Map<String, String> data)
    {
        int orderId =
                Integer.parseInt(
                        data.get("orderId")
                );

        LocalDate expectedDate =
                LocalDate.parse(
                        data.get("expectedDate")
                );


        return ResponseEntity.ok(
                staffService.setExpectedDeliveryDate(
                        orderId,
                        expectedDate
                )
        );
    }
    @GetMapping("/stock-batches/{productId}")
    public ResponseEntity<?> viewProductStockBatches(
            @PathVariable int productId)
    {
        return ResponseEntity.ok(
                staffService.viewProductStockBatches(productId)
        );
    }
}