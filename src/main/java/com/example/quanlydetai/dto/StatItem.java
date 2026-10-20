package com.example.quanlydetai.dto;

/** Một cột/thanh trong biểu đồ thống kê; percent là độ dài thanh so với giá trị lớn nhất (0-100). */
public record StatItem(String label, long value, int percent) {
}
