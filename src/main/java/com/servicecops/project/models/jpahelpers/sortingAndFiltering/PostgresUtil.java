package com.servicecops.project.models.jpahelpers.sortingAndFiltering;

import com.alibaba.fastjson2.JSONObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostgresUtil<T> {

    private final EntityManager entityManager;


    public List<T> searchSortAndPaginate(Class<T> entityClass, JSONObject searchInput, String sortField, boolean ascending, Integer page, Integer size) {
        System.out.println(searchInput);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();

        if (searchInput != null && !searchInput.isEmpty()) {
            searchInput.forEach((key, value) -> {
                String dataType = value.getClass().getSimpleName();

                if (!dataType.equals("String")) {
                    if (dataType.contains("List") || dataType.contains("JSONArray")) {
                        handleRangeSearch(key, (List<Object>) value, predicates, cb, root);
                    } else {
                        predicates.add(cb.equal(root.get(key), value));
                    }

                } else {
                    predicates.add(cb.like(cb.lower(root.get(key)), "%" + ((String) value).toLowerCase() + "%"));
                }
            });
        }

        Predicate finalPredicate = cb.and(predicates.toArray(new Predicate[0]));

        query.where(finalPredicate);

        if (StringUtils.isNotBlank(sortField)) {
            Order order = ascending ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField));
            query.orderBy(order);
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(query);

        if (page != null && size != null) {
            int batch = (page - 1) * size;
            if (batch < 0) batch = 0;
            typedQuery.setFirstResult(batch);
            typedQuery.setMaxResults(size);
        }

        return typedQuery.getResultList();
    }

    private static <T> void handleRangeSearch(String key, List<Object> value, List<Predicate> predicates, CriteriaBuilder cb, Root<T> root) {
        var rangeValues = value.toArray();

        Object first = rangeValues[0];
        Object last = rangeValues[1];

        if (first == null) {
            throw new IllegalStateException("Please provide range start value");
        }

        if (last == null) {
            predicates.add(cb.equal(root.get(key), first));
            return;
        }

        String startValueType = first.getClass().getSimpleName();
        String endValueType = last.getClass().getSimpleName();

        if (!startValueType.equals(endValueType)) {
            throw new IllegalStateException("Range start value must be same as range end value");
        }

        if (startValueType.equals("Integer")) {
            predicates.add(cb.between(root.get(key), Integer.valueOf((String) first), Integer.valueOf((String) last)));
        }

        if (startValueType.equals("Double")) {
            predicates.add(cb.between(root.get(key), Double.valueOf((String) first), Double.valueOf((String) last)));
        }

        if (startValueType.equals("Float")) {
            predicates.add(cb.between(root.get(key), Float.valueOf((String) first), Float.valueOf((String) last)));
        }

        if (startValueType.equals("BigDecimal")) {
            predicates.add(cb.between(root.get(key), new BigDecimal((String) first), new BigDecimal((String) last)));
        }

        if (startValueType.equals("Timestamp") || startValueType.equals("String")) {
            predicates.add(cb.between(root.get(key), Timestamp.valueOf((String) first), Timestamp.valueOf((String) last)));
        }
    }


}

