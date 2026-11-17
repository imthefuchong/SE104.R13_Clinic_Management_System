package com.example.model;

import java.math.BigDecimal;

public class QuiDinhModel {
    private BigDecimal giaTri;

    public QuiDinhModel(BigDecimal giaTri) {
        this.giaTri = giaTri;
    }

    public BigDecimal getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(BigDecimal giaTri) {
        this.giaTri = giaTri;
    }
}


