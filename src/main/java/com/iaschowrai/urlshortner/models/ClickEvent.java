package com.iaschowrai.urlshortner.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "click_events")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime clickDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "url_mapping_id", nullable = false)
    private UrlMapping urlMapping;

}
