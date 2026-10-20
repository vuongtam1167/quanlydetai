package com.example.quanlydetai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Form thành lập/sửa hội đồng: 3-5 giảng viên gồm 1 chủ tịch + 1 thư ký + ủy viên. */
public class CouncilForm {

    private Long id;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long roundId;

    @NotBlank(message = "Tên hội đồng không được để trống")
    @Size(max = 150, message = "Tên hội đồng tối đa 150 ký tự")
    private String name;

    @Size(max = 100, message = "Phòng họp tối đa 100 ký tự")
    private String location;

    @NotNull(message = "Ngày giờ bảo vệ là bắt buộc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime defenseDate;

    @NotNull(message = "Vui lòng chọn Chủ tịch hội đồng")
    private Long chairId;

    @NotNull(message = "Vui lòng chọn Thư ký hội đồng")
    private Long secretaryId;

    private List<Long> memberIds = new ArrayList<>();
    private List<Long> groupIds = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getDefenseDate() { return defenseDate; }
    public void setDefenseDate(LocalDateTime defenseDate) { this.defenseDate = defenseDate; }
    public Long getChairId() { return chairId; }
    public void setChairId(Long chairId) { this.chairId = chairId; }
    public Long getSecretaryId() { return secretaryId; }
    public void setSecretaryId(Long secretaryId) { this.secretaryId = secretaryId; }
    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds == null ? new ArrayList<>() : memberIds; }
    public List<Long> getGroupIds() { return groupIds; }
    public void setGroupIds(List<Long> groupIds) { this.groupIds = groupIds == null ? new ArrayList<>() : groupIds; }
}
