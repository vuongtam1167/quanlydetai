package com.example.quanlydetai.service;

import com.example.quanlydetai.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Kiểm tra điểm hợp lệ (thang 10) dùng chung cho chấm phản biện và chấm hội đồng. */
public final class Scores {

    private Scores() {
    }

    public static BigDecimal validate(BigDecimal score) {
        if (score == null) {
            throw new BusinessException("Vui lòng nhập điểm.");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
            throw new BusinessException("Điểm phải nằm trong khoảng từ 0 đến 10.");
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }
}
