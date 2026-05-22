package com.unext.backend.audit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public record AuditContext(
        String performedBy,
        String performedByName,
        String userRole,
        String ipAddress,
        String userAgent,
        String reason,
        String documentRef
) {

    /** Creates a basic AuditContext from the current security context and HTTP request. */
    public static AuditContext of(Authentication auth, HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "anonymous";
        String role = auth != null
                ? auth.getAuthorities().stream().findFirst()
                        .map(a -> a.getAuthority().replace("ROLE_", "")).orElse("UNKNOWN")
                : "UNKNOWN";
        return new AuditContext(username, username, role, extractIp(request),
                request.getHeader("User-Agent"), null, null);
    }

    public AuditContext withReason(String reason, String documentRef) {
        return new AuditContext(performedBy, performedByName, userRole, ipAddress, userAgent, reason, documentRef);
    }

    private static String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
