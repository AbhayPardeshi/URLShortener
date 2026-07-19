package com.springbootprojects.urlshortner.repository;

import com.springbootprojects.urlshortner.entities.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, UUID> {
}
