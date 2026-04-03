package com.mykart.mykart.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User Entity — yeh database mein user ka data store karta hai.
 *
 * Spring Security ko "UserDetails" interface chahiye taaki woh jaane:
 *   - username kya hai
 *   - password kya hai (hashed)
 *   - user ke roles/authorities kya hain
 *   - account active hai ya nahi
 *
 * Isliye yeh class UserDetails implement karti hai — Spring Security
 * isko directly authentication ke liye use kar sakta hai.
 *
 * Table name "users" rakha hai kyunki "user" H2 mein reserved keyword hai.
 */
@Entity
@Table(name = "users") // "user" H2 DB mein reserved word hai, isliye "users" use kiya
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // unique username — login ke liye use hoga

    @Column(nullable = false)
    private String password; // BCrypt se hashed password store hoga

    private String role; // e.g., "ROLE_USER" — Spring Security authority ke liye

    // ─── Constructors ───

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ─── UserDetails Interface Methods ───

    /**
     * Spring Security yeh method call karta hai user ke roles jaanne ke liye.
     * Hum user ka role (e.g., "ROLE_USER") ek SimpleGrantedAuthority mein wrap karke return karte hain.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Neeche ke 4 methods account status batate hain.
     * Hum sab mein "true" return kar rahe hain — matlab account hamesha active hai.
     * Production mein in fields ko DB mein store karke dynamically check karte hain.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Account kabhi expire nahi hoga
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account kabhi lock nahi hoga
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Password kabhi expire nahi hoga
    }

    @Override
    public boolean isEnabled() {
        return true; // Account hamesha enabled rahega
    }

    // ─── Getters & Setters ───

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
