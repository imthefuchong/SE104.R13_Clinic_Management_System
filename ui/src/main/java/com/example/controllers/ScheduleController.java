package com.example.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.WeekView;
import com.calendarfx.view.page.WeekPage;
import com.example.DAO.DutyShiftDAO;
import com.example.DAO.StaffDAO; // Thêm import cho StaffDAO
import com.example.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.calendarfx.model.Entry;

public class ScheduleController {

    public HBox ShiftDayAddBox;
    public GridPane StaffInfo;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<String> shiftChoice;
    @FXML private ComboBox<String> codeField;
    @FXML private ComboBox<String> nameField;
    @FXML private ComboBox<String> roleField;
    @FXML private Label statusLabel;
    @FXML private StackPane calendarPane;

    private List<DutyShiftModel> allStaff;
    private boolean isUserSelectingRole = false;
    private List<StaffModel> allStaffList; // Danh sách tất cả nhân viên
    private CalendarView calendarView;
    private CalendarSource calendarSource;
    private final Map<Role, Calendar> roleCalendars = new HashMap<>();

    @FXML
    public void initialize() {
        handlePermission();
        // Tạo nguồn lịch chung
        calendarSource = new CalendarSource("Nguồn lịch");
        nameField.setOnShowing(event -> {
            // Lưu lại giá trị hiện tại khi mở dropdown
            nameField.setUserData(nameField.getValue());
        });

        nameField.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Nếu người dùng tự chọn, rollback về giá trị cũ
            if (nameField.isShowing()) {
                nameField.setValue((String) nameField.getUserData());
            }
        });

        codeField.setEditable(false);
        nameField.setEditable(false);
        roleField.setEditable(false);

        // Khởi tạo CalendarView
        calendarView = new CalendarView();
        calendarView.getCalendarSources().add(calendarSource);
        calendarView.showWeekPage();
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSourceTray(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setEntryFactory(param -> null);
        calendarView.setEntryDetailsPopOverContentCallback(param -> null);
        calendarPane.getChildren().add(calendarView);

        // Bắt sự kiện phím DELETE để xóa lịch trực
        calendarView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DELETE) {
                System.out.println("Delete key pressed - selections: " + calendarView.getSelections().size());

                var selections = calendarView.getSelections();
                if (!selections.isEmpty()) {
                    Entry<?> selectedEntry = selections.iterator().next();

                    if (selectedEntry instanceof ScheduleEntry) {
                        ScheduleEntry entry = (ScheduleEntry) selectedEntry;

                        boolean confirmed = confirmDelete(entry.getTitle());
                        if (!confirmed) return;

                        // Xóa trong CSDL
                        boolean deleted = DutyShiftDAO.deleteDutyShift(entry.getMaLichTruc());
                        if (deleted) {
                            entry.getCalendar().removeEntry(entry);
                            showStatus("Xóa lịch trực thành công.", false);
                        } else {
                            showStatus("Xóa lịch trực thất bại.", true);
                        }

                        // Consume event để prevent default behavior
                        event.consume();
                    }
                } else {
                    showStatus("Vui lòng chọn một lịch trực để xóa.", true);
                }
            }
        });


        // Cấu hình WeekView
        WeekPage weekPage = calendarView.getWeekPage();
        WeekView weekView = weekPage.getDetailedWeekView().getWeekView();
        weekView.setStartTime(LocalTime.of(7, 0));
        weekView.setVisibleHours(14);
        weekView.setHourHeight(40);
        weekView.setEarlyLateHoursStrategy(DayViewBase.EarlyLateHoursStrategy.HIDE);

        // Chặn tạo Entry bằng chuột
        weekView.setEntryFactory(param -> null);
        weekView.setEntryDetailsPopOverContentCallback(param -> null);

        // Cài đặt lựa chọn ca trực
        shiftChoice.getItems().addAll(ScheduleEntry.SHIFTS.keySet());
        shiftChoice.getSelectionModel().selectFirst();

        // Load dữ liệu nhân viên
        loadStaffData();

        // Cài đặt listener cho codeField
        setupCodeFieldListener();
        setupRoleFieldListener();

        //Load Role
        roleField.getItems().add(""); // Cho phép bỏ trống
        for (Role role : Role.values()) {
            if(role == Role.ADMIN)
            {
                continue;
            }
            roleField.getItems().add(role.toVietnamese());
        }

        // Tải dữ liệu mẫu
        loadSampleEntries();
    }

    private StaffModel createSampleStaff(String id, String last, String first, String role, String phone) {
        StaffModel s = new StaffModel();
        s.setId(id);
        s.setLastname(last);
        s.setFirstname(first);
        s.setRole(role);
        s.setPhone(phone);
        return s;
    }

    private void loadStaffData() {
        try {
            allStaffList = StaffDAO.getAll();
        } catch (Exception e) {
            allStaffList = null;
        }

        if (allStaffList == null || allStaffList.isEmpty()) {
            allStaffList = List.of(
                    createSampleStaff("NV001", "Nguyễn", "Văn A", "Bác sĩ", "0901234567"),
                    createSampleStaff("NV002", "Trần", "Thị B", "Điều dưỡng", "0912345678"),
                    createSampleStaff("NV003", "Lê", "Văn C", "Dược sĩ", "0923456789"),
                    createSampleStaff("NV004", "Phạm", "Thị D", "Điều dưỡng", "0934567890"),
                    createSampleStaff("NV005", "Hoàng", "Văn E", "Bác sĩ", "0945678901")
            );
        }

        // Xóa dữ liệu cũ
        codeField.getItems().clear();
        nameField.getItems().clear();

        // Thêm mã nhân viên vào codeField
        for (StaffModel staff : allStaffList) {
            Role role = convertStringToRole(staff.getRole());
            if (role == Role.ADMIN) continue;

            codeField.getItems().add(staff.getId());
            String fullName = staff.getLastname() + " " + staff.getFirstname();
            nameField.getItems().add(fullName);
        }
    }

    private boolean confirmDelete(String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa lịch trực này?");
        alert.setContentText(title);

        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        return result == ButtonType.OK;
    }


    private void setupCodeFieldListener() {
        codeField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Tìm nhân viên theo mã
                StaffModel selectedStaff = findStaffByCode(newValue);
                if (selectedStaff != null) {
                    // Cập nhật nameField - tên đầy đủ
                    String fullName = selectedStaff.getLastname() + " " + selectedStaff.getFirstname();
                    nameField.setValue(fullName);

                    // Cập nhật roleField
                    try {
                        // Chuyển đổi role từ String sang Role enum
                        Role role = convertStringToRole(selectedStaff.getRole());
                        if (role != null) {
                            roleField.setValue(role.toVietnamese());
                        } else {
                            roleField.setValue("");
                        }
                    } catch (Exception e) {
                        // Nếu không có vai trò hoặc lỗi, để trống
                        roleField.setValue("");
                    }
                }
            }
        });
    }


    private StaffModel findStaffByCode(String maNhanVien) {
        if (allStaffList == null) return null;

        return allStaffList.stream()
                .filter(staff -> staff.getId().equals(maNhanVien))
                .findFirst()
                .orElse(null);
    }

    private Role convertStringToRole(String roleString) {
        if (roleString == null || roleString.isEmpty()) return null;

        try {
            // Giả sử role trong database được lưu bằng tiếng Việt
            return Role.fromVietnamese(roleString);
        } catch (IllegalArgumentException e) {
            // Nếu không match, thử convert từ English
            try {
                return Role.valueOf(roleString.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    private void setupRoleFieldListener() {
        roleField.setOnShowing(event -> isUserSelectingRole = true);

        roleField.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (!isUserSelectingRole) return;

            Platform.runLater(() -> {
                // Reset flag trước, tránh lặp
                isUserSelectingRole = false;

                if (newValue != null && !newValue.equals(oldValue)) {
                    // Đóng dropdown trước khi thay đổi
                    roleField.hide();

                    // Xóa dữ liệu cũ
                    codeField.getItems().setAll();
                    nameField.getItems().setAll();
                    codeField.getSelectionModel().clearSelection();
                    nameField.getSelectionModel().clearSelection();
                    codeField.setValue(null);
                    nameField.setValue(null);

                    if (newValue.isEmpty()) {
                        loadAllStaffToFields();
                    } else {
                        filterStaffByRole(newValue);
                    }
                }
            });
        });
    }


    private void loadAllStaffToFields() {
        if (allStaffList == null) return;

        for (StaffModel staff : allStaffList) {
            Role role = convertStringToRole(staff.getRole());
            if (role == Role.ADMIN) continue;
            codeField.getItems().add(staff.getId());
            String fullName = staff.getLastname() + " " + staff.getFirstname();
            nameField.getItems().add(fullName);
        }
    }

    private void filterStaffByRole(String selectedRoleVietnamese) {
        if (allStaffList == null) return;

        try {
            // Chuyển đổi vai trò từ tiếng Việt sang enum
            Role selectedRole = Role.fromVietnamese(selectedRoleVietnamese);

            // Lọc nhân viên theo vai trò
            for (StaffModel staff : allStaffList) {
                Role staffRole = convertStringToRole(staff.getRole());

                // Chỉ thêm nhân viên có vai trò trùng khớp
                if (staffRole != null && staffRole.equals(selectedRole)) {
                    codeField.getItems().add(staff.getId());
                    String fullName = staff.getLastname() + " " + staff.getFirstname();
                    nameField.getItems().add(fullName);
                }
            }
        } catch (IllegalArgumentException e) {
            showStatus("Vai trò không hợp lệ: " + selectedRoleVietnamese, true);
        }
    }

    @FXML
    private void handleAddShift() {
        LocalDate date = datePicker.getValue();
        String shift = shiftChoice.getValue();
        String code = codeField.getValue();
        String name = nameField.getValue();
        String roleText = roleField.getValue();

        if (date == null || code == null || code.isEmpty() || name.isEmpty() || roleText.isEmpty()) {
            showStatus("Vui lòng nhập đầy đủ thông tin.", true);
            return;
        }

        Role role;
        try {
            role = Role.fromVietnamese(roleText);
        } catch (IllegalArgumentException ex) {
            showStatus("Vai trò không hợp lệ.", true);
            return;
        }

        // Sử dụng mã nhân viên đã chọn thay vì hardcode
        String maLichTruc = UUID.randomUUID().toString().substring(0, 20);
        DutyShiftModel duty = new DutyShiftModel(maLichTruc, code, name, role, date, shift);

        boolean success = DutyShiftDAO.insertDutyShift(duty, maLichTruc);
        if (!success) {
            showStatus("Lỗi khi lưu lịch trực vào CSDL.", true);
            return;
        }

        // Lấy hoặc tạo Calendar theo vai trò
        Calendar roleCalendar = roleCalendars.computeIfAbsent(role, r -> {
            Calendar c = new Calendar(r.toVietnamese());
            c.setStyle(getStyleForRole(r));
            calendarSource.getCalendars().add(c);
            return c;
        });

        // Tạo entry và đồng bộ từ model
        ScheduleEntry entry = new ScheduleEntry(name, duty);
        entry.syncFromModel();
        entry.setCalendar(roleCalendar);

        showStatus("Thêm lịch trực thành công.", false);

        // Reset form
        codeField.setValue(null);
        nameField.setValue(null);
        roleField.setValue(null);

        calendarView.refreshData();
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "red;" : "green;"));
    }

    private void loadSampleEntries() {
        try {
            List<DutyShiftModel> shiftList = DutyShiftDAO.getAllDutyShifts();
            if (shiftList != null && !shiftList.isEmpty()) {
                for (DutyShiftModel duty : shiftList) {
                    addSampleEntry(duty);
                }
                return;
            }
        } catch (Exception e) {
            System.err.println("Không thể nạp ca trực từ DB: " + e.getMessage());
        }

        // Dữ liệu mẫu demo
        LocalDate today = LocalDate.now();
        List<DutyShiftModel> sampleShifts = List.of(
                new DutyShiftModel(UUID.randomUUID().toString(), "NV001", "Nguyễn Văn A", Role.DOCTOR, today, "Sáng"),
                new DutyShiftModel(UUID.randomUUID().toString(), "NV002", "Trần Thị B", Role.NURSE, today, "Sáng"),
                new DutyShiftModel(UUID.randomUUID().toString(), "NV005", "Hoàng Văn E", Role.DOCTOR, today.plusDays(1), "Chiều"),
                new DutyShiftModel(UUID.randomUUID().toString(), "NV004", "Phạm Thị D", Role.NURSE, today.plusDays(1), "Chiều")
        );
        for (DutyShiftModel duty : sampleShifts) {
            addSampleEntry(duty);
        }
    }

    private void addSampleEntry(DutyShiftModel duty) {
        Calendar roleCalendar = roleCalendars.computeIfAbsent(duty.getVaiTro(), r -> {
            Calendar c = new Calendar(r.toVietnamese());
            c.setStyle(getStyleForRole(r));
            calendarSource.getCalendars().add(c);
            return c;
        });

        ScheduleEntry entry = new ScheduleEntry(duty.getTenNguoiTruc(), duty);
        entry.syncFromModel();
        entry.setCalendar(roleCalendar);
    }

    private Calendar.Style getStyleForRole(Role role) {
        return switch (role) {
            case DOCTOR -> Calendar.Style.STYLE1;
            case NURSE -> Calendar.Style.STYLE2;
            case MANAGER -> Calendar.Style.STYLE3;
            case ADMIN -> Calendar.Style.STYLE4;
        };
    }
    private void handlePermission(){
        Role role = UserContext.getInstance().getRole();
        if (role == null) {
            role = Role.ADMIN;
        }
        switch (role) {
            case ADMIN -> {

            }
            case DOCTOR -> {
                StaffInfo.setVisible(false);
                StaffInfo.setManaged(false);
                ShiftDayAddBox.setVisible(false);
                ShiftDayAddBox.setManaged(false);
            }
            case NURSE -> {
                StaffInfo.setVisible(false);
                StaffInfo.setManaged(false);
                ShiftDayAddBox.setVisible(false);
                ShiftDayAddBox.setManaged(false);
            }
            case MANAGER -> {

            }
        }
    }
}