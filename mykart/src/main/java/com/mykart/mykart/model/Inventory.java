package com.mykart.mykart.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    private Long productId; // Maps to Product
    private double buyAmt;
    private double saleAmt;
    private int inventoryCount;
    private double profitLoss;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public double getBuyAmt() {
        return buyAmt;
    }

    public void setBuyAmt(double buyAmt) {
        this.buyAmt = buyAmt;
    }

    public double getSaleAmt() {
        return saleAmt;
    }

    public void setSaleAmt(double saleAmt) {
        this.saleAmt = saleAmt;
    }

    public int getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(int inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(double profitLoss) {
        this.profitLoss = profitLoss;
    }
}
