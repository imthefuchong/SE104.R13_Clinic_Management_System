package com.example.controllers;

import com.example.DAO.PatientDAO;
import com.example.model.PatientModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PatientController implements PatientDataChangeListener {
    @FXML
    private TableView<PatientModel> tvPatient;
    @FXML
    private TableColumn<PatientModel, String> nameCol;
    @FXML
    private TableColumn<PatientModel, String> idCol;
    @FXML
    private TableColumn<PatientModel, String> genderCol;
    @FXML
    private TableColumn<PatientModel, String> phoneCol;
    @FXML
    private TableColumn<PatientModel, String> birthCol;
    @FXML
    private TableColumn<PatientModel, String> addressCol;
    @FXML
    private TextField tfSearch;
    @FXML
    private Label lblTotalPatients;
    @FXML
    private Button btnSearchHistory;

    private ObservableList<PatientModel> patientList;
    private FilteredList<PatientModel> filteredData;
    private Runnable onDataChanged;
    private Runnable onOpenHistory;

    @FXML
    public void initialize() {
        nameCol.prefWidthProperty().bind(tvPatient.widthProperty().multiply(0.19));
        idCol.prefWidthProperty().bind(tvPatient.widthProperty().multiply(0.15));
        genderCol.prefWidthProperty().bind(tvPatient.widthProperty().multiply(0.12));
        phoneCol.prefWidthProperty().bind(tvPatient.widthProperty().multiply(0.16));
        birthCol.prefWidthProperty().bind(tvPatient.widthProperty().multiply(0.12));
        addressCol.prefWidthProperty().bind(tvPatient.widthProperty().multiply(0.26));

        // Thiết lập cách hiển thị dữ liệu cho mỗi cột
        setupTableColumns();

        // Load dữ liệu
        loadPatientData();

        tvPatient.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                PatientModel patientModel = tvPatient.getSelectionModel().getSelectedItem();
                if (patientModel != null) {
                    showPatientDetailPopUp(patientModel);
                }
            }
        });

        // Thiết lập tìm kiếm
        setupSearch();

        // Số lượng bệnh nhân
        updatePatientCount();
    }

    public void setOnOpenHistory(Runnable onOpenHistory) {
        this.onOpenHistory = onOpenHistory;
    }

    @FXML
    private void handleOpenHistory() {
        if (onOpenHistory != null) {
            onOpenHistory.run();
        } else {
            showAlert("Không thể mở", "Chức năng tra cứu lịch sử khám chưa được nối với điều hướng.");
        }
    }

    private void showPatientDetailPopUp(PatientModel patientModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/patient_detail_dialog.fxml"));
            Parent root = loader.load();

            // Lấy controller để truyền dữ liệu
            PatientDetailDialogController controller = loader.getController();
            controller.setPatient(patientModel);

            // Tạo stage mới (window mới)
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chi tiết bệnh nhân");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));

            // SET DIALOG STAGE CHO CONTROLLER
            controller.setDialogStage(dialogStage);
            controller.setDataChangeListener(this);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
                                    
    private void setupTableColumns() {
        // Thiết lập cách hiển thị dữ liệu cho từng cột
        idCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMaBenhNhan()));

        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHoTen()));

        phoneCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSoDienThoai()));

        birthCol.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getNgaySinh() == null
                            ? ""
                            : cellData.getValue().getNgaySinh().format(formatter));
        });

        genderCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGioiTinh()));

        addressCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDiaChi() == null ? "" : cellData.getValue().getDiaChi()));
    }

    @Override
    public void onDataChanged(PatientModel updatedPatient, String action) {
        ObservableList<TableColumn<PatientModel, ?>> sortOrder = FXCollections.observableArrayList(tvPatient.getSortOrder());
        int selectedIndex = tvPatient.getSelectionModel().getSelectedIndex();
        if ("INSERT".equals(action)) {
            patientList.add(updatedPatient);
        } else if ("UPDATE".equals(action)) {
            for (int i = 0; i < patientList.size(); i++) {
                if (patientList.get(i).getMaBenhNhan().equals(updatedPatient.getMaBenhNhan())) {
                    patientList.set(i, updatedPatient);
                    break;
                }
            }
        } else if ("DELETE".equals(action)) {
            patientList.removeIf(p -> p.getMaBenhNhan().equals(updatedPatient.getMaBenhNhan()));
        }

        updatePatientCount();
        tvPatient.getSortOrder().setAll(sortOrder);
        if (selectedIndex >= 0 && selectedIndex < tvPatient.getItems().size()) {
            tvPatient.getSelectionModel().select(selectedIndex);
            tvPatient.scrollTo(selectedIndex);
        }
    }


    private void loadPatientData() {
        List<PatientModel> list = null;
        try {
            list = PatientDAO.getAll();
        } catch (Exception e) {
            System.err.println("Không thể nạp dữ liệu bệnh nhân từ DB: " + e.getMessage());
        }

        if (list == null || list.isEmpty()) {
            list = List.of(
                    new PatientModel("BN001", "Nguyễn Văn H", java.time.LocalDate.of(1985, 5, 15), "09798001234", "Nam", "123 Nguyễn Văn Cừ, Hà Nội"),
                    new PatientModel("BN002", "Trần Thị I", java.time.LocalDate.of(1990, 8, 20), "09090002345", "Nữ", "45 Lê Lợi, Đà Nẵng"),
                    new PatientModel("BN003", "Lê Văn J", java.time.LocalDate.of(1992, 3, 10), "09090003456", "Nam", "78 Phạm Văn Đồng, Hà Nội"),
                    new PatientModel("BN004", "Phạm Thị K", java.time.LocalDate.of(1991, 11, 25), "0912345678", "Nữ", "32 Trần Hưng Đạo, TP.HCM"),
                    new PatientModel("BN005", "Hoàng Văn L", java.time.LocalDate.of(1988, 2, 14), "0912345679", "Nam", "56 Nguyễn Huệ, Hà Nội"),
                    new PatientModel("BN006", "Vũ Thị M", java.time.LocalDate.of(1993, 1, 9), "0923456780", "Nữ", "12 Lý Thường Kiệt, Đà Nẵng"),
                    new PatientModel("BN007", "Đặng Văn N", java.time.LocalDate.of(1990, 4, 18), "0923456781", "Nam", "89 Bà Triệu, Hà Nội"),
                    new PatientModel("BN008", "Bùi Thị O", java.time.LocalDate.of(1994, 6, 12), "0934567892", "Nữ", "29 Hai Bà Trưng, TP.HCM"),
                    new PatientModel("BN009", "Ngô Văn P", java.time.LocalDate.of(1987, 9, 7), "0934567893", "Nam", "66 Trần Phú, Đà Nẵng"),
                    new PatientModel("BN010", "Đỗ Thị Q", java.time.LocalDate.of(1995, 10, 3), "0978901234", "Nữ", "77 Nguyễn Văn Cừ, Hà Nội"),
                    new PatientModel("BN011", "Lê Văn R", java.time.LocalDate.of(1991, 12, 21), "0978901235", "Nam", "88 Lê Thánh Tông, TP.HCM"),
                    new PatientModel("BN012", "Cao Thị S", java.time.LocalDate.of(1993, 2, 17), "0989012345", "Nữ", "11 Trần Duy Hưng, Hà Nội"),
                    new PatientModel("BN013", "Trương Văn T", java.time.LocalDate.of(1989, 7, 30), "0990123456", "Nam", "23 Nguyễn Khuyến, Đà Nẵng"),
                    new PatientModel("BN014", "Lý Thị U", java.time.LocalDate.of(1992, 3, 8), "0901234567", "Nữ", "33 Phan Đình Phùng, Hà Nội"),
                    new PatientModel("BN015", "Mai Văn V", java.time.LocalDate.of(1990, 9, 15), "0912345670", "Nam", "44 Võ Văn Tần, TP.HCM")
            );
        }

        patientList = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(patientList, p -> true);

        SortedList<PatientModel> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tvPatient.comparatorProperty());

        tvPatient.setItems(sortedData);
        updatePatientCount();
    }

    private void setupSearch() {
        // Lắng nghe thay đổi trong ô tìm kiếm
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(patient -> {
                // Nếu filter text rỗng, hiển thị tất cả
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // So sánh tên, mã bệnh nhân, số điện thoại
                String lowerCaseFilter = newValue.toLowerCase();

                if (patient.getHoTen().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (patient.getMaBenhNhan().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (patient.getSoDienThoai().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (patient.getDiaChi() != null
                        && patient.getDiaChi().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Không khớp
            });
        });
    }

    private void updatePatientCount() {
        lblTotalPatients.setText("Tổng số bệnh nhân (" + patientList.size() + ")");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method để refresh dữ liệu sau khi thêm/sửa/xóa
    public void refreshData() {
        loadPatientData();
    }
}
