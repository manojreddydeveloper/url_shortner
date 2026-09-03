package com.example.urlshortener.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByShortCode(String shortCode);
}
