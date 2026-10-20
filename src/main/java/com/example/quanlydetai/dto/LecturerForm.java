package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LecturerForm {
    private Long id;

    @NotBlank(message = "Mã giảng viên không được để trống")
    @Size(max = 20, message = "Mã giảng viên tối đa 20 ký tự")
    private String lecturerCode;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Size(max = 50, message = "Học hàm/học vị tối đa 50 ký tự")
    private String academicRank;

    @NotNull(message = "Vui lòng chọn bộ môn")
    private Long departmentId;

    @NotNull(message = "Vui lòng chọn vai trò")
    private Role role = Role.GIANG_VIEN;

    /** Chỉ dùng khi tạo mới; để trống sẽ lấy mã giảng viên (chữ thường). */
    private String username;

    /** Để trống: tạo mới dùng 123456, sửa thì giữ nguyên mật khẩu cũ. */
    private String password;

    public static LecturerForm from(Lecturer l) {
        LecturerForm f = new LecturerForm();
        f.id = l.getId();
        f.lecturerCode = l.getLecturerCode();
        f.fullName = l.getFullName();
        f.email = l.getEmail();
        f.phone = l.getPhone();
        f.academicRank = l.getAcademicRank();
        f.departmentId = l.getDepartment().getId();
        f.role = l.getUser().getRole();
        f.username = l.getUser().getUsername();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLecturerCode() { return lecturerCode; }
    public void setLecturerCode(String v) { this.lecturerCode = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getAcademicRank() { return academicRank; }
    public void setAcademicRank(String v) { this.academicRank = v; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long v) { this.departmentId = v; }
    public Role getRole() { return role; }
    public void setRole(Role v) { this.role = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
}
