package com.example.controllers;

import com.example.DAO.StaffDAO;
import com.example.model.StaffModel;
import com.example.service.EmailService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Random;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class EmailVerificationController {
    @FXML private TextField emailField;
    @FXML private TextField maNhanVienField;
    @FXML private Button btnXacNhan;


    private static String currentOTP;
    private static String currentEmail;
    private static String curentID;
    private static boolean demoOTP;

    @FXML
    private void handleSendOTP() {
        String email = emailField.getText().trim();
        String maNhanVien = maNhanVienField.getText().trim();

        if (email.isEmpty() || maNhanVien.isEmpty()) {
            showAlert("Vui lòng nhập đầy đủ email và mã nhân viên.");
            return;
        }

        currentOTP = String.format("%06d", new Random().nextInt(999999));
        currentEmail = email;
        curentID = maNhanVien;

        try {
            StaffModel staff = StaffDAO.findByEmailAndId(email, maNhanVien);
            if (staff == null) {
                demoOTP = true;
            }
        } catch (RuntimeException exception) {
            demoOTP = true;
        }

        // Cập nhật UI ngay
        btnXacNhan.setText("Đang gửi...");
        btnXacNhan.setDisable(true);

        // Chạy gửi email trong thread khác
        new Thread(() -> {
            try {
                if (demoOTP) {
                    Platform.runLater(() -> {
                        btnXacNhan.setText("Gửi lại mã");
                        btnXacNhan.setDisable(false);
                        showAlert("Chế độ minh họa: mã OTP là " + currentOTP);
                        openOTPDialog();
                    });
                    return;
                }

                EmailService.sendOTP(email, currentOTP); // Gửi OTP

                // Sau khi gửi thành công, quay lại UI thread
                Platform.runLater(() -> {
                    btnXacNhan.setText("Gửi lại mã");
                    btnXacNhan.setDisable(false);
                    openOTPDialog(); // Hiện dialog nhập OTP
                });

            } catch (MessagingException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Không thể gửi email xác thực: " + e.getMessage());
                    btnXacNhan.setText("Gửi mã");
                    btnXacNhan.setDisable(false);
                });
            }
        }).start();
    }



    private void openOTPDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OTP_verification.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Xác minh OTP");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            // Đóng cửa sổ hiện tại nếu cần:
             ((Stage) emailField.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Không thể mở giao diện xác minh OTP.");
        }
    }

    public static String getCurrentOTP() {
        return currentOTP;
    }

    public static String getCurrentID(){
        return curentID;
    }

    public static String getCurrentEmail() {
        return currentEmail;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
