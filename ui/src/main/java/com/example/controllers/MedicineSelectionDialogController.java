package com.example.controllers;

import com.example.DAO.MedicineDAO;
import com.example.model.MedicineModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class MedicineSelectionDialogController {

    @FXML private TextField tfSearch;
    @FXML private TableView<MedicineModel> tableMedicine;
    @FXML private TableColumn<MedicineModel, String> colTenThuoc;
    @FXML private TableColumn<MedicineModel, Integer> colSoLuong;
    @FXML private TableColumn<MedicineModel, Double> colGiaTien;
    @FXML private TableColumn<MedicineModel, String> colDonVi;

    @FXML private TextField tfSelectedTenThuoc, tfSelectedDonVi, tfSelectedSoLuongKho, tfSelectedGiaTien;
    @FXML private Spinner<Integer> spSelectedSoLuong;
    @FXML private ComboBox<String> cbSelectedHuongDan;
    @FXML private Label lblValidation;

    private ObservableList<MedicineModel> allMedicines = FXCollections.observableArrayList();
    private MedicineModel selectedMedicine;
    private Consumer<MedicineModel> onMedicineSelected;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAllMedicines();
        spSelectedSoLuong.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1)
        );
        spSelectedSoLuong.setEditable(true);
        setupTableSelection();
        if (!allMedicines.isEmpty()) {
            tableMedicine.getSelectionModel().selectFirst();
        }
        setupSearchListener();
    }

    private void setupTableColumns() {
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colGiaTien.setCellValueFactory(new PropertyValueFactory<>("giaTien"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));

        // Format giá tiền
        colGiaTien.setCellFactory(column -> new TableCell<MedicineModel, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item));
                }
            }
        });
    }

    private void loadAllMedicines() {
        List<MedicineModel> medicines = null;
        try {
            medicines = MedicineDAO.getAllMedicines();
        } catch (Exception e) {
            System.err.println("DB error loading medicines: " + e.getMessage());
        }
        if (medicines == null || medicines.isEmpty()) {
            medicines = List.of(
                    new MedicineModel("T01", "Paracetamol 500mg", "Giảm đau, hạ sốt", 100, 2000.0, "Viên", "Uống 1-2 viên khi đau, tối đa 6 viên/ngày"),
                    new MedicineModel("T02", "Omeprazole 20mg", "Đau dạ dày", 50, 5000.0, "Viên", "Uống trước ăn 30 phút"),
                    new MedicineModel("T03", "Amoxicillin 500mg", "Kháng sinh nhiễm khuẩn", 80, 8000.0, "Viên", "Uống sau ăn, ngày 2 lần, mỗi lần 1 viên")
            );
        }
        allMedicines.setAll(medicines.stream()
                .filter(medicine -> !"Ngưng sử dụng".equalsIgnoreCase(medicine.getTrangThai()))
                .toList());
        tableMedicine.setItems(allMedicines);
    }

    private void setupTableSelection() {
        tableMedicine.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedMedicine = newSelection;
            if (selectedMedicine != null) {
                updateSelectedMedicineInfo();
            } else {
                clearSelectedMedicineInfo();
            }
        });
    }

    private void setupSearchListener() {
        tfSearch.textProperty().addListener((obs, oldText, newText) -> {
            filterMedicines(newText);
        });
    }

    private void filterMedicines(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tableMedicine.setItems(allMedicines);
        } else {
            String lowerKeyword = keyword.trim().toLowerCase();
            ObservableList<MedicineModel> filtered = allMedicines.filtered(medicine ->
                (medicine.getTenThuoc() != null && medicine.getTenThuoc().toLowerCase().contains(lowerKeyword)) ||
                (medicine.getMaThuoc() != null && medicine.getMaThuoc().toLowerCase().contains(lowerKeyword))
            );
            tableMedicine.setItems(filtered);
        }
        tableMedicine.getSelectionModel().selectFirst();
    }

    private void updateSelectedMedicineInfo() {
        tfSelectedTenThuoc.setText(selectedMedicine.getTenThuoc());
        tfSelectedDonVi.setText(selectedMedicine.getDonVi());
        tfSelectedSoLuongKho.setText(String.valueOf(selectedMedicine.getSoLuong()));
        tfSelectedGiaTien.setText(String.format("%,.0f", selectedMedicine.getGiaTien()));
        spSelectedSoLuong.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, Math.max(1, selectedMedicine.getSoLuong()), 1
                )
        );

        cbSelectedHuongDan.getItems().setAll(
                "Uống sau ăn",
                "Uống trước ăn",
                "Uống 1-2 viên khi đau, tối đa 6 viên/ngày"
        );
        String guide = selectedMedicine.getHuongDanSuDung();
        if (guide != null && !guide.isBlank()
                && !cbSelectedHuongDan.getItems().contains(guide)) {
            cbSelectedHuongDan.getItems().add(0, guide);
        }
        cbSelectedHuongDan.setValue(guide);
        lblValidation.setText("Số lượng phải lớn hơn 0 và không vượt quá số lượng tồn");
    }

    private void clearSelectedMedicineInfo() {
        tfSelectedTenThuoc.clear();
        tfSelectedDonVi.clear();
        tfSelectedSoLuongKho.clear();
        tfSelectedGiaTien.clear();
        cbSelectedHuongDan.getItems().clear();
        cbSelectedHuongDan.setValue(null);
    }

    @FXML
    private void handleSearch() {
        filterMedicines(tfSearch.getText());
    }

    @FXML
    private void handleAddToBill() {
        if (selectedMedicine == null) {
            showAlert("Lỗi", "Vui lòng chọn một loại thuốc!", Alert.AlertType.ERROR);
            return;
        }

        try {
            int soLuongCan = Integer.parseInt(spSelectedSoLuong.getEditor().getText().trim());
            if (soLuongCan <= 0) {
                showAlert("Lỗi", "Số lượng phải lớn hơn 0!", Alert.AlertType.ERROR);
                return;
            }

            if (soLuongCan > selectedMedicine.getSoLuong()) {
                showAlert("Lỗi", "Số lượng trong kho không đủ! Kho chỉ còn " + selectedMedicine.getSoLuong() + " " + selectedMedicine.getDonVi(), Alert.AlertType.ERROR);
                return;
            }

            // Tạo thuốc mới với số lượng đã chọn
            MedicineModel medicineForBill = new MedicineModel(
                selectedMedicine.getMaThuoc(),
                selectedMedicine.getTenThuoc(),
                selectedMedicine.getCongDung(),
                soLuongCan,
                selectedMedicine.getGiaTien(),
                selectedMedicine.getDonVi(),
                cbSelectedHuongDan.getEditor().getText().trim()
            );

            try {
                boolean reduced = MedicineDAO.reduceMedicineQuantity(
                        selectedMedicine.getMaThuoc(), soLuongCan
                );
                if (!reduced) {
                    showAlert("Lỗi", "Không thể cập nhật tồn kho. Hãy tải lại danh sách thuốc.", Alert.AlertType.ERROR);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Lỗi", "Không thể cập nhật tồn kho: " + ex.getMessage(), Alert.AlertType.ERROR);
                return;
            }

            if (onMedicineSelected != null) {
                onMedicineSelected.accept(medicineForBill);
            }
            showAlert("Thành công", "Đã thêm thuốc vào đơn khám!", Alert.AlertType.INFORMATION);

            Stage stage = (Stage) tfSearch.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số lượng hợp lệ!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) tfSearch.getScene().getWindow();
        stage.close();
    }

    public void setOnMedicineSelected(Consumer<MedicineModel> callback) {
        this.onMedicineSelected = callback;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 