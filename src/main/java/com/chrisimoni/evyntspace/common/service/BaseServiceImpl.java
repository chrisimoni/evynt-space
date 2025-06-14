package com.chrisimoni.evyntspace.common.service;

import com.chrisimoni.evyntspace.common.exception.ResourceNotFoundException;
import com.chrisimoni.evyntspace.common.model.ActivatableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class BaseServiceImpl<T, ID> implements BaseService<T, ID> {
    // The specific repository for the entity, injected by the subclass
    private final JpaRepository<T, ID> repository;
    private final String resourceName;

    public BaseServiceImpl(JpaRepository<T, ID> repository, String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
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
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void updateStatus(ID id, Boolean newStatus) {
        T entity = findById(id);

        if (entity instanceof ActivatableEntity activatable) {
            activatable.setActive(newStatus);
            activatable.setDeactivatedAt(!newStatus ? LocalDateTime.now() : null);
            repository.save(entity);
        }
    }
}