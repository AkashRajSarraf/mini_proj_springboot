package com.mykart.mykart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter — Har incoming HTTP request ko intercept karke JWT token check karta hai.
 *
 * ════════════════════════════════════════════════════════════════
 * Yeh Filter kya karta hai? (Step-by-step)
 * ════════════════════════════════════════════════════════════════
 *
 *   1. Request aati hai → Filter sabse pehle run hota hai
 *   2. "Authorization" header check karta hai
 *   3. Agar header mein "Bearer <token>" milta hai → token extract karta hai
 *   4. Token se username nikalte hain (JwtService.extractUsername)
 *   5. Username se user ka data DB se laate hain (UserDetailsService)
 *   6. Token validate karta hai (signature + expiry check)
 *   7. Agar sab theek hai → SecurityContext mein user ko set karta hai
 *      (ab Spring Security jaanta hai ki yeh authenticated user hai)
 *   8. Request aage controller tak jaati hai
 *
 * ════════════════════════════════════════════════════════════════
 * OncePerRequestFilter kya hai?
 * ════════════════════════════════════════════════════════════════
 *   → Spring ka ek special filter hai
 *   → Guarantee karta hai ki yeh filter ek request mein SIRF EK BAAR run hoga
 *   → Agar forward/redirect hota hai toh bhi dobara nahi chalega
 *
 * ════════════════════════════════════════════════════════════════
 * SecurityContext kya hai?
 * ════════════════════════════════════════════════════════════════
 *   → Spring Security ka in-memory storage hai
 *   → Current request ke liye authenticated user ki info yahan store hoti hai
 *   → Controller se @AuthenticationPrincipal se access kar sakte hain
 *   → Request complete hone pe automatically clear ho jaata hai
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Constructor Injection — Spring automatically JwtService aur UserDetailsService inject karega
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Har request pe yeh method call hota hai.
     * Yeh JWT token ko extract, validate, aur SecurityContext mein set karta hai.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ─── Step 1: Authorization header extract karo ───
        final String authHeader = request.getHeader("Authorization");

        // Agar header nahi hai ya "Bearer " se start nahi hota → skip karo, aage jaao
        // (Public endpoints jaise /auth/login yahan se directly pass ho jaayenge)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Agle filter/controller ko bhejo
            return;
        }

        // ─── Step 2: "Bearer " ke baad ka actual token extract karo ───
        // "Bearer eyJhbGciOiJIUzI1NiJ9..." → "eyJhbGciOiJIUzI1NiJ9..."
        final String jwt = authHeader.substring(7);

        // ─── Step 3: Token se username nikalo ───
        final String username = jwtService.extractUsername(jwt);

        // ─── Step 4: Validate karo ───
        // Conditions:
        //   - username null nahi hona chahiye
        //   - SecurityContext mein pehle se koi authenticated user nahi hona chahiye
        //     (agar hai toh dobara validate karne ki zaroorat nahi)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // DB se user details laao
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Token valid hai? (username match + not expired)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // ─── Step 5: Authentication token banao aur SecurityContext mein set karo ───
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,        // Principal (authenticated user)
                        null,               // Credentials (password nahi chahiye ab, token valid hai)
                        userDetails.getAuthorities() // User ke roles/permissions
                );

                // Request ki extra details (IP address, session ID, etc.) attach karo
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext mein set karo — ab Spring Security jaanta hai user authenticated hai
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ─── Step 6: Request ko aage bhejo (next filter ya controller) ───
        filterChain.doFilter(request, response);
    }
}
