package com.bjdev.ecomercebase.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/** Generic pagination envelope for any list endpoint — see ItemController for the first user. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
