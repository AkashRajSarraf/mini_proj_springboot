package com.mykart.mykart.controller;

import com.mykart.mykart.model.Inventory;
import com.mykart.mykart.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // GET: View inventory standing
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    // GET: View inventory of a specific product
    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT: Update sale price
    @PutMapping("/update-price/{productId}")
    public ResponseEntity<Inventory> updateSalePrice(@PathVariable Long productId, @RequestParam Double price) {
        return ResponseEntity.ok(inventoryService.updateSalePrice(productId, price));
    }
}
