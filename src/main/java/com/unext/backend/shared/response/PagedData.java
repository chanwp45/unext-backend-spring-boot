package com.unext.backend.shared.response;

import java.util.List;

public record PagedData<T>(List<T> items, PaginationMeta pagination) {}
