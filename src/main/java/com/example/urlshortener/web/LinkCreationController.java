package com.example.urlshortener.web;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.urlshortener.config.UrlShortenerProperties;
import com.example.urlshortener.url.LinkCreationService;
import com.example.urlshortener.web.error.ApiException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/links")
public class LinkCreationController {
    private final LinkCreationService service;
    private final UrlShortenerProperties properties;
    public LinkCreationController(LinkCreationService service, UrlShortenerProperties properties) { this.service = service; this.properties = properties; }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Response> create(@RequestBody Request request) {
        if (request == null || request.url() == null) throw validation();
        try {
            LinkCreationService.Result result = service.create(request.url());
            return ResponseEntity.status(HttpStatus.CREATED).body(new Response(result.code(), shortUrl(result.code()), result.url(), result.createdAt(), result.analyticsToken()));
        } catch (IllegalArgumentException exception) { throw validation(); }
          catch (LinkCreationService.CreationUnavailableException exception) { throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An internal creation failure occurred."); }
          catch (LinkCreationService.DependencyUnavailableException exception) { throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", "The link service is temporarily unavailable."); }
    }
    private String shortUrl(String code) { URI base = properties.publicBaseUrl(); return base.toString().replaceFirst("/$", "") + "/" + code; }
    private static ApiException validation() { return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "The request contains invalid fields."); }
    public record Request(String url) { }
    public record Response(String code, String shortUrl, String url, java.time.Instant createdAt, String analyticsToken) { }
}
