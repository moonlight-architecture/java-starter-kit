package com.servicecops.project.models.jpahelpers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * This is a custom repository that provides a functionality of refresh.
 * This refresh is used to get fields that were populated on the db level say in triggers.
 * It should be the preferred choice over the normal JpaRepository
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public interface JetRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    void refresh(T t);
}
