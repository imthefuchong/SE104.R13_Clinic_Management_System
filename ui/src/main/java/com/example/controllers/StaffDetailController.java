package com.example.controllers;

import com.example.model.StaffModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StaffDetailController {
    @FXML private TextField tfId;
    @FXML private TextField tfFullName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfAddress;
    @FXML private TextField tfPassword;
    @FXML private TextField tfCCCD;
    @FXML private TextField tfSalary;

    @FXML private ComboBox<String> cbRole;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpBirth;
    @FXML private ToggleButton btnMale;
    @FXML private ToggleButton btnFemale;
    @FXML private Button btnRegister;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    private StaffDataChangeListener dataChangeListener;
    private boolean isEditMode = false;

    public void setDataChangeListener(StaffDataChangeListener listener) {
        this.dataChangeListener = listener;
    }

    @FXML
    public void initialize() {
        if (cbRole != null) {
            cbRole.getItems().setAll("Bác sĩ", "Điều dưỡng", "Dược sĩ", "Quản lý");
            cbRole.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !isEditMode && tfId != null) {
                    generateStaffId(newValue);
                }
            });
        }

        if (cbStatus != null) {
            cbStatus.getItems().setAll("Đang làm việc", "Nghỉ việc");
            cbStatus.setValue("Đang làm việc");
        }

        if (btnMale != null && btnFemale != null) {
            ToggleGroup genderGroup = new ToggleGroup();
            btnMale.setToggleGroup(genderGroup);
            btnFemale.setToggleGroup(genderGroup);
            btnMale.setSelected(true);
        }

        if (dpBirth != null) {
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
        }

        if (tfId != null) {
            tfId.setEditable(false);
        }

        if (btnRegister != null) {
            btnRegister.setVisible(true);
            btnRegister.setManaged(true);
        }
        if (btnUpdate != null) {
            btnUpdate.setVisible(false);
            btnUpdate.setManaged(false);
        }
        if (btnDelete != null) {
            btnDelete.setVisible(false);
            btnDelete.setManaged(false);
        }
    }

    private void generateStaffId(String roleName) {
        try {
            String prefix = getRolePrefix(roleName);
            int nextNumber = com.example.DAO.StaffDAO.getNextIdNumber(prefix);
            String newId = prefix + String.format("%03d", nextNumber);
            tfId.setText(newId);
        } catch (Exception ex) {
            tfId.setText("NV001");
        }
    }

    private String getRolePrefix(String role) {
        if (role == null) return "NV";
        return switch (role) {
            case "Bác sĩ", "DOCTOR" -> "BS";
            case "Điều dưỡng", "NURSE" -> "YT";
            case "Dược sĩ", "PHARMACIST" -> "DS";
            case "Quản lý", "MANAGER", "ADMIN" -> "QL";
            default -> "NV";
        };
    }

    private String mapRoleToDb(String vnRole) {
        if (vnRole == null) return "DOCTOR";
        return switch (vnRole) {
            case "Bác sĩ" -> "DOCTOR";
            case "Điều dưỡng" -> "NURSE";
            case "Dược sĩ" -> "PHARMACIST";
            case "Quản lý" -> "MANAGER";
            default -> vnRole;
        };
    }

    private String mapRoleToVn(String dbRole) {
        if (dbRole == null) return "Bác sĩ";
        return switch (dbRole) {
            case "DOCTOR" -> "Bác sĩ";
            case "NURSE" -> "Điều dưỡng";
            case "PHARMACIST" -> "Dược sĩ";
            case "MANAGER", "ADMIN" -> "Quản lý";
            default -> dbRole;
        };
    }

    public void setStaff(StaffModel staffModel) {
        if (staffModel != null) {
            isEditMode = true;

            if (btnRegister != null) {
                btnRegister.setVisible(false);
                btnRegister.setManaged(false);
            }
            if (btnUpdate != null) {
                btnUpdate.setVisible(true);
                btnUpdate.setManaged(true);
            }
            if (btnDelete != null) {
                btnDelete.setVisible(true);
                btnDelete.setManaged(true);
            }

            if (tfId != null) tfId.setText(staffModel.getId());
            if (tfFullName != null) tfFullName.setText(staffModel.getFullName());
            if (tfLastName != null) tfLastName.setText(staffModel.getLastname());
            if (tfName != null) tfName.setText(staffModel.getFirstname());
            if (tfEmail != null) tfEmail.setText(staffModel.getEmail());
            if (tfPhone != null) tfPhone.setText(staffModel.getPhone());
            if (tfAddress != null) tfAddress.setText(staffModel.getAddress());
            if (tfPassword != null) tfPassword.setText(staffModel.getPassword());
            if (tfCCCD != null) tfCCCD.setText(staffModel.getCccd());

            if (cbRole != null) cbRole.setValue(mapRoleToVn(staffModel.getRole()));
            if (cbStatus != null) cbStatus.setValue(staffModel.getStatus());
            if (dpBirth != null) dpBirth.setValue(staffModel.getBirthday());

            String gioitinh = staffModel.getGender();
            if (btnMale != null && btnFemale != null) {
                if ("Nam".equalsIgnoreCase(gioitinh)) {
                    btnMale.setSelected(true);
                    btnFemale.setSelected(false);
                } else {
                    btnFemale.setSelected(true);
                    btnMale.setSelected(false);
                }
            }
        } else {
            isEditMode = false;
            if (tfId != null) tfId.setText("");
        }
    }

    public void register(ActionEvent actionEvent) {
        saveOrUpdate(true);
    }

    public void update(ActionEvent actionEvent) {
        saveOrUpdate(false);
    }

    private void saveOrUpdate(boolean isNew) {
        try {
            String id = tfId != null ? tfId.getText() : "";
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Mã nhân viên không được để trống!");
            }

            String fullName = "";
            if (tfFullName != null && !tfFullName.getText().trim().isEmpty()) {
                fullName = tfFullName.getText().trim();
            } else if (tfLastName != null || tfName != null) {
                String l = tfLastName != null ? tfLastName.getText() : "";
                String n = tfName != null ? tfName.getText() : "";
                fullName = (l + " " + n).trim();
            }

            if (fullName.isEmpty()) {
                throw new IllegalArgumentException("Họ và tên không được để trống!");
            }

            String lastName;
            String firstName;
            int lastSpace = fullName.lastIndexOf(' ');
            if (lastSpace > 0) {
                lastName = fullName.substring(0, lastSpace);
                firstName = fullName.substring(lastSpace + 1);
            } else {
                lastName = "";
                firstName = fullName;
            }

            String email = tfEmail != null ? tfEmail.getText() : "";
            String phone = tfPhone != null ? tfPhone.getText() : "";
            String address = tfAddress != null ? tfAddress.getText() : "";
            String password = (tfPassword != null && !tfPassword.getText().isEmpty()) ? tfPassword.getText() : "123456";
            String cccd = (tfCCCD != null && !tfCCCD.getText().isEmpty()) ? tfCCCD.getText() : "";

            String rawRole = cbRole != null ? cbRole.getValue() : "Bác sĩ";
            if (rawRole == null) throw new IllegalArgumentException("Vui lòng chọn chức vụ!");
            String role = mapRoleToDb(rawRole);

            LocalDate birthday = dpBirth != null ? dpBirth.getValue() : null;
            if (birthday == null) birthday = LocalDate.of(1990, 1, 1);

            String gender = (btnMale != null && btnMale.isSelected()) ? "Nam" : "Nữ";
            double luong = 10000000;
            if (tfSalary != null && !tfSalary.getText().isEmpty()) {
                try { luong = Double.parseDouble(tfSalary.getText()); } catch (Exception ignored) {}
            }

            StaffModel staff = new StaffModel(id, lastName, firstName, role, luong, birthday, gender, cccd, address, email, phone, password);
            if (cbStatus != null && cbStatus.getValue() != null) {
                staff.setStatus(cbStatus.getValue());
            }

            boolean success;
            if (isNew) {
                success = com.example.DAO.StaffDAO.insertStaff(staff);
                if (success) {
                    showAlert("Thành công", "Thêm nhân viên thành công!", Alert.AlertType.INFORMATION);
                    closeWindow();
                    if (dataChangeListener != null) dataChangeListener.onDataChanged(staff, "INSERT");
                } else {
                    showAlert("Thất bại", "Thêm nhân viên thất bại!", Alert.AlertType.ERROR);
                }
            } else {
                success = com.example.DAO.StaffDAO.updateStaff(staff);
                if (success) {
                    showAlert("Thành công", "Cập nhật nhân viên thành công!", Alert.AlertType.INFORMATION);
                    closeWindow();
                    if (dataChangeListener != null) dataChangeListener.onDataChanged(staff, "UPDATE");
                } else {
                    showAlert("Thất bại", "Cập nhật nhân viên thất bại!", Alert.AlertType.ERROR);
                }
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Thông báo", ex.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception ex) {
            showAlert("Lỗi", "Lỗi: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void delete(ActionEvent actionEvent) {
        try {
            String id = tfId != null ? tfId.getText() : "";
            if (id.isEmpty()) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa/nghỉ việc nhân viên này?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                StaffModel staff = new StaffModel();
                staff.setId(id);
                staff.setStatus("Nghỉ việc");
                boolean success = com.example.DAO.StaffDAO.deleteStaff(id);
                if (success) {
                    showAlert("Thành công", "Đã xóa nhân viên!", Alert.AlertType.INFORMATION);
                    closeWindow();
                    if (dataChangeListener != null) dataChangeListener.onDataChanged(staff, "DELETE");
                } else {
                    showAlert("Thất bại", "Xóa nhân viên thất bại!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception ex) {
            showAlert("Lỗi", "Không thể xóa: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void closeWindow() {
        Stage stage = null;
        if (btnUpdate != null && btnUpdate.getScene() != null) stage = (Stage) btnUpdate.getScene().getWindow();
        else if (btnRegister != null && btnRegister.getScene() != null) stage = (Stage) btnRegister.getScene().getWindow();
        if (stage != null) stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
