package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.enums.Role;

public record UserRow(Long id, String username, Role role, String roleLabel, boolean enabled, String fullName) {
}
