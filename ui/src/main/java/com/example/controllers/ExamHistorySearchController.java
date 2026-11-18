package com.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExamHistorySearchController {

    public record ExamSearchItem(
            String stt,
            String maBN,
            String hoTen,
            String gioiTinh,
            String namSinh,
            String diaChi,
            String trangThai
    ) {}

    @FXML private DatePicker dpNgayKham;
    @FXML private TextField tfHoTen;
    @FXML private ComboBox<String> cbLoaiBenh;
    @FXML private TextField tfTrieuChung;
    @FXML private Button btnSearch;
    @FXML private Button btnReset;

    @FXML private TableView<ExamSearchItem> tvResults;
    @FXML private TableColumn<ExamSearchItem, String> colSTT;
    @FXML private TableColumn<ExamSearchItem, String> colMaBN;
    @FXML private TableColumn<ExamSearchItem, String> colHoTen;
    @FXML private TableColumn<ExamSearchItem, String> colGioiTinh;
    @FXML private TableColumn<ExamSearchItem, String> colNamSinh;
    @FXML private TableColumn<ExamSearchItem, String> colDiaChi;
    @FXML private TableColumn<ExamSearchItem, String> colTrangThai;

    @FXML private Label lblSoKetQua;
    @FXML private Button btnBack;

    private final ObservableList<ExamSearchItem> masterData = FXCollections.observableArrayList();
    private FilteredList<ExamSearchItem> filteredData;

    @FXML
    private void initialize() {
        // Khởi tạo ngày hiện tại
        dpNgayKham.setValue(LocalDate.now());

        cbLoaiBenh.getItems().addAll(
                "Tất cả",
                "Loại bệnh 1",
                "Loại bệnh 2",
                "Loại bệnh 3"
        );
        cbLoaiBenh.setValue("Tất cả");

        // Cấu hình các cột
        colSTT.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().stt()));
        colMaBN.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().maBN()));
        colHoTen.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().hoTen()));
        colGioiTinh.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().gioiTinh()));
        colNamSinh.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().namSinh()));
        colDiaChi.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().diaChi()));
        colTrangThai.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().trangThai()));

        // Badge màu sắc cho cột trạng thái
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            private final Circle dot = new Circle(4);
            private final Label label = new Label();
            private final HBox box = new HBox(6, dot, label);

            {
                box.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                label.setText(item);
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

                if ("Đã khám".equalsIgnoreCase(item.trim())) {
                    dot.setFill(Color.web("#38A169"));
                    label.setTextFill(Color.web("#22543D"));
                    box.setStyle("-fx-background-color: #C6F6D5; -fx-background-radius: 12; -fx-padding: 3 10;");
                } else {
                    dot.setFill(Color.web("#DD6B20"));
                    label.setTextFill(Color.web("#7B341E"));
                    box.setStyle("-fx-background-color: #FEEBC8; -fx-background-radius: 12; -fx-padding: 3 10;");
                }

                setGraphic(box);
                setText(null);
            }
        });

        // Nạp dữ liệu mẫu
        loadInitialData();

        filteredData = new FilteredList<>(masterData, p -> true);
        tvResults.setItems(filteredData);
        updateCountLabel();
    }

    private void loadInitialData() {
        masterData.clear();
        masterData.addAll(
                new ExamSearchItem("1", "BN001", "Nguyễn Văn An", "Nam", "1990", "Hà Nội", "Đã khám"),
                new ExamSearchItem("2", "BN002", "Trần Thị Bình", "Nữ", "1985", "Đà Nẵng", "Đã khám"),
                new ExamSearchItem("3", "BN003", "Lê Văn Cường", "Nam", "2000", "TP. Hồ Chí Minh", "Đang chờ"),
                new ExamSearchItem("4", "BN004", "Phạm Thị Dung", "Nữ", "1995", "Cần Thơ", "Đang chờ"),
                new ExamSearchItem("5", "BN005", "Hoàng Văn Em", "Nam", "1988", "Hải Phòng", "Đã khám"),
                new ExamSearchItem("6", "BN006", "Vũ Thị Giang", "Nữ", "1992", "Huế", "Đang chờ"),
                new ExamSearchItem("7", "BN007", "Đặng Văn Hải", "Nam", "1998", "Nghệ An", "Đã khám")
        );
    }

    @FXML
    public void handleSearch() {
        String keyword = tfHoTen.getText() == null ? "" : tfHoTen.getText().trim().toLowerCase();
        String selectedDisease = cbLoaiBenh.getValue();
        String symptom = tfTrieuChung.getText() == null ? "" : tfTrieuChung.getText().trim().toLowerCase();

        filteredData.setPredicate(item -> {
            boolean matchName = keyword.isEmpty()
                    || item.hoTen().toLowerCase().contains(keyword)
                    || item.maBN().toLowerCase().contains(keyword);

            boolean matchDisease = selectedDisease == null
                    || "Tất cả".equalsIgnoreCase(selectedDisease)
                    || item.diaChi().toLowerCase().contains(selectedDisease.toLowerCase());
            boolean matchSymptom = symptom.isEmpty()
                    || item.trangThai().toLowerCase().contains(symptom);

            return matchName && matchDisease && matchSymptom;
        });

        updateCountLabel();
    }

    @FXML
    public void handleReset() {
        dpNgayKham.setValue(LocalDate.now());
        tfHoTen.clear();
        cbLoaiBenh.setValue("Tất cả");
        tfTrieuChung.clear();
        filteredData.setPredicate(p -> true);
        updateCountLabel();
    }

    @FXML
    public void handleBack() {
        // Tùy chọn quay lại màn hình trước
        handleReset();
    }

    private void updateCountLabel() {
        int count = filteredData != null ? filteredData.size() : masterData.size();
        lblSoKetQua.setText("Số kết quả: " + count);
    }
}
