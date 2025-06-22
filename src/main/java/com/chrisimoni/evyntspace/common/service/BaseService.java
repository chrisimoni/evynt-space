package com.chrisimoni.evyntspace.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface BaseService<T, ID> {
    T save(T entity);
    T findById(ID id);
    Page<T> findAll(Pageable pageable); // Generic pagination/sorting
    Page<T> findAll(Specification<T> spec, Pageable pageable); //Generic filtering with Specification
    void updateStatus(ID id, Boolean newStatus);
}
