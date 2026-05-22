package com.unext.backend.shared.response;

public record PaginationMeta(int page, int limit, long total, int totalPages) {

    public static PaginationMeta of(int page, int limit, long total) {
        int totalPages = limit > 0 ? (int) Math.ceil((double) total / limit) : 0;
        return new PaginationMeta(page, limit, total, totalPages);
    }
}
