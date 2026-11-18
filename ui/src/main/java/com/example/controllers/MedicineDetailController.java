package com.example.controllers;

import com.example.DAO.MedicineDAO;
import com.example.model.MedicineModel;
import com.example.model.Role;
import com.example.model.UserContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLIntegrityConstraintViolationException;

public class MedicineDetailController {
    @FXML
    private TextField tfId, tfName, tfQuantity, tfCost, tfGuide;
    @FXML private TextArea tfUse;
    @FXML
    private ComboBox<String> cbUnit, cbStatus;
    @FXML
    private Button btnAdd, btnUpdate, btnDelete, btnClose;

    private MedicineDataChangeListener dataChangeListener;

    public void setDataChangeListener(MedicineDataChangeListener listener) {
        this.dataChangeListener = listener;
    }

    @FXML
    public void initialize() {
        handlePermission();
        tfId.setEditable(false);
        tfId.setStyle("-fx-background-color: #f0f0f0;");
        int nextIdNumber = MedicineDAO.getNextIdNumber("T");
        String nextId = String.format("T%03d", nextIdNumber); // VD: T001, T002, ...
        tfId.setText(nextId);
        cbUnit.getItems().addAll("viên", "vỉ", "gói", "ống", "chai", "lọ", "tuýp", "ml", "mg", "g", "mcg", "IU");
        cbUnit.setEditable(true);
        cbStatus.getItems().setAll("Đang sử dụng", "Ngưng sử dụng");
        cbStatus.setValue("Đang sử dụng");
        btnUpdate.setVisible(false);
        btnUpdate.setManaged(false);
        btnDelete.setVisible(false);
        btnDelete.setManaged(false);
    }

    public void setMedicine(MedicineModel medicineModel) {
        if (medicineModel != null) {

            tfId.setText(medicineModel.getMaThuoc());
            tfUse.setText(medicineModel.getCongDung());
            tfName.setText(medicineModel.getTenThuoc());
            tfQuantity.setText(String.valueOf(medicineModel.getSoLuong()));
            tfCost.setText(String.valueOf(medicineModel.getGiaTien()));
            tfGuide.setText(medicineModel.getHuongDanSuDung());
            cbUnit.setValue(medicineModel.getDonVi());
            cbUnit.getEditor().setText(medicineModel.getDonVi());
            cbStatus.setValue(medicineModel.getTrangThai());
            btnAdd.setVisible(false);
            btnAdd.setManaged(false);
            btnUpdate.setVisible(true);
            btnUpdate.setManaged(true);
            btnDelete.setVisible(true);
            btnDelete.setManaged(true);
        }
    }

    public void setReadOnly(boolean readOnly) {
        if (!readOnly) {
            return;
        }
        tfName.setEditable(false);
        tfUse.setEditable(false);
        tfQuantity.setEditable(false);
        tfCost.setEditable(false);
        tfGuide.setEditable(false);
        cbUnit.setDisable(true);
        cbStatus.setDisable(true);
        btnAdd.setVisible(false);
        btnAdd.setManaged(false);
        btnUpdate.setVisible(false);
        btnUpdate.setManaged(false);
        btnDelete.setVisible(false);
        btnDelete.setManaged(false);
    }

    private String getUnitText() {
        String unit = cbUnit.getEditor().getText();
        return unit == null ? "" : unit.trim();
    }

    public void add(ActionEvent actionEvent) {
        try {
            // Kiểm tra đầu vào
            String id = tfId.getText();
            if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Mã thuốc không được để trống!");
            String ten = tfName.getText();
            if (ten == null || ten.trim().isEmpty()) throw new IllegalArgumentException("Tên thuốc không được để trống!");
            String congDung = tfUse.getText();
            int soLuong = Integer.parseInt(tfQuantity.getText());
            if (soLuong < 0) throw new IllegalArgumentException("Số lượng không thể âm!");
            double giaTien = Double.parseDouble(tfCost.getText());
            if (giaTien < 0) throw new IllegalArgumentException("Giá tiền không thể âm!");
            String donVi = getUnitText();
            String huongDan = tfGuide.getText();

            MedicineModel medicine = new MedicineModel(
                    id, ten, congDung, soLuong, giaTien, donVi, huongDan, cbStatus.getValue()
            );
            try {
                boolean success = com.example.DAO.MedicineDAO.insertMedicine(medicine, java.time.LocalDate.now());
                if (success) {
                    showAlert("Thành công", "Thêm thuốc thành công!", Alert.AlertType.INFORMATION);
                    Stage stage = (Stage) btnAdd.getScene().getWindow();
                    stage.close();
                    if (dataChangeListener != null) {
                        dataChangeListener.onDataChanged(medicine, "INSERT");
                    }
                } else {
                    showAlert("Thất bại", "Thêm thuốc thất bại! Vui lòng kiểm tra lại thông tin.", Alert.AlertType.ERROR);
                }
            } catch (RuntimeException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    String msg = cause.getMessage();
                    if (msg.contains("PRIMARY") || msg.contains("MaThuoc")) {
                        showAlert("Thất bại", "Mã thuốc đã tồn tại!", Alert.AlertType.ERROR);
                    } else {
                        showAlert("Thất bại", "Dữ liệu bị trùng lặp!", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Thất bại", "Lỗi: " + cause.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số lượng và giá tiền hợp lệ!", Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            showAlert("Lỗi", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi không xác định: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void update(ActionEvent actionEvent) {
        try {
            String id = tfId.getText();
            String ten = tfName.getText();
            if (ten == null || ten.trim().isEmpty()) throw new IllegalArgumentException("Tên thuốc không được để trống!");
            String congDung = tfUse.getText();
            int soLuong = Integer.parseInt(tfQuantity.getText());
            if (soLuong < 0) throw new IllegalArgumentException("Số lượng không thể âm!");
            double giaTien = Double.parseDouble(tfCost.getText());
            if (giaTien < 0) throw new IllegalArgumentException("Giá tiền không thể âm!");
            String donVi = getUnitText();
            String huongDan = tfGuide.getText();

            MedicineModel medicine = new MedicineModel(
                    id, ten, congDung, soLuong, giaTien, donVi, huongDan, cbStatus.getValue()
            );
            try {
                boolean success = com.example.DAO.MedicineDAO.updateMedicine(medicine);
                if (success) {
                    showAlert("Thành công", "Cập nhật thuốc thành công!", Alert.AlertType.INFORMATION);
                    Stage stage = (Stage) btnUpdate.getScene().getWindow();
                    stage.close();
                    if (dataChangeListener != null) {
                        dataChangeListener.onDataChanged(medicine, "UPDATE");
                    }
                } else {
                    showAlert("Thất bại", "Cập nhật thuốc thất bại! Thuốc không tồn tại.", Alert.AlertType.ERROR);
                }
            } catch (RuntimeException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    showAlert("Thất bại", "Dữ liệu bị trùng lặp!", Alert.AlertType.ERROR);
                } else {
                    showAlert("Thất bại", "Lỗi: " + cause.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số lượng và giá tiền hợp lệ!", Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            showAlert("Lỗi", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi không xác định: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void delete(ActionEvent actionEvent) {
        try {
            String id = tfId.getText();
            try {
                boolean success = com.example.DAO.MedicineDAO.deleteMedicine(id);
                if (success) {
                    showAlert("Thành công", "Xóa thuốc thành công!", Alert.AlertType.INFORMATION);
                    Stage stage = (Stage) btnDelete.getScene().getWindow();
                    stage.close();
                    if (dataChangeListener != null) {
                        dataChangeListener.onDataChanged(new MedicineModel(id, "", "", 0, 0, "", ""), "DELETE");
                    }
                } else {
                    showAlert("Thất bại", "Xóa thuốc thất bại! Thuốc không tồn tại.", Alert.AlertType.ERROR);
                }
            } catch (RuntimeException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    String msg = cause.getMessage();
                    if (msg.contains("ctdonthuoc") || msg.contains("mathuoc")) {
                        showAlert("Thất bại", "Không thể xóa thuốc với mã " + id + " vì đang được tham chiếu trong bảng chi tiết đơn thuốc (ctdonthuoc).", Alert.AlertType.ERROR);
                    } else if (msg.contains("foreign key")) {
                        showAlert("Thất bại", "Không thể xóa thuốc vì đang được sử dụng ở bảng khác!", Alert.AlertType.ERROR);
                    } else {
                        showAlert("Thất bại", "Lỗi ràng buộc dữ liệu!", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Thất bại", "Lỗi: " + cause.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi không xác định: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void closeDialog() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    public void btnLuuPhieu(ActionEvent actionEvent) {
        try {
            String id = tfId.getText();
            if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Mã thuốc không được để trống!");
            String ten = tfName.getText();
            if (ten == null || ten.trim().isEmpty()) throw new IllegalArgumentException("Tên thuốc không được để trống!");
            String congDung = tfUse.getText();
            int soLuong = Integer.parseInt(tfQuantity.getText());
            if (soLuong < 0) throw new IllegalArgumentException("Số lượng không thể âm!");
            double giaTien = Double.parseDouble(tfCost.getText());
            if (giaTien < 0) throw new IllegalArgumentException("Giá tiền không thể âm!");
            String donVi = getUnitText();
            String huongDan = tfGuide.getText();

            MedicineModel medicine = new MedicineModel(id, ten, congDung, soLuong, giaTien, donVi, huongDan);
            try {
                MedicineModel existing = com.example.DAO.MedicineDAO.getMedicineById(id);
                boolean success;
                String action;
                if (existing == null) {
                    success = com.example.DAO.MedicineDAO.insertMedicine(medicine, java.time.LocalDate.now());
                    action = "INSERT";
                } else {
                    success = com.example.DAO.MedicineDAO.updateMedicine(medicine);
                    action = "UPDATE";
                }
                if (success) {
                    showAlert("Thành công", (existing == null ? "Thêm mới" : "Cập nhật") + " phiếu thuốc thành công!", Alert.AlertType.INFORMATION);
                    Stage stage = (Stage) btnAdd.getScene().getWindow();
                    stage.close();
                    if (dataChangeListener != null) {
                        dataChangeListener.onDataChanged(medicine, action);
                    }
                } else {
                    showAlert("Thất bại", (existing == null ? "Thêm mới" : "Cập nhật") + " phiếu thuốc thất bại!", Alert.AlertType.ERROR);
                }
            } catch (RuntimeException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    String msg = cause.getMessage();
                    if (msg.contains("PRIMARY") || msg.contains("MaThuoc")) {
                        showAlert("Thất bại", "Mã thuốc đã tồn tại!", Alert.AlertType.ERROR);
                    } else {
                        showAlert("Thất bại", "Dữ liệu bị trùng lặp!", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Thất bại", "Lỗi: " + cause.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số lượng và giá tiền hợp lệ!", Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            showAlert("Lỗi", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi không xác định: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void handlePermission() {
        Role role = UserContext.getInstance().getRole();
        if (role == null) {
            role = Role.ADMIN;
        }
        switch (role) {
            case ADMIN -> {

            }
            case DOCTOR -> {
                btnAdd.setVisible(false);
                btnAdd.setManaged(false);
                btnUpdate.setVisible(false);
                btnUpdate.setManaged(false);
                btnDelete.setVisible(false);
                btnDelete.setManaged(false);
            }
            case NURSE -> {
                btnAdd.setVisible(false);
                btnAdd.setManaged(false);
                btnUpdate.setVisible(false);
                btnUpdate.setManaged(false);
                btnDelete.setVisible(false);
                btnDelete.setManaged(false);
            }
            case MANAGER -> {
                btnAdd.setVisible(true);
                btnAdd.setManaged(true);
                btnUpdate.setVisible(true);
                btnUpdate.setManaged(true);
                btnDelete.setVisible(true);
                btnDelete.setManaged(true);
            }
        }
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}