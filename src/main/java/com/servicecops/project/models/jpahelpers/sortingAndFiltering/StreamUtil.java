package com.servicecops.project.models.jpahelpers.sortingAndFiltering;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Service
public class StreamUtil<T> {

    public List<T> search(List<T> list, Predicate<T> condition) {
        return list.stream()
                .filter(condition)
                .toList();
    }

    public List<T> sort(List<T> list, Comparator<T> comparator, boolean ascending) {
        return list.stream()
                .sorted(ascending ? comparator : comparator.reversed())
                .toList();
    }

    public List<T> paginate(List<T> list, long page, long size) {
        return list.stream()
                .skip((page - 1) * size)
                .limit(size)
                .toList();
    }

    public List<T> searchSortAndPaginate(List<T> list,
                                         Predicate<T> searchCondition,
                                         Comparator<T> sortComparator,
                                         boolean ascending,
                                         int page,
                                         int size) {
        return list.stream()
                .filter(searchCondition)
                .sorted(ascending ? sortComparator : sortComparator.reversed())
                .skip((long) (page - 1) * size)
                .limit(size)
                .toList();
    }
}

