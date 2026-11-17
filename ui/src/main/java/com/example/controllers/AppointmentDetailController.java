package com.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentDetailController {

    @FXML private TextField txtNgayKham;
    @FXML private TextField txtMaBenhNhan;
    @FXML private TextField txtHoTen;
    @FXML private ComboBox<String> cbGioiTinh;
    @FXML private TextField txtNamSinh;
    @FXML private TextField txtDiaChi;
    @FXML private TextField txtSoDienThoai;
    @FXML private TextField txtSoThuTu;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<DemoPatient> demoPatients = List.of(
            new DemoPatient(
                    "BN000001",
                    "Nguyễn Văn An",
                    "Nam",
                    "1995",
                    "Quận 1, TP.HCM",
                    "0900000001"
            ),
            new DemoPatient(
                    "BN000002",
                    "Trần Thị Bình",
                    "Nữ",
                    "2000",
                    "Quận 3, TP.HCM",
                    "0900000002"
            ),
            new DemoPatient(
                    "BN000003",
                    "Lê Minh Châu",
                    "Nam",
                    "1988",
                    "TP. Thủ Đức",
                    "0900000003"
            )
    );

    @FXML
    private void initialize() {
        cbGioiTinh.setItems(
                FXCollections.observableArrayList(
                        "Nam", "Nữ"
                )
        );

        setExamDate(LocalDate.now());

        txtMaBenhNhan.clear();
        txtSoThuTu.clear();
    }

    public void setExamDate(LocalDate date) {
        LocalDate examDate = date == null
                ? LocalDate.now()
                : date;

        txtNgayKham.setText(
                examDate.format(DATE_FORMAT)
        );
    }

    @FXML
    private void handleChonBenhNhanCu() {
        ChoiceDialog<DemoPatient> dialog =
                new ChoiceDialog<>(
                        demoPatients.get(0),
                        demoPatients
                );

        dialog.initOwner(
                txtHoTen.getScene().getWindow()
        );

        dialog.setTitle("Chọn bệnh nhân mẫu");
        dialog.setHeaderText(
                "Danh sách minh họa — không kết nối CSDL"
        );
        dialog.setContentText("Bệnh nhân:");

        dialog.showAndWait().ifPresent(
                this::displayPatient
        );
    }

    private void displayPatient(DemoPatient patient) {
        txtMaBenhNhan.setText(patient.id());
        txtHoTen.setText(patient.name());
        cbGioiTinh.setValue(patient.gender());
        txtNamSinh.setText(patient.birthYear());
        txtDiaChi.setText(patient.address());
        txtSoDienThoai.setText(patient.phone());

        // Hồ sơ mẫu đã có chỉ dùng để xem.
        txtHoTen.setEditable(false);
        cbGioiTinh.setDisable(true);
        txtNamSinh.setEditable(false);
        txtDiaChi.setEditable(false);
        txtSoDienThoai.setEditable(false);
    }

    @FXML
    private void handleThemVaoDanhSach() {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.initOwner(
                txtHoTen.getScene().getWindow()
        );

        alert.setTitle("Bản trình diễn giao diện");
        alert.setHeaderText(null);
        alert.setContentText(
                "Chức năng tiếp nhận chưa được triển khai.\n"
                + "Chưa lưu hồ sơ, chưa cấp số thứ tự "
                + "và chưa thêm bệnh nhân vào danh sách."
        );

        alert.showAndWait();
    }

    @FXML
    private void handleHuy() {
        Stage stage = (Stage) txtHoTen
                .getScene()
                .getWindow();

        stage.close();
    }

    private record DemoPatient(
            String id,
            String name,
            String gender,
            String birthYear,
            String address,
            String phone
    ) {
        @Override
        public String toString() {
            return id + " — " + name;
        }
    }
}