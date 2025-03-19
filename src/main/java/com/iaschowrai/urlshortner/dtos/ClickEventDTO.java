package com.iaschowrai.urlshortner.dtos;


import java.time.LocalDate;


public class ClickEventDTO {

    private LocalDate clickDate;
    private Long count;

    public ClickEventDTO(LocalDate clickDate, Long count) {
        this.clickDate = clickDate;
        this.count = count;
    }

    public ClickEventDTO() {
    }

    public LocalDate getClickDate() {
        return clickDate;
    }

    public void setClickDate(LocalDate clickDate) {
        this.clickDate = clickDate;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
