package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentForm {
    private Long id;

    @NotBlank(message = "Mã bộ môn không được để trống")
    @Size(max = 20, message = "Mã bộ môn tối đa 20 ký tự")
    private String code;

    @NotBlank(message = "Tên bộ môn không được để trống")
    @Size(max = 150, message = "Tên bộ môn tối đa 150 ký tự")
    private String name;

    private String description;

    public static DepartmentForm from(Department d) {
        DepartmentForm f = new DepartmentForm();
        f.id = d.getId();
        f.code = d.getCode();
        f.name = d.getName();
        f.description = d.getDescription();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
