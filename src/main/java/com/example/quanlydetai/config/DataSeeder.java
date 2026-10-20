package com.example.quanlydetai.config;

import com.example.quanlydetai.model.*;
import com.example.quanlydetai.model.enums.*;
import com.example.quanlydetai.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository, DepartmentRepository departmentRepository,
            LecturerRepository lecturerRepository, StudentRepository studentRepository,
            RegistrationRoundRepository roundRepository, TopicRepository topicRepository,
            TopicAdvisorRepository advisorRepository, StudentGroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository, SubmissionRepository submissionRepository,
            ReviewAssignmentRepository reviewRepository, CouncilRepository councilRepository,
            CouncilMemberRepository councilMemberRepository, CouncilEvaluationRepository evaluationRepository,
            AnnouncementRepository announcementRepository, PasswordEncoder encoder,
            PlatformTransactionManager txManager,
            com.example.quanlydetai.service.ProfileRepairService profileRepair) {

        return args -> new TransactionTemplate(txManager).executeWithoutResult(status -> {
            // Kiểm tra nếu database đã có dữ liệu thì không seed lại để tránh lỗi Duplicate
            if (departmentRepository.count() > 0) {
                profileRepair.repair(); // bổ sung hồ sơ cho Trưởng bộ môn nếu thiếu
                return;
            }

            // Dọn tài khoản mặc định cũ (do SecurityConfig phiên bản trước tạo) để không trùng username
            announcementRepository.deleteAll();
            userRepository.deleteAll();
            System.out.println("Bắt đầu khởi tạo dữ liệu mẫu (Data Seeding) từ SQL Dump...");
            String defaultPwd = encoder.encode("123456");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // ==========================================
            // 1. Khởi tạo Departments (20 rows)
            // ==========================================
            String[][] deptData = {
                {"KTPM", "Kỹ thuật phần mềm", "Chuyên ngành Kỹ thuật phần mềm"},
                {"HTTT", "Hệ thống thông tin", "Chuyên ngành Hệ thống thông tin"},
                {"KHMT", "Khoa học máy tính", "Chuyên ngành Khoa học máy tính"},
                {"MMT", "Mạng máy tính", "Chuyên ngành Mạng và truyền thông"},
                {"ATTT", "An toàn thông tin", "Chuyên ngành An toàn thông tin"},
                {"CNKT", "Công nghệ kỹ thuật", "Công nghệ kỹ thuật máy tính"},
                {"DPT", "Đa phương tiện", "Công nghệ đa phương tiện"},
                {"AI", "Trí tuệ nhân tạo", "Ứng dụng trí tuệ nhân tạo"},
                {"IOT", "Internet vạn vật", "Internet of Things"},
                {"ROBOT", "Robotics", "Robotics và tự động hóa"},
                {"DLT", "Dữ liệu lớn", "Big Data & Data Science"},
                {"DTVT", "Điện tử viễn thông", "Điện tử viễn thông"},
                {"CĐT", "Cơ điện tử", "Cơ điện tử"},
                {"MKT", "Marketing số", "Digital Marketing"},
                {"TMDT", "Thương mại điện tử", "Thương mại điện tử"},
                {"QTKD", "Quản trị kinh doanh", "Quản trị kinh doanh CNTT"},
                {"NN", "Ngoại ngữ", "Tiếng Anh chuyên ngành"},
                {"CB", "Cơ bản", "Toán, Lý cơ bản"},
                {"CTCT", "Chính trị", "Công tác chính trị"},
                {"GDTC", "Giáo dục thể chất", "Giáo dục thể chất"}
            };
            List<Department> departments = new ArrayList<>();
            for (String[] d : deptData) {
                Department dept = new Department();
                dept.setCode(d[0]);
                dept.setName(d[1]);
                dept.setDescription(d[2]);
                departments.add(departmentRepository.save(dept));
            }

            // ==========================================
            // 2. Khởi tạo Users (50 rows)
            // ==========================================
            String[][] userData = {
                {"admin", "ADMIN"}, {"truongkhoa_cnnt", "TRUONG_KHOA"}, {"truongkhoa_kt", "TRUONG_KHOA"},
                {"truongbomon_ktpm", "TRUONG_BO_MON"}, {"truongbomon_httt", "TRUONG_BO_MON"},
                {"gv01", "GIANG_VIEN"}, {"gv02", "GIANG_VIEN"}, {"gv03", "GIANG_VIEN"}, {"gv04", "GIANG_VIEN"}, {"gv05", "GIANG_VIEN"},
                {"gv06", "GIANG_VIEN"}, {"gv07", "GIANG_VIEN"}, {"gv08", "GIANG_VIEN"}, {"gv09", "GIANG_VIEN"}, {"gv10", "GIANG_VIEN"},
                {"gv11", "GIANG_VIEN"}, {"gv12", "GIANG_VIEN"}, {"gv13", "GIANG_VIEN"}, {"gv14", "GIANG_VIEN"}, {"gv15", "GIANG_VIEN"},
                {"gv16", "GIANG_VIEN"}, {"gv17", "GIANG_VIEN"}, {"gv18", "GIANG_VIEN"}, {"gv19", "GIANG_VIEN"}, {"gv20", "GIANG_VIEN"},
                {"sv01", "SINH_VIEN"}, {"sv02", "SINH_VIEN"}, {"sv03", "SINH_VIEN"}, {"sv04", "SINH_VIEN"}, {"sv05", "SINH_VIEN"},
                {"sv06", "SINH_VIEN"}, {"sv07", "SINH_VIEN"}, {"sv08", "SINH_VIEN"}, {"sv09", "SINH_VIEN"}, {"sv10", "SINH_VIEN"},
                {"sv11", "SINH_VIEN"}, {"sv12", "SINH_VIEN"}, {"sv13", "SINH_VIEN"}, {"sv14", "SINH_VIEN"}, {"sv15", "SINH_VIEN"},
                {"sv16", "SINH_VIEN"}, {"sv17", "SINH_VIEN"}, {"sv18", "SINH_VIEN"}, {"sv19", "SINH_VIEN"}, {"sv20", "SINH_VIEN"},
                {"sv21", "SINH_VIEN"}, {"sv22", "SINH_VIEN"}, {"sv23", "SINH_VIEN"}, {"sv24", "SINH_VIEN"}, {"sv25", "SINH_VIEN"}
            };
            List<User> users = new ArrayList<>();
            for (String[] u : userData) {
                User user = new User();
                user.setUsername(u[0]);
                user.setPassword(defaultPwd);
                user.setRole(Role.valueOf(u[1]));
                users.add(userRepository.save(user));
            }

            // ==========================================
            // 3. Khởi tạo Lecturers (20 rows)
            // ==========================================
            Object[][] lecData = {
                {5, 0, "GV001", "Nguyen Van A", "gva@edu.vn", "0901234561", "ThS"},
                {6, 1, "GV002", "Tran Thi B", "gvb@edu.vn", "0901234562", "TS"},
                {7, 2, "GV003", "Le Van C", "gvc@edu.vn", "0901234563", "PGS.TS"},
                {8, 3, "GV004", "Pham Thi D", "gvd@edu.vn", "0901234564", "ThS"},
                {9, 4, "GV005", "Hoang Van E", "gve@edu.vn", "0901234565", "TS"},
                {10, 0, "GV006", "Do Thi F", "gvf@edu.vn", "0901234566", "ThS"},
                {11, 1, "GV007", "Ngo Van G", "gvg@edu.vn", "0901234567", "TS"},
                {12, 2, "GV008", "Vu Thi H", "gvh@edu.vn", "0901234568", "GS.TS"},
                {13, 3, "GV009", "Dang Van I", "gvi@edu.vn", "0901234569", "ThS"},
                {14, 4, "GV010", "Bui Thi K", "gvk@edu.vn", "0901234570", "TS"},
                {15, 5, "GV011", "Truong Van L", "gvl@edu.vn", "0901234571", "ThS"},
                {16, 6, "GV012", "Ly Thi M", "gvm@edu.vn", "0901234572", "PGS.TS"},
                {17, 7, "GV013", "Mai Van N", "gvn@edu.vn", "0901234573", "TS"},
                {18, 8, "GV014", "Dinh Thi O", "gvo@edu.vn", "0901234574", "ThS"},
                {19, 9, "GV015", "Lam Van P", "gvp@edu.vn", "0901234575", "TS"},
                {20, 10, "GV016", "Phan Thi Q", "gvq@edu.vn", "0901234576", "PGS.TS"},
                {21, 11, "GV017", "Chau Van R", "gvr@edu.vn", "0901234577", "TS"},
                {22, 0, "GV018", "Tong Thi S", "gvs@edu.vn", "0901234578", "ThS"},
                {23, 1, "GV019", "Trinh Van T", "gvt@edu.vn", "0901234579", "ThS"},
                {24, 2, "GV020", "Mac Thi U", "gvu@edu.vn", "0901234580", "TS"}
            };
            List<Lecturer> lecturers = new ArrayList<>();
            for (Object[] l : lecData) {
                Lecturer lec = new Lecturer();
                lec.setUser(users.get((int) l[0]));
                lec.setDepartment(departments.get((int) l[1]));
                lec.setLecturerCode((String) l[2]);
                lec.setFullName((String) l[3]);
                lec.setEmail((String) l[4]);
                lec.setPhone((String) l[5]);
                lec.setAcademicRank((String) l[6]);
                lecturers.add(lecturerRepository.save(lec));
            }

            // ==========================================
            // 4. Khởi tạo Students (25 rows)
            // ==========================================
            Object[][] stuData = {
                {25, "SV001", "Tran Van A", "sva@st.edu.vn", "0981234501", "IT01"},
                {26, "SV002", "Nguyen Thi B", "svb@st.edu.vn", "0981234502", "IT01"},
                {27, "SV003", "Le Van C", "svc@st.edu.vn", "0981234503", "IT02"},
                {28, "SV004", "Pham Thi D", "svd@st.edu.vn", "0981234504", "IT02"},
                {29, "SV005", "Hoang Van E", "sve@st.edu.vn", "0981234505", "IT03"},
                {30, "SV006", "Do Thi F", "svf@st.edu.vn", "0981234506", "IT03"},
                {31, "SV007", "Ngo Van G", "svg@st.edu.vn", "0981234507", "IT04"},
                {32, "SV008", "Vu Thi H", "svh@st.edu.vn", "0981234508", "IT04"},
                {33, "SV009", "Dang Van I", "svi@st.edu.vn", "0981234509", "IT01"},
                {34, "SV010", "Bui Thi K", "svk@st.edu.vn", "0981234510", "IT01"},
                {35, "SV011", "Truong Van L", "svl@st.edu.vn", "0981234511", "IT02"},
                {36, "SV012", "Ly Thi M", "svm@st.edu.vn", "0981234512", "IT02"},
                {37, "SV013", "Mai Van N", "svn@st.edu.vn", "0981234513", "IT03"},
                {38, "SV014", "Dinh Thi O", "svo@st.edu.vn", "0981234514", "IT03"},
                {39, "SV015", "Lam Van P", "svp@st.edu.vn", "0981234515", "IT04"},
                {40, "SV016", "Phan Thi Q", "svq@st.edu.vn", "0981234516", "IT04"},
                {41, "SV017", "Chau Van R", "svr@st.edu.vn", "0981234517", "IT01"},
                {42, "SV018", "Tong Thi S", "svs@st.edu.vn", "0981234518", "IT01"},
                {43, "SV019", "Trinh Van T", "svt@st.edu.vn", "0981234519", "IT02"},
                {44, "SV020", "Mac Thi U", "svu@st.edu.vn", "0981234520", "IT02"},
                {45, "SV021", "Vo Van V", "svv@st.edu.vn", "0981234521", "IT03"},
                {46, "SV022", "Cao Thi W", "svw@st.edu.vn", "0981234522", "IT03"},
                {47, "SV023", "Luc Van X", "svx@st.edu.vn", "0981234523", "IT04"},
                {48, "SV024", "Hong Thi Y", "svy@st.edu.vn", "0981234524", "IT04"},
                {49, "SV025", "Quach Van Z", "svz@st.edu.vn", "0981234525", "IT01"}
            };
            List<Student> students = new ArrayList<>();
            for (Object[] s : stuData) {
                Student stu = new Student();
                stu.setUser(users.get((int) s[0]));
                stu.setStudentCode((String) s[1]);
                stu.setFullName((String) s[2]);
                stu.setEmail((String) s[3]);
                stu.setPhone((String) s[4]);
                stu.setClassName((String) s[5]);
                students.add(studentRepository.save(stu));
            }

            // ==========================================
            // 5. Khởi tạo Registration Rounds (20 rows)
            // ==========================================
            Object[][] roundData = {
                {"KLTN HK1 2024", "KLTN", "2024-01-01 00:00:00", "2024-01-15 00:00:00", "2024-01-16 00:00:00", "2024-01-30 00:00:00", "2024-05-15 00:00:00", "2024-05-30 00:00:00", true},
                {"TLCN HK1 2024", "TLCN", "2024-02-01 00:00:00", "2024-02-15 00:00:00", "2024-02-16 00:00:00", "2024-02-28 00:00:00", "2024-06-15 00:00:00", null, true},
                {"NCKH Đợt 1 2024", "NCKH", "2024-03-01 00:00:00", "2024-03-15 00:00:00", "2024-03-16 00:00:00", "2024-03-30 00:00:00", "2024-07-15 00:00:00", "2024-07-30 00:00:00", true},
                {"Môn Học PTPM 2024", "MON_HOC", "2024-04-01 00:00:00", "2024-04-15 00:00:00", "2024-04-16 00:00:00", "2024-04-30 00:00:00", null, null, true},
                {"KLTN HK2 2024", "KLTN", "2024-08-01 00:00:00", "2024-08-15 00:00:00", "2024-08-16 00:00:00", "2024-08-30 00:00:00", "2024-12-15 00:00:00", "2024-12-30 00:00:00", true},
                {"TLCN HK2 2024", "TLCN", "2024-09-01 00:00:00", "2024-09-15 00:00:00", "2024-09-16 00:00:00", "2024-09-30 00:00:00", "2025-01-15 00:00:00", null, true},
                {"NCKH Đợt 2 2024", "NCKH", "2024-10-01 00:00:00", "2024-10-15 00:00:00", "2024-10-16 00:00:00", "2024-10-30 00:00:00", "2025-02-15 00:00:00", "2025-02-28 00:00:00", false},
                {"Môn Học CSDL 2024", "MON_HOC", "2024-11-01 00:00:00", "2024-11-15 00:00:00", "2024-11-16 00:00:00", "2024-11-30 00:00:00", null, null, false},
                {"KLTN HK1 2025", "KLTN", "2025-01-01 00:00:00", "2025-01-15 00:00:00", "2025-01-16 00:00:00", "2025-01-30 00:00:00", "2025-05-15 00:00:00", "2025-05-30 00:00:00", false},
                {"TLCN HK1 2025", "TLCN", "2025-02-01 00:00:00", "2025-02-15 00:00:00", "2025-02-16 00:00:00", "2025-02-28 00:00:00", "2025-06-15 00:00:00", null, false},
                {"NCKH Đợt 1 2025", "NCKH", "2025-03-01 00:00:00", "2025-03-15 00:00:00", "2025-03-16 00:00:00", "2025-03-30 00:00:00", "2025-07-15 00:00:00", "2025-07-30 00:00:00", false},
                {"Môn Học Mạng 2025", "MON_HOC", "2025-04-01 00:00:00", "2025-04-15 00:00:00", "2025-04-16 00:00:00", "2025-04-30 00:00:00", null, null, false},
                {"KLTN HK2 2025", "KLTN", "2025-08-01 00:00:00", "2025-08-15 00:00:00", "2025-08-16 00:00:00", "2025-08-30 00:00:00", "2025-12-15 00:00:00", "2025-12-30 00:00:00", false},
                {"TLCN HK2 2025", "TLCN", "2025-09-01 00:00:00", "2025-09-15 00:00:00", "2025-09-16 00:00:00", "2025-09-30 00:00:00", "2026-01-15 00:00:00", null, false},
                {"NCKH Đợt 2 2025", "NCKH", "2025-10-01 00:00:00", "2025-10-15 00:00:00", "2025-10-16 00:00:00", "2025-10-30 00:00:00", "2026-02-15 00:00:00", "2026-02-28 00:00:00", false},
                {"Môn Học AI 2025", "MON_HOC", "2025-11-01 00:00:00", "2025-11-15 00:00:00", "2025-11-16 00:00:00", "2025-11-30 00:00:00", null, null, false},
                {"KLTN HK1 2026", "KLTN", "2026-01-01 00:00:00", "2026-01-15 00:00:00", "2026-01-16 00:00:00", "2026-01-30 00:00:00", "2026-05-15 00:00:00", "2026-05-30 00:00:00", false},
                {"TLCN HK1 2026", "TLCN", "2026-02-01 00:00:00", "2026-02-15 00:00:00", "2026-02-16 00:00:00", "2026-02-28 00:00:00", "2026-06-15 00:00:00", null, false},
                {"NCKH Đợt 1 2026", "NCKH", "2026-03-01 00:00:00", "2026-03-15 00:00:00", "2026-03-16 00:00:00", "2026-03-30 00:00:00", "2026-07-15 00:00:00", "2026-07-30 00:00:00", false},
                {"Môn Học IOT 2026", "MON_HOC", "2026-04-01 00:00:00", "2026-04-15 00:00:00", "2026-04-16 00:00:00", "2026-04-30 00:00:00", null, null, false}
            };
            List<RegistrationRound> rounds = new ArrayList<>();
            for (Object[] r : roundData) {
                RegistrationRound round = new RegistrationRound();
                round.setName((String) r[0]);
                round.setType(RoundType.valueOf((String) r[1]));
                round.setLecturerStartDate(LocalDateTime.parse((String) r[2], formatter));
                round.setLecturerEndDate(LocalDateTime.parse((String) r[3], formatter));
                round.setStudentStartDate(LocalDateTime.parse((String) r[4], formatter));
                round.setStudentEndDate(LocalDateTime.parse((String) r[5], formatter));
                round.setReviewerDeadline(r[6] != null ? LocalDateTime.parse((String) r[6], formatter) : null);
                round.setCouncilReportDate(r[7] != null ? LocalDateTime.parse((String) r[7], formatter) : null);
                round.setIsPublished((Boolean) r[8]);
                rounds.add(roundRepository.save(round));
            }

            // ==========================================
            // 6. Khởi tạo Topics (20 rows)
            // ==========================================
            Object[][] topicData = {
                {0, 0, "DT001", "Hệ thống Quản lý Sinh viên", "Xây dựng website quản lý SV", "Java, Spring, MySQL", 3, "APPROVED", 0},
                {0, 1, "DT002", "Phân tích dữ liệu doanh nghiệp", "Dashboard BI", "Python, PowerBI", 3, "APPROVED", 1},
                {0, 2, "DT003", "Nhận diện khuôn mặt", "Sử dụng OpenCV", "Python, OpenCV", 2, "APPROVED", 2},
                {1, 3, "DT004", "Mạng lưới IoT cho nông nghiệp", "Cảm biến DHT11, ESP8266", "C++, IoT", 4, "APPROVED", 3},
                {1, 4, "DT005", "Hệ thống IDS/IPS", "Phát hiện xâm nhập", "Linux, Python", 3, "APPROVED", 4},
                {2, 0, "DT006", "Phần mềm bán hàng", "App di động bán hàng", "Flutter, Firebase", 4, "APPROVED", 5},
                {2, 1, "DT007", "Gợi ý sản phẩm E-commerce", "Recommendation system", "Python, scikit-learn", 3, "APPROVED", 6},
                {3, 2, "DT008", "Dự đoán giá nhà", "Machine Learning regression", "Python, Pandas", 3, "APPROVED", 7},
                {3, 3, "DT009", "Tối ưu hóa định tuyến mạng", "Thuật toán Dijkstra", "C++, Network", 2, "APPROVED", 8},
                {4, 4, "DT010", "Mã hóa dữ liệu y tế", "Bảo mật hồ sơ bệnh án", "Cryptography, Java", 3, "APPROVED", 9},
                {4, 0, "DT011", "Website du lịch", "Book tour trực tuyến", "ReactJS, NodeJS", 4, "APPROVED", 10},
                {5, 1, "DT012", "Hệ thống ERP mini", "Quản lý kho và nhân sự", "C#, SQL Server", 4, "APPROVED", 11},
                {5, 2, "DT013", "Xử lý ngôn ngữ tự nhiên (NLP)", "Chatbot tư vấn tuyển sinh", "Python, NLP", 3, "APPROVED", 12},
                {6, 3, "DT014", "Giám sát giao thông bằng AI", "Camera traffic", "YOLOv8, Python", 4, "PENDING", 13},
                {6, 4, "DT015", "Smart Home Security", "Báo động thông minh", "Arduino, MQTT", 3, "PENDING", 14},
                {7, 0, "DT016", "Ứng dụng quản lý tài chính cá nhân", "App mobile", "React Native", 3, "PENDING", 15},
                {7, 1, "DT017", "Phân tích hành vi khách hàng", "Dữ liệu siêu thị", "R, Tableau", 2, "DEAN_APPROVED", 16},
                {8, 2, "DT018", "Nhận diện giọng nói", "Voice to text", "Python, Deep Learning", 3, "DEAN_APPROVED", 17},
                {8, 3, "DT019", "Hệ thống SDN", "Software Defined Networking", "Mininet, Python", 3, "REJECTED", 18},
                {9, 4, "DT020", "Mạng riêng ảo VPN", "Setup và cấu hình", "OpenVPN, Linux", 2, "REJECTED", 19}
            };
            List<Topic> topics = new ArrayList<>();
            for (Object[] t : topicData) {
                Topic topic = new Topic();
                topic.setRound(rounds.get((int) t[0]));
                topic.setDepartment(departments.get((int) t[1]));
                topic.setCode((String) t[2]);
                topic.setTitle((String) t[3]);
                topic.setDescription((String) t[4]);
                topic.setRequirements((String) t[5]);
                topic.setMaxMembers((int) t[6]);
                topic.setStatus(TopicStatus.valueOf((String) t[7]));
                topic.setCreatedBy(lecturers.get((int) t[8]));
                topics.add(topicRepository.save(topic));
            }

            // ==========================================
            // 7. Khởi tạo Topic Advisors (20 rows)
            // ==========================================
            for (int i = 0; i < 20; i++) {
                TopicAdvisor ta = new TopicAdvisor();
                ta.setTopic(topics.get(i));
                ta.setLecturer(lecturers.get(i));
                ta.setIsMainAdvisor(true);
                advisorRepository.save(ta);
            }

            // ==========================================
            // 8. Khởi tạo Student Groups (20 rows)
            // ==========================================
            Object[][] groupData = {
                {"Nhóm 01", 0, 0, "COMPLETED", "8.50"}, {"Nhóm 02", 0, 1, "COMPLETED", "9.00"},
                {"Nhóm 03", 0, 2, "COMPLETED", "7.50"}, {"Nhóm 04", 1, 3, "COMPLETED", "8.00"},
                {"Nhóm 05", 1, 4, "COMPLETED", "8.75"}, {"Nhóm 06", 2, 5, "SUBMITTED", null},
                {"Nhóm 07", 2, 6, "SUBMITTED", null}, {"Nhóm 08", 3, 7, "IN_PROGRESS", null},
                {"Nhóm 09", 3, 8, "IN_PROGRESS", null}, {"Nhóm 10", 4, 9, "REGISTERED", null},
                {"Nhóm 11", 4, 10, "REGISTERED", null}, {"Nhóm 12", 5, 11, "FORMING", null},
                {"Nhóm 13", 5, 12, "FORMING", null}, {"Nhóm 14", 0, null, "FORMING", null},
                {"Nhóm 15", 1, null, "FORMING", null}, {"Nhóm 16", 2, null, "FORMING", null},
                {"Nhóm 17", 3, null, "FORMING", null}, {"Nhóm 18", 4, null, "FORMING", null},
                {"Nhóm 19", 5, null, "FORMING", null}, {"Nhóm 20", 0, null, "FORMING", null}
            };
            List<StudentGroup> groups = new ArrayList<>();
            for (Object[] g : groupData) {
                StudentGroup sg = new StudentGroup();
                sg.setGroupName((String) g[0]);
                sg.setRound(rounds.get((int) g[1]));
                sg.setTopic(g[2] != null ? topics.get((int) g[2]) : null);
                sg.setStatus(GroupStatus.valueOf((String) g[3]));
                sg.setFinalScore(g[4] != null ? new BigDecimal((String) g[4]) : null);
                groups.add(groupRepository.save(sg));
            }

            // ==========================================
            // 9. Khởi tạo Group Members (25 rows)
            // ==========================================
            Object[][] gmData = {
                {0, 0, true}, {0, 1, false}, {1, 2, true}, {1, 3, false},
                {2, 4, true}, {2, 5, false}, {3, 6, true}, {3, 7, false},
                {4, 8, true}, {4, 9, false}, {5, 10, true}, {5, 11, false},
                {6, 12, true}, {6, 13, false}, {7, 14, true}, {7, 15, false},
                {8, 16, true}, {8, 17, false}, {9, 18, true}, {9, 19, false},
                {10, 20, true}, {11, 21, true}, {12, 22, true}, {13, 23, true}, {14, 24, true}
            };
            for (Object[] gm : gmData) {
                GroupMember member = new GroupMember();
                member.setGroup(groups.get((int) gm[0]));
                member.setStudent(students.get((int) gm[1]));
                member.setIsLeader((boolean) gm[2]);
                groupMemberRepository.save(member);
            }

            // ==========================================
            // 10. Khởi tạo Submissions (20 rows)
            // ==========================================
            Object[][] subData = {
                {0, 0, "/uploads/G1_report.zip", "https://github.com/g1", "Báo cáo hoàn chỉnh"},
                {1, 2, "/uploads/G2_report.zip", "https://github.com/g2", "Báo cáo hoàn chỉnh"},
                {2, 4, "/uploads/G3_report.zip", "https://github.com/g3", "Báo cáo hoàn chỉnh"},
                {3, 6, "/uploads/G4_report.zip", "https://github.com/g4", "Báo cáo hoàn chỉnh"},
                {4, 8, "/uploads/G5_report.zip", "https://github.com/g5", "Báo cáo hoàn chỉnh"},
                {5, 10, "/uploads/G6_report.zip", "https://github.com/g6", "Báo cáo lần 1"},
                {6, 12, "/uploads/G7_report.zip", "https://github.com/g7", "Báo cáo lần 1"},
                {7, 14, "/uploads/G8_draft.zip", null, "Bản nháp"},
                {8, 16, "/uploads/G9_draft.zip", null, "Bản nháp"},
                {9, 18, "/uploads/G10_draft.zip", null, "Đề cương"},
                {10, 20, "/uploads/G11_draft.zip", null, "Đề cương"},
                {11, 21, "/uploads/G12_draft.zip", null, "Chưa có file"},
                {0, 0, "/uploads/G1_final.zip", "https://github.com/g1/final", "Bản cuối"},
                {1, 2, "/uploads/G2_final.zip", "https://github.com/g2/final", "Bản cuối"},
                {2, 4, "/uploads/G3_final.zip", "https://github.com/g3/final", "Bản cuối"},
                {3, 6, "/uploads/G4_final.zip", "https://github.com/g4/final", "Bản cuối"},
                {4, 8, "/uploads/G5_final.zip", "https://github.com/g5/final", "Bản cuối"},
                {5, 10, "/uploads/G6_update.zip", "https://github.com/g6/update", "Update lần 2"},
                {6, 12, "/uploads/G7_update.zip", "https://github.com/g7/update", "Update lần 2"},
                {7, 14, "/uploads/G8_update.zip", null, "Update bản nháp"}
            };
            for (Object[] s : subData) {
                Submission sub = new Submission();
                sub.setGroup(groups.get((int) s[0]));
                sub.setSubmittedBy(students.get((int) s[1]));
                sub.setFilePath((String) s[2]);
                sub.setSourceCodeUrl((String) s[3]);
                sub.setNotes((String) s[4]);
                submissionRepository.save(sub);
            }

            // ==========================================
            // 11. Khởi tạo Review Assignments (20 rows)
            // ==========================================
            Object[][] reviewData = {
                {0, 1, "8.5", "Tốt, cần cải thiện UI", "2024-05-10 00:00:00"},
                {1, 2, "9.0", "Xuất sắc", "2024-05-11 00:00:00"},
                {2, 3, "7.5", "Khá, thuật toán chưa tối ưu", "2024-05-12 00:00:00"},
                {3, 4, "8.0", "Mạch thiết kế ổn", "2024-06-10 00:00:00"},
                {4, 5, "8.5", "Phân tích tốt", "2024-06-11 00:00:00"},
                {5, 6, null, null, null}, {6, 7, null, null, null}, {7, 8, null, null, null}, {8, 9, null, null, null},
                {9, 10, null, null, null}, {10, 11, null, null, null}, {11, 12, null, null, null}, {12, 13, null, null, null},
                {13, 14, null, null, null}, {14, 15, null, null, null}, {15, 16, null, null, null}, {16, 17, null, null, null},
                {17, 18, null, null, null}, {18, 19, null, null, null}, {19, 0, null, null, null}
            };
            for (Object[] r : reviewData) {
                ReviewAssignment ra = new ReviewAssignment();
                ra.setTopic(topics.get((int) r[0]));
                ra.setReviewer(lecturers.get((int) r[1]));
                ra.setScore(r[2] != null ? new BigDecimal((String) r[2]) : null);
                ra.setComments((String) r[3]);
                ra.setSubmissionDate(r[4] != null ? LocalDateTime.parse((String) r[4], formatter) : null);
                reviewRepository.save(ra);
            }

            // ==========================================
            // 12. Khởi tạo Councils (20 rows)
            // ==========================================
            Object[][] councilData = {
                {0, "Hội đồng KLTN 1", "Phòng A101", "2024-05-25 08:00:00", "COMPLETED"},
                {0, "Hội đồng KLTN 2", "Phòng A102", "2024-05-25 13:00:00", "COMPLETED"},
                {0, "Hội đồng KLTN 3", "Phòng A103", "2024-05-26 08:00:00", "COMPLETED"},
                {1, "Hội đồng TLCN 1", "Phòng B201", "2024-06-25 08:00:00", "COMPLETED"},
                {1, "Hội đồng TLCN 2", "Phòng B202", "2024-06-25 13:00:00", "COMPLETED"},
                {2, "Hội đồng NCKH 1", "Phòng C301", "2024-07-25 08:00:00", "PLANNED"},
                {2, "Hội đồng NCKH 2", "Phòng C302", "2024-07-25 13:00:00", "PLANNED"},
                {4, "Hội đồng KLTN HK2 1", "Phòng A101", "2024-12-25 08:00:00", "PLANNED"},
                {4, "Hội đồng KLTN HK2 2", "Phòng A102", "2024-12-25 13:00:00", "PLANNED"},
                {5, "Hội đồng TLCN HK2 1", "Phòng B201", "2025-01-25 08:00:00", "PLANNED"},
                {5, "Hội đồng TLCN HK2 2", "Phòng B202", "2025-01-25 13:00:00", "PLANNED"},
                {6, "Hội đồng NCKH 2025 1", "Phòng C301", "2025-02-25 08:00:00", "PLANNED"},
                {6, "Hội đồng NCKH 2025 2", "Phòng C302", "2025-02-25 13:00:00", "PLANNED"},
                {8, "Hội đồng KLTN 2025 1", "Phòng A101", "2025-05-25 08:00:00", "PLANNED"},
                {8, "Hội đồng KLTN 2025 2", "Phòng A102", "2025-05-25 13:00:00", "PLANNED"},
                {9, "Hội đồng TLCN 2025 1", "Phòng B201", "2025-06-25 08:00:00", "PLANNED"},
                {9, "Hội đồng TLCN 2025 2", "Phòng B202", "2025-06-25 13:00:00", "PLANNED"},
                {10, "Hội đồng NCKH 2025 3", "Phòng C301", "2025-07-25 08:00:00", "PLANNED"},
                {10, "Hội đồng NCKH 2025 4", "Phòng C302", "2025-07-25 13:00:00", "PLANNED"},
                {12, "Hội đồng KLTN 2025 3", "Phòng A103", "2025-12-25 08:00:00", "PLANNED"}
            };
            List<Council> councils = new ArrayList<>();
            for (Object[] c : councilData) {
                Council council = new Council();
                council.setRound(rounds.get((int) c[0]));
                council.setName((String) c[1]);
                council.setLocation((String) c[2]);
                council.setDefenseDate(LocalDateTime.parse((String) c[3], formatter));
                council.setStatus(CouncilStatus.valueOf((String) c[4]));
                councils.add(councilRepository.save(council));
            }

            // ==========================================
            // 13. Khởi tạo Council Members (20 rows)
            // ==========================================
            Object[][] cmData = {
                {0, 9, "CHAIR"}, {0, 10, "SECRETARY"}, {0, 11, "MEMBER"},
                {1, 12, "CHAIR"}, {1, 13, "SECRETARY"}, {1, 14, "MEMBER"},
                {2, 15, "CHAIR"}, {2, 16, "SECRETARY"}, {2, 17, "MEMBER"},
                {3, 0, "CHAIR"}, {3, 1, "SECRETARY"}, {3, 2, "MEMBER"},
                {4, 3, "CHAIR"}, {4, 4, "SECRETARY"}, {4, 5, "MEMBER"},
                {5, 6, "CHAIR"}, {5, 7, "SECRETARY"}, {5, 8, "MEMBER"},
                {6, 9, "CHAIR"}, {7, 10, "CHAIR"}
            };
            List<CouncilMember> councilMembers = new ArrayList<>();
            for (Object[] cm : cmData) {
                CouncilMember member = new CouncilMember();
                member.setCouncil(councils.get((int) cm[0]));
                member.setLecturer(lecturers.get((int) cm[1]));
                member.setRoleInCouncil(CouncilRole.valueOf((String) cm[2]));
                councilMembers.add(councilMemberRepository.save(member));
            }

            // ==========================================
            // 14. Khởi tạo Council Evaluations (20 rows)
            // ==========================================
            Object[][] evalData = {
                {0, 0, 0, "8.5", "Trình bày rõ ràng"}, {0, 0, 1, "8.0", "Code tốt"}, {0, 0, 2, "9.0", "Đề tài hay"},
                {1, 1, 3, "9.0", "Khá khen"}, {1, 1, 4, "9.5", "Rất tốt"}, {1, 1, 5, "8.5", "Logic tốt"},
                {2, 2, 6, "7.5", "Cần cố gắng"}, {2, 2, 7, "7.0", "Chưa sâu"}, {2, 2, 8, "8.0", "Tạm ổn"},
                {3, 3, 9, "8.0", "Ổn"}, {3, 3, 10, "8.5", "Thực tế"}, {3, 3, 11, "7.5", "Mạch điện được"},
                {4, 4, 12, "8.5", "Được"}, {4, 4, 13, "9.0", "Sáng tạo"}, {4, 4, 14, "8.75", "Báo cáo đẹp"},
                {0, 1, 0, "8.0", "Tốt"}, {0, 1, 1, "8.5", "Được"}, {0, 1, 2, "8.0", "Tạm"},
                {1, 2, 3, "7.0", "Bình thường"}, {1, 2, 4, "7.5", "Tạm"}
            };
            for (Object[] e : evalData) {
                CouncilEvaluation eval = new CouncilEvaluation();
                eval.setCouncil(councils.get((int) e[0]));
                eval.setGroup(groups.get((int) e[1]));
                eval.setCouncilMember(councilMembers.get((int) e[2]));
                eval.setScore(new BigDecimal((String) e[3]));
                eval.setComments((String) e[4]);
                evaluationRepository.save(eval);
                // Gắn nhóm vào hội đồng đã chấm nhóm đó (để màn hình Admin/Chủ tịch hiển thị đúng)
                StudentGroup evaluated = groups.get((int) e[1]);
                if (evaluated.getCouncil() == null) {
                    evaluated.setCouncil(councils.get((int) e[0]));
                    groupRepository.save(evaluated);
                }
            }

            // ==========================================
            // 15. Khởi tạo Announcements (20 rows)
            // ==========================================
            Object[][] annData = {
                {"Kế hoạch đăng ký đề tài KLTN", "Sinh viên chú ý thời gian Giai đoạn 2", 0, true},
                {"Thông báo nộp báo cáo tiến độ", "Các nhóm nộp báo cáo trước ngày 15/10", 1, false},
                {"Lịch bảo vệ Khóa luận tốt nghiệp", "Danh sách hội đồng đã được cập nhật", 0, true},
                {"Hướng dẫn viết Tiểu luận chuyên ngành", "Định dạng file Word chuẩn", 2, false},
                {"Thông báo gia hạn nộp đề cương", "Hạn chót dời sang 20/10", 0, false},
                {"Mở đợt Đăng ký NCKH 2024", "Dành cho sinh viên năm 3,4", 1, true},
                {"Quy định về đạo văn", "Nghiêm cấm sao chép source code", 0, true},
                {"Hướng dẫn sinh viên sử dụng hệ thống", "Xem chi tiết tài liệu HDSD", 0, false},
                {"Kết quả xét duyệt đề tài GĐ1", "Giảng viên vui lòng kiểm tra email", 1, false},
                {"Danh sách phân công GV Phản biện", "Giảng viên chú ý thời hạn nộp điểm", 0, false},
                {"Cập nhật quy chế chấm điểm", "Áp dụng barem điểm mới từ 2024", 0, false},
                {"Thông báo họp Hội đồng", "Các thư ký chuẩn bị biên bản", 2, false},
                {"Khen thưởng NCKH Sinh viên", "Lễ trao giải sẽ diễn ra vào 30/11", 1, false},
                {"Khảo sát chất lượng đề tài", "Sinh viên hoàn thành form khảo sát", 0, false},
                {"Thông báo v/v đổi đề tài", "Mẫu đơn xin đổi đề tài", 0, false},
                {"Hỗ trợ kỹ thuật hệ thống", "Liên hệ IT khoa nếu gặp lỗi", 0, false},
                {"Lịch thi Môn học PTPM", "SV xem danh sách phòng thi", 3, false},
                {"Danh sách nợ môn", "Sinh viên lưu ý không được đk KLTN", 0, false},
                {"Đăng ký tài khoản hệ thống", "Tân sinh viên kích hoạt tài khoản", 0, false},
                {"Nghỉ Tết Nguyên Đán", "Chúc mừng năm mới", 0, true}
            };
            for (Object[] a : annData) {
                Announcement ann = new Announcement();
                ann.setTitle((String) a[0]);
                ann.setContent((String) a[1]);
                ann.setCreatedBy(users.get((int) a[2]));
                ann.setPinned((Boolean) a[3]);
                announcementRepository.save(ann);
            }

            profileRepair.repair();
            System.out.println("✅ Đã khởi tạo hoàn tất CHÍNH XÁC " + deptData.length + " Bộ môn, " + userData.length + " User, " + lecData.length + " Giảng viên, " + stuData.length + " Sinh viên, và hàng trăm bản ghi liên quan khác đúng theo Data Dump!");
        });
    }
}
