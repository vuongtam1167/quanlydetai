package com.example.quanlydetai.dto;

/**
 * Đề tài của giảng viên: kèm nhóm đã đăng ký và việc còn được sửa/xóa hay không.
 */
public record MyTopicRow(TopicRow topic, String groupName, boolean editable) {
}
