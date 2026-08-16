package com.klef.project.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_history_table")
public class StockHistory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String changeType; 
    // ADD_STOCK, OFFLINE_SALE, ONLINE_ORDER, ORDER_CANCELLED

    private int quantityChanged;

    private int oldStock;

    private int newStock;

    private String changedBy;

    @Column(length = 500)
    private String remarks;

    private LocalDateTime dateTime;

    /*
     * Expiry date of this stock batch.
     */
    private LocalDate expiryDate;

    /*
     * Remaining quantity in this particular batch.
     *
     * This is mainly used for ADD_STOCK records.
     */
    private int remainingQuantity;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Product getProduct()
    {
        return product;
    }

    public void setProduct(Product product)
    {
        this.product = product;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public int getQuantityChanged()
    {
        return quantityChanged;
    }

    public void setQuantityChanged(int quantityChanged)
    {
        this.quantityChanged = quantityChanged;
    }

    public int getOldStock()
    {
        return oldStock;
    }

    public void setOldStock(int oldStock)
    {
        this.oldStock = oldStock;
    }

    public int getNewStock()
    {
        return newStock;
    }

    public void setNewStock(int newStock)
    {
        this.newStock = newStock;
    }

    public String getChangedBy()
    {
        return changedBy;
    }

    public void setChangedBy(String changedBy)
    {
        this.changedBy = changedBy;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public LocalDateTime getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime)
    {
        this.dateTime = dateTime;
    }

    public LocalDate getExpiryDate()
    {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate)
    {
        this.expiryDate = expiryDate;
    }

    public int getRemainingQuantity()
    {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity)
    {
        this.remainingQuantity = remainingQuantity;
    }
}