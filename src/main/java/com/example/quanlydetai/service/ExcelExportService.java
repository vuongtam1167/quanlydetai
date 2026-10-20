package com.example.quanlydetai.service;

import com.example.quanlydetai.dto.ResultRow;
import com.example.quanlydetai.exception.ResourceNotFoundException;
import com.example.quanlydetai.model.GroupMember;
import com.example.quanlydetai.model.RegistrationRound;
import com.example.quanlydetai.model.StudentGroup;
import com.example.quanlydetai.repository.GroupMemberRepository;
import com.example.quanlydetai.repository.RegistrationRoundRepository;
import com.example.quanlydetai.repository.ReviewAssignmentRepository;
import com.example.quanlydetai.repository.StudentGroupRepository;
import com.example.quanlydetai.repository.TopicAdvisorRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

/** Xuất bảng kết quả của một đợt ra file Excel (.xlsx) để Khoa lưu trữ/in. */
@Service
@Transactional(readOnly = true)
public class ExcelExportService {

    private static final String[] HEADERS = {"STT", "Nhóm", "Mã đề tài", "Tên đề tài", "Sinh viên",
            "GV hướng dẫn", "GV phản biện", "Điểm GVPB", "Điểm TB hội đồng", "Điểm tổng kết", "Kết quả"};
    private static final int[] WIDTHS = {6, 18, 12, 46, 46, 28, 24, 12, 16, 14, 14};

    private final RegistrationRoundRepository roundRepository;
    private final ResultService resultService;
    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final TopicAdvisorRepository advisorRepository;
    private final ReviewAssignmentRepository reviewRepository;

    public ExcelExportService(RegistrationRoundRepository roundRepository, ResultService resultService,
                              StudentGroupRepository groupRepository, GroupMemberRepository memberRepository,
                              TopicAdvisorRepository advisorRepository,
                              ReviewAssignmentRepository reviewRepository) {
        this.roundRepository = roundRepository;
        this.resultService = resultService;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.advisorRepository = advisorRepository;
        this.reviewRepository = reviewRepository;
    }

    public byte[] exportResults(Long roundId) {
        RegistrationRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký."));
        List<ResultRow> rows = resultService.rows(roundId);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Ket qua");

            Font bold = wb.createFont();
            bold.setBold(true);
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(bold);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(headerStyle);

            CellStyle bodyStyle = wb.createCellStyle();
            bodyStyle.setWrapText(true);
            setBorder(bodyStyle);

            Row title = sheet.createRow(0);
            Cell titleCell = title.createCell(0);
            titleCell.setCellValue("KẾT QUẢ ĐỀ TÀI - " + round.getName().toUpperCase());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            Row header = sheet.createRow(2);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(HEADERS[i]);
                c.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, WIDTHS[i] * 256);
            }

            int rowIndex = 3;
            int stt = 1;
            for (ResultRow r : rows) {
                StudentGroup g = groupRepository.findById(r.groupId()).orElse(null);
                String students = "";
                String advisors = "";
                String reviewer = "";
                if (g != null) {
                    students = memberRepository.findByGroupId(g.getId()).stream()
                            .map(this::studentLabel).collect(Collectors.joining("\n"));
                    advisors = advisorRepository.findByTopicId(g.getTopic().getId()).stream()
                            .map(a -> a.getLecturer().getFullName()).collect(Collectors.joining(", "));
                    reviewer = reviewRepository.findByTopicId(g.getTopic().getId())
                            .map(a -> a.getReviewer().getFullName()).orElse("");
                }
                String result = !r.hasFinal() ? "Thiếu điểm" : (r.passed() ? "ĐẠT" : "KHÔNG ĐẠT");
                String[] values = {String.valueOf(stt++), r.groupName(), r.topicCode(), r.topicTitle(), students,
                        advisors, reviewer,
                        r.reviewerScore() == null ? "" : r.reviewerScore().toPlainString(),
                        r.councilScore() == null ? "" : r.councilScore().toPlainString(),
                        r.finalScore() == null ? "" : r.finalScore().toPlainString(), result};
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < values.length; i++) {
                    Cell c = row.createCell(i);
                    c.setCellValue(values[i]);
                    c.setCellStyle(bodyStyle);
                }
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Không thể tạo file Excel", e);
        }
    }

    private String studentLabel(GroupMember m) {
        return m.getStudent().getStudentCode() + " - " + m.getStudent().getFullName()
                + (Boolean.TRUE.equals(m.getIsLeader()) ? " (NT)" : "");
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
