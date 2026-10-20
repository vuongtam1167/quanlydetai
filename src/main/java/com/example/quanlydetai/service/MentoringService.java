package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.MemberView;
import com.example.quanlydetai.dto.MentoringRow;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.model.Submission;
import com.example.quanlydetai.model.Topic;
import com.example.quanlydetai.model.TopicAdvisor;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.SubmissionRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Giảng viên theo dõi nhóm sinh viên mình hướng dẫn và tài nguyên nhóm đã nộp. */
@Service
@Transactional(readOnly = true)
public class MentoringService {

    private final CurrentLecturerService currentLecturer;
    private final TopicAdvisorRepository advisorRepository;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final SubmissionRepository submissionRepository;

    public MentoringService(CurrentLecturerService currentLecturer, TopicAdvisorRepository advisorRepository,
                            StudentGroupRepository groupRepository, GroupMemberRepository memberRepository,
                            SubmissionRepository submissionRepository) {
        this.currentLecturer = currentLecturer;
        this.advisorRepository = advisorRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<MentoringRow> rows(String username) {
        Lecturer me = currentLecturer.require(username);
        Map<Long, Topic> map = new LinkedHashMap<>();
        for (TopicAdvisor a : advisorRepository.findByLecturerId(me.getId())) {
            map.putIfAbsent(a.getTopic().getId(), a.getTopic());
        }
        List<Topic> topics = new ArrayList<>(map.values());
        topics.sort(Comparator.comparing(Topic::getId).reversed());

        List<MentoringRow> rows = new ArrayList<>();
        for (Topic t : topics) {
            String coAdvisor = advisorRepository.findByTopicId(t.getId()).stream()
                    .filter(a -> !a.getLecturer().getId().equals(me.getId()))
                    .map(a -> a.getLecturer().getFullName())
                    .collect(Collectors.joining(", "));
            StudentGroup g = groupRepository.findByTopicId(t.getId()).orElse(null);
            if (g == null) {
                rows.add(new MentoringRow(t.getId(), t.getCode(), t.getTitle(), t.getRound().getName(),
                        coAdvisor, null, List.of(), false, null, null, null));
                continue;
            }
            List<MemberView> members = new ArrayList<>();
            for (GroupMember m : memberRepository.findByGroupId(g.getId())) {
                members.add(new MemberView(m.getStudent().getStudentCode(), m.getStudent().getFullName(),
                        Boolean.TRUE.equals(m.getIsLeader())));
            }
            Optional<Submission> sub = submissionRepository.findFirstByGroupIdOrderByIdDesc(g.getId());
            rows.add(new MentoringRow(t.getId(), t.getCode(), t.getTitle(), t.getRound().getName(),
                    coAdvisor, g.getGroupName(), members, sub.isPresent(),
                    sub.map(Submission::getFilePath).orElse(null),
                    sub.map(Submission::getSourceCodeUrl).orElse(null),
                    sub.map(Submission::getNotes).orElse(null)));
        }
        return rows;
    }
}
