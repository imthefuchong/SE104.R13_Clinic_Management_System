package com.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class OTPVerificationController {
    @FXML private TextField otpField;

    @FXML
    private void handleVerifyOTP() {
        String inputOTP = otpField.getText() == null ? "" : otpField.getText().trim();

        if (inputOTP.isEmpty()) {
            showAlert("Vui lòng nhập mã OTP.");
        } else if (inputOTP.equals(EmailVerificationController.getCurrentOTP())) {
            openResetPassword();
            ((Stage) otpField.getScene().getWindow()).close();
        } else {
            showAlert("Mã OTP không đúng. Vui lòng thử lại.");
        }
    }

    private void openResetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/password_reset.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Đặt lại mật khẩu");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Không thể mở giao diện đặt lại mật khẩu.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
