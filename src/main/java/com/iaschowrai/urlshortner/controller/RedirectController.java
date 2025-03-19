package com.iaschowrai.urlshortner.controller;

import com.iaschowrai.urlshortner.models.UrlMapping;
import com.iaschowrai.urlshortner.service.urlservice.UrlMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RedirectController {
    private static final Logger logger = LoggerFactory.getLogger(RedirectController.class);

    @Autowired
    private final UrlMappingService urlMappingService;

    public RedirectController(UrlMappingService urlMappingService) {
        this.urlMappingService = urlMappingService;
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirects(@PathVariable String shortUrl) {
        if (shortUrl == null || shortUrl.trim().isEmpty()) {
            logger.warn("Received an empty or null short URL.");
            return ResponseEntity.badRequest().build();
        }

        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);
        if (urlMapping == null) {
            logger.warn("Short URL not found: {}", shortUrl);
            return ResponseEntity.notFound().build();
        }else  {
            logger.info("Redirecting short URL: {} to {}", shortUrl, urlMapping.getOriginalUrl());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl());
            return ResponseEntity.status(HttpStatus.FOUND).headers(httpHeaders).build();
        }
    }
}
