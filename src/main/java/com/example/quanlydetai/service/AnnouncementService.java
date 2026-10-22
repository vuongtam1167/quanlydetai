package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.AnnouncementForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Announcement;
import com.example.quanlydetai.model.User;
import com.example.quanlydetai.repository.AnnouncementRepository;
import com.example.quanlydetai.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Tìm kiếm + phân trang; thông báo ghim luôn nằm trên cùng, sau đó mới nhất trước.
     */
    @Transactional(readOnly = true)
    public Page<Announcement> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size,
                Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("createdAt")));
        if (keyword == null || keyword.isBlank()) {
            return announcementRepository.findAll(pageable);
        }
        String k = keyword.trim();
        return announcementRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(k, k, pageable);
    }

    public Announcement create(AnnouncementForm form, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản đăng thông báo."));
        Announcement a = new Announcement();
        a.setTitle(form.getTitle().trim());
        a.setContent(form.getContent().trim());
        a.setPinned(form.isPinned());
        a.setCreatedBy(author);
        return announcementRepository.save(a);
    }

    @Transactional(readOnly = true)
    public AnnouncementForm toForm(Long id) {
        Announcement a = get(id);
        AnnouncementForm form = new AnnouncementForm();
        form.setId(a.getId());
        form.setTitle(a.getTitle());
        form.setContent(a.getContent());
        form.setPinned(Boolean.TRUE.equals(a.getPinned()));
        return form;
    }

    public Announcement update(AnnouncementForm form) {
        if (form.getId() == null) {
            throw new BusinessException("Mã thông báo cần sửa không được để trống.");
        }
        Announcement a = get(form.getId());
        a.setTitle(form.getTitle().trim());
        a.setContent(form.getContent().trim());
        a.setPinned(form.isPinned());
        return announcementRepository.save(a);
    }

    public void togglePin(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo."));
        a.setPinned(!Boolean.TRUE.equals(a.getPinned()));
        announcementRepository.save(a);
    }

    public void delete(Long id) {
        announcementRepository.delete(get(id));
    }

    private Announcement get(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo."));
    }
}
