package com.mykart.mykart.dto;

/**
 * AuthRequest DTO — Client se aane wala login/register request ka data.
 *
 * DTO (Data Transfer Object) kya hai?
 *   → Yeh ek simple class hai jo sirf data carry karti hai.
 *   → Entity (User.java) mein bahut saare fields hain jo client ko dikhane ki zaroorat nahi.
 *   → DTO se hum sirf required fields (username, password) lete hain.
 *
 * Yeh class /auth/register aur /auth/login dono endpoints mein use hogi.
 * Client JSON body mein { "username": "akash", "password": "pass123" } bhejega.
 */
public class AuthRequest {

    private String username; // User ka username
    private String password; // User ka plain-text password (server pe hash hoga)

    // ─── Constructors ───

    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ─── Getters & Setters ───

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
