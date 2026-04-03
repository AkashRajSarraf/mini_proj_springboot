package com.mykart.mykart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService — JWT Token banane aur validate karne ki poori responsibility isi class ki hai.
 *
 * ════════════════════════════════════════════════════════════════
 * JWT (JSON Web Token) kya hota hai?
 * ════════════════════════════════════════════════════════════════
 *
 * JWT ek encoded string hai jo 3 parts se bani hoti hai:
 *   HEADER.PAYLOAD.SIGNATURE
 *
 *   1. HEADER   → Algorithm (HS256) aur token type (JWT)
 *   2. PAYLOAD  → Data (claims) — e.g., username, role, expiry time
 *   3. SIGNATURE → Header + Payload ko secret key se sign kiya hua
 *                  Agar koi payload modify kare toh signature match nahi karega
 *                  → Token invalid ho jaayega (tamper-proof!)
 *
 * Example JWT:
 *   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJha2FzaCIsImlhdCI6MTcwMH0.abc123signature
 *
 * ════════════════════════════════════════════════════════════════
 * HMAC-SHA256 kya hai?
 * ════════════════════════════════════════════════════════════════
 *   → Ek signing algorithm hai
 *   → Secret key + data ko mila ke ek unique hash (signature) banata hai
 *   → Same key se dubara hash banao → same result aayega
 *   → Agar data change ho → hash change ho jaayega → tampering detect!
 *
 * ════════════════════════════════════════════════════════════════
 * Is class ke methods ka flow:
 * ════════════════════════════════════════════════════════════════
 *
 *   Login Request
 *       │
 *       ▼
 *   generateToken(userDetails)  ← Token banata hai username + expiry ke saath
 *       │
 *       ▼
 *   Client ko JWT milta hai
 *       │
 *       ▼ (next request mein)
 *   extractUsername(token)       ← Token se username nikalte hain
 *       │
 *       ▼
 *   isTokenValid(token, user)   ← Check: username match? + Token expired toh nahi?
 *       │
 *       ▼
 *   Request allowed ✓ ya rejected ✗
 */
@Service
public class JwtService {

    /**
     * Secret key — yeh application.properties se aati hai.
     * Isse JWT sign hota hai. Yeh key sirf server ko pata honi chahiye.
     * Agar yeh leak ho gayi toh koi bhi fake tokens bana sakta hai!
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Token kitne time tak valid rahega (milliseconds mein).
     * Default: 86400000ms = 24 hours
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ═══════════════════════════════════════════════════════════
    // TOKEN GENERATE KARNA
    // ═══════════════════════════════════════════════════════════

    /**
     * Naya JWT token generate karta hai sirf username ke basis pe.
     * Extra claims (additional data) nahi hain toh empty map bhejte hain.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * JWT token generate karta hai with optional extra claims.
     *
     * Token mein yeh data hota hai:
     *   - subject (sub): username
     *   - issuedAt (iat): token kab bana
     *   - expiration (exp): token kab expire hoga
     *   - extra claims: koi bhi additional data (e.g., role)
     *
     * Phir isko HMAC-SHA256 se sign karke return karta hai.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)                                          // Extra data (agar ho toh)
                .subject(userDetails.getUsername())                            // Username as subject
                .issuedAt(new Date(System.currentTimeMillis()))               // Token banane ka time
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Expiry time
                .signWith(getSigningKey())                                    // Secret key se sign karo
                .compact();                                                   // Final JWT string banao
    }

    // ═══════════════════════════════════════════════════════════
    // TOKEN SE DATA EXTRACT KARNA
    // ═══════════════════════════════════════════════════════════

    /**
     * Token se username (subject claim) extract karta hai.
     * Har incoming request mein yeh call hota hai — taaki pata chale kaun hai.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Token se koi bhi specific claim extract karta hai.
     * Generic method hai — claimsResolver function pass karo jo desired claim return kare.
     *
     * Example:
     *   extractClaim(token, Claims::getSubject)     → username milega
     *   extractClaim(token, Claims::getExpiration)   → expiry date milega
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Token se SAARE claims (payload data) extract karta hai.
     *
     * Internally kya hota hai:
     *   1. Secret key se token ki signature verify hoti hai
     *   2. Agar signature valid hai toh payload (claims) return hota hai
     *   3. Agar signature invalid hai toh exception throw hota hai
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Same secret key se verify karo
                .build()
                .parseSignedClaims(token)     // Token parse karo (signature check hota hai yahan)
                .getPayload();                // Payload (claims) return karo
    }

    // ═══════════════════════════════════════════════════════════
    // TOKEN VALIDATE KARNA
    // ═══════════════════════════════════════════════════════════

    /**
     * Token valid hai ya nahi — 2 cheezein check karta hai:
     *   1. Token mein jo username hai woh database user se match karta hai?
     *   2. Token expire toh nahi ho gaya?
     *
     * Dono conditions true → token valid ✓
     * Koi bhi false → token invalid ✗
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Token expired hai ya nahi — expiration date ko current time se compare karta hai.
     * Agar expiration date past mein hai → token expired hai.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Token se expiration date extract karta hai.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ═══════════════════════════════════════════════════════════
    // SIGNING KEY
    // ═══════════════════════════════════════════════════════════

    /**
     * Secret key string ko SecretKey object mein convert karta hai.
     *
     * Process:
     *   1. Base64-encoded string ko decode karte hain → raw bytes milte hain
     *   2. Un bytes se HMAC-SHA key object banate hain
     *   3. Yeh key object token sign aur verify dono mein use hota hai
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
