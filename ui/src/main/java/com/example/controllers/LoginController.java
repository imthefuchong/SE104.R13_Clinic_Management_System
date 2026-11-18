package com.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField txtTaiKhoan;

    @FXML
    private PasswordField txtMatKhau;

    @FXML
    private TextField txtMatKhauVisible;

    @FXML
    private CheckBox chkShowPassword;

    @FXML
    private Button btnDangNhap;

    @FXML
    private Label lbltest;

    @FXML
    public void initialize() {
        hideMessage();

        txtMatKhauVisible.textProperty()
                .bindBidirectional(txtMatKhau.textProperty());

        txtMatKhauVisible.visibleProperty()
                .bind(chkShowPassword.selectedProperty());

        txtMatKhauVisible.managedProperty()
                .bind(txtMatKhauVisible.visibleProperty());

        txtMatKhau.visibleProperty()
                .bind(chkShowPassword.selectedProperty().not());

        txtMatKhau.managedProperty()
                .bind(txtMatKhau.visibleProperty());

        btnDangNhap.setDefaultButton(true);
        btnDangNhap.setOnAction(event -> openMainScreen());
    }

    private void openMainScreen() {
        hideMessage();

        try {
            URL view = requireResource("/views/menu.fxml");

            FXMLLoader loader = new FXMLLoader(view);
            Parent root = loader.load();

            Scene scene = new Scene(root);

            URL menuCss = getClass()
                    .getResource("/CSS/menu-style.css");

            if (menuCss != null
                    && !scene.getStylesheets().contains(
                            menuCss.toExternalForm()
                    )) {
                scene.getStylesheets().add(
                        menuCss.toExternalForm()
                );
            }

            Stage stage = (Stage) btnDangNhap
                    .getScene()
                    .getWindow();

            stage.setTitle(
                    "Quản lý phòng mạch tư — Demo giao diện"
            );
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception exception) {
            exception.printStackTrace();

            showMessage(
                    "Chưa mở được màn hình chính. "
                    + "Xem chi tiết lỗi trong Terminal."
            );
        }
    }

    @FXML
    private void handleForgotPassword(MouseEvent event) {
        hideMessage();

        try {
            URL view = requireResource(
                    "/views/email_verification.fxml"
            );

            FXMLLoader loader = new FXMLLoader(view);
            Parent root = loader.load();

            Stage dialog = new Stage();

            dialog.initOwner(
                    btnDangNhap.getScene().getWindow()
            );
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle(
                    "Quên mật khẩu — Demo giao diện"
            );
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.show();

        } catch (Exception exception) {
            exception.printStackTrace();

            Alert alert = new Alert(
                    Alert.AlertType.ERROR
            );

            alert.initOwner(
                    btnDangNhap.getScene().getWindow()
            );
            alert.setTitle("Thông báo");
            alert.setHeaderText(
                    "Chưa mở được giao diện quên mật khẩu"
            );
            alert.setContentText(
                    "Xem chi tiết lỗi trong Terminal. "
                    + "Controller của màn hình này "
                    + "sẽ được chỉnh sang chế độ demo."
            );
            alert.showAndWait();
        }
    }

    private URL requireResource(String path)
            throws IOException {

        URL resource = getClass().getResource(path);

        if (resource == null) {
            throw new IOException(
                    "Không tìm thấy tài nguyên: " + path
            );
        }

        return resource;
    }

    private void showMessage(String message) {
        lbltest.setText(message);
        lbltest.setVisible(true);
        lbltest.setManaged(true);
    }

    private void hideMessage() {
        lbltest.setText("");
        lbltest.setVisible(false);
        lbltest.setManaged(false);
    }
}