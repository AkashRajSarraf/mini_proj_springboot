package com.mykart.mykart.dto;

/**
 * AuthResponse DTO — Server se client ko jaane wala response after login/register.
 *
 * Jab user successfully login ya register karta hai, toh hum usse yeh return karte hain:
 *   - token: JWT token (isse client har agle request mein header mein bhejega)
 *   - username: confirm karne ke liye ki kis user ka token hai
 *   - message: success/failure message (e.g., "Login successful!")
 *
 * Client isko receive karke token ko localStorage/sessionStorage mein save karega
 * aur har API call mein "Authorization: Bearer <token>" header mein bhejega.
 */
public class AuthResponse {

    private String token;    // JWT token string
    private String username; // Logged-in user ka naam
    private String message;  // Status message (e.g., "Registration successful!")

    // ─── Constructors ───

    public AuthResponse() {}

    public AuthResponse(String token, String username, String message) {
        this.token = token;
        this.username = username;
        this.message = message;
    }

    // ─── Getters & Setters ───

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
