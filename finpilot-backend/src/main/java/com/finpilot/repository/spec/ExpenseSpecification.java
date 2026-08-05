package com.finpilot.repository.spec;

import com.finpilot.dto.request.TransactionSearchRequest;
import com.finpilot.entity.Expense;
import com.finpilot.entity.PaymentMethod;
import com.finpilot.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ExpenseSpecification {

    private ExpenseSpecification() {
    }

    public static Specification<Expense> build(User user, TransactionSearchRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user"), user));

            if (StringUtils.hasText(filter.getKeyword())) {
                String likePattern = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate descMatch = cb.like(cb.lower(root.get("description")), likePattern);
                Predicate merchantMatch = cb.like(cb.lower(root.get("merchant")), likePattern);
                predicates.add(cb.or(descMatch, merchantMatch));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getEndDate()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            if (StringUtils.hasText(filter.getPaymentMethod())) {
                predicates.add(cb.equal(root.get("paymentMethod"), PaymentMethod.valueOf(filter.getPaymentMethod())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}