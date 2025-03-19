package com.iaschowrai.urlshortner.service.urlservice;

import com.iaschowrai.urlshortner.dtos.ClickEventDTO;
import com.iaschowrai.urlshortner.dtos.UrlMappingDTO;
import com.iaschowrai.urlshortner.models.ClickEvent;
import com.iaschowrai.urlshortner.models.UrlMapping;
import com.iaschowrai.urlshortner.models.User;
import com.iaschowrai.urlshortner.repository.ClickEventRepository;
import com.iaschowrai.urlshortner.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UrlMappingService {

    private static final Logger logger = LoggerFactory.getLogger(UrlMappingService.class);
    private final UrlMappingRepository urlMappingRepository;
    private final ClickEventRepository clickEventRepository;
    public UrlMappingService(UrlMappingRepository urlMappingRepository, ClickEventRepository clickEventRepository) {
        this.urlMappingRepository = urlMappingRepository;
        this.clickEventRepository = clickEventRepository;
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

    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if(urlMapping == null){
            logger.warn("No URL mapping found for short URL: {}", shortUrl);
            return List.of();
        }else{
            return clickEventRepository
                    .findByUrlMappingAndClickDateBetween(urlMapping,start,end)
                    .stream()
                    .collect(
                            Collectors.groupingBy(
                                    click -> click.getClickDate().toLocalDate()
                                    ,Collectors.counting()
                            )
                    )
                    .entrySet()
                    .stream().map(
                            entry -> {
                                ClickEventDTO clickEventDTO = new ClickEventDTO();
                                clickEventDTO.setClickDate(entry.getKey());
                                clickEventDTO.setCount(entry.getValue());
                                return clickEventDTO;
                            }
                    )
                    .collect(Collectors.toList());
        }
    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {

        // Find all URL mappings by the user
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);

        // Fetch all ClickEvents in the given date range for these URL mappings
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(urlMappings, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
      return clickEvents
              .stream()
              .collect(
                      Collectors.groupingBy(
                              click -> click.getClickDate()
                                      .toLocalDate(), Collectors.counting()
                      )
              );
    }

    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping == null) {
            logger.warn("Short URL not found: {}", shortUrl);
            return null;
        }else{
            urlMapping.setClickCount(urlMapping.getClickCount()+1);
            urlMappingRepository.save(urlMapping);

            // Record Click Events
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now());
            clickEvent.setUrlMapping(urlMapping);
            clickEventRepository.save(clickEvent);
        }
        logger.info("Redirecting to original URL: {} with updated click count: {}", urlMapping.getOriginalUrl(), urlMapping.getClickCount());
        return urlMapping;
    }
}
