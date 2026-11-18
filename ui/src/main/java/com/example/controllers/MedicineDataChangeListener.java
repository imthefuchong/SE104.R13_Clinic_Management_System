package com.example.controllers;

import com.example.model.MedicineModel;

public interface MedicineDataChangeListener {
    void onDataChanged(MedicineModel updatedMedicine, String action);
}