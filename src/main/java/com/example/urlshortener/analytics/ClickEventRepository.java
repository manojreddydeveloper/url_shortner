package com.example.urlshortener.analytics;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClickEventRepository extends JpaRepository<ClickEventEntity, Long>, ClickEventRepositoryCustom {

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ClickEventEntity event where event.occurredAt <= :cutoffInclusive")
    int deleteExpired(@Param("cutoffInclusive") Instant cutoffInclusive);
}
