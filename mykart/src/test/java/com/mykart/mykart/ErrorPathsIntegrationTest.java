package com.mykart.mykart;

import com.mykart.mykart.controller.InventoryController;
import com.mykart.mykart.controller.TradeController;
import com.mykart.mykart.exception.GlobalExceptionHandler;
import com.mykart.mykart.exception.InsufficientStockException;
import com.mykart.mykart.exception.ResourceNotFoundException;
import com.mykart.mykart.service.InventoryService;
import com.mykart.mykart.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorPathsIntegrationTest {

    private MockMvc mockMvc;

    private InventoryService inventoryService;
    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        inventoryService = Mockito.mock(InventoryService.class);
        tradeService = Mockito.mock(TradeService.class);

        InventoryController inventoryController = new InventoryController(inventoryService);
        TradeController tradeController = new TradeController(tradeService);
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        this.mockMvc = MockMvcBuilders.standaloneSetup(inventoryController, tradeController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void updatePrice_notFound_returns404_withErrorResponse() throws Exception {
        when(inventoryService.updateSalePrice(anyLong(), anyDouble()))
                .thenThrow(new ResourceNotFoundException("Product not found in inventory"));

        mockMvc.perform(put("/inventory/update-price/1").param("price", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found in inventory"))
                .andExpect(jsonPath("$.path").value("/inventory/update-price/1"));
    }

    @Test
    void sell_insufficientStock_returns409_withErrorResponse() throws Exception {
        when(tradeService.sellProduct(anyLong(), anyInt(), anyDouble()))
                .thenThrow(new InsufficientStockException("Insufficient stock to sell"));

        mockMvc.perform(post("/trade/sell")
                        .param("productId", "1")
                        .param("quantity", "1000")
                        .param("price", "50"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Insufficient stock to sell"))
                .andExpect(jsonPath("$.path").value("/trade/sell"));
    }

    @Test
    void sell_invalidPrice_returns400_withErrorResponse() throws Exception {
        when(tradeService.sellProduct(anyLong(), anyInt(), anyDouble()))
                .thenThrow(new IllegalArgumentException("Sale price is lower than buy price, loss will occur!"));

        mockMvc.perform(post("/trade/sell")
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("price", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Sale price is lower than buy price, loss will occur!"))
                .andExpect(jsonPath("$.path").value("/trade/sell"));
    }

    @Test
    void buy_genericException_returns500_withGenericMessage() throws Exception {
        when(tradeService.buyProduct(anyLong(), anyInt(), anyDouble()))
                .thenThrow(new RuntimeException("unexpected failure"));

        mockMvc.perform(post("/trade/buy")
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("price", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/trade/buy"));
    }
}
