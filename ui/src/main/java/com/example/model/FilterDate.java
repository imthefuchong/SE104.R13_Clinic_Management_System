package com.example.model;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

public class FilterDate {
    private Year year;
    private YearMonth yearMonth;
    private LocalDate localDate;

    private String mode; // "Năm", "Tháng", "Ngày"

    public FilterDate(String mode, int day, int month, int yearValue) {
        this.mode = mode;
        switch (mode) {
            case "Năm":
                year = Year.of(yearValue);
                break;
            case "Tháng":
                yearMonth = YearMonth.of(yearValue, month);
                break;
            case "Ngày":
                localDate = LocalDate.of(yearValue, month, day);
                break;
        }
    }

    public String toString() {
        return switch (mode) {
            case "Năm" -> year.toString();
            case "Tháng" -> yearMonth.toString(); // ISO 8601: 2024-12
            case "Ngày" -> localDate.toString(); // ISO 8601: 2024-12-01
            default -> "";
        };
    }

    public static FilterDate fromLocalDate(LocalDate date) {
        return new FilterDate("Ngày", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    public Year getYear() { return year; }
    public YearMonth getYearMonth() { return yearMonth; }
    public LocalDate getLocalDate() { return localDate; }
    public String getMode() {
        return mode;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
