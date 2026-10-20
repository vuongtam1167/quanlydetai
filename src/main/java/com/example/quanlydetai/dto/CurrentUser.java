package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.enums.Role;

/** Thông tin người đăng nhập hiển thị ở sidebar/header. */
public record CurrentUser(String username, String displayName, String code,
                          Role role, String roleLabel, String initial) {
}
