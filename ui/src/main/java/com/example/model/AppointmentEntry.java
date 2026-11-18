package com.example.model;

import com.calendarfx.model.Entry;

public class AppointmentEntry extends Entry<String> {

    private AppointmentModel model;

    public AppointmentEntry(String title, AppointmentModel model) {
        super(title); // tiêu đề hiển thị trong calendar
        this.model = model;
    }

    public AppointmentModel getModel() {
        return model;
    }

    public void setModel(AppointmentModel model) {
        this.model = model;
    }
}
