package com.example.model;

import java.time.LocalDate;

public class PatientReportModel {
    private LocalDate date;
    private int patientCount;
    private double revenue;
    private double rate;

    public PatientReportModel() {}

    public PatientReportModel(LocalDate date, int patientCount, double revenue, double rate) {
        this.date = date;
        this.patientCount = patientCount;
        this.revenue = revenue;
        this.rate = rate;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getPatientCount() {
        return patientCount;
    }
    public void setPatientCount(int patientCount) {
        this.patientCount = patientCount;
    }

    public double getRevenue() {
        return revenue;
    }
    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getRate() {
        return rate;
    }
    public void setRate(double rate) {
        this.rate = rate;
    }
}
