package com.chrisimoni.evyntspace.common.service;

import java.util.List;
import java.util.Optional;

public interface BaseService<T, ID> {
    T save(T entity);
    T findById(ID id);
    List<T> findAll();
    void updateStatus(ID id, Boolean newStatus);
}
