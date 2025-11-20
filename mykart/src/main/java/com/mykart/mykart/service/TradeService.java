package com.mykart.mykart.service;

import com.mykart.mykart.model.Inventory;
import com.mykart.mykart.model.Trade;
import com.mykart.mykart.repository.InventoryRepository;
import com.mykart.mykart.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final InventoryRepository inventoryRepository;

    public TradeService(TradeRepository tradeRepository, InventoryRepository inventoryRepository) {
        this.tradeRepository = tradeRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // Fetch trade history for all products
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    // Fetch trade history for a specific product
    public List<Trade> getTradeHistoryByProductId(Long productId) {
        return tradeRepository.findByProductId(productId);
    }

    // Buy a product (increase inventory)
    @Transactional
    public Trade buyProduct(Long productId, Integer quantity, Double price) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

        inventory.setBuyAmt(price);
        inventory.setInventoryCount(inventory.getInventoryCount() + quantity);
        inventoryRepository.save(inventory);

        Trade trade = new Trade(null, productId, quantity, price, "BUY", null);
        return tradeRepository.save(trade);
    }

    // Sell a product (decrease inventory)
    @Transactional
    public Trade sellProduct(Long productId, Integer quantity, Double price) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

        if (inventory.getInventoryCount() < quantity) {
            throw new RuntimeException("Insufficient stock to sell");
        }

        if (price < inventory.getBuyAmt()) {
            throw new RuntimeException("Sale price is lower than buy price, loss will occur!");
        }

        inventory.setInventoryCount(inventory.getInventoryCount() - quantity);
        inventory.setSaleAmt(price);
        inventoryRepository.save(inventory);

        Trade trade = new Trade(null, productId, quantity, price, "SELL", null);
        return tradeRepository.save(trade);
    }
}

