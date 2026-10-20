package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.CouncilForm;
import com.example.quanlydetai.dto.CouncilRow;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Council;
import com.example.quanlydetai.model.CouncilMember;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.enums.CouncilRole;
import com.example.quanlydetai.model.enums.CouncilStatus;
import com.example.quanlydetai.repository.CouncilEvaluationRepository;
import com.example.quanlydetai.repository.CouncilMemberRepository;
import com.example.quanlydetai.repository.CouncilRepository;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Thành lập hội đồng: 3-5 giảng viên (1 chủ tịch + 1 thư ký + ủy viên), gắn các nhóm sẽ bảo vệ.
 * Ràng buộc BR-05: thành viên hội đồng không được là GVHD của nhóm được phân vào hội đồng đó.
 */
@Service
@Transactional
public class CouncilAdminService {

    public static final int MIN_MEMBERS = 3;
    public static final int MAX_MEMBERS = 5;

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository memberRepository;
    private final CouncilEvaluationRepository evaluationRepository;
    private final StudentGroupRepository groupRepository;
    private final LecturerRepository lecturerRepository;
    private final RegistrationRoundRepository roundRepository;
    private final TopicAdvisorRepository advisorRepository;

    public CouncilAdminService(CouncilRepository councilRepository, CouncilMemberRepository memberRepository,
                               CouncilEvaluationRepository evaluationRepository,
                               StudentGroupRepository groupRepository, LecturerRepository lecturerRepository,
                               RegistrationRoundRepository roundRepository,
                               TopicAdvisorRepository advisorRepository) {
        this.councilRepository = councilRepository;
        this.memberRepository = memberRepository;
        this.evaluationRepository = evaluationRepository;
        this.groupRepository = groupRepository;
        this.lecturerRepository = lecturerRepository;
        this.roundRepository = roundRepository;
        this.advisorRepository = advisorRepository;
    }

    @Transactional(readOnly = true)
    public List<CouncilRow> list() {
        List<CouncilRow> rows = new ArrayList<>();
        for (Council c : councilRepository.findAllByOrderByDefenseDateDesc()) {
            List<CouncilMember> members = memberRepository.findByCouncilId(c.getId());
            String chair = nameOf(members, CouncilRole.CHAIR);
            String secretary = nameOf(members, CouncilRole.SECRETARY);
            rows.add(new CouncilRow(c.getId(), c.getName(), c.getRound().getName(), c.getDefenseDate(),
                    c.getLocation(), c.getStatus(), chair, secretary, members.size(),
                    groupRepository.countByCouncilId(c.getId()),
                    evaluationRepository.existsByCouncilId(c.getId())));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public CouncilForm toForm(Long id) {
        Council c = get(id);
        CouncilForm f = new CouncilForm();
        f.setId(c.getId());
        f.setRoundId(c.getRound().getId());
        f.setName(c.getName());
        f.setLocation(c.getLocation());
        f.setDefenseDate(c.getDefenseDate());
        List<Long> memberIds = new ArrayList<>();
        for (CouncilMember m : memberRepository.findByCouncilId(id)) {
            if (m.getRoleInCouncil() == CouncilRole.CHAIR) {
                f.setChairId(m.getLecturer().getId());
            } else if (m.getRoleInCouncil() == CouncilRole.SECRETARY) {
                f.setSecretaryId(m.getLecturer().getId());
            } else {
                memberIds.add(m.getLecturer().getId());
            }
        }
        f.setMemberIds(memberIds);
        List<Long> groupIds = new ArrayList<>();
        for (StudentGroup g : groupRepository.findByCouncilId(id)) {
            groupIds.add(g.getId());
        }
        f.setGroupIds(groupIds);
        return f;
    }

    @Transactional(readOnly = true)
    public boolean isLocked(Long id) {
        return id != null && evaluationRepository.existsByCouncilId(id);
    }

    /** Nhóm đã đăng ký đề tài và chưa thuộc hội đồng nào (hoặc đang thuộc hội đồng đang sửa). */
    @Transactional(readOnly = true)
    public List<StudentGroup> eligibleGroups(Long councilId) {
        return groupRepository.findByTopicIsNotNullOrderByRoundIdDescIdAsc().stream()
                .filter(g -> g.getCouncil() == null
                        || (councilId != null && g.getCouncil().getId().equals(councilId)))
                .toList();
    }

    public Council save(CouncilForm f) {
        if (f.getChairId().equals(f.getSecretaryId())) {
            throw new BusinessException("Chủ tịch và Thư ký phải là hai giảng viên khác nhau.");
        }
        Set<Long> memberIds = new LinkedHashSet<>();
        for (Long id : f.getMemberIds()) {
            if (id != null && !id.equals(f.getChairId()) && !id.equals(f.getSecretaryId())) {
                memberIds.add(id);
            }
        }
        int total = 2 + memberIds.size();
        if (total < MIN_MEMBERS || total > MAX_MEMBERS) {
            throw new BusinessException("Hội đồng phải có từ " + MIN_MEMBERS + " đến " + MAX_MEMBERS
                    + " giảng viên (hiện có " + total + "). Hãy chọn 1 đến 3 ủy viên.");
        }

        Council council;
        if (f.getId() == null) {
            council = new Council();
            council.setStatus(CouncilStatus.PLANNED);
        } else {
            council = get(f.getId());
            if (evaluationRepository.existsByCouncilId(council.getId())) {
                throw new BusinessException("Hội đồng đã có điểm chấm nên không thể chỉnh sửa.");
            }
        }
        RegistrationRound round = roundRepository.findById(f.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
        if (f.getDefenseDate() == null || !f.getDefenseDate().isAfter(round.getStudentEndDate())) {
            throw new BusinessException("Ngày bảo vệ hội đồng phải sau thời điểm kết thúc Giai đoạn 2.");
        }
        Lecturer chair = lecturer(f.getChairId());
        Lecturer secretary = lecturer(f.getSecretaryId());
        List<Lecturer> members = new ArrayList<>();
        for (Long id : memberIds) {
            members.add(lecturer(id));
        }

        // Kiểm tra các nhóm được chọn
        List<StudentGroup> selected = new ArrayList<>();
        Set<Long> allLecturerIds = new LinkedHashSet<>();
        allLecturerIds.add(chair.getId());
        allLecturerIds.add(secretary.getId());
        members.forEach(m -> allLecturerIds.add(m.getId()));
        for (Long gid : new LinkedHashSet<>(f.getGroupIds())) {
            StudentGroup g = groupRepository.findById(gid)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm."));
            if (!g.getRound().getId().equals(round.getId())) {
                throw new BusinessException(g.getGroupName() + " không thuộc đợt \"" + round.getName() + "\".");
            }
            if (g.getTopic() == null) {
                throw new BusinessException(g.getGroupName() + " chưa đăng ký đề tài.");
            }
            if (g.getCouncil() != null && (council.getId() == null
                    || !g.getCouncil().getId().equals(council.getId()))) {
                throw new BusinessException(g.getGroupName() + " đã thuộc hội đồng khác.");
            }
            boolean conflict = advisorRepository.findByTopicId(g.getTopic().getId()).stream()
                    .anyMatch(a -> allLecturerIds.contains(a.getLecturer().getId()));
            if (conflict) {
                throw new BusinessException("Hội đồng có giảng viên đang hướng dẫn đề tài của " + g.getGroupName()
                        + " (GV không được chấm đề tài mình hướng dẫn - BR-05).");
            }
            selected.add(g);
        }

        council.setRound(round);
        council.setName(f.getName().trim());
        council.setLocation(f.getLocation() == null ? null : f.getLocation().trim());
        council.setDefenseDate(f.getDefenseDate());
        council = councilRepository.save(council);

        // Ghi lại thành viên (xóa cũ + flush để tránh vi phạm unique khi thêm lại)
        memberRepository.deleteByCouncilId(council.getId());
        memberRepository.flush();
        saveMember(council, chair, CouncilRole.CHAIR);
        saveMember(council, secretary, CouncilRole.SECRETARY);
        for (Lecturer m : members) {
            saveMember(council, m, CouncilRole.MEMBER);
        }

        // Cập nhật các nhóm thuộc hội đồng
        Set<Long> selectedIds = new LinkedHashSet<>();
        selected.forEach(g -> selectedIds.add(g.getId()));
        for (StudentGroup old : groupRepository.findByCouncilId(council.getId())) {
            if (!selectedIds.contains(old.getId())) {
                old.setCouncil(null);
                groupRepository.save(old);
            }
        }
        for (StudentGroup g : selected) {
            g.setCouncil(council);
            groupRepository.save(g);
        }
        return council;
    }

    public void delete(Long id) {
        Council c = get(id);
        if (evaluationRepository.existsByCouncilId(id)) {
            throw new BusinessException("Hội đồng đã có điểm chấm nên không thể xóa.");
        }
        for (StudentGroup g : groupRepository.findByCouncilId(id)) {
            g.setCouncil(null);
            groupRepository.save(g);
        }
        memberRepository.deleteByCouncilId(id);
        memberRepository.flush();
        councilRepository.delete(c);
    }

    private Council get(Long id) {
        return councilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng."));
    }

    private Lecturer lecturer(Long id) {
        return lecturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên."));
    }

    private void saveMember(Council council, Lecturer lecturer, CouncilRole role) {
        CouncilMember m = new CouncilMember();
        m.setCouncil(council);
        m.setLecturer(lecturer);
        m.setRoleInCouncil(role);
        memberRepository.save(m);
    }

    private String nameOf(List<CouncilMember> members, CouncilRole role) {
        return members.stream().filter(m -> m.getRoleInCouncil() == role)
                .map(m -> m.getLecturer().getFullName()).findFirst().orElse("—");
    }
}
