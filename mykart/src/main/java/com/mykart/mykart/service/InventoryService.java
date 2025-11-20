package com.mykart.mykart.service;

import com.mykart.mykart.model.Inventory;
import com.mykart.mykart.model.Product;
import com.mykart.mykart.repository.InventoryRepository;
import com.mykart.mykart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    // Fetch full inventory
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // Fetch inventory details of a specific product
    public Optional<Inventory> getInventoryByProductId(Long productId) {
        return inventoryRepository.findById(productId);
    }

    // Update sale price of a product
    public Inventory updateSalePrice(Long productId, Double newPrice) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

        inventory.setSaleAmt(newPrice);
        return inventoryRepository.save(inventory);
    }
}
