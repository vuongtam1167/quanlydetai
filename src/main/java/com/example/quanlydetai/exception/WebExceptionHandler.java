package com.example.quanlydetai.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.net.URI;

/**
 * Bắt lỗi nghiệp vụ cho các controller trả HTML:
 * - BusinessException: quay lại trang trước + hiện thông báo đỏ (flashError)
 * - ResourceNotFoundException: hiện trang 404
 * Không bắt AccessDeniedException nên Spring Security vẫn trả 403 đúng chuẩn.
 */
@ControllerAdvice(basePackages = "com.example.quanlydetai.controller")
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebExceptionHandler {

    public static final String ERROR_MESSAGE_ATTR = "app.errorMessage";

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request) {
        FlashMap flash = RequestContextUtils.getOutputFlashMap(request);
        flash.put("flashError", ex.getMessage());
        // GET lỗi -> về trang chủ (tránh vòng lặp chuyển hướng); POST lỗi -> quay lại
        // trang trước
        String target = "GET".equalsIgnoreCase(request.getMethod()) ? "/" : backPath(request);
        return "redirect:" + target;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleNotFound(ResourceNotFoundException ex, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        request.setAttribute(ERROR_MESSAGE_ATTR, ex.getMessage());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /** Chỉ quay về cùng host (tránh open-redirect), mặc định về trang chủ. */
    private String backPath(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = URI.create(referer);
                String host = uri.getHost();
                if (host == null || host.equalsIgnoreCase(request.getServerName())) {
                    String path = uri.getRawPath();
                    if (path == null || path.isBlank()) {
                        path = "/";
                    }
                    return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
                }
            } catch (IllegalArgumentException ignored) {
                // Referer sai định dạng -> dùng mặc định
            }
        }
        return "/";
    }
}
