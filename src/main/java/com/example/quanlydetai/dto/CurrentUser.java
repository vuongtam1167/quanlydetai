package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.enums.Role;

/** Thông tin người đăng nhập */
public record CurrentUser(String username, String displayName, String code,
        Role role, String roleLabel, String initial) {
}
