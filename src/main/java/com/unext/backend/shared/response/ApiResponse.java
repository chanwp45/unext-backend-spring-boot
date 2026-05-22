package com.unext.backend.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String status,
        int code,
        String message,
        T data,
        List<FieldError> errors
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("success", 200, null, data, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>("success", 200, message, data, null);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>("success", 201, message, data, null);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>("success", 204, null, null, null);
    }

    public static ApiResponse<Void> badRequest(String message) {
        return new ApiResponse<>("error", 400, message, null, null);
    }

    public static ApiResponse<Void> unauthorized(String message) {
        return new ApiResponse<>("error", 401, message, null, null);
    }

    public static ApiResponse<Void> forbidden(String message) {
        return new ApiResponse<>("error", 403, message, null, null);
    }

    public static ApiResponse<Void> notFound(String message) {
        return new ApiResponse<>("error", 404, message, null, null);
    }

    public static ApiResponse<Void> conflict(String message) {
        return new ApiResponse<>("error", 409, message, null, null);
    }

    public static ApiResponse<Void> validationError(List<FieldError> errors) {
        return new ApiResponse<>("error", 422, "Validation failed", null, errors);
    }

    public static ApiResponse<Void> internalError() {
        return new ApiResponse<>("error", 500, "An unexpected error occurred.", null, null);
    }

    public record FieldError(String field, String message) {}
}
