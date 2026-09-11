package com.servicecops.project.models.jpahelpers.repository;

import com.jet.moonlight.services.JetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * Preferred base repository. Adds refresh (for DB-trigger columns), saveAndRefresh,
 * getRequired, and request-driven paging.
 *
 * @param <T>  entity type
 * @param <ID> id type
 */
@NoRepositoryBean
public interface JetRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    void refresh(T entity);

    T saveAndRefresh(T entity);

    default T getRequired(ID id) {
        return findById(id).orElseThrow(() -> new IllegalStateException("Not found"));
    }

    default Page<T> page(JetRequest request) {
        int page = Optional.ofNullable(request.getInteger("page")).orElse(0);
        int size = Optional.ofNullable(request.getInteger("size")).orElse(20);
        size = Math.min(Math.max(size, 1), 100);
        return findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }
}
