package com.example.urlshortener.cache;

import com.example.urlshortener.config.UrlShortenerCacheProperties;
import com.example.urlshortener.persistence.LinkEntity;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class LinkCache {
    private static final String KEY_PREFIX = "url-shortener:links:cache:";
    private static final String INDEX_KEY = KEY_PREFIX + "index";

    private final int maxEntries;
    private final Duration redisTtl;
    private final StringRedisTemplate redisTemplate;
    private final LinkedHashMap<String, LinkEntity> entries;

    @Autowired
    public LinkCache(UrlShortenerCacheProperties properties, ObjectProvider<StringRedisTemplate> redisTemplate) {
        this(properties.maxEntries(), properties.redisTtl(), redisTemplate.getIfAvailable());
    }

    public LinkCache(int maxEntries) {
        this(maxEntries, Duration.ofMinutes(30), null);
    }

    LinkCache(int maxEntries, Duration redisTtl, StringRedisTemplate redisTemplate) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be at least 1");
        }
        this.maxEntries = maxEntries;
        this.redisTtl = Objects.requireNonNull(redisTtl, "redisTtl");
        this.redisTemplate = redisTemplate;
        this.entries = new LinkedHashMap<>(16, 0.75f, true);
    }

    public Optional<LinkEntity> get(String shortCode) {
        if (shortCode == null) {
            return Optional.empty();
        }
        if (redisTemplate != null) {
            return readFromRedis(shortCode);
        }
        synchronized (entries) {
            return Optional.ofNullable(entries.get(shortCode));
        }
    }

    public void put(LinkEntity link) {
        Objects.requireNonNull(link, "link");
        if (redisTemplate != null) {
            writeToRedis(link);
            return;
        }
        mirrorLocally(link);
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
        if (redisTemplate == null) {
            return;
        }
        try {
            Set<String> codes = redisTemplate.opsForSet().members(INDEX_KEY);
            if (codes != null && !codes.isEmpty()) {
                List<String> keys = new ArrayList<>(codes.size() + 1);
                for (String code : codes) {
                    keys.add(cacheKey(code));
                }
                keys.add(INDEX_KEY);
                redisTemplate.delete(keys);
            } else {
                redisTemplate.delete(INDEX_KEY);
            }
        } catch (RuntimeException ignored) {
        }
    }

    public int size() {
        if (redisTemplate == null) {
            synchronized (entries) {
                return entries.size();
            }
        }
        try {
            Set<String> codes = redisTemplate.opsForSet().members(INDEX_KEY);
            if (codes == null || codes.isEmpty()) {
                return 0;
            }
            int count = 0;
            List<String> staleCodes = new ArrayList<>();
            for (String code : codes) {
                String stored = redisTemplate.opsForValue().get(cacheKey(code));
                if (stored == null) {
                    staleCodes.add(code);
                } else {
                    count++;
                }
            }
            if (!staleCodes.isEmpty()) {
                redisTemplate.opsForSet().remove(INDEX_KEY, (Object[]) staleCodes.toArray(new String[0]));
            }
            return count;
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private Optional<LinkEntity> readFromRedis(String shortCode) {
        if (redisTemplate == null) {
            return Optional.empty();
        }
        try {
            String serialized = redisTemplate.opsForValue().get(cacheKey(shortCode));
            if (serialized == null) {
                removeIndexEntry(shortCode);
                return Optional.empty();
            }
            return Optional.of(deserialize(serialized));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private void writeToRedis(LinkEntity link) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(cacheKey(link.getShortCode()), serialize(link), redisTtl);
            redisTemplate.opsForSet().add(INDEX_KEY, link.getShortCode());
        } catch (RuntimeException ignored) {
        }
    }

    private void removeIndexEntry(String shortCode) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForSet().remove(INDEX_KEY, shortCode);
        } catch (RuntimeException ignored) {
        }
    }

    private void mirrorLocally(LinkEntity link) {
        synchronized (entries) {
            entries.put(link.getShortCode(), link);
            trimToSize();
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

    private static String cacheKey(String shortCode) {
        return KEY_PREFIX + shortCode;
    }

    private static String serialize(LinkEntity link) {
        return String.join("|",
                link.getId() == null ? "" : link.getId().toString(),
                link.getShortCode(),
                encodeText(link.getDestinationUrl()),
                encodeBytes(link.getAnalyticsTokenHash()),
                Long.toString(link.getCreatedAt().toEpochMilli()));
    }

    private static LinkEntity deserialize(String serialized) {
        String[] parts = serialized.split("\\|", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid cache payload");
        }
        Long id = parts[0].isEmpty() ? null : Long.valueOf(parts[0]);
        String shortCode = parts[1];
        String destinationUrl = decodeText(parts[2]);
        byte[] analyticsTokenHash = decodeBytes(parts[3]);
        Instant createdAt = Instant.ofEpochMilli(Long.parseLong(parts[4]));
        return id == null
                ? new LinkEntity(shortCode, destinationUrl, analyticsTokenHash, createdAt)
                : new LinkEntity(id, shortCode, destinationUrl, analyticsTokenHash, createdAt);
    }

    private static String encodeText(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeText(String encoded) {
        return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    private static String encodeBytes(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static byte[] decodeBytes(String encoded) {
        return Base64.getUrlDecoder().decode(encoded);
    }
}
