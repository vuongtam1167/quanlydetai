package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.RoundView;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RegistrationRoundService {

    private final RegistrationRoundRepository roundRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final EmailService emailService;

    public RegistrationRoundService(RegistrationRoundRepository roundRepository, 
                                    StudentGroupRepository groupRepository, 
                                    GroupMemberRepository memberRepository, 
                                    EmailService emailService) {
        this.roundRepository = roundRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.emailService = emailService;
    }


    /** Các đợt gần nhất kèm tiến độ hiện tại (hiển thị ở trang chủ). */
    @Transactional(readOnly = true)
    public List<RoundView> getRecentRoundViews(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return roundRepository
                .findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "lecturerStartDate")))
                .getContent().stream()
                .map(r -> RoundView.of(r, now))
                .toList();
    }

    public List<RegistrationRound> findAll() {
        return roundRepository.findAll();
    }

    public RegistrationRound findById(Long id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt đăng ký: " + id));
    }

    public RegistrationRound createRound(RegistrationRound round) {
        return roundRepository.save(round);
    }
    
    // Kích hoạt công bố và gửi mail
    public RegistrationRound publishResults(Long roundId) {
        RegistrationRound round = findById(roundId);
        round.setIsPublished(true);
        roundRepository.save(round);

        // Quét tất cả các nhóm trong đợt và gửi mail cho từng thành viên
        List<StudentGroup> groups = groupRepository.findByRoundId(roundId);
        for (StudentGroup group : groups) {
            String topicName = group.getTopic() != null ? group.getTopic().getTitle() : "Chưa có đề tài";
            String finalScore = group.getFinalScore() != null ? group.getFinalScore().toString() : "Chưa có điểm";
            
            List<GroupMember> members = memberRepository.findByGroupId(group.getId());
            for (GroupMember member : members) {
                String email = member.getStudent().getEmail();
                String subject = "Thông báo kết quả điểm đợt: " + round.getName();
                String body = String.format("Chào %s,\n\nĐiểm tổng kết cho đề tài '%s' của nhóm bạn là: %s điểm.\n\nTrân trọng,\nKhoa CNTT.", 
                                            member.getStudent().getFullName(), topicName, finalScore);
                
                try {
                    emailService.sendResultEmail(email, subject, body);
                } catch (Exception e) {
                    System.err.println("Không thể gửi mail cho " + email + ": " + e.getMessage());
                }
            }
        }
        return round;
    }
}