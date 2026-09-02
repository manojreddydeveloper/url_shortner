package com.example.urlshortener.web;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.urlshortener.redirect.LinkResolver;
import com.example.urlshortener.persistence.LinkRepository;
import com.example.urlshortener.web.error.ApiException;

@RestController
@ConditionalOnBean(LinkRepository.class)
public class RedirectController {
    private final LinkResolver resolver;
    public RedirectController(LinkResolver resolver) { this.resolver = resolver; }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        LinkResolver.Resolution result = resolver.resolve(code);
        return switch (result.outcome()) {
            case ACTIVE -> ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(result.link().getDestinationUrl()))
                    .cacheControl(CacheControl.noStore())
                    .build();
            case NOT_FOUND -> throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested link was not found.");
            case DEPENDENCY_UNAVAILABLE -> throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", "The link service is temporarily unavailable.");
        };
    }
}
