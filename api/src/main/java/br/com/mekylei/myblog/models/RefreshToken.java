package br.com.mekylei.myblog.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refreshtoken")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(name = "expirydate", nullable = false)
    private LocalDateTime expiryDate;

    public RefreshToken() {
    }

    public RefreshToken(String token, String email, LocalDateTime expiryDate) {
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

}
