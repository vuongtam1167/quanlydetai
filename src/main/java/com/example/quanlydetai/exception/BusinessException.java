package com.example.quanlydetai.exception;

/**
 * Lỗi nghiệp vụ có thông điệp hiển thị trực tiếp cho người dùng
 * (vd: "Nhóm đã đủ thành viên"). WebExceptionHandler sẽ chuyển thành thông báo đỏ.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
