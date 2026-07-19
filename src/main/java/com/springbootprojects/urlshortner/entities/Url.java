package com.springbootprojects.urlshortner.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String shortURL;
    @Column(columnDefinition = "TEXT")
    String longURL;
    boolean isCustomAlias;
    Instant createdAt;
    Instant expiredAt;
    long clickCount;
}
