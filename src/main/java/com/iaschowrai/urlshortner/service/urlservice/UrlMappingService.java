package com.iaschowrai.urlshortner.service.urlservice;

import com.iaschowrai.urlshortner.dtos.UrlMappingDTO;
import com.iaschowrai.urlshortner.models.UrlMapping;
import com.iaschowrai.urlshortner.models.User;
import com.iaschowrai.urlshortner.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UrlMappingService {

    private static final Logger logger = LoggerFactory.getLogger(UrlMappingService.class);
    private final UrlMappingRepository urlMappingRepository;

    public UrlMappingService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        // Validate Inputs
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            logger.error("Failed to create short URL: Original URL is null or empty.");
            throw new IllegalArgumentException("Original URL cannot be null or empty.");
        }
        if (user == null || user.getUsername() == null) {
            logger.error("Failed to create short URL: User is null or invalid.");
            throw new IllegalArgumentException("User cannot be null.");
        }

        try {
            String shortUrl = generateShortUrl();
            logger.info("Generated short URL: {}", shortUrl);
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setOriginalUrl(originalUrl);
            urlMapping.setShortUrl(shortUrl);
            urlMapping.setUser(user);
            urlMapping.setCreatedDate(LocalDateTime.now());

            // Save to Database
            UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
            logger.info("Successfully saved URL mapping: {}", savedUrlMapping.getId());

            return convertToDto(savedUrlMapping);

        } catch (Exception e) {
            logger.error("Error occurred while creating short URL: {}", e.getMessage());
            throw new RuntimeException("Failed to create short URL", e);
        }
    }

    private UrlMappingDTO convertToDto(UrlMapping urlMapping) {
        if (urlMapping == null) {
            logger.error("Attempted to convert a null UrlMapping to DTO.");
            throw new IllegalArgumentException("UrlMapping cannot be null.");
        }

        return new UrlMappingDTO(
                urlMapping.getId(),
                urlMapping.getOriginalUrl(),
                urlMapping.getShortUrl(),
                urlMapping.getClickCount(),
                urlMapping.getCreatedDate(),
                Optional.ofNullable(urlMapping.getUser()).map(User::getUsername).orElse("Unknown")
        );
    }

    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder shortUrl = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }

        logger.debug("Generated short URL: {}", shortUrl);
        return shortUrl.toString();
    }

    public List<UrlMappingDTO> getUrlsByUser(User user) {
        if (user == null || user.getUsername() == null) {
            logger.error("Failed to get URL: User is null or invalid.");
            throw new IllegalArgumentException("User cannot be null.");
        }


        return urlMappingRepository.findByUser(user)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
}
