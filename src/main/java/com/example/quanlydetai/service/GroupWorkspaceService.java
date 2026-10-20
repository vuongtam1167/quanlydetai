package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.GroupMemberView;
import com.example.quanlydetai.dto.GroupView;
import com.example.quanlydetai.dto.Labels;
import com.example.quanlydetai.dto.RoundOption;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.enums.GroupStatus;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Không gian nhóm sinh viên. Ràng buộc nghiệp vụ:
 *  - Mỗi nhóm có tối đa N thành viên (app.group.max-members), cần tối thiểu M để đăng ký đề tài (app.group.min-members).
 *  - Có đúng 1 nhóm trưởng (người lập nhóm); chỉ nhóm trưởng thêm/xóa thành viên, giải tán nhóm.
 *  - Mỗi sinh viên chỉ thuộc 1 nhóm trong một đợt: đã ở nhóm A thì không vào được nhóm B.
 *  - Không đổi thành viên khi nhóm đã đăng ký đề tài hoặc đợt đã hết thời gian đăng ký.
 */
@Service
@Transactional
public class GroupWorkspaceService {

    private final CurrentStudentService currentStudent;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRoundRepository roundRepository;
    private final int minMembers;
    private final int maxMembers;

    public GroupWorkspaceService(CurrentStudentService currentStudent, StudentGroupRepository groupRepository,
                                 GroupMemberRepository memberRepository, StudentRepository studentRepository,
                                 RegistrationRoundRepository roundRepository,
                                 @Value("${app.group.min-members:3}") int minMembers,
                                 @Value("${app.group.max-members:5}") int maxMembers) {
        this.currentStudent = currentStudent;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.studentRepository = studentRepository;
        this.roundRepository = roundRepository;
        this.minMembers = minMembers;
        this.maxMembers = maxMembers;
    }

    public int getMinMembers() {
        return minMembers;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    /** Đợt sinh viên có thể thao tác: đợt đã có nhóm của mình hoặc đợt chưa hết hạn đăng ký. */
    @Transactional(readOnly = true)
    public List<RoundOption> roundOptions(String username) {
        Student me = currentStudent.require(username);
        Set<Long> mine = myRoundIds(me);
        LocalDateTime now = LocalDateTime.now();
        List<RoundOption> list = new ArrayList<>();
        for (RegistrationRound r : roundRepository.findAllByOrderByLecturerStartDateDesc()) {
            if (mine.contains(r.getId()) || !now.isAfter(r.getStudentEndDate())) {
                list.add(new RoundOption(r.getId(), r.getName(), Labels.roundTypeShort(r.getType()),
                        mine.contains(r.getId())));
            }
        }
        return list;
    }

    /** Chỉ các đợt mà sinh viên đã có nhóm (dùng cho nộp báo cáo, tra cứu điểm). */
    @Transactional(readOnly = true)
    public List<RoundOption> myRounds(String username) {
        return roundOptions(username).stream().filter(RoundOption::hasGroup).toList();
    }

    public Long defaultRoundId(List<RoundOption> options) {
        for (RoundOption o : options) {
            if (o.hasGroup()) {
                return o.id();
            }
        }
        return options.isEmpty() ? null : options.get(0).id();
    }

    @Transactional(readOnly = true)
    public GroupView myGroup(String username, Long roundId) {
        Student me = currentStudent.require(username);
        StudentGroup g = findMyGroup(me, roundId);
        return g == null ? null : toView(g, me);
    }

    @Transactional(readOnly = true)
    public StudentGroup findMyGroup(Student me, Long roundId) {
        if (roundId == null) {
            return null;
        }
        for (GroupMember m : memberRepository.findByStudentId(me.getId())) {
            if (m.getGroup().getRound().getId().equals(roundId)) {
                return m.getGroup();
            }
        }
        return null;
    }

    public GroupView toView(StudentGroup g, Student me) {
        List<GroupMemberView> members = new ArrayList<>();
        boolean leader = false;
        for (GroupMember m : memberRepository.findByGroupId(g.getId())) {
            boolean isLeader = Boolean.TRUE.equals(m.getIsLeader());
            boolean isMe = m.getStudent().getId().equals(me.getId());
            if (isMe && isLeader) {
                leader = true;
            }
            members.add(new GroupMemberView(m.getId(), m.getStudent().getId(), m.getStudent().getStudentCode(),
                    m.getStudent().getFullName(), m.getStudent().getClassName(), isLeader, isMe));
        }
        members.sort(Comparator.comparing(GroupMemberView::leader).reversed());
        return new GroupView(g.getId(), g.getGroupName(), g.getRound().getId(), g.getRound().getName(),
                g.getStatus() == null ? "" : g.getStatus().name(), leader, g.getTopic() != null,
                g.getTopic() == null ? null : g.getTopic().getCode(),
                g.getTopic() == null ? null : g.getTopic().getTitle(),
                members, minMembers, maxMembers);
    }

    /** Lấy nhóm theo id và bắt buộc người dùng là nhóm trưởng của nhóm đó. */
    public StudentGroup requireLeaderGroup(Student me, Long groupId) {
        StudentGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm."));
        boolean leader = memberRepository.findByGroupId(groupId).stream()
                .anyMatch(m -> m.getStudent().getId().equals(me.getId()) && Boolean.TRUE.equals(m.getIsLeader()));
        if (!leader) {
            throw new BusinessException("Chỉ nhóm trưởng mới được thực hiện thao tác này.");
        }
        return g;
    }

    public StudentGroup create(String username, Long roundId, String groupName) {
        Student me = currentStudent.require(username);
        RegistrationRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
        if (LocalDateTime.now().isAfter(round.getStudentEndDate())) {
            throw new BusinessException("Đợt này đã hết thời gian đăng ký nên không thể lập nhóm mới.");
        }
        if (groupName == null || groupName.isBlank()) {
            throw new BusinessException("Vui lòng nhập tên nhóm.");
        }
        String name = groupName.trim();
        if (memberRepository.existsByStudentIdAndGroupRoundId(me.getId(), roundId)) {
            throw new BusinessException("Bạn đã thuộc một nhóm trong đợt này (mỗi sinh viên chỉ tham gia 1 nhóm).");
        }
        if (groupRepository.existsByRoundIdAndGroupNameIgnoreCase(roundId, name)) {
            throw new BusinessException("Tên nhóm \"" + name + "\" đã tồn tại trong đợt này.");
        }
        StudentGroup g = new StudentGroup();
        g.setGroupName(name);
        g.setRound(round);
        g.setStatus(GroupStatus.FORMING);
        g = groupRepository.save(g);
        addMemberRow(g, me, true);
        return g;
    }

    public void addMember(String username, Long groupId, String studentCode) {
        Student me = currentStudent.require(username);
        StudentGroup g = requireLeaderGroup(me, groupId);
        requireEditable(g);
        final String code = studentCode == null ? "" : studentCode.trim();
        if (code.isEmpty()) {
            throw new BusinessException("Vui lòng nhập MSSV của sinh viên cần thêm.");
        }
        if (memberRepository.countByGroupId(groupId) >= maxMembers) {
            throw new BusinessException("Nhóm đã đủ tối đa " + maxMembers + " thành viên.");
        }
        Student target = studentRepository.findByStudentCode(code)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên có MSSV \"" + code + "\"."));
        if (memberRepository.existsByStudentIdAndGroupRoundId(target.getId(), g.getRound().getId())) {
            throw new BusinessException("Sinh viên " + target.getFullName() + " (" + code
                    + ") đã thuộc một nhóm trong đợt này.");
        }
        addMemberRow(g, target, false);
    }

    public void removeMember(String username, Long groupId, Long memberId) {
        Student me = currentStudent.require(username);
        StudentGroup g = requireLeaderGroup(me, groupId);
        requireEditable(g);
        GroupMember m = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên."));
        if (!m.getGroup().getId().equals(groupId)) {
            throw new BusinessException("Thành viên không thuộc nhóm này.");
        }
        if (Boolean.TRUE.equals(m.getIsLeader())) {
            throw new BusinessException("Không thể xóa nhóm trưởng khỏi nhóm. Hãy giải tán nhóm nếu cần.");
        }
        memberRepository.delete(m);
    }

    /** Thành viên thường tự rời nhóm. */
    public void leave(String username, Long groupId) {
        Student me = currentStudent.require(username);
        StudentGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm."));
        requireEditable(g);
        GroupMember mine = memberRepository.findByGroupIdAndStudentId(groupId, me.getId())
                .orElseThrow(() -> new BusinessException("Bạn không thuộc nhóm này."));
        if (Boolean.TRUE.equals(mine.getIsLeader())) {
            throw new BusinessException("Nhóm trưởng không thể rời nhóm. Hãy giải tán nhóm nếu cần.");
        }
        memberRepository.delete(mine);
    }

    public void disband(String username, Long groupId) {
        Student me = currentStudent.require(username);
        StudentGroup g = requireLeaderGroup(me, groupId);
        requireEditable(g);
        memberRepository.deleteByGroupId(groupId);
        memberRepository.flush();
        groupRepository.delete(g);
    }

    private void requireEditable(StudentGroup g) {
        if (g.getTopic() != null) {
            throw new BusinessException("Nhóm đã đăng ký đề tài nên không thể thay đổi thành viên.");
        }
        if (LocalDateTime.now().isAfter(g.getRound().getStudentEndDate())) {
            throw new BusinessException("Đợt này đã hết thời gian đăng ký nên không thể thay đổi nhóm.");
        }
    }

    private void addMemberRow(StudentGroup g, Student s, boolean leader) {
        GroupMember m = new GroupMember();
        m.setGroup(g);
        m.setStudent(s);
        m.setIsLeader(leader);
        memberRepository.save(m);
    }

    private Set<Long> myRoundIds(Student me) {
        Set<Long> ids = new HashSet<>();
        for (GroupMember m : memberRepository.findByStudentId(me.getId())) {
            ids.add(m.getGroup().getRound().getId());
        }
        return ids;
    }
}
