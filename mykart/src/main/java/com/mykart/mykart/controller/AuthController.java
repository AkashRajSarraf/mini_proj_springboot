package com.mykart.mykart.controller;

import com.mykart.mykart.dto.AuthRequest;
import com.mykart.mykart.dto.AuthResponse;
import com.mykart.mykart.model.User;
import com.mykart.mykart.repository.UserRepository;
import com.mykart.mykart.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Register aur Login ke endpoints yahan hain.
 *
 * ════════════════════════════════════════════════════════════════
 * Authentication Flow:
 * ════════════════════════════════════════════════════════════════
 *
 *   ┌─────────────────────────────────────────────────────────┐
 *   │                    REGISTER FLOW                        │
 *   │                                                         │
 *   │  Client ──POST /auth/register──→ AuthController         │
 *   │    { "username": "akash", "password": "pass123" }       │
 *   │                                                         │
 *   │  1. Check: username already exists?                     │
 *   │     → Yes: return 409 Conflict                          │
 *   │     → No: continue                                      │
 *   │  2. Password ko BCrypt se hash karo                     │
 *   │  3. User ko DB mein save karo                           │
 *   │  4. JWT token generate karo                             │
 *   │  5. Token client ko return karo                         │
 *   └─────────────────────────────────────────────────────────┘
 *
 *   ┌─────────────────────────────────────────────────────────┐
 *   │                     LOGIN FLOW                          │
 *   │                                                         │
 *   │  Client ──POST /auth/login──→ AuthController            │
 *   │    { "username": "akash", "password": "pass123" }       │
 *   │                                                         │
 *   │  1. AuthenticationManager credentials verify karta hai  │
 *   │     → Fail: return 401 Unauthorized                     │
 *   │     → Success: continue                                 │
 *   │  2. JWT token generate karo                             │
 *   │  3. Token client ko return karo                         │
 *   └─────────────────────────────────────────────────────────┘
 *
 * Dono cases mein client ko JWT token milta hai.
 * Client usse agle requests mein "Authorization: Bearer <token>" header mein bhejega.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTER — Naya user create karo
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /auth/register
     *
     * Request Body: { "username": "akash", "password": "pass123" }
     *
     * Kya karta hai:
     *   1. Check karta hai ki username pehle se toh nahi hai
     *   2. Password ko BCrypt se hash karta hai (plain text kabhi store nahi hota!)
     *   3. User ko "ROLE_USER" role ke saath DB mein save karta hai
     *   4. JWT token generate karke return karta hai
     *
     * Response: { "token": "eyJhbGci...", "username": "akash", "message": "Registration successful!" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        // Check: ya username already used hai?
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409 Conflict
                    .body(new AuthResponse(null, request.getUsername(),
                            "Username already exists! Doosra username try karo."));
        }

        // Naya user banao — password hash karke store karo
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()), // BCrypt hash
                "ROLE_USER" // Default role
        );
        userRepository.save(user);

        // JWT token generate karo
        String token = jwtService.generateToken(user);

        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(new AuthResponse(token, user.getUsername(), "Registration successful!"));
    }

    // ═══════════════════════════════════════════════════════════
    // LOGIN — Existing user ko authenticate karo
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /auth/login
     *
     * Request Body: { "username": "akash", "password": "pass123" }
     *
     * Kya karta hai:
     *   1. AuthenticationManager credentials verify karta hai
     *      → Internally: DB se user laata hai, BCrypt se password match karta hai
     *   2. Agar match → JWT token generate karke return karta hai
     *   3. Agar mismatch → 401 Unauthorized return karta hai
     *
     * Response (success): { "token": "eyJhbGci...", "username": "akash", "message": "Login successful!" }
     * Response (failure): { "token": null, "username": "akash", "message": "Invalid credentials!" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // AuthenticationManager se credentials verify karo
            // Internally: UserDetailsService se user laayega + BCrypt se password check karega
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Authentication successful → JWT generate karo
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(
                    new AuthResponse(token, userDetails.getUsername(), "Login successful!")
            );

        } catch (BadCredentialsException e) {
            // Username ya password galat hai
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401 Unauthorized
                    .body(new AuthResponse(null, request.getUsername(),
                            "Invalid username or password! Dobara try karo."));
        }
    }
}
