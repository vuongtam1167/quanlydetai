# Hệ thống quản lý đề tài sinh viên - Khoa CNTT (HCM-UTE)

Đồ án cuối kỳ môn **Lập trình web (WEPR330479)**. Ứng dụng quản lý toàn bộ quy trình đề tài
môn học / NCKH / TLCN / KLTN: tạo đợt 2 giai đoạn → giảng viên đề xuất → Trưởng bộ môn thẩm định →
Trưởng khoa phê duyệt → sinh viên lập nhóm và đăng ký → nộp báo cáo → phản biện + hội đồng chấm điểm →
công bố kết quả (email) → sinh viên tra cứu điểm.

## 1. Công nghệ (kiến trúc Spring MVC)

| Kỹ thuật Spring | Dùng ở đâu |
|---|---|
| **Spring MVC + Thymeleaf** | `controller/**` trả view trong `templates/**`; layout dùng chung `fragments/layout.html` |
| **Spring Data JPA (Hibernate)** | `model/**` (16 entity), `repository/**` (query derivation) |
| **Spring Security** | `config/SecurityConfig` (phân quyền URL + `@PreAuthorize`), BCrypt, form login, HTTP Basic cho API |
| **Bean Validation** | `dto/*Form` với `@NotBlank`, `@NotNull`, `@Email`, `@Size` + `BindingResult` |
| **Dependency Injection / IoC** | Constructor injection ở mọi controller và service |
| **Spring Mail** | `service/EmailService` gửi email công bố kết quả |
| **REST (@RestController)** | `controller/api/ApiController` trả JSON bằng DTO |
| **@ControllerAdvice** | `exception/WebExceptionHandler`, `controller/api/ApiExceptionHandler` |
| **Apache POI** | `service/ExcelExportService` xuất kết quả ra .xlsx |

Luồng dữ liệu: `Request → Security Filter Chain → DispatcherServlet → Controller → Service (@Transactional) → Repository → DB → Model → Thymeleaf View`.
Controller chỉ nhận tham số, gọi Service, đưa dữ liệu vào Model; toàn bộ nghiệp vụ nằm ở `service/**`.

## 2. Cài đặt và chạy

Yêu cầu: **JDK 25** (hoặc hạ `java.version` trong `pom.xml` xuống 21/17), **MySQL 8**.

```bash
# 1) Có MySQL chạy ở localhost:3306 (CSDL tự tạo: topic_management_db)
# 2) Đặt biến môi trường nếu khác mặc định (root / 123456):
#    DB_URL, DB_USERNAME, DB_PASSWORD, MAIL_USERNAME, MAIL_PASSWORD
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
Mở http://localhost:8080 . Lần chạy đầu `DataSeeder` tự tạo dữ liệu mẫu (bộ môn, giảng viên, sinh viên, đợt, đề tài, nhóm, hội đồng, điểm).

Email: dùng Gmail App Password đặt vào `MAIL_USERNAME` / `MAIL_PASSWORD`. Nếu chưa cấu hình, chức năng công bố
vẫn chạy bình thường, chỉ báo "email thất bại".

Quy định nghiệp vụ chỉnh ở `application.properties`: `app.group.min-members` (3), `app.group.max-members` (5),
`app.demo-accounts` (hiện bảng tài khoản demo ở trang đăng nhập; đặt `false` khi triển khai thật).

## 3. Tài khoản demo (mật khẩu `123456`)

| Vai trò | Tài khoản |
|---|---|
| Quản trị viên | `admin` |
| Trưởng khoa | `truongkhoa_cnnt` |
| Trưởng bộ môn | `truongbomon_ktpm`, `truongbomon_httt` |
| Giảng viên | `gv01` … `gv20` |
| Sinh viên | `sv01` … `sv25` |

## 4. Kịch bản demo nhanh

1. **Admin/Trưởng khoa** → *Đợt đăng ký* → tạo đợt mới (GĐ1 đang diễn ra).
2. **gv01** → *Đề xuất đề tài* → gửi cho Trưởng bộ môn.
3. **truongbomon_ktpm** → *Thẩm định đề tài* → Thông qua.
4. **truongkhoa_cnnt** → *Phê duyệt đề tài* → Duyệt.
5. Trưởng khoa sửa đợt: GĐ1 kết thúc, GĐ2 bắt đầu ngay.
6. **sv01** lập nhóm, thêm sv02, sv03 (MSSV) → *Đăng ký đề tài* → *Nộp báo cáo*.
7. Trưởng khoa *Phân công GV phản biện* (GVHD bị ẩn khỏi danh sách) và *Thành lập hội đồng* (3-5 GV).
8. GVPB chấm điểm; thành viên hội đồng chấm; Chủ tịch xem tổng hợp và *Hoàn tất hội đồng*.
9. Trưởng khoa *Kết quả & công bố* → Công bố + gửi email; có nút **Xuất Excel**.
10. **sv01** → *Tra cứu điểm*.

## 5. Bảng minh chứng chức năng (dùng cho file báo cáo kết quả dự án)

| Tính năng | Controller / Service chính | Route |
|---|---|---|
| CRUD đề tài | `LecturerTopicController`, `LecturerTopicService` | `/lecturer/topics` |
| CRUD giảng viên, sinh viên, bộ môn, tài khoản, thông báo | `controller/admin/Admin{Lecturer,Student,Department,User,Announcement}Controller` | `/admin/lecturers`, `/admin/students`, `/admin/departments`, `/admin/users`, `/admin/announcements` |
| CRUD đợt đăng ký 2 giai đoạn, ràng buộc ngày theo loại | `AdminRoundController`, `RoundAdminService` | `/admin/rounds` |
| Đăng nhập / đăng xuất / bảo mật / BCrypt | `SecurityConfig`, `CustomUserDetailsService` | `/login`, `/logout` |
| GĐ1: GV đề xuất (1-2 GVHD) → TBM → Trưởng khoa duyệt | `LecturerTopicService`, `DepartmentTopicService`, `TopicApprovalService` | `/lecturer/topics`, `/department/topics`, `/admin/topics` |
| GĐ2: chỉ SV đăng ký đề tài đã duyệt, trong thời gian của đợt | `TopicRegistrationService` | `/student/topics` |
| Nhóm 3-5 SV, 1 nhóm trưởng, SV ở nhóm A không vào nhóm B | `GroupWorkspaceService` | `/student/group` |
| Chỉ nhóm trưởng nộp báo cáo | `SubmissionWorkService` | `/student/submission` |
| Phân công GVPB, GVPB ≠ GVHD | `ReviewerAdminService` | `/admin/reviewers` |
| Hội đồng 3-5 GV, chủ tịch + thư ký, GV không chấm đề tài mình hướng dẫn | `CouncilAdminService`, `CouncilGradingService` | `/admin/councils`, `/lecturer/councils` |
| Điểm tổng kết = trung bình cộng thành phần | `ResultService` | `/admin/results` |
| Công bố kết quả + gửi email từng SV | `ResultService`, `EmailService` | `POST /admin/results/{id}/publish` |
| SV chỉ tra cứu điểm đợt mình đăng ký và đã công bố | `StudentScoreService` | `/student/scores` |
| Mở rộng: thống kê trực quan nhiều đợt | `StatisticsService` | `/admin/statistics` |
| Mở rộng: xuất Excel kết quả | `ExcelExportService` | `/admin/results/{id}/export` |
| Mở rộng: tìm kiếm thông báo, đề tài, GV, SV, tài khoản | các service `search(...)` | các trang danh sách |
| Mở rộng: REST API JSON | `controller/api/ApiController` | `/api/v1/rounds`, `/api/v1/topics`, `/api/v1/statistics` |

Quy ước trạng thái đề tài: `PENDING` chờ bộ môn → `DEAN_APPROVED` bộ môn đã thông qua, chờ Trưởng khoa → `APPROVED` đã phê duyệt;
`REJECTED` = bị từ chối / cần sửa (kèm lý do ở `reviewNote`, giảng viên sửa rồi gửi lại về `PENDING`).

## 6. REST API (HTTP Basic)

```bash
curl -u admin:123456 http://localhost:8080/api/v1/rounds
curl -u sv01:123456  "http://localhost:8080/api/v1/topics?roundId=1"      # sinh viên chỉ thấy đề tài đã duyệt
curl -u admin:123456 http://localhost:8080/api/v1/statistics
```

## 7. Triển khai (tùy chọn)

`Dockerfile` đi kèm. Trên Render/Railway: đặt `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (MySQL), `PORT` do nền tảng cấp,
`app.demo-accounts=false` bằng biến `APP_DEMO_ACCOUNTS=false`.
