package com.example.quanlydetai.exception;

/** Không tìm thấy dữ liệu theo id -> trả trang 404 thân thiện. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
