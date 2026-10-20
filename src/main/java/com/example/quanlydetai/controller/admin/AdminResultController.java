package com.example.quanlydetai.controller.admin;

import com.example.quanlydetai.dto.PublishSummary;
import com.example.quanlydetai.dto.ResultRow;
import com.example.quanlydetai.dto.RoundView;
import com.example.quanlydetai.service.ExcelExportService;
import com.example.quanlydetai.service.ResultService;
import com.example.quanlydetai.service.RoundAdminService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Tổng kết điểm theo đợt và công bố kết quả chính thức (kèm gửi email cho từng sinh viên). */
@Controller
@RequestMapping("/admin/results")
@PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA')")
public class AdminResultController {

    private final ResultService resultService;
    private final RoundAdminService roundService;
    private final ExcelExportService excelService;

    public AdminResultController(ResultService resultService, RoundAdminService roundService,
                                 ExcelExportService excelService) {
        this.resultService = resultService;
        this.roundService = roundService;
        this.excelService = excelService;
    }

    @GetMapping
    public String view(@RequestParam(value = "roundId", required = false) Long roundId, Model model) {
        List<RoundView> rounds = roundService.listViews(null);
        Long selected = roundId;
        List<ResultRow> rows = List.of();
        if (selected == null) {
            // Mặc định: đợt gần nhất có nhóm đã đăng ký đề tài
            for (RoundView r : rounds) {
                List<ResultRow> candidate = resultService.rows(r.id());
                if (!candidate.isEmpty()) {
                    selected = r.id();
                    rows = candidate;
                    break;
                }
            }
        } else {
            rows = resultService.rows(selected);
        }
        model.addAttribute("rounds", rounds);
        model.addAttribute("roundId", selected);
        model.addAttribute("rows", rows);
        model.addAttribute("published", selected != null && resultService.isPublished(selected));
        return "admin/results/index";
    }

    @PostMapping("/{roundId}/recalculate")
    public String recalculate(@PathVariable Long roundId, RedirectAttributes redirect) {
        int scored = resultService.recalculate(roundId);
        redirect.addFlashAttribute("flashSuccess", "Đã tính lại điểm tổng kết: " + scored + " nhóm có điểm.");
        return "redirect:/admin/results?roundId=" + roundId;
    }

    @PostMapping("/{roundId}/publish")
    public String publish(@PathVariable Long roundId, RedirectAttributes redirect) {
        PublishSummary s = resultService.publish(roundId);
        String msg = "Đã công bố kết quả cho " + s.scoredGroups() + " nhóm. Email: gửi thành công "
                + s.emailsSent() + ", thất bại " + s.emailsFailed() + ".";
        redirect.addFlashAttribute("flashSuccess", msg);
        if (s.missingGroups() > 0) {
            redirect.addFlashAttribute("flashWarning", s.missingGroups() + " nhóm chưa có điểm nên chưa được thông báo.");
        }
        return "redirect:/admin/results?roundId=" + roundId;
    }

    @PostMapping("/{roundId}/unpublish")
    public String unpublish(@PathVariable Long roundId, RedirectAttributes redirect) {
        resultService.unpublish(roundId);
        redirect.addFlashAttribute("flashSuccess", "Đã hủy công bố. Sinh viên không còn xem được điểm của đợt này.");
        return "redirect:/admin/results?roundId=" + roundId;
    }

    /** Tải bảng kết quả của đợt dưới dạng Excel (.xlsx). */
    @GetMapping("/{roundId}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long roundId) {
        byte[] data = excelService.exportResults(roundId);
        String filename = "ket-qua-dot-" + roundId + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
