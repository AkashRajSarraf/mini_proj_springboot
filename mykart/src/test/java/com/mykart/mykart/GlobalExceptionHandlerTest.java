package com.mykart.mykart;

import com.mykart.mykart.exception.ErrorResponse;
import com.mykart.mykart.exception.GlobalExceptionHandler;
import com.mykart.mykart.exception.InsufficientStockException;
import com.mykart.mykart.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404_withExpectedBody() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/test/notfound");

        ResponseEntity<ErrorResponse> resp = handler.handleNotFound(new ResourceNotFoundException("not found"), req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getMessage()).isEqualTo("not found");
        assertThat(body.getPath()).isEqualTo("/test/notfound");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    void handleInsufficientStock_returns409_withExpectedBody() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/trade/sell");

        ResponseEntity<ErrorResponse> resp = handler.handleInsufficientStock(new InsufficientStockException("insufficient"), req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(409);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getMessage()).isEqualTo("insufficient");
        assertThat(body.getPath()).isEqualTo("/trade/sell");
    }

    @Test
    void handleIllegalArgument_returns400_withExpectedBody() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/trade/sell");

        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(new IllegalArgumentException("bad arg"), req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getMessage()).isEqualTo("bad arg");
        assertThat(body.getPath()).isEqualTo("/trade/sell");
    }

    @Test
    void handleGeneric_returns500_withGenericMessage() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/any/path");

        ResponseEntity<ErrorResponse> resp = handler.handleGeneric(new Exception("boom"), req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(500);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(body.getPath()).isEqualTo("/any/path");
    }
}

