package com.example.controllers;

import com.example.DAO.StaffDAO;
import com.example.model.StaffModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StaffController implements StaffDataChangeListener {
    @FXML private TableView<StaffModel> tvStaff;
    @FXML private TableColumn<StaffModel, String> idCol;
    @FXML private TableColumn<StaffModel, String> nameCol;
    @FXML private TableColumn<StaffModel, String> roleCol;
    @FXML private TableColumn<StaffModel, String> phoneCol;
    @FXML private TableColumn<StaffModel, String> statusCol;
    @FXML private TableColumn<StaffModel, String> salaryCol;
    @FXML private TableColumn<StaffModel, String> birthCol;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private Label lblTotalStaffs;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnLeave;

    private ObservableList<StaffModel> staffList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (idCol != null) idCol.prefWidthProperty().bind(tvStaff.widthProperty().multiply(0.18));
        if (nameCol != null) nameCol.prefWidthProperty().bind(tvStaff.widthProperty().multiply(0.24));
        if (roleCol != null) roleCol.prefWidthProperty().bind(tvStaff.widthProperty().multiply(0.18));
        if (phoneCol != null) phoneCol.prefWidthProperty().bind(tvStaff.widthProperty().multiply(0.22));
        if (statusCol != null) statusCol.prefWidthProperty().bind(tvStaff.widthProperty().multiply(0.18));

        if (statusCol != null) {
            statusCol.setCellFactory(col -> new TableCell<StaffModel, String>() {
                private final Label badge = new Label();
                private final HBox container = new HBox(badge);
                {
                    container.setAlignment(Pos.CENTER);
                    badge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3 12; -fx-background-radius: 12;");
                }

                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setGraphic(null);
                    } else {
                        badge.setText(status);
                        if ("Nghỉ việc".equalsIgnoreCase(status)) {
                            badge.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3 12; -fx-background-radius: 12;");
                        } else {
                            badge.setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3 12; -fx-background-radius: 12;");
                        }
                        setGraphic(container);
                    }
                }
            });
        }

        tvStaff.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StaffModel selected = tvStaff.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showStaffDetailPopUp(selected);
                }
            }
        });

        if (btnAdd != null) {
            btnAdd.setOnAction(event -> openAddStaffDialog());
        }

        if (btnEdit != null) {
            btnEdit.setOnAction(event -> {
                StaffModel selected = tvStaff.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showStaffDetailPopUp(selected);
                } else {
                    showAlert("Thông báo", "Vui lòng chọn một nhân viên từ danh sách để chỉnh sửa.", Alert.AlertType.INFORMATION);
                }
            });
        }

        if (btnLeave != null) {
            btnLeave.setOnAction(event -> {
                StaffModel selected = tvStaff.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selected.setStatus("Nghỉ việc");
                    tvStaff.refresh();
                    showAlert("Thành công", "Đã cập nhật trạng thái nhân viên " + selected.getFullName() + " thành Nghỉ việc.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Thông báo", "Vui lòng chọn một nhân viên từ danh sách.", Alert.AlertType.INFORMATION);
                }
            });
        }

        if (cbStatusFilter != null) {
            cbStatusFilter.getItems().setAll("Tất cả", "Đang làm việc", "Nghỉ việc");
            cbStatusFilter.setValue("Tất cả");
        }

        loadStaffData();
    }

    private void openAddStaffDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/staff_detail.fxml"));
            Parent root = loader.load();
            StaffDetailController controller = loader.getController();
            controller.setDataChangeListener(this);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm nhân viên");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ thêm nhân viên: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showStaffDetailPopUp(StaffModel staffModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/staff_detail.fxml"));
            Parent root = loader.load();
            StaffDetailController controller = loader.getController();
            controller.setStaff(staffModel);
            controller.setDataChangeListener(this);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chi tiết nhân viên");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ chi tiết nhân viên: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Override
    public void onDataChanged(StaffModel updatedStaff, String action) {
        if ("INSERT".equals(action)) {
            staffList.add(updatedStaff);
        } else if ("UPDATE".equals(action)) {
            for (int i = 0; i < staffList.size(); i++) {
                if (staffList.get(i).getId().equals(updatedStaff.getId())) {
                    staffList.set(i, updatedStaff);
                    break;
                }
            }
        } else if ("DELETE".equals(action)) {
            staffList.removeIf(staff -> staff.getId().equals(updatedStaff.getId()));
        }

        tvStaff.refresh();
        updateTotalLabel(staffList.size());
    }

    public void loadStaffData() {
        try {
            var dbList = StaffDAO.getAll();
            if (dbList != null && !dbList.isEmpty()) {
                staffList = FXCollections.observableArrayList(dbList);
            } else {
                staffList = getSampleStaffData();
            }
        } catch (Exception ex) {
            // Fallback dữ liệu mẫu chuẩn PDF nếu DB chưa mở
            staffList = getSampleStaffData();
        }

        if (idCol != null) {
            idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        }
        if (nameCol != null) {
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        }
        if (roleCol != null) {
            roleCol.setCellValueFactory(data -> new SimpleStringProperty(formatRole(data.getValue().getRole())));
        }
        if (phoneCol != null) {
            phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        }
        if (statusCol != null) {
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        }
        if (salaryCol != null) {
            salaryCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.0f", data.getValue().getLuong())));
        }
        if (birthCol != null) {
            birthCol.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getBirthday() != null ? data.getValue().getBirthday().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
        }

        FilteredList<StaffModel> filteredData = new FilteredList<>(staffList, p -> true);

        // Lắng nghe tìm kiếm từ khóa và lọc trạng thái
        Runnable updateFilter = () -> {
            String keyword = tfSearch != null && tfSearch.getText() != null ? tfSearch.getText().toLowerCase().trim() : "";
            String statusFilter = cbStatusFilter != null && cbStatusFilter.getValue() != null ? cbStatusFilter.getValue() : "Tất cả";

            filteredData.setPredicate(staff -> {
                boolean matchesKeyword = keyword.isEmpty()
                        || staff.getId().toLowerCase().contains(keyword)
                        || staff.getFullName().toLowerCase().contains(keyword)
                        || staff.getRole().toLowerCase().contains(keyword)
                        || staff.getPhone().toLowerCase().contains(keyword);

                boolean matchesStatus = "Tất cả".equalsIgnoreCase(statusFilter)
                        || staff.getStatus().equalsIgnoreCase(statusFilter);

                return matchesKeyword && matchesStatus;
            });
            updateTotalLabel(filteredData.size());
        };

        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldV, newV) -> updateFilter.run());
        }
        if (cbStatusFilter != null) {
            cbStatusFilter.valueProperty().addListener((obs, oldV, newV) -> updateFilter.run());
        }

        SortedList<StaffModel> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tvStaff.comparatorProperty());
        tvStaff.setItems(sortedData);
        updateTotalLabel(staffList.size());
    }

    private void updateTotalLabel(int count) {
        if (lblTotalStaffs != null) {
            lblTotalStaffs.setText("Tổng Dữ liệu viên: " + count);
        }
    }

    private String formatRole(String role) {
        if (role == null) return "Bác sĩ";
        return switch (role) {
            case "DOCTOR" -> "Bác sĩ";
            case "NURSE" -> "Điều dưỡng";
            case "PHARMACIST" -> "Dược sĩ";
            case "MANAGER", "ADMIN" -> "Quản lý";
            default -> role;
        };
    }

    private ObservableList<StaffModel> getSampleStaffData() {
        ObservableList<StaffModel> list = FXCollections.observableArrayList();
        StaffModel s1 = new StaffModel("NV001", "Nguyễn", "Văn A", "Bác sĩ", 15000000, LocalDate.of(1990, 5, 15), "Nam", "123456789", "123 Nguyễn Trãi, Q1", "admin@phongmach.com", "0901234567", "123456");
        s1.setStatus("Đang làm việc");

        StaffModel s2 = new StaffModel("NV002", "Trần", "Thị B", "Điều dưỡng", 10000000, LocalDate.of(1993, 8, 20), "Nữ", "234567890", "45 Lê Lợi, Q1", "tranb@phongmach.com", "0912345678", "123456");
        s2.setStatus("Đang làm việc");

        StaffModel s3 = new StaffModel("NV003", "Lê", "Văn C", "Dược sĩ", 12000000, LocalDate.of(1988, 3, 10), "Nam", "345678901", "78 Phạm Văn Đồng", "levanc@phongmach.com", "0923456789", "123456");
        s3.setStatus("Nghỉ việc");

        StaffModel s4 = new StaffModel("NV004", "Phạm", "Thị D", "Điều dưỡng", 10000000, LocalDate.of(1995, 11, 25), "Nữ", "456789012", "56 Nguyễn Huệ", "phamthid@phongmach.com", "0934567890", "123456");
        s4.setStatus("Đang làm việc");

        StaffModel s5 = new StaffModel("NV005", "Hoàng", "Văn E", "Bác sĩ", 18000000, LocalDate.of(1985, 2, 14), "Nam", "567890123", "99 Hai Bà Trưng", "hoangvane@phongmach.com", "0945678901", "123456");
        s5.setStatus("Đang làm việc");

        list.addAll(s1, s2, s3, s4, s5);
        return list;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
