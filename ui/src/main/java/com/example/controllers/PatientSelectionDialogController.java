package com.example.controllers;

import com.example.model.PatientModel;
import com.example.DAO.PatientDAO; // đảm bảo bạn có lớp DAO này

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientSelectionDialogController {

    @FXML private TextField txtSearch;
    @FXML private TableView<PatientModel> tableBenhNhan;
    @FXML private TableColumn<PatientModel, String> colHoTen;
    @FXML private TableColumn<PatientModel, String> colSoDienThoai;
    @FXML private TableColumn<PatientModel, String> colNgaySinh;
    @FXML private TableColumn<PatientModel, String> colGioiTinh;
    @FXML private Button btnChon;
    @FXML private Button btnHuy;

    private PatientModel selectedBenhNhan;

    public PatientModel getSelectedBenhNhan() {
        return selectedBenhNhan;
    }

    @FXML
    private void initialize() {
        // Thiết lập cell value factory với SimpleStringProperty
        colHoTen.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHoTen()));
        colSoDienThoai.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSoDienThoai()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colNgaySinh.setCellValueFactory(data -> {
            String formatted = data.getValue().getNgaySinh() != null
                    ? data.getValue().getNgaySinh().format(formatter)
                    : "";
            return new SimpleStringProperty(formatted);
        });

        colGioiTinh.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGioiTinh()));

        // Lấy danh sách bệnh nhân từ DAO
        List<PatientModel> list = PatientDAO.getAll();
        FilteredList<PatientModel> filteredList = new FilteredList<>(FXCollections.observableArrayList(list), p -> true);
        tableBenhNhan.setItems(filteredList);

        // Tìm kiếm theo tên, sdt, mã
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredList.setPredicate(bn ->
                    bn.getHoTen().toLowerCase().contains(lower) ||
                            bn.getSoDienThoai().toLowerCase().contains(lower) ||
                            bn.getMaBenhNhan().toLowerCase().contains(lower)
            );
        });

        // Chọn bệnh nhân
        btnChon.setOnAction(e -> {
            selectedBenhNhan = tableBenhNhan.getSelectionModel().getSelectedItem();
            closeStage();
        });

        // Hủy chọn
        btnHuy.setOnAction(e -> {
            selectedBenhNhan = null;
            closeStage();
        });
    }

    private void closeStage() {
        Stage stage = (Stage) txtSearch.getScene().getWindow();
        stage.close();
    }
}
