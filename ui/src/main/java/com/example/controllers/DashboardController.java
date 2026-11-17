package com.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class DashboardController {

    @FXML
    private DatePicker dpWorkingDate;

    @FXML
    private Label lblReceived;

    @FXML
    private Label lblWaiting;

    @FXML
    private Label lblExamined;

    @FXML
    private TableView<DemoPatient> tblAppointments;

    @FXML
    private TableColumn<DemoPatient, String> colOrder;

    @FXML
    private TableColumn<DemoPatient, String> colPatientId;

    @FXML
    private TableColumn<DemoPatient, String> colFullName;

    @FXML
    private TableColumn<DemoPatient, String> colGender;

    @FXML
    private TableColumn<DemoPatient, String> colBirthYear;

    @FXML
    private TableColumn<DemoPatient, String> colAddress;

    @FXML
    private TableColumn<DemoPatient, String> colStatus;

    private Runnable openAppointmentList;

    private final LocalDate demoDate = LocalDate.now();

    private final List<DemoPatient> demoPatients = List.of(
            new DemoPatient(
                    "1", "BN000001", "Nguyễn Văn An",
                    "Nam", "1995", "Quận 1, TP.HCM",
                    "Đã khám"
            ),
            new DemoPatient(
                    "2", "BN000002", "Trần Thị Bình",
                    "Nữ", "2000", "Quận 3, TP.HCM",
                    "Đã khám"
            ),
            new DemoPatient(
                    "3", "BN000003", "Lê Minh Châu",
                    "Nam", "1988", "TP. Thủ Đức",
                    "Đang chờ"
            ),
            new DemoPatient(
                    "4", "BN000004", "Phạm Ngọc Dung",
                    "Nữ", "2002", "Quận 7, TP.HCM",
                    "Đang chờ"
            ),
            new DemoPatient(
                    "5", "BN000005", "Võ Hoàng Nam",
                    "Nam", "1990", "Quận Bình Thạnh",
                    "Đang chờ"
            )
    );

    @FXML
    private void initialize() {
        tblAppointments.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        configureColumn(colOrder, DemoPatient::order);
        configureColumn(colPatientId, DemoPatient::patientId);
        configureColumn(colFullName, DemoPatient::fullName);
        configureColumn(colGender, DemoPatient::gender);
        configureColumn(colBirthYear, DemoPatient::birthYear);
        configureColumn(colAddress, DemoPatient::address);
        configureColumn(colStatus, DemoPatient::status);

        dpWorkingDate.setValue(demoDate);

        dpWorkingDate.valueProperty().addListener(
                (observable, oldDate, newDate) ->
                        refreshDemoData()
        );

        refreshDemoData();
    }

    private void configureColumn(
            TableColumn<DemoPatient, String> column,
            Function<DemoPatient, String> valueProvider
    ) {
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        valueProvider.apply(cell.getValue())
                )
        );

        column.setSortable(false);
    }

    private void refreshDemoData() {
        List<DemoPatient> rows =
                demoDate.equals(dpWorkingDate.getValue())
                        ? demoPatients
                        : List.of();

        tblAppointments.setItems(
                FXCollections.observableArrayList(rows)
        );

        long waiting = rows.stream()
                .filter(patient ->
                        "Đang chờ".equals(patient.status())
                )
                .count();

        long examined = rows.stream()
                .filter(patient ->
                        "Đã khám".equals(patient.status())
                )
                .count();

        lblReceived.setText(String.valueOf(rows.size()));
        lblWaiting.setText(String.valueOf(waiting));
        lblExamined.setText(String.valueOf(examined));
    }

    public void setOpenAppointmentList(Runnable action) {
        this.openAppointmentList = action;
    }

    public LocalDate getWorkingDate() {
        return dpWorkingDate.getValue();
    }

    @FXML
    private void handleOpenAppointmentList() {
        if (openAppointmentList != null) {
            openAppointmentList.run();
        }
    }

    public void cleanup() {
        // Bản demo không tạo tác vụ nền hoặc kết nối CSDL.
    }

    public record DemoPatient(
            String order,
            String patientId,
            String fullName,
            String gender,
            String birthYear,
            String address,
            String status
    ) {
    }
}