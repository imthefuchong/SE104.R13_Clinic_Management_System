package com.example.controllers;

import com.example.model.StaffModel;

public interface StaffDataChangeListener {
    void onDataChanged(StaffModel updatedStaff, String action);
}