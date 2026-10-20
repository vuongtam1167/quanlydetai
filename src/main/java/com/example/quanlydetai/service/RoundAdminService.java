package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.RoundForm;
import com.example.quanlydetai.dto.RoundView;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Council;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.enums.RoundType;
import com.example.quanlydetai.repository.CouncilRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Quản lý đợt đăng ký (Trưởng khoa): CRUD + kiểm tra ràng buộc thời gian theo loại đợt. */
@Service
@Transactional
public class RoundAdminService {

    private final RegistrationRoundRepository roundRepository;
    private final TopicRepository topicRepository;
    private final StudentGroupRepository groupRepository;
    private final CouncilRepository councilRepository;

    public RoundAdminService(RegistrationRoundRepository roundRepository, TopicRepository topicRepository,
                             StudentGroupRepository groupRepository, CouncilRepository councilRepository) {
        this.roundRepository = roundRepository;
        this.topicRepository = topicRepository;
        this.groupRepository = groupRepository;
        this.councilRepository = councilRepository;
    }

    @Transactional(readOnly = true)
    public List<RoundView> listViews(String type) {
        LocalDateTime now = LocalDateTime.now();
        RoundType filter = parseType(type);
        return roundRepository.findAllByOrderByLecturerStartDateDesc().stream()
                .filter(r -> filter == null || r.getType() == filter)
                .map(r -> RoundView.of(r, now))
                .toList();
    }

    /** Số đề tài của từng đợt (id đợt -> số đề tài). */
    @Transactional(readOnly = true)
    public Map<Long, Long> topicCounts() {
        Map<Long, Long> map = new HashMap<>();
        for (RegistrationRound r : roundRepository.findAll()) {
            map.put(r.getId(), topicRepository.countByRoundId(r.getId()));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> groupCounts() {
        Map<Long, Long> map = new HashMap<>();
        for (RegistrationRound r : roundRepository.findAll()) {
            map.put(r.getId(), groupRepository.countByRoundId(r.getId()));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public RegistrationRound get(Long id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
    }

    @Transactional(readOnly = true)
    public RoundForm toForm(Long id) {
        return RoundForm.from(get(id));
    }

    public RegistrationRound save(RoundForm f) {
        validate(f);
        RegistrationRound r = f.getId() == null ? new RegistrationRound() : get(f.getId());
        RoundType t = f.getType();
        r.setName(f.getName().trim());
        r.setType(t);
        r.setLecturerStartDate(f.getLecturerStartDate());
        r.setLecturerEndDate(f.getLecturerEndDate());
        r.setStudentStartDate(f.getStudentStartDate());
        r.setStudentEndDate(f.getStudentEndDate());
        // Theo quy chế: hạn GVPB chỉ cho TLCN/KLTN; ngày báo cáo hội đồng chỉ cho KLTN
        r.setReviewerDeadline(t == RoundType.TLCN || t == RoundType.KLTN ? f.getReviewerDeadline() : null);
        r.setCouncilReportDate(t == RoundType.KLTN ? f.getCouncilReportDate() : null);
        return roundRepository.save(r);
    }

    public void delete(Long id) {
        RegistrationRound r = get(id);
        if (topicRepository.countByRoundId(id) > 0 || groupRepository.countByRoundId(id) > 0
                || councilRepository.existsByRoundId(id)) {
            throw new BusinessException("Đợt này đã có đề tài/nhóm/hội đồng nên không thể xóa.");
        }
        roundRepository.delete(r);
    }

    private void validate(RoundForm f) {
        if (!f.getLecturerStartDate().isBefore(f.getLecturerEndDate())) {
            throw new BusinessException("Giai đoạn 1: thời gian bắt đầu phải trước thời gian kết thúc.");
        }
        if (!f.getStudentStartDate().isBefore(f.getStudentEndDate())) {
            throw new BusinessException("Giai đoạn 2: thời gian bắt đầu phải trước thời gian kết thúc.");
        }
        if (f.getStudentStartDate().isBefore(f.getLecturerEndDate())) {
            throw new BusinessException("Giai đoạn 2 (sinh viên đăng ký) phải bắt đầu sau khi Giai đoạn 1 (giảng viên đề xuất) kết thúc.");
        }
        RoundType t = f.getType();
        if ((t == RoundType.TLCN || t == RoundType.KLTN)
                && f.getReviewerDeadline() == null) {
            throw new BusinessException(
                    "Đợt TLCN/KLTN bắt buộc nhập hạn chót GV phản biện nộp điểm.");
        }

        if ((t == RoundType.TLCN || t == RoundType.KLTN)
                && !f.getReviewerDeadline().isAfter(f.getStudentEndDate())) {
            throw new BusinessException(
                    "Hạn chót GVPB phải sau thời điểm kết thúc Giai đoạn 2.");
        }

        if (t == RoundType.KLTN && f.getCouncilReportDate() == null) {
            throw new BusinessException("Đợt KLTN bắt buộc nhập ngày báo cáo hội đồng.");
        }

        if (t == RoundType.KLTN) {
            if (!f.getCouncilReportDate().isAfter(f.getStudentEndDate())) {
                throw new BusinessException(
                        "Ngày báo cáo hội đồng phải sau thời điểm kết thúc Giai đoạn 2.");
            }
            if (f.getReviewerDeadline().isAfter(f.getCouncilReportDate())) {
                throw new BusinessException(
                        "Hạn chót GVPB phải bằng hoặc trước ngày báo cáo hội đồng.");
            }
        }

        // Không để ngày bảo vệ đã lập rơi vào bên trong Giai đoạn 2 khi sửa đợt.
        if (f.getId() != null) {
            for (Council council : councilRepository.findByRoundId(f.getId())) {
                if (!council.getDefenseDate().isAfter(f.getStudentEndDate())) {
                    throw new BusinessException(
                            "Ngày kết thúc Giai đoạn 2 phải trước ngày bảo vệ của hội đồng \""
                                    + council.getName() + "\".");
                }
            }
        }
    }

    private RoundType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return RoundType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
