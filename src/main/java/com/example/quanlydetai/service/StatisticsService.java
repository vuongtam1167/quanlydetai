package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.StatItem;
import com.example.quanlydetai.dto.StatisticsView;
import com.example.quanlydetai.model.Department;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.ReviewAssignment;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.enums.TopicStatus;
import com.example.quanlydetai.repository.CouncilRepository;
import com.example.quanlydetai.repository.DepartmentRepository;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.StudentRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Thống kê trực quan để Trưởng khoa theo dõi nhiều đợt đăng ký cùng lúc. */
@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private final RegistrationRoundRepository roundRepository;
    private final TopicRepository topicRepository;
    private final StudentGroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final CouncilRepository councilRepository;
    private final DepartmentRepository departmentRepository;
    private final ReviewAssignmentRepository reviewRepository;

    public StatisticsService(RegistrationRoundRepository roundRepository, TopicRepository topicRepository,
                             StudentGroupRepository groupRepository, StudentRepository studentRepository,
                             LecturerRepository lecturerRepository, CouncilRepository councilRepository,
                             DepartmentRepository departmentRepository,
                             ReviewAssignmentRepository reviewRepository) {
        this.roundRepository = roundRepository;
        this.topicRepository = topicRepository;
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.councilRepository = councilRepository;
        this.departmentRepository = departmentRepository;
        this.reviewRepository = reviewRepository;
    }

    public StatisticsView build() {
        List<RegistrationRound> rounds = roundRepository.findAllByOrderByLecturerStartDateDesc();
        long published = rounds.stream().filter(r -> Boolean.TRUE.equals(r.getIsPublished())).count();

        // Đề tài theo trạng thái
        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("Chờ bộ môn thẩm định", topicRepository.countByStatus(TopicStatus.PENDING));
        byStatus.put("Chờ Trưởng khoa duyệt", topicRepository.countByStatus(TopicStatus.DEAN_APPROVED));
        byStatus.put("Đã phê duyệt", topicRepository.countByStatus(TopicStatus.APPROVED));
        byStatus.put("Bị từ chối / cần sửa", topicRepository.countByStatus(TopicStatus.REJECTED));

        // Đề tài theo bộ môn
        Map<String, Long> byDept = new LinkedHashMap<>();
        for (Department d : departmentRepository.findAll()) {
            byDept.put(d.getName(), topicRepository.countByDepartmentId(d.getId()));
        }

        // Số nhóm theo đợt (8 đợt gần nhất)
        Map<String, Long> byRound = new LinkedHashMap<>();
        for (RegistrationRound r : rounds.stream().limit(8).toList()) {
            byRound.put(r.getName(), groupRepository.countByRoundId(r.getId()));
        }

        // Nhóm đã đăng ký đề tài: kiểm tra việc còn thiếu + phổ điểm
        List<StudentGroup> registered = groupRepository.findByTopicIsNotNullOrderByRoundIdDescIdAsc();
        long withoutReviewer = 0;
        long withoutCouncil = 0;
        long[] buckets = new long[5];
        for (StudentGroup g : registered) {
            if (reviewRepository.findByTopicId(g.getTopic().getId()).isEmpty()) {
                withoutReviewer++;
            }
            if (g.getCouncil() == null) {
                withoutCouncil++;
            }
            BigDecimal f = g.getFinalScore();
            if (f != null) {
                double v = f.doubleValue();
                if (v < 5.0) {
                    buckets[0]++;
                } else if (v < 6.5) {
                    buckets[1]++;
                } else if (v < 8.0) {
                    buckets[2]++;
                } else if (v < 9.0) {
                    buckets[3]++;
                } else {
                    buckets[4]++;
                }
            }
        }
        Map<String, Long> scoreMap = new LinkedHashMap<>();
        scoreMap.put("Dưới 5.0 (không đạt)", buckets[0]);
        scoreMap.put("5.0 - 6.4", buckets[1]);
        scoreMap.put("6.5 - 7.9", buckets[2]);
        scoreMap.put("8.0 - 8.9", buckets[3]);
        scoreMap.put("9.0 - 10", buckets[4]);

        // Tải phản biện của giảng viên (10 người nhiều nhất)
        Map<String, Long> load = new LinkedHashMap<>();
        Map<String, Long> tmp = new LinkedHashMap<>();
        for (ReviewAssignment ra : reviewRepository.findAll()) {
            tmp.merge(ra.getReviewer().getFullName(), 1L, Long::sum);
        }
        tmp.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .forEach(e -> load.put(e.getKey(), e.getValue()));

        return new StatisticsView(rounds.size(), published, topicRepository.count(), groupRepository.count(),
                studentRepository.count(), lecturerRepository.count(), councilRepository.count(),
                topicRepository.countByStatus(TopicStatus.DEAN_APPROVED),
                topicRepository.countByStatus(TopicStatus.PENDING), withoutReviewer, withoutCouncil,
                toItems(byStatus), toItems(byDept), toItems(byRound), toItems(scoreMap), toItems(load));
    }

    private List<StatItem> toItems(Map<String, Long> map) {
        long max = map.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<StatItem> items = new ArrayList<>();
        for (Map.Entry<String, Long> e : map.entrySet()) {
            int percent = max == 0 ? 0 : (int) Math.round(e.getValue() * 100.0 / max);
            items.add(new StatItem(e.getKey(), e.getValue(), percent));
        }
        return items;
    }
}
