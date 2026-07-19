package com.springbootprojects.urlshortner.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class IdempotencyKey {
    @Id
    UUID idempotencyId;

    // allows you to store strings of unlimited length overwrite the max 255 char
    @Column(columnDefinition = "TEXT")
    String responseBody;
    Instant createdAt;
}
