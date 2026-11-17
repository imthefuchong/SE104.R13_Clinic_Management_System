package com.example.controllers;

import com.example.DAO.StaffDAO;
import com.example.model.StaffModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PasswordResetController {
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheckBox;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private CheckBox showConfirmPasswordCheckBox;

    @FXML
    private void initialize() {
        // Đồng bộ nội dung giữa PasswordField và TextField cho mật khẩu mới
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordTextField.getText().equals(newValue)) {
                passwordTextField.setText(newValue);
            }
        });

        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordField.getText().equals(newValue)) {
                passwordField.setText(newValue);
            }
        });

        // Đồng bộ nội dung giữa PasswordField và TextField cho xác nhận mật khẩu
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!confirmPasswordTextField.getText().equals(newValue)) {
                confirmPasswordTextField.setText(newValue);
            }
        });

        confirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!confirmPasswordField.getText().equals(newValue)) {
                confirmPasswordField.setText(newValue);
            }
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheckBox.isSelected()) {
            // Hiện text field, ẩn password field
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.requestFocus();
            // Đặt cursor về cuối text
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            // Hiện password field, ẩn text field
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.requestFocus();
            // Đặt cursor về cuối text
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        if (showConfirmPasswordCheckBox.isSelected()) {
            // Hiện text field, ẩn password field
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordTextField.requestFocus();
            // Đặt cursor về cuối text
            confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());
        } else {
            // Hiện password field, ẩn text field
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.requestFocus();
            // Đặt cursor về cuối text
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
        }
    }

    @FXML
    private void handleReset() {
        String newPass = passwordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();

        // Kiểm tra mật khẩu không được để trống
        if (newPass.isEmpty()) {
            showAlert("Vui lòng nhập mật khẩu mới.", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra xác nhận mật khẩu không được để trống
        if (confirmPass.isEmpty()) {
            showAlert("Vui lòng xác nhận mật khẩu.", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra mật khẩu có độ dài tối thiểu
        if (newPass.length() < 6) {
            showAlert("Mật khẩu phải có ít nhất 6 ký tự.", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp nhau không
        if (!newPass.equals(confirmPass)) {
            showAlert("Mật khẩu xác nhận không khớp với mật khẩu mới.", Alert.AlertType.WARNING);
            return;
        }

        // Lấy email từ controller xác thực
        String email = EmailVerificationController.getCurrentEmail();
        String id = EmailVerificationController.getCurrentID();

        if (email == null || email.isEmpty()) {
            showAlert("Không tìm thấy thông tin email. Vui lòng thực hiện lại quá trình xác thực.", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (StaffDAO.resetPasswordByUsername(id, newPass)) {
                showAlert("Đặt lại mật khẩu thành công!", Alert.AlertType.INFORMATION);
                ((Stage) passwordField.getScene().getWindow()).close();
            } else {
                showAlert("Không thể cập nhật mật khẩu. Kiểm tra tên đăng nhập hoặc kết nối CSDL.",
                        Alert.AlertType.ERROR);
            }
        } catch (RuntimeException exception) {
            showAlert("Lỗi khi cập nhật mật khẩu: " + exception.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}