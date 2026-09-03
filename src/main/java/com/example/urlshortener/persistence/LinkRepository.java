package com.example.urlshortener.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public interface LinkRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByShortCode(String shortCode);
}
