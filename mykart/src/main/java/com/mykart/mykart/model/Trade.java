package com.mykart.mykart.model;

import jakarta.persistence.*;
        import lombok.*;

        import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradeId;

    private Long productId;
    private int quantity;
    private double price;
    private String buySellFlag; // "BUY" or "SELL"
    private LocalDateTime executionDate;

    public Trade(Long tradeId, Long productId, int quantity, double price, String buySellFlag, LocalDateTime executionDate) {
        this.tradeId = tradeId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.buySellFlag = buySellFlag;
        this.executionDate = executionDate;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBuySellFlag() {
        return buySellFlag;
    }

    public void setBuySellFlag(String buySellFlag) {
        this.buySellFlag = buySellFlag;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }
}
