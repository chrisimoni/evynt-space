package com.chrisimoni.evyntspace.user.repository;

import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.model.User;
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
public class UserSpecification implements Specification<User> {
    private final UserSearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(criteria.getName())) {
            String searchName = "%" + criteria.getName().toLowerCase() + "%";

            Predicate firstNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchName);
            Predicate lastNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchName);

            predicates.add(criteriaBuilder.or(firstNameLike, lastNameLike));
        }

        if (Objects.nonNull(criteria.getActive())) {
            predicates.add(criteriaBuilder.equal(root.get("active"), criteria.getActive()));
        }

        if (Objects.nonNull(criteria.getFromDate())) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), criteria.getFromDate()));
        }

        if (Objects.nonNull(criteria.getToDate())) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.getToDate()));
        }

        if (Objects.nonNull(criteria.getActive())) {
            predicates.add(criteriaBuilder.equal(root.get("active"), criteria.getActive()));
        }

        if (predicates.isEmpty()) {
            return null;
        }

        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }
}
