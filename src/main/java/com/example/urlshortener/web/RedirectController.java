package com.example.urlshortener.web;

import java.net.URI;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.urlshortener.redirect.LinkResolver;
import com.example.urlshortener.web.error.ApiException;
import com.example.urlshortener.analytics.AnalyticsCapture;

@RestController
public class RedirectController {
    private final LinkResolver resolver;
    private final AnalyticsCapture analytics;
    public RedirectController(LinkResolver resolver, AnalyticsCapture analytics) { this.resolver = resolver; this.analytics = analytics; }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        LinkResolver.Resolution result = resolver.resolve(code);
        return switch (result.outcome()) {
            case ACTIVE -> { analytics.capture(result.link(), null); yield ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(result.link().getDestinationUrl()))
                    .cacheControl(CacheControl.noStore())
                    .build(); }
            case NOT_FOUND -> throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested link was not found.");
            case DEPENDENCY_UNAVAILABLE -> throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", "The link service is temporarily unavailable.");
        };
    }
}
