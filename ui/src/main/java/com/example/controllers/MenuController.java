package com.example.controllers;

import com.example.helper.NavigationHelper;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;

public class MenuController {

    @FXML private HBox btnMyprofile;
    @FXML private HBox btnDashboard;
    @FXML private HBox btnStaff;
    @FXML private HBox btnPatients;
    @FXML private HBox btnAnalytics;
    @FXML private HBox btnAppointment;
    @FXML private HBox btnDutySchedule;
    @FXML private HBox btnMedicalReport;
    @FXML private HBox btnMedicine;
    @FXML private HBox btnAccounts;
    @FXML private HBox btnRules;
    @FXML private HBox btnLogout;

    @FXML private StackPane contentArea;
    @FXML private Label lblHeaderTitle;
    @FXML private Label lblHeaderUserName;
    @FXML private Label lblHeaderUserRole;
    @FXML private javafx.scene.control.TextField tfGlobalSearch;

    private HBox currentActiveItem;

    @FXML
    private void initialize() {
        if (lblHeaderTitle != null) {
            lblHeaderTitle.setText("Bảng điều khiển");
        }
        com.example.model.UserContext userContext =
                com.example.model.UserContext.getInstance();

        if (lblHeaderUserName != null) {
            String username = userContext.getUsername();
            lblHeaderUserName.setText(
                    username != null && !username.isBlank()
                            ? username
                            : "Người dùng"
            );
        }

        if (lblHeaderUserRole != null
                && userContext.getRole() != null) {
            lblHeaderUserRole.setText(
                    userContext.getRole().toVietnamese()
            );
        }
        setItemText(
                btnAppointment,
                "Danh sách khám trong ngày"
        );

        setItemText(
                btnMedicalReport,
                "Lịch sử khám"
        );

        setItemText(btnLogout, "Đăng xuất");

        btnDashboard.setOnMouseClicked(
                event -> openDashboard()
        );

        btnAppointment.setOnMouseClicked(
                event -> openAppointmentList(
                        LocalDate.now()
                )
        );

        btnMedicalReport.setOnMouseClicked(event ->
                openScreen(btnMedicalReport, "Lịch sử khám", "/views/exam_history.fxml")
        );

        btnDutySchedule.setOnMouseClicked(event ->
                openScreen(btnDutySchedule, "Lịch trực", "/views/schedule_view.fxml")
        );

        btnStaff.setOnMouseClicked(event ->
                openScreen(btnStaff, "Danh sách nhân viên", "/views/staff.fxml")
        );

        btnPatients.setOnMouseClicked(event ->
                openPatientScreen()
        );

        btnMedicine.setOnMouseClicked(event ->
                openScreen(btnMedicine, "Danh mục thuốc", "/views/medicine.fxml")
        );

        btnAnalytics.setOnMouseClicked(event ->
                openScreen(btnAnalytics, "Báo cáo thống kê", "/views/monthly_report.fxml")
        );

        btnAccounts.setOnMouseClicked(event ->
                openScreen(btnAccounts, "Quản lý tài khoản", "/views/account_management.fxml")
        );

        btnRules.setOnMouseClicked(event ->
                openScreen(btnRules, "Quy định phòng mạch", "/views/clinic_rules.fxml")
        );

        btnMyprofile.setOnMouseClicked(event ->
                openScreen(btnMyprofile, "Thông tin cá nhân", "/views/myprofile.fxml")
        );

        btnLogout.setOnMouseClicked(event ->
                {
                    com.example.model.UserContext.getInstance().clear();
                    NavigationHelper.switchTo("/views/login.fxml");
                }
        );

        openDashboard();
    }

    private FXMLLoader createLoader(String path) {
        URL view = getClass().getResource(path);

        if (view == null) {
            throw new IllegalStateException(
                    "Không tìm thấy giao diện: " + path
            );
        }

        return new FXMLLoader(view);
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = createLoader(
                    "/views/dashboard.fxml"
            );

            Parent dashboard = loader.load();

            DashboardController controller =
                    loader.getController();

            controller.setOpenAppointmentList(() ->
                    openAppointmentList(
                            controller.getWorkingDate()
                    )
            );

            contentArea.getChildren().setAll(dashboard);
            setMenuActive(btnDashboard);
            if (lblHeaderTitle != null) lblHeaderTitle.setText("Bảng điều khiển");

        } catch (Exception exception) {
            showLoadError(
                    btnDashboard,
                    "Bảng điều khiển",
                    exception
            );
        }
    }

    private void openAppointmentList(LocalDate date) {
        try {
            FXMLLoader loader = createLoader(
                    "/views/appointment.fxml"
            );

            Parent view = loader.load();

            AppointmentController controller =
                    loader.getController();

            controller.setExamDate(date);

            contentArea.getChildren().setAll(view);
            setMenuActive(btnAppointment);
            if (lblHeaderTitle != null) lblHeaderTitle.setText("Danh sách khám trong ngày");

        } catch (Exception exception) {
            showLoadError(
                    btnAppointment,
                    "Danh sách khám trong ngày",
                    exception
            );
        }
    }

    private void openPatientScreen() {
        try {
            FXMLLoader loader = createLoader("/views/patient.fxml");
            Parent view = loader.load();
            PatientController controller = loader.getController();
            controller.setOnOpenHistory(() ->
                    openScreen(btnMedicalReport, "Lịch sử khám", "/views/exam_history.fxml")
            );
            contentArea.getChildren().setAll(view);
            setMenuActive(btnPatients);
            if (lblHeaderTitle != null) lblHeaderTitle.setText("Danh sách bệnh nhân");
        } catch (Exception exception) {
            showLoadError(btnPatients, "Danh sách bệnh nhân", exception);
        }
    }

    private void openScreen(HBox menuItem, String title, String fxmlPath) {
        try {
            FXMLLoader loader = createLoader(fxmlPath);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            setMenuActive(menuItem);
            if (lblHeaderTitle != null) lblHeaderTitle.setText(title);
        } catch (Exception exception) {
            showLoadError(menuItem, title, exception);
        }
    }

    private void showLoadError(
            HBox menuItem,
            String title,
            Exception exception
    ) {
        exception.printStackTrace();

        showPanel(
                menuItem,
                "Chưa mở được " + title,
                "Xem chi tiết lỗi trong Terminal.\n"
                + "Kiểm tra đã thay đồng thời "
                + "FXML và controller tương ứng."
        );
    }

    private void bindPendingScreen(
            HBox menuItem,
            String title
    ) {
        menuItem.setOnMouseClicked(event ->
                showPanel(
                        menuItem,
                        title,
                        "Giao diện chi tiết chưa được nối.\n"
                        + "Controller cũ chưa được chạy "
                        + "để tránh truy cập cơ sở dữ liệu."
                )
        );
    }

    private void showPanel(
            HBox menuItem,
            String title,
            String message
    ) {
        Label heading = new Label(title);
        heading.setWrapText(true);
        heading.setStyle(
                "-fx-font-size: 26px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #2E7D32;"
        );

        Label description = new Label(message);
        description.setWrapText(true);
        description.setStyle(
                "-fx-font-size: 15px;"
                + "-fx-text-fill: #607D8B;"
        );

        Label note = new Label(
                "Demo giao diện — không kết nối CSDL"
        );
        note.setWrapText(true);
        note.setStyle(
                "-fx-background-color: #E8F5E9;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 10 14;"
                + "-fx-text-fill: #388E3C;"
        );

        VBox panel = new VBox(
                18, heading, description, note
        );

        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(32));
        panel.setStyle(
                "-fx-background-color: #FAFAFA;"
        );

        contentArea.getChildren().setAll(panel);
        setMenuActive(menuItem);
    }

    private void setItemText(
            HBox menuItem,
            String text
    ) {
        for (var child : menuItem.getChildren()) {
            if (child instanceof Label label) {
                label.setText(text);
                label.setWrapText(true);
                break;
            }
        }
    }

    private void setMenuActive(HBox selectedItem) {
        if (currentActiveItem != null) {
            currentActiveItem.getStyleClass()
                    .remove("active");
        }

        if (!selectedItem.getStyleClass()
                .contains("active")) {
            selectedItem.getStyleClass().add("active");
        }

        currentActiveItem = selectedItem;

        FadeTransition fade = new FadeTransition(
                Duration.millis(150),
                selectedItem
        );

        fade.setFromValue(0.85);
        fade.setToValue(1.0);
        fade.play();
    }
}