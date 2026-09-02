package com.example.urlshortener.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("url-shortener")
public record UrlShortenerProperties(URI publicBaseUrl) {

    public UrlShortenerProperties {
        if (publicBaseUrl == null) {
            throw new IllegalArgumentException("url-shortener.public-base-url is required");
        }
        if (!publicBaseUrl.isAbsolute() || publicBaseUrl.getHost() == null) {
            throw new IllegalArgumentException(
                    "url-shortener.public-base-url must be an absolute URL with a host");
        }
        if (!isHttpScheme(publicBaseUrl.getScheme())) {
            throw new IllegalArgumentException(
                    "url-shortener.public-base-url must use http or https");
        }
        if (publicBaseUrl.getUserInfo() != null
                || publicBaseUrl.getQuery() != null
                || publicBaseUrl.getFragment() != null
                || hasNonRootPath(publicBaseUrl)) {
            throw new IllegalArgumentException(
                    "url-shortener.public-base-url must be an origin without credentials, path, query, or fragment");
        }
    }

    private static boolean isHttpScheme(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static boolean hasNonRootPath(URI uri) {
        String path = uri.getPath();
        return path != null && !path.isEmpty() && !"/".equals(path);
    }
}
