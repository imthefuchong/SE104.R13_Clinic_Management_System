package com.example.controllers;

import com.example.model.AccountModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Predicate;

public class AccountDialogController {
    public record AccountForm(
            String employeeId,
            String employeeName,
            String username,
            String password,
            String roleId,
            String status
    ) {}

    private enum Mode {
        CREATE,
        EDIT,
        RESET_PASSWORD
    }

    @FXML private Label lblTitle;
    @FXML private Label lblEmployee;
    @FXML private Label lblPasswordNote;
    @FXML private HBox employeeRow;
    @FXML private ComboBox<String> cbEmployee;
    @FXML private TextField tfEmployee;
    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private ComboBox<String> cbStatus;
    @FXML private VBox roleRow;
    @FXML private VBox statusRow;
    @FXML private VBox passwordRow;
    @FXML private VBox confirmPasswordRow;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private Mode mode;
    private AccountModel account;
    private Predicate<AccountForm> onSave;

    @FXML
    private void initialize() {
        cbRole.getItems().setAll("Quản trị viên", "Bác sĩ", "Y tá", "Quản lý");
        cbRole.setValue("Bác sĩ");
        cbStatus.getItems().setAll("Hoạt động", "Đã khóa");
        cbStatus.setValue("Hoạt động");
    }

    public void configureCreate(List<String> employeeLabels, Predicate<AccountForm> onSave) {
        mode = Mode.CREATE;
        this.onSave = onSave;
        lblTitle.setText("Tạo tài khoản");
        lblEmployee.setText("Nhân viên");
        cbEmployee.getItems().setAll(employeeLabels);
        cbEmployee.getSelectionModel().selectFirst();
        cbEmployee.setVisible(true);
        cbEmployee.setManaged(true);
        tfEmployee.setVisible(false);
        tfEmployee.setManaged(false);
        roleRow.setVisible(true);
        roleRow.setManaged(true);
        statusRow.setVisible(true);
        statusRow.setManaged(true);
        passwordRow.setVisible(true);
        passwordRow.setManaged(true);
        confirmPasswordRow.setVisible(false);
        confirmPasswordRow.setManaged(false);
        pfPassword.setPromptText("Mật khẩu mới");
        lblPasswordNote.setText("Mật khẩu có ít nhất 8 ký tự");
        btnSave.setText("Tạo tài khoản");
    }

    public void configureEdit(AccountModel account, Predicate<AccountForm> onSave) {
        mode = Mode.EDIT;
        this.account = account;
        this.onSave = onSave;
        lblTitle.setText("Cập nhật tài khoản");
        lblEmployee.setText("Nhân viên");
        cbEmployee.setVisible(false);
        cbEmployee.setManaged(false);
        tfEmployee.setText(account.getEmployeeId() + " - " + account.getEmployeeName());
        tfEmployee.setVisible(true);
        tfEmployee.setManaged(true);
        tfUsername.setText(account.getUsername());
        tfUsername.setEditable(false);
        cbRole.setValue(account.getRoleLabel());
        cbStatus.setValue(account.getStatus());
        roleRow.setVisible(true);
        roleRow.setManaged(true);
        statusRow.setVisible(true);
        statusRow.setManaged(true);
        passwordRow.setVisible(false);
        passwordRow.setManaged(false);
        confirmPasswordRow.setVisible(false);
        confirmPasswordRow.setManaged(false);
        lblPasswordNote.setVisible(false);
        lblPasswordNote.setManaged(false);
        btnSave.setText("Lưu thay đổi");
    }

    public void configureResetPassword(AccountModel account, Predicate<AccountForm> onSave) {
        mode = Mode.RESET_PASSWORD;
        this.account = account;
        this.onSave = onSave;
        lblTitle.setText("Đặt lại mật khẩu");
        lblEmployee.setText("Nhân viên");
        cbEmployee.setVisible(false);
        cbEmployee.setManaged(false);
        tfEmployee.setText(account.getEmployeeId() + " - " + account.getEmployeeName());
        tfEmployee.setVisible(true);
        tfEmployee.setManaged(true);
        tfUsername.setText(account.getUsername());
        tfUsername.setEditable(false);
        roleRow.setVisible(false);
        roleRow.setManaged(false);
        statusRow.setVisible(false);
        statusRow.setManaged(false);
        passwordRow.setVisible(true);
        passwordRow.setManaged(true);
        confirmPasswordRow.setVisible(true);
        confirmPasswordRow.setManaged(true);
        lblPasswordNote.setText("Mật khẩu có ít nhất 8 ký tự");
        btnSave.setText("Xác nhận");
    }

    @FXML
    private void handleSave() {
        String username = tfUsername.getText() == null ? "" : tfUsername.getText().trim();
        String password = pfPassword.getText() == null ? "" : pfPassword.getText();
        String employeeId;
        String employeeName;

        if (mode == Mode.CREATE) {
            String selectedEmployee = cbEmployee.getValue();
            if (selectedEmployee == null || !selectedEmployee.contains(" - ")) {
                showError("Vui lòng chọn nhân viên.");
                return;
            }
            String[] parts = selectedEmployee.split(" - ", 2);
            employeeId = parts[0].trim();
            employeeName = parts.length > 1 ? parts[1].trim() : "";
            if (username.isBlank()) {
                showError("Vui lòng nhập tên đăng nhập.");
                return;
            }
            if (password.length() < 8) {
                showError("Mật khẩu phải có ít nhất 8 ký tự.");
                return;
            }
        } else {
            employeeId = account.getEmployeeId();
            employeeName = account.getEmployeeName();
            username = account.getUsername();
        }

        if (mode == Mode.RESET_PASSWORD) {
            if (password.length() < 8) {
                showError("Mật khẩu phải có ít nhất 8 ký tự.");
                return;
            }
            if (!password.equals(pfConfirmPassword.getText())) {
                showError("Mật khẩu nhập lại không khớp.");
                return;
            }
        }

        String roleId = mode == Mode.RESET_PASSWORD
                ? account.getRoleId()
                : roleIdFromLabel(cbRole.getValue());
        String status = mode == Mode.RESET_PASSWORD
                ? account.getStatus()
                : cbStatus.getValue();
        AccountForm form = new AccountForm(
                employeeId, employeeName, username, password, roleId, status
        );
        if (onSave != null && onSave.test(form)) {
            close();
        }
    }

    private String roleIdFromLabel(String label) {
        return switch (label == null ? "" : label) {
            case "Quản trị viên" -> "ADMIN";
            case "Bác sĩ" -> "DOCTOR";
            case "Y tá" -> "NURSE";
            case "Quản lý" -> "MANAGER";
            default -> "";
        };
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông tin chưa hợp lệ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
