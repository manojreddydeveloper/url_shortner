package com.example.urlshortener.cache;

import com.example.urlshortener.config.UrlShortenerCacheProperties;
import com.example.urlshortener.persistence.LinkEntity;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class LinkCache {
    private final int maxEntries;
    private final LinkedHashMap<String, LinkEntity> entries;

    @Autowired
    public LinkCache(UrlShortenerCacheProperties properties) {
        this(properties.maxEntries());
    }

    public LinkCache(int maxEntries) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be at least 1");
        }
        this.maxEntries = maxEntries;
        this.entries = new LinkedHashMap<>(16, 0.75f, true);
    }

    public Optional<LinkEntity> get(String shortCode) {
        synchronized (entries) {
            return Optional.ofNullable(entries.get(shortCode));
        }
    }

    public void put(LinkEntity link) {
        Objects.requireNonNull(link, "link");
        synchronized (entries) {
            entries.put(link.getShortCode(), link);
            trimToSize();
        }
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
    }

    public int size() {
        synchronized (entries) {
            return entries.size();
        }
    }

    private void trimToSize() {
        while (entries.size() > maxEntries) {
            Iterator<Map.Entry<String, LinkEntity>> iterator = entries.entrySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
            iterator.remove();
        }
    }
}
