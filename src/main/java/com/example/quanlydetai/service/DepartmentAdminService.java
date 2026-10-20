package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.DepartmentForm;
import com.example.quanlydetai.exception.BusinessException;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.Department;
import com.example.quanlydetai.repository.DepartmentRepository;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DepartmentAdminService {

    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;
    private final TopicRepository topicRepository;

    public DepartmentAdminService(DepartmentRepository departmentRepository, LecturerRepository lecturerRepository,
                                  TopicRepository topicRepository) {
        this.departmentRepository = departmentRepository;
        this.lecturerRepository = lecturerRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> list() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> lecturerCounts() {
        Map<Long, Long> map = new HashMap<>();
        for (Department d : departmentRepository.findAll()) {
            map.put(d.getId(), lecturerRepository.countByDepartmentId(d.getId()));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Department get(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn."));
    }

    public Department save(DepartmentForm f) {
        String code = f.getCode().trim();
        Optional<Department> same = departmentRepository.findByCode(code);
        if (same.isPresent() && !same.get().getId().equals(f.getId())) {
            throw new BusinessException("Mã bộ môn \"" + code + "\" đã tồn tại.");
        }
        Department d = f.getId() == null ? new Department() : get(f.getId());
        d.setCode(code);
        d.setName(f.getName().trim());
        d.setDescription(f.getDescription());
        return departmentRepository.save(d);
    }

    public void delete(Long id) {
        Department d = get(id);
        if (lecturerRepository.countByDepartmentId(id) > 0 || topicRepository.existsByDepartmentId(id)) {
            throw new BusinessException("Bộ môn đang có giảng viên/đề tài nên không thể xóa.");
        }
        departmentRepository.delete(d);
    }
}
