package com.mykart.mykart.security;

import com.mykart.mykart.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — Poore application ki security yahan configure hoti hai.
 *
 * ════════════════════════════════════════════════════════════════
 * Yeh class kya kya configure karti hai?
 * ════════════════════════════════════════════════════════════════
 *
 *   1. Kaun se URLs public hain (bina login ke access)
 *   2. Kaun se URLs protected hain (JWT chahiye)
 *   3. CSRF disable (API ke liye zaroori hai)
 *   4. Session management — STATELESS (har request mein JWT se auth hoga)
 *   5. JWT filter ko security chain mein add karna
 *   6. Password encoder (BCrypt) ka bean
 *   7. AuthenticationManager aur AuthenticationProvider beans
 *
 * ════════════════════════════════════════════════════════════════
 * Key Concepts:
 * ════════════════════════════════════════════════════════════════
 *
 * CSRF (Cross-Site Request Forgery):
 *   → Browser-based attack hai jahan malicious site user ke behalf pe
 *     request bhejti hai. REST APIs mein CSRF disable karte hain kyunki
 *     hum tokens use karte hain, cookies nahi.
 *
 * STATELESS Session:
 *   → Server koi session nahi banata (traditional web apps jaise)
 *   → Har request apna JWT token laati hai → server token se user identify karta hai
 *   → Scalable hai — koi bhi server instance request handle kar sakta hai
 *
 * BCrypt:
 *   → Password hashing algorithm hai
 *   → "password123" → "$2a$10$abc123..." (one-way hash, reverse nahi ho sakta)
 *   → Har baar alag hash banta hai (salt ke wajah se) — rainbow table attacks se safe
 *
 * SecurityFilterChain:
 *   → Spring Security mein ek chain of filters hoti hai
 *   → Har filter ek security check karta hai
 *   → Hum apna JwtAuthFilter isko chain mein add karte hain
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
    }

    // ═══════════════════════════════════════════════════════════
    // UserDetailsService Bean
    // ═══════════════════════════════════════════════════════════

    /**
     * Spring Security ko batata hai ki user ka data kahan se laana hai.
     * Yeh bean username se DB mein user dhundhta hai.
     * Agar nahi milta → UsernameNotFoundException throw hota hai.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User nahi mila: " + username
                ));
    }

    // ═══════════════════════════════════════════════════════════
    // Security Filter Chain — Main security rules
    // ═══════════════════════════════════════════════════════════

    /**
     * HTTP security rules define karta hai:
     *   - /auth/** → PUBLIC (register/login ke liye)
     *   - /h2-console/** → PUBLIC (development ke liye)
     *   - Baaki sab → AUTHENTICATED (JWT chahiye)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disable — REST API mein cookies use nahi karte, toh CSRF attack ka risk nahi
            .csrf(csrf -> csrf.disable())

            // H2 console iframe mein chalta hai, uske liye frame options disable karna padta hai
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // URL authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()        // Login/Register → public
                .requestMatchers("/h2-console/**").permitAll()  // H2 DB console → public
                .anyRequest().authenticated()                   // Baaki sab → JWT chahiye
            )

            // Session management — STATELESS (koi server-side session nahi)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authentication provider set karo (DB + BCrypt password check)
            .authenticationProvider(authenticationProvider())

            // Humara JWT filter, Spring ke default UsernamePasswordAuthenticationFilter se PEHLE run hoga
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ═══════════════════════════════════════════════════════════
    // Authentication Provider
    // ═══════════════════════════════════════════════════════════

    /**
     * DaoAuthenticationProvider — yeh provider:
     *   1. UserDetailsService se user laata hai (DB se)
     *   2. PasswordEncoder se password compare karta hai (BCrypt hash match)
     *   3. Dono match → authentication successful ✓
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ═══════════════════════════════════════════════════════════
    // Authentication Manager
    // ═══════════════════════════════════════════════════════════

    /**
     * AuthenticationManager — Spring Security ka main entry point hai authentication ke liye.
     * Login endpoint mein hum isko call karte hain user credentials verify karne ke liye.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ═══════════════════════════════════════════════════════════
    // Password Encoder
    // ═══════════════════════════════════════════════════════════

    /**
     * BCryptPasswordEncoder — password hash karne ke liye.
     * Register ke time: plain password → BCrypt hash (store in DB)
     * Login ke time: entered password ko hash karke DB hash se compare
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
