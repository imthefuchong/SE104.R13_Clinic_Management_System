package com.example.controllers;

import com.example.DAO.MedicalReportDAO;
import com.example.DAO.PatientDAO;
import com.example.model.BillModel;
import com.example.model.MedicalReportModel;
import com.example.model.PatientModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PatientDetailDialogController {
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfId;
    @FXML
    private TextField tfPhone;
    @FXML
    private TextField tfAddress;
    @FXML
    private ToggleButton btnMale;
    @FXML
    private ToggleButton btnFemale;
    @FXML
    private DatePicker dpBirth;
    /*Cách in ra ngày theo định dạng dd/mm/yyyy nè
      LocalDate date = dpBirth.getValue();
      String format = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));*/
    @FXML
    private TableView<MedicalReportModel> tvKhamBenh;
    @FXML
    private TableColumn<MedicalReportModel, String> dateCol;
    @FXML
    private TableColumn<MedicalReportModel, String> resultCol;
    @FXML
    private TableColumn<MedicalReportModel, String> costCol;
    @FXML
    private TableColumn<MedicalReportModel, String> reasonCol;
    @FXML
    private TableColumn<MedicalReportModel, String> doctorCol;
    private PatientDataChangeListener dataChangeListener;
    private PatientModel currentPatient;
    private Stage dialogStage;
    private Runnable onDataChanged;
    private ObservableList<MedicalReportModel> MedicalReportList;
    private SortedList<MedicalReportModel> MedicalReportData;

    public void setDataChangeListener(PatientDataChangeListener listener) {
        this.dataChangeListener = listener;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    public void initialize() {
        // code của toggle button
        ToggleGroup genderGroup = new ToggleGroup();
        btnMale.setToggleGroup(genderGroup);
        btnFemale.setToggleGroup(genderGroup);

        btnMale.setSelected(true);
        
        // code của date picker
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        dpBirth.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });
        
        // code của table view
        dateCol.prefWidthProperty().bind(tvKhamBenh.widthProperty().multiply(0.2));
        resultCol.prefWidthProperty().bind(tvKhamBenh.widthProperty().multiply(0.2));
        reasonCol.prefWidthProperty().bind(tvKhamBenh.widthProperty().multiply(0.2));
        costCol.prefWidthProperty().bind(tvKhamBenh.widthProperty().multiply(0.2));
        doctorCol.prefWidthProperty().bind(tvKhamBenh.widthProperty().multiply(0.2));

        tvKhamBenh.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                MedicalReportModel medicalReportModel = tvKhamBenh.getSelectionModel().getSelectedItem();
                if (medicalReportModel != null) {
                    showMedicalReportPopUp(medicalReportModel);
                }
            }
        });

    }

    private void loadMedicalReport() {
        try {
            // Lấy dữ liệu từ database
            MedicalReportList = FXCollections.observableArrayList(
                    MedicalReportDAO.getMedicalReportsByPatientId(currentPatient.getMaBenhNhan())
            );
            MedicalReportData = new SortedList<>(MedicalReportList);
            MedicalReportData.comparatorProperty().bind(tvKhamBenh.comparatorProperty());

            // Thiết lập cell value factory cho các cột
            setupTableColumns();

            // Gán dữ liệu vào TableView
            tvKhamBenh.setItems(MedicalReportData);

        } catch (Exception e) {
            System.err.println("Lỗi khi load dữ liệu bệnh nhân: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu bệnh nhân: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        // Thiết lập CellValueFactory cho từng cột dựa trên tên cột

        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime ngay = cellData.getValue().getNgayLap();
            String formatted = ngay != null
                    ? ngay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "";
            return new SimpleStringProperty(formatted);
        });

        // Cột kết quả/chẩn đoán (resultCol)
        resultCol.setCellValueFactory(new PropertyValueFactory<>("chanDoan"));

        // Cột điều trị/lý do khám (treatCol)
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("lyDoKham"));

        // Cột chi phí (costCol) - từ hoaDon
        costCol.setCellValueFactory(cellData -> {
            BillModel hoaDon = cellData.getValue().getHoaDon();
            if (hoaDon != null) {
                // Giả sử BillModel có method getTongTien()
                return new SimpleStringProperty(String.format("%.0f VNĐ", hoaDon.getTongTien()));
            }
            return new SimpleStringProperty("Chưa có");
        });

        // Cột bác sĩ (doctorCol)
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("tenBacSi"));

    }


    private void showMedicalReportPopUp(MedicalReportModel medicalReportModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/medical_report.fxml"));
            Parent root = loader.load();

            // Lấy controller để truyền dữ liệu
            MedicalReportController controller = loader.getController();
            
            // Sử dụng phương thức mới để load dữ liệu từ database
            controller.loadMedicalReportByMaKhamBenh(medicalReportModel.getMaKhamBenh());

            // Tạo stage mới (window mới)
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Phiếu khám bệnh");
            dialogStage.setScene(new Scene(root, 800, 600)); // Set kích thước cửa sổ

            dialogStage.setResizable(false); // Không cho resize
            dialogStage.initModality(Modality.APPLICATION_MODAL); // chặn tương tác với window chính
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPatient(PatientModel patient) {
        if (patient != null) {
            this.currentPatient = patient;
            tfName.setText(patient.getHoTen());
            tfId.setText(patient.getMaBenhNhan());
            tfPhone.setText(patient.getSoDienThoai());
            tfAddress.setText(patient.getDiaChi());

            String gioitinh = patient.getGioiTinh();
            if ("Nam".equalsIgnoreCase(gioitinh))
                btnMale.setSelected(true);
            else
                btnFemale.setSelected(true);

            LocalDate date = patient.getNgaySinh();
            dpBirth.setValue(date);
            loadMedicalReport();
        }
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    @FXML
    public void save(ActionEvent actionEvent) {
        try {
            // Validate dữ liệu đầu vào
            if (!validateInput()) {
                return;
            }

            // Lấy dữ liệu từ UI
            String hoTen = tfName.getText().trim();
            String soDienThoai = tfPhone.getText().trim();
            String diaChi = tfAddress.getText().trim();
            LocalDate ngaySinh = dpBirth.getValue();
            String gioiTinh = btnMale.isSelected() ? "Nam" : "Nữ";

            // Cập nhật thông tin vào model hiện tại
            currentPatient.setHoTen(hoTen);
            currentPatient.setSoDienThoai(soDienThoai);
            currentPatient.setDiaChi(diaChi);
            currentPatient.setNgaySinh(ngaySinh);
            currentPatient.setGioiTinh(gioiTinh);

            // Gọi DAO để update database
            boolean success = PatientDAO.update(currentPatient);

            if (success) {
                // Hiển thị thông báo thành công
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Cập nhật thông tin bệnh nhân thành công!");

                // THÔNG BÁO CHO PatientController ĐỂ REFRESH DỮ LIỆU
                if (dataChangeListener != null) {
                    dataChangeListener.onDataChanged(currentPatient, "UPDATE");
                }

                // Đóng dialog
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } else {
                // Hiển thị thông báo lỗi
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Không thể cập nhật thông tin bệnh nhân. Vui lòng thử lại!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi",
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        // Kiểm tra họ tên
        if (tfName.getText() == null || tfName.getText().trim().isEmpty()) {
            errorMessage.append("Vui lòng nhập họ tên!\n");
        }

        // Kiểm tra số điện thoại
        if (tfPhone.getText() == null || tfPhone.getText().trim().isEmpty()) {
            errorMessage.append("Vui lòng nhập số điện thoại!\n");
        } else if (!tfPhone.getText().trim().matches("\\d{10,11}")) {
            errorMessage.append("Số điện thoại phải có 10-11 chữ số!\n");
        }

        if (tfAddress.getText() == null || tfAddress.getText().trim().isEmpty()) {
            errorMessage.append("Vui lòng nhập địa chỉ!\n");
        } else if (tfAddress.getText().trim().length() > 100) {
            errorMessage.append("Địa chỉ không được vượt quá 100 ký tự!\n");
        }

        // Kiểm tra ngày sinh
        if (dpBirth.getValue() == null) {
            errorMessage.append("Vui lòng chọn ngày sinh!\n");
        } else if (dpBirth.getValue().isAfter(LocalDate.now())) {
            errorMessage.append("Ngày sinh không thể là tương lai!\n");
        }

        // Kiểm tra giới tính
        if (!btnMale.isSelected() && !btnFemale.isSelected()) {
            errorMessage.append("Vui lòng chọn giới tính!\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ",
                    errorMessage.toString());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
