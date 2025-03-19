package com.iaschowrai.urlshortner.repository;

import com.iaschowrai.urlshortner.models.ClickEvent;
import com.iaschowrai.urlshortner.models.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickEventRepository  extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findByUrlMappingAndClickDateBetween(UrlMapping urlMapping, LocalDateTime startDate, LocalDateTime endDate);

    List<ClickEvent> findByUrlMappingInAndClickDateBetween(List<UrlMapping> urlMapping,
                                                                          LocalDateTime startDate,
                                                                          LocalDateTime endDate);
}
