package com.example.controllers;

import com.example.DAO.QuiDinhDAO;
import com.example.model.AppointmentModel;
import com.example.model.MedicalReportModel;
import com.example.model.StaffModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import com.example.DAO.MedicalReportDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import com.example.DAO.HenKhamBenhDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentListController {
    @FXML
    TextField tfSearch;
    @FXML
    DatePicker dpDate;
    @FXML
    private TableView<MedicalReportModel> tvAppointment;
    @FXML
    private TableColumn<MedicalReportModel, String> reasonCol;
    @FXML
    private TableColumn<MedicalReportModel, String> diagnoseCol;
    @FXML
    private TableColumn<MedicalReportModel, String> nameCol;
    @FXML
    private TableColumn<MedicalReportModel, String> resultCol;
    @FXML
    private TableColumn<MedicalReportModel, String> treatCol;
    @FXML
    private TableColumn<MedicalReportModel, String> costCol;

    private ObservableList<MedicalReportModel> allAppointments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Thiết lập độ rộng cột
        reasonCol.prefWidthProperty().bind(tvAppointment.widthProperty().multiply(0.18));
        nameCol.prefWidthProperty().bind(tvAppointment.widthProperty().multiply(0.18));
        resultCol.prefWidthProperty().bind(tvAppointment.widthProperty().multiply(0.14));
        treatCol.prefWidthProperty().bind(tvAppointment.widthProperty().multiply(0.18));
        costCol.prefWidthProperty().bind(tvAppointment.widthProperty().multiply(0.14));
        diagnoseCol.prefWidthProperty().bind(tvAppointment.widthProperty().multiply(0.18));

        // Gán cell value factory cho các cột
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        reasonCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLyDoKham()));
        resultCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKetQuaKham()));
        diagnoseCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getChanDoan()));
        treatCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDieuTri()));
        costCol.setCellValueFactory(cellData -> {
            BigDecimal tien = QuiDinhDAO.getGiaTri("DEFAULT_TIEN_KHAM");

            if (tien == null) tien = BigDecimal.ZERO;
            return new SimpleStringProperty(String.format("%,.0f", tien));
        });

        // Thiết lập datepicker
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpDate.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });

        LocalDateTime dateTime = LocalDateTime.now();
        dpDate.setValue(dateTime.toLocalDate());

        // Load dữ liệu lần đầu
        loadAppointmentsByDate(dpDate.getValue());

        // Khi đổi ngày thì load lại dữ liệu
        dpDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                loadAppointmentsByDate(newDate);
            }
        });

        // Lắng nghe thay đổi ô tìm kiếm
        tfSearch.textProperty().addListener((obs, oldText, newText) -> {
            filterAppointmentsByName(newText);
        });

        tvAppointment.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                MedicalReportModel medicalReportModel = tvAppointment.getSelectionModel().getSelectedItem();
                if (medicalReportModel != null) {
                    showMedicalReportPopUp(medicalReportModel);
                }
            }
        });
    }

    private void loadAppointmentsByDate(LocalDate date) {
        // Lấy danh sách các mã khám bệnh đúng ngày
        List<AppointmentModel> appointments = HenKhamBenhDAO.getAll();
        List<String> maKhamBenhList = new ArrayList<>();
        for (AppointmentModel ap : appointments) {
            if (ap.getNgayKham() != null && ap.getNgayKham().isEqual(date)) {
                maKhamBenhList.add(ap.getMaKhamBenh());
            }
        }
        List<MedicalReportModel> reports = new ArrayList<>();
        for (String maKhamBenh : maKhamBenhList) {
            MedicalReportModel report = MedicalReportDAO.getCompleteMedicalReportByMaKhamBenh(maKhamBenh);
            if (report != null) {
                reports.add(report);
            }
        }
        allAppointments.setAll(reports);
        filterAppointmentsByName(tfSearch.getText());
    }

    private void filterAppointmentsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tvAppointment.setItems(allAppointments);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            ObservableList<MedicalReportModel> filtered = allAppointments.filtered(
                report -> report.getHoTen() != null && report.getHoTen().toLowerCase().contains(lowerKeyword)
            );
            tvAppointment.setItems(filtered);
        }
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
            dialogStage.setScene(new Scene(root, 800, 600)); // Chỉ gọi 1 lần

            dialogStage.setResizable(false); // Không cho resize
            dialogStage.initModality(Modality.APPLICATION_MODAL); // chặn tương tác với window chính
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
