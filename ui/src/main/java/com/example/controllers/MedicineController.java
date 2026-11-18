package com.example.controllers;

import com.example.model.MedicineModel;
import com.example.DAO.MedicineDAO;
import javafx.beans.property.SimpleStringProperty;
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

public class MedicineController implements MedicineDataChangeListener {
    @FXML
    private TableView<MedicineModel> tvMedicine;
    @FXML
    private TableColumn<MedicineModel, String> idCol;
    @FXML
    private TableColumn<MedicineModel, String> nameCol;
    @FXML
    private TableColumn<MedicineModel, String> useCol;
    @FXML
    private TableColumn<MedicineModel, String> quantityCol;
    @FXML
    private TableColumn<MedicineModel, String> costCol;
    @FXML private TableColumn<MedicineModel, String> unitCol;
    @FXML private TableColumn<MedicineModel, String> statusCol;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML
    private TextField tfSearch;
    @FXML
    private Label lblTotalMedicines;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnViewDetails;
    @FXML private Button btnEdit;
    @FXML private Button btnStop;

    private ObservableList<MedicineModel> medicineList = FXCollections.observableArrayList();
    private FilteredList<MedicineModel> filteredMedicines;
    private SortedList<MedicineModel> sortedMedicines;

    @FXML
    public void initialize() {
        idCol.prefWidthProperty().bind(tvMedicine.widthProperty().multiply(0.1));
        nameCol.prefWidthProperty().bind(tvMedicine.widthProperty().multiply(0.19));
        unitCol.prefWidthProperty().bind(tvMedicine.widthProperty().multiply(0.09));
        costCol.prefWidthProperty().bind(tvMedicine.widthProperty().multiply(0.1));
        quantityCol.prefWidthProperty().bind(tvMedicine.widthProperty().multiply(0.11));
        useCol.prefWidthProperty().bind(tvMedicine.widthProperty().multiply(0.26));
        statusCol.prefWidthProperty().bind(tvMedicine.widthProperty().multiply(0.15));

        cbStatusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang sử dụng", "Ngưng sử dụng"
        ));
        cbStatusFilter.setValue("Tất cả");
        tfSearch.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        cbStatusFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());

        btnViewDetails.setDisable(true);
        btnEdit.setDisable(true);
        btnStop.setDisable(true);
        tvMedicine.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldMedicine, selectedMedicine) -> updateSelectionActions(selectedMedicine)
        );

        tvMedicine.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                MedicineModel medicineModel = tvMedicine.getSelectionModel().getSelectedItem();
                if (medicineModel != null) {
                    showMedicineDetailPopUp(medicineModel, true);
                }
            }
        });

        loadMedicineData();
    }

    @FXML
    private void handleAddMedicine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/medicine_detail.fxml"));
            Parent root = loader.load();
            MedicineDetailController controller = loader.getController();
            controller.setDataChangeListener(this);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm thuốc");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (btnAdd.getScene() != null && btnAdd.getScene().getWindow() != null) {
                dialogStage.initOwner(btnAdd.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ thêm thuốc: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewDetails() {
        MedicineModel medicine = tvMedicine.getSelectionModel().getSelectedItem();
        if (medicine != null) {
            showMedicineDetailPopUp(medicine, true);
        }
    }

    @FXML
    private void handleEditMedicine() {
        MedicineModel medicine = tvMedicine.getSelectionModel().getSelectedItem();
        if (medicine != null) {
            showMedicineDetailPopUp(medicine, false);
        }
    }

    @FXML
    private void handleStopMedicine() {
        MedicineModel medicine = tvMedicine.getSelectionModel().getSelectedItem();
        if (medicine == null) {
            return;
        }
        if (!"Đang sử dụng".equalsIgnoreCase(medicine.getTrangThai())) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Ngưng sử dụng thuốc");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Ngưng đưa thuốc \"" + medicine.getTenThuoc() + "\" vào đơn mới?");
        confirmation.showAndWait().filter(ButtonType.OK::equals).ifPresent(response -> {
            try {
                if (!MedicineDAO.updateMedicineStatus(medicine.getMaThuoc(), "Ngưng sử dụng")) {
                    showAlert("Lỗi", "Không tìm thấy thuốc cần cập nhật.", Alert.AlertType.ERROR);
                    return;
                }
                medicine.setTrangThai("Ngưng sử dụng");
                int index = medicineList.indexOf(medicine);
                if (index >= 0) {
                    medicineList.set(index, medicine);
                }
                applyFilter();
            } catch (Exception exception) {
                exception.printStackTrace();
                showAlert("Lỗi", "Không thể ngưng sử dụng thuốc: " + exception.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleRefresh() {
        tfSearch.clear();
        cbStatusFilter.setValue("Tất cả");
        loadMedicineData();
    }

    private void showMedicineDetailPopUp(MedicineModel medicineModel, boolean readOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/medicine_detail.fxml"));
            Parent root = loader.load();
            MedicineDetailController controller = loader.getController();
            controller.setMedicine(medicineModel);
            controller.setReadOnly(readOnly);
            controller.setDataChangeListener(this);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chi tiết thuốc");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (tvMedicine.getScene() != null && tvMedicine.getScene().getWindow() != null) {
                dialogStage.initOwner(tvMedicine.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ chi tiết thuốc: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Override
    public void onDataChanged(MedicineModel updatedMedicine, String action) {
        ObservableList<TableColumn<MedicineModel, ?>> sortOrder = FXCollections.observableArrayList(tvMedicine.getSortOrder());
        int selectedIndex = tvMedicine.getSelectionModel().getSelectedIndex();

        if ("INSERT".equals(action)) {
            medicineList.add(updatedMedicine);
        } else if ("UPDATE".equals(action)) {
            for (int i = 0; i < medicineList.size(); i++) {
                if (medicineList.get(i).getMaThuoc().equals(updatedMedicine.getMaThuoc())) {
                    medicineList.set(i, updatedMedicine);
                    break;
                }
            }
        } else if ("DELETE".equals(action)) {
            medicineList.removeIf(med -> med.getMaThuoc().equals(updatedMedicine.getMaThuoc()));
        }

        applyFilter();
        lblTotalMedicines.setText("Tổng số thuốc: " + medicineList.size());
        tvMedicine.getSortOrder().setAll(sortOrder);
        if (selectedIndex >= 0 && selectedIndex < tvMedicine.getItems().size()) {
            tvMedicine.getSelectionModel().select(selectedIndex);
            tvMedicine.scrollTo(selectedIndex);
        }
    }

    public void loadMedicineData() {
        try {
            List<MedicineModel> list = null;
            try {
                list = MedicineDAO.getAllMedicines();
            } catch (Exception e) {
                System.err.println("Không thể nạp thuốc từ DB: " + e.getMessage());
            }

            if (list == null || list.isEmpty()) {
                list = List.of(
                        new MedicineModel("TH001", "Paracetamol 500mg", "Giảm đau, hạ sốt", 500, 2000, "Viên", "Uống 1-2 viên", "Đang sử dụng"),
                        new MedicineModel("TH002", "Amlodipine 5mg", "Ổn định huyết áp", 300, 3500, "Viên", "Uống mỗi ngày", "Đang sử dụng"),
                        new MedicineModel("TH003", "Omeprazole 20mg", "Giảm tiết acid dạ dày", 200, 4000, "Viên", "Uống trước ăn", "Đang sử dụng"),
                        new MedicineModel("TH004", "Vitamin C 500mg", "Tăng sức đề kháng", 800, 1500, "Viên", "Uống buổi sáng", "Ngưng sử dụng"),
                        new MedicineModel("TH005", "Ibuprofen 400mg", "Giảm đau, kháng viêm", 750, 3000, "Viên", "Uống sau ăn", "Đang sử dụng"),
                        new MedicineModel("TH006", "Metformin 500mg", "Điều trị tiểu đường", 600, 2500, "Viên", "Uống theo chỉ định", "Đang sử dụng"),
                        new MedicineModel("TH007", "Atorvastatin 20mg", "Hạ mỡ máu", 400, 5000, "Viên", "Uống buổi tối", "Ngưng sử dụng"),
                        new MedicineModel("TH008", "Losartan 50mg", "Điều trị tăng huyết áp", 550, 4500, "Viên", "Uống mỗi ngày", "Đang sử dụng"),
                        new MedicineModel("TH009", "Theophylline 100mg", "Giãn phế quản", 300, 3500, "Viên", "Uống sau ăn", "Đang sử dụng"),
                        new MedicineModel("TH010", "Vitamin D 1000IU", "Bổ sung vitamin D", 650, 2000, "Viên", "Uống buổi sáng", "Đang sử dụng")
                );
            }

            medicineList.setAll(list);
            idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaThuoc()));
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenThuoc()));
            useCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCongDung()));
            quantityCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getSoLuong())));
            costCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f", data.getValue().getGiaTien())));
            unitCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDonVi()));
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTrangThai()));
            statusCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    setText(empty ? null : status);
                    setStyle(empty ? "" : ("Ngưng sử dụng".equalsIgnoreCase(status)
                            ? "-fx-text-fill: #C53030; -fx-font-weight: bold;"
                            : "-fx-text-fill: #2F855A; -fx-font-weight: bold;"));
                }
            });
            if (filteredMedicines == null) {
                filteredMedicines = new FilteredList<>(medicineList, medicine -> true);
                sortedMedicines = new SortedList<>(filteredMedicines);
                sortedMedicines.comparatorProperty().bind(tvMedicine.comparatorProperty());
                tvMedicine.setItems(sortedMedicines);
            }
            applyFilter();
            lblTotalMedicines.setText("Tổng số thuốc: " + medicineList.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải danh sách thuốc: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFilter() {
        if (filteredMedicines == null) {
            return;
        }
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String selectedStatus = cbStatusFilter.getValue();
        filteredMedicines.setPredicate(medicine -> {
            boolean matchesText = keyword.isEmpty()
                    || contains(medicine.getMaThuoc(), keyword)
                    || contains(medicine.getTenThuoc(), keyword)
                    || contains(medicine.getCongDung(), keyword);
            boolean matchesStatus = selectedStatus == null || "Tất cả".equals(selectedStatus)
                    || selectedStatus.equalsIgnoreCase(medicine.getTrangThai());
            return matchesText && matchesStatus;
        });
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void updateSelectionActions(MedicineModel selectedMedicine) {
        boolean selected = selectedMedicine != null;
        btnViewDetails.setDisable(!selected);
        btnEdit.setDisable(!selected);
        btnStop.setDisable(!selected
                || !"Đang sử dụng".equalsIgnoreCase(selectedMedicine.getTrangThai()));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}