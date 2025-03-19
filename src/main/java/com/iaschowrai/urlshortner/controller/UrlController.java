package com.iaschowrai.urlshortner.controller;


import com.iaschowrai.urlshortner.dtos.ClickEventDTO;
import com.iaschowrai.urlshortner.dtos.UrlMappingDTO;
import com.iaschowrai.urlshortner.models.User;
import com.iaschowrai.urlshortner.service.UserService;
import com.iaschowrai.urlshortner.service.urlservice.UrlMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.rmi.MarshalledObject;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

    private final UrlMappingService urlMappingService;
    private final UserService userService;

    public UrlController(UrlMappingService urlMappingService, UserService userService) {
        this.urlMappingService = urlMappingService;
        this.userService = userService;
    }
    /**
     * API to create a short URL.
     * Logs request details and tracks URL shortening metrics.
     */
    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String, String> request, Principal principal) {
        String originalUrl = request.get("originalUrl");
        logger.info("Received request to shorten URL: {} by user: {}", originalUrl, principal.getName());

        User user = userService.findByUsername(principal.getName());
        UrlMappingDTO urlMappingDto = urlMappingService.createShortUrl(originalUrl, user);

        // Track total URLs shortened by user
//        meterRegistry.counter("urlshortener.urls.shortened", "user", principal.getName()).increment();

        logger.info("Short URL created: {}", urlMappingDto.getShortUrl());
        return ResponseEntity.ok(urlMappingDto);
    }

    /**
     * API to get all shortened URLs for the authenticated user.
     */
    @GetMapping("/myUrls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrls(Principal principal) {
        logger.info("Fetching URLs for user: {}", principal.getName());

        User user = userService.findByUsername(principal.getName());
        List<UrlMappingDTO> urls = urlMappingService.getUrlsByUser(user);

        logger.info("Retrieved {} URLs for user: {}", urls.size(), principal.getName());
        return ResponseEntity.ok(urls);
    }

    /**
     * API to get click analytics for a short URL within a date range.
     * Validates date format and logs request details.
     */
    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>> getUrlAnalytics(
            @PathVariable String shortUrl,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        logger.info("Fetching analytics for short URL: {} between {} and {}", shortUrl, startDate, endDate);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startDate.trim(), formatter);
            LocalDateTime end = LocalDateTime.parse(endDate.trim(), formatter);

            List<ClickEventDTO> clickEventDTOS = urlMappingService.getClickEventsByDate(shortUrl, start, end);
            logger.info("Analytics retrieved successfully for short URL: {}", shortUrl);
            return ResponseEntity.ok(clickEventDTOS);
        } catch (Exception e) {
            logger.error("Error parsing date parameters: {} - {}", startDate, endDate, e);
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * API to get total clicks on all short URLs for a user within a date range.
     */
    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicksByDate(
            Principal principal,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        logger.info("Fetching total clicks for user: {} between {} and {}", principal.getName(), startDate, endDate);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            User user = userService.findByUsername(principal.getName());
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            Map<LocalDate, Long> totalClicks = urlMappingService.getTotalClicksByUserAndDate(user, start, end);

            logger.info("Total clicks retrieved successfully for user: {}", principal.getName());
            return ResponseEntity.ok(totalClicks);
        } catch (Exception e) {
            logger.error("Error parsing date parameters for total clicks: {} - {}", startDate, endDate, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
