package com.example.controllers;

import com.example.model.PatientModel;

public interface PatientDataChangeListener {
    void onDataChanged(PatientModel updatedPatient, String action);
}
