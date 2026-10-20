package com.example.quanlydetai.dto;

import com.example.quanlydetai.model.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StudentForm {
    private Long id;

    @NotBlank(message = "MSSV không được để trống")
    @Size(max = 20, message = "MSSV tối đa 20 ký tự")
    private String studentCode;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @NotBlank(message = "Lớp không được để trống")
    @Size(max = 50, message = "Lớp tối đa 50 ký tự")
    private String className;

    /** Để trống: tạo mới dùng 123456, sửa thì giữ nguyên mật khẩu cũ. */
    private String password;

    public static StudentForm from(Student s) {
        StudentForm f = new StudentForm();
        f.id = s.getId();
        f.studentCode = s.getStudentCode();
        f.fullName = s.getFullName();
        f.email = s.getEmail();
        f.phone = s.getPhone();
        f.className = s.getClassName();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String v) { this.studentCode = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getClassName() { return className; }
    public void setClassName(String v) { this.className = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
}
