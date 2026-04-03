package com.mykart.mykart.repository;

import com.mykart.mykart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — User entity ke liye database operations.
 *
 * Spring Data JPA automatically iska implementation generate karega.
 * Hume sirf method signature likhni hai, query khud ban jaayegi.
 *
 * findByUsername() → SELECT * FROM users WHERE username = ?
 * Yeh login/register ke time user ko DB se dhundhne ke liye use hota hai.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Username se user dhundhna — login ke time credentials verify karne ke liye.
     * Optional isliye return karte hain kyunki user mil bhi sakta hai aur nahi bhi.
     */
    Optional<User> findByUsername(String username);
}
