package com.mykart.mykart.controller;

import com.mykart.mykart.model.Trade;
import com.mykart.mykart.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trade")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    // GET: View all trade history
    @GetMapping("/history")
    public ResponseEntity<List<Trade>> getAllTradeHistory() {
        return ResponseEntity.ok(tradeService.getAllTrades());
    }

    // GET: View trade history for a specific product
    @GetMapping("/history/{productId}")
    public ResponseEntity<List<Trade>> getTradeHistoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(tradeService.getTradeHistoryByProductId(productId));
    }

    // POST: Buy a product
    @PostMapping("/buy")
    public ResponseEntity<Trade> buyProduct(@RequestParam Long productId, @RequestParam Integer quantity, @RequestParam Double price) {
        return ResponseEntity.ok(tradeService.buyProduct(productId, quantity, price));
    }

    // POST: Sell a product
    @PostMapping("/sell")
    public ResponseEntity<Trade> sellProduct(@RequestParam Long productId, @RequestParam Integer quantity, @RequestParam Double price) {
        return ResponseEntity.ok(tradeService.sellProduct(productId, quantity, price));
    }
}

