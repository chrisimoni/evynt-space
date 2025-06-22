package com.chrisimoni.evyntspace.common.service;

import com.chrisimoni.evyntspace.common.exception.ResourceNotFoundException;
import com.chrisimoni.evyntspace.common.model.ActivatableEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public class BaseServiceImpl<T, ID> implements BaseService<T, ID> {
    // The specific repository for the entity, injected by the subclass
    private final JpaRepository<T, ID> repository;
    private final JpaSpecificationExecutor<T> specRepository;
    private final String resourceName;

    public BaseServiceImpl(
            JpaRepository<T, ID> repository,
            String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
        this.specRepository = extractSpecificationExecutor(repository);
    }

    @SuppressWarnings("unchecked")
    private JpaSpecificationExecutor<T> extractSpecificationExecutor(JpaRepository<T, ID> repository) {
        if (repository instanceof JpaSpecificationExecutor) {
            return (JpaSpecificationExecutor<T>) repository; // Explicit cast
        } else {
            return null; // No support for specifications
        }
    }

    @Override
    @Transactional
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName, "id", id));
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        int pageNumber = Math.max(0, pageable.getPageNumber() - 1); // This is the 1-indexed number from client
        int pageSize = pageable.getPageSize();
        Pageable newPageable = PageRequest.of(pageNumber, pageSize);
        return repository.findAll(newPageable);
    }

    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        if (specRepository == null) {
            throw new UnsupportedOperationException(
                    "Specification-based search is not supported for '" + resourceName +
                            "' entity. Its repository does not extend JpaSpecificationExecutor."
            );
        }

        return specRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public void updateStatus(ID id, Boolean newStatus) {
        T entity = findById(id);

        if (entity instanceof ActivatableEntity activatable) {
            activatable.setActive(newStatus);
            activatable.setDeactivatedAt(!newStatus ? Instant.now() : null);
            repository.save(entity);
        }
    }
}