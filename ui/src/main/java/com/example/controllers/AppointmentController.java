package com.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class AppointmentController {

    @FXML private Label lblExamDate;

    @FXML
    private TableView<DemoPatient> tblAppointments;

    @FXML private TableColumn<DemoPatient, String> colOrder;
    @FXML private TableColumn<DemoPatient, String> colPatientId;
    @FXML private TableColumn<DemoPatient, String> colFullName;
    @FXML private TableColumn<DemoPatient, String> colGender;
    @FXML private TableColumn<DemoPatient, String> colBirthYear;
    @FXML private TableColumn<DemoPatient, String> colAddress;
    @FXML private TableColumn<DemoPatient, String> colStatus;

    private LocalDate examDate = LocalDate.of(2025, 7, 8);

    @FXML
    private void initialize() {
        configureColumn(colOrder, DemoPatient::order);
        configureColumn(colPatientId, DemoPatient::patientId);
        configureColumn(colFullName, DemoPatient::fullName);
        configureColumn(colGender, DemoPatient::gender);
        configureColumn(colBirthYear, DemoPatient::birthYear);
        configureColumn(colAddress, DemoPatient::address);
        configureColumn(colStatus, DemoPatient::status);

        configureStatusCells();

        tblAppointments.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tblAppointments.setItems(
                FXCollections.observableArrayList(
                        createDemoPatients()
                )
        );

        updateDateLabel();
    }

    private void configureColumn(
            TableColumn<DemoPatient, String> column,
            Function<DemoPatient, String> valueProvider
    ) {
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        valueProvider.apply(cell.getValue())
                )
        );

        column.setSortable(false);
        column.setReorderable(false);
    }

    private void configureStatusCells() {
        colStatus.setCellFactory(column ->
                new TableCell<DemoPatient, String>() {

                    private final Circle dot = new Circle(4);
                    private final Label label = new Label();
                    private final HBox content =
                            new HBox(8, dot, label);

                    {
                        content.setAlignment(Pos.CENTER_LEFT);
                        label.getStyleClass().add("status-text");
                    }

                    @Override
                    protected void updateItem(
                            String status,
                            boolean empty
                    ) {
                        super.updateItem(status, empty);

                        setText(null);

                        if (empty || status == null) {
                            setGraphic(null);
                            return;
                        }

                        label.setText(status);

                        dot.setFill(
                                "Đã khám".equals(status)
                                        ? Color.web("#559CE0")
                                        : Color.web("#68BC58")
                        );

                        setGraphic(content);
                    }
                }
        );
    }

    private List<DemoPatient> createDemoPatients() {
        return List.of(
                new DemoPatient(
                        "1", "BN001", "Nguyễn Văn H",
                        "Nam", "1985",
                        "123 Nguyễn Văn Cừ, Hà Nội", "Đang chờ"
                ),
                new DemoPatient(
                        "2", "BN002", "Trần Thị L",
                        "Nữ", "1990",
                        "45 Lê Lợi, Đà Nẵng", "Đang chờ"
                ),
                new DemoPatient(
                        "3", "BN003", "Lê Văn J",
                        "Nam", "1992",
                        "78 Phạm Văn Đồng, Hà Nội", "Đã khám"
                ),
                new DemoPatient(
                        "4", "BN004", "Phạm Thị K",
                        "Nữ", "1991",
                        "32 Trần Hưng Đạo, TP.HCM", "Đang chờ"
                ),
                new DemoPatient(
                        "5", "BN005", "Hoàng Văn L",
                        "Nam", "1988",
                        "56 Nguyễn Huệ, Hà Nội", "Đã khám"
                ),
                new DemoPatient(
                        "6", "BN006", "Võ Thị M",
                        "Nữ", "1993",
                        "12 Lý Thường Kiệt, Đà Nẵng", "Đang chờ"
                ),
                new DemoPatient(
                        "7", "BN007", "Đặng Văn N",
                        "Nam", "1990",
                        "99 Hai Bà Trưng, Hà Nội", "Đang chờ"
                ),
                new DemoPatient(
                        "8", "BN008", "Bùi Thị Q",
                        "Nữ", "1994",
                        "21 Nguyễn Trãi, TP.HCM", "Đã khám"
                ),
                new DemoPatient(
                        "9", "BN009", "Ngô Văn P",
                        "Nam", "1987",
                        "66 Trần Phú, Đà Nẵng", "Đang chờ"
                ),
                new DemoPatient(
                        "10", "BN010", "Đỗ Thị Q",
                        "Nữ", "1995",
                        "77 Nguyễn Văn Cừ, Hà Nội", "Đã khám"
                )
        );
    }

    public void setExamDate(LocalDate date) {
        if (date != null) {
            examDate = date;
        }

        updateDateLabel();
    }

    private void updateDateLabel() {
        String weekday = switch (examDate.getDayOfWeek()) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };

        lblExamDate.setText(
                weekday + ", "
                + String.format("%02d", examDate.getDayOfMonth())
                + " tháng " + examDate.getMonthValue()
                + " năm " + examDate.getYear()
        );
    }

    @FXML
    private void handleAddPatient() {
        try {
            URL resource = getClass().getResource(
                    "/views/appointment_detail.fxml"
            );

            if (resource == null) {
                throw new IllegalStateException(
                        "Không tìm thấy appointment_detail.fxml"
                );
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            AppointmentDetailController controller =
                    loader.getController();

            controller.setExamDate(examDate);

            ScrollPane scrollPane = new ScrollPane(root);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(
                    ScrollPane.ScrollBarPolicy.NEVER
            );
            scrollPane.setStyle(
                    "-fx-background: white;"
                    + "-fx-background-color: white;"
            );

            Stage dialog = new Stage();

            dialog.initOwner(
                    tblAppointments.getScene().getWindow()
            );
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Tiếp nhận bệnh nhân");

            var bounds = Screen.getPrimary()
                    .getVisualBounds();

            dialog.setScene(
                    new Scene(
                            scrollPane,
                            Math.min(640, bounds.getWidth() - 60),
                            Math.min(720, bounds.getHeight() - 100)
                    )
            );

            dialog.showAndWait();

        } catch (Exception exception) {
            exception.printStackTrace();

            showMessage(
                    "Chưa mở được hộp thoại tiếp nhận. "
                    + "Xem lỗi chi tiết trong Terminal."
            );
        }
    }

    @FXML
    private void handleRemovePatient() {
        showMessage(
                "Bản trình diễn giao diện: "
                + "chưa xử lý bỏ bệnh nhân khỏi danh sách."
        );
    }

    private void showMessage(String message) {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.initOwner(
                tblAppointments.getScene().getWindow()
        );
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public record DemoPatient(
            String order,
            String patientId,
            String fullName,
            String gender,
            String birthYear,
            String address,
            String status
    ) {
    }
}