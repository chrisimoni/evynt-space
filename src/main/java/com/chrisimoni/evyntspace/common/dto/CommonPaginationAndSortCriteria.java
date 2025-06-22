package com.chrisimoni.evyntspace.common.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
public class CommonPaginationAndSortCriteria {
    @Min(value = 0, message = "Page number cannot be negative")
    private Integer page;
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size;
    private String sortBy = "createdAt";
    private boolean sortDesc;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // For ISO 8601 strings (e.g., "2023-01-01T00:00:00Z")
    private Instant fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant toDate;
    private Boolean active;

    public Pageable toPageable() {
        // Apply default values if client didn't provide them
        page = Objects.isNull(page) || page < 1 ? 0 : page - 1;
        size = Objects.isNull(size) ? 10 : size;

        Sort sort = sortDesc ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }
}
