package com.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExamHistoryController {
    private record ExamHistoryEntry(
            String stt,
            String hoTen,
            LocalDate ngayKham,
            String loaiBenh,
            String trieuChung
    ) {}

    @FXML private TextField tfHoTen;
    @FXML private DatePicker dpNgayKham;
    @FXML private ComboBox<String> cbLoaiBenh;
    @FXML private TextField tfTrieuChung;
    @FXML private TableView<ExamHistoryEntry> tvHistory;
    @FXML private TableColumn<ExamHistoryEntry, String> colSTT;
    @FXML private TableColumn<ExamHistoryEntry, String> colHoTen;
    @FXML private TableColumn<ExamHistoryEntry, String> colNgayKham;
    @FXML private TableColumn<ExamHistoryEntry, String> colLoaiBenh;
    @FXML private TableColumn<ExamHistoryEntry, String> colTrieuChung;
    @FXML private Label lblCount;
    @FXML private Button btnViewReport;

    private final ObservableList<ExamHistoryEntry> history =
            FXCollections.observableArrayList(
                    new ExamHistoryEntry(
                            "1",
                            "Bệnh nhân mẫu 01",
                            LocalDate.of(2026, 11, 10),
                            "Loại bệnh 1",
                            "Triệu chứng minh họa"
                    )
            );
    private FilteredList<ExamHistoryEntry> filteredHistory;

    @FXML
    private void initialize() {
        tvHistory.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpNgayKham.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : formatter.format(date);
            }

            @Override
            public LocalDate fromString(String value) {
                return value == null || value.isBlank()
                        ? null
                        : LocalDate.parse(value, formatter);
            }
        });

        cbLoaiBenh.setItems(FXCollections.observableArrayList(
                "Tất cả", "Loại bệnh 1", "Loại bệnh 2"
        ));
        cbLoaiBenh.setValue("Tất cả");

        colSTT.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().stt()));
        colHoTen.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().hoTen()));
        colNgayKham.setCellValueFactory(row -> new ReadOnlyStringWrapper(
                row.getValue().ngayKham().format(formatter)
        ));
        colLoaiBenh.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().loaiBenh()));
        colTrieuChung.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().trieuChung()));

        filteredHistory = new FilteredList<>(history, row -> true);
        tvHistory.setItems(filteredHistory);
        btnViewReport.setDisable(false);
        tvHistory.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleViewReport();
            }
        });

        tvHistory.getSelectionModel().selectFirst();
        updateCount();
    }

    @FXML
    private void handleSearch() {
        String name = normalize(tfHoTen.getText());
        String symptoms = normalize(tfTrieuChung.getText());
        LocalDate date = dpNgayKham.getValue();
        String disease = cbLoaiBenh.getValue();

        filteredHistory.setPredicate(row ->
                (name.isEmpty() || normalize(row.hoTen()).contains(name))
                        && (date == null || date.equals(row.ngayKham()))
                        && (disease == null || "Tất cả".equals(disease)
                                || disease.equals(row.loaiBenh()))
                        && (symptoms.isEmpty() || normalize(row.trieuChung()).contains(symptoms))
        );
        tvHistory.getSelectionModel().selectFirst();
        updateCount();
    }

    @FXML
    private void handleReset() {
        tfHoTen.clear();
        dpNgayKham.setValue(null);
        cbLoaiBenh.setValue("Tất cả");
        tfTrieuChung.clear();
        filteredHistory.setPredicate(row -> true);
        tvHistory.getSelectionModel().selectFirst();
        updateCount();
    }

    @FXML
    private void handleViewReport() {
        ExamHistoryEntry selectedEntry = tvHistory.getSelectionModel().getSelectedItem();
        if (selectedEntry == null && !filteredHistory.isEmpty()) {
            selectedEntry = filteredHistory.get(0);
        }
        if (selectedEntry == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/medical_report.fxml")
            );
            Parent reportView = loader.load();
            MedicalReportController controller = loader.getController();
            controller.loadDemoReport();

            Stage dialog = new Stage();
            dialog.setTitle("Phiếu khám bệnh");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tvHistory.getScene() != null && tvHistory.getScene().getWindow() != null) {
                dialog.initOwner(tvHistory.getScene().getWindow());
            }
            dialog.setScene(new Scene(reportView, 900, 720));
            dialog.showAndWait();
        } catch (IOException exception) {
            exception.printStackTrace();
            showError("Không thể mở phiếu khám bệnh: " + exception.getMessage());
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void updateCount() {
        lblCount.setText("Tổng số lịch sử: " + filteredHistory.size());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
