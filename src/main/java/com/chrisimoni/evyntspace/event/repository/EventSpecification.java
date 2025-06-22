package com.chrisimoni.evyntspace.event.repository;

import com.chrisimoni.evyntspace.event.dto.EventSearchCriteria;
import com.chrisimoni.evyntspace.event.model.Event;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class EventSpecification implements Specification<Event> {
    private final EventSearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        // Filter by title (case-insensitive, contains)
        if (StringUtils.hasText(criteria.getTitle())) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                    "%" + criteria.getTitle().toLowerCase() + "%"));
        }

        // Filter by status
        if (Objects.nonNull(criteria.getStatus())) {
            predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
        }

        // Filter by createdAt date range
        if (Objects.nonNull(criteria.getFromDate())) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), criteria.getFromDate()));
        }

        if (Objects.nonNull(criteria.getToDate())) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.getToDate()));
        }

        if (StringUtils.hasText(criteria.getCountry())) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("country")),
                    "%" + criteria.getCountry().toLowerCase() + "%"));
        }

        if (Objects.nonNull(criteria.getOrganizerId())) {
            predicates.add(criteriaBuilder.equal(root.get("organizer").get("id"), criteria.getOrganizerId()));
        }

        if (Objects.nonNull(criteria.getActive())) {
            predicates.add(criteriaBuilder.equal(root.get("active"), criteria.getActive()));
        }

        // If no predicates are added, return null or a true predicate (conjunction)
        // Returning null means no predicate will be applied for this specification,
        // which is often desired for empty filter criteria.
        if (predicates.isEmpty()) {
            return null; // Or criteriaBuilder.conjunction(); if you explicitly need a "true" predicate
        }

        // Convert the list of predicates to an array for criteriaBuilder.and()
        // Using Predicate[]::new is the most robust way (Java 8+ method reference)
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }
}
