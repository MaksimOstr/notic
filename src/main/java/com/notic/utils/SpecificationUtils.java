package com.notic.utils;

import com.notic.entity.Note;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUtils {
    public static Specification<Note> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), userId);
    }

    public static Specification<Note> iLike(String field, String value) {
        if (value == null || value.isBlank()) {
            return Specification.where(null);
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    public static <S, T> Specification<S> in(String field, Collection<? extends T> values) {
        if (values == null || values.isEmpty()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> root.get(field).in(values);
    }

    public static Specification<Note> createdOn(LocalDate date) {
        if(date == null) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            return cb.between(root.get("createdAt"), startOfDay, endOfDay);
        };
    }
}
