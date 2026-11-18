package com.example.controllers;

import com.example.DAO.AccountDAO;
import com.example.model.AccountModel;
import com.example.model.UserContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountController {
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbRoleFilter;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TableView<AccountModel> tvAccounts;
    @FXML private TableColumn<AccountModel, String> colUsername;
    @FXML private TableColumn<AccountModel, String> colEmployeeId;
    @FXML private TableColumn<AccountModel, String> colEmployeeName;
    @FXML private TableColumn<AccountModel, String> colRole;
    @FXML private TableColumn<AccountModel, String> colStatus;
    @FXML private TableColumn<AccountModel, AccountModel> colAction;
    @FXML private Button btnEdit;
    @FXML private Button btnResetPassword;
    @FXML private Button btnToggleStatus;
    @FXML private Label lblTotal;
    @FXML private Label lblDataNote;

    private final ObservableList<AccountModel> accounts = FXCollections.observableArrayList();
    private FilteredList<AccountModel> filteredAccounts;
    private List<String> employeeChoices = List.of();
    private boolean demoMode;

    @FXML
    private void initialize() {
        tvAccounts.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        cbRoleFilter.getItems().setAll("Tất cả", "Quản trị viên", "Bác sĩ", "Y tá", "Quản lý");
        cbRoleFilter.setValue("Tất cả");
        cbStatusFilter.getItems().setAll("Tất cả", "Đang đăng nhập", "Hoạt động", "Đã khóa");
        cbStatusFilter.setValue("Tất cả");

        colUsername.setCellValueFactory(row -> new javafx.beans.property.ReadOnlyStringWrapper(
                row.getValue().getUsername()
        ));
        colEmployeeId.setCellValueFactory(row -> new javafx.beans.property.ReadOnlyStringWrapper(
                row.getValue().getEmployeeId()
        ));
        colEmployeeName.setCellValueFactory(row -> new javafx.beans.property.ReadOnlyStringWrapper(
                row.getValue().getEmployeeName()
        ));
        colRole.setCellValueFactory(row -> new javafx.beans.property.ReadOnlyStringWrapper(
                row.getValue().getRoleLabel()
        ));
        colStatus.setCellValueFactory(row -> new javafx.beans.property.ReadOnlyStringWrapper(
                row.getValue().getDisplayStatus()
        ));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                setText(empty ? null : status);
                setStyle(empty ? "" : ("Đã khóa".equals(status)
                        ? "-fx-text-fill: #B91C1C; -fx-font-weight: bold;"
                        : "-fx-text-fill: #15803D; -fx-font-weight: bold;"));
            }
        });
        configureActionColumn();

        filteredAccounts = new FilteredList<>(accounts, account -> true);
        tvAccounts.setItems(filteredAccounts);
        tfSearch.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        cbRoleFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        cbStatusFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        tvAccounts.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, selected) -> updateSelectionActions(selected)
        );
        btnEdit.setDisable(true);
        btnResetPassword.setDisable(true);
        btnToggleStatus.setDisable(true);
        loadAccounts();
    }

    private void configureActionColumn() {
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button();

            {
                button.setStyle(
                        "-fx-background-color: #F1F5F9; -fx-background-radius: 7;"
                                + "-fx-text-fill: #334155; -fx-font-size: 11px;"
                );
                button.setOnAction(event -> {
                    AccountModel account = getTableView().getItems().get(getIndex());
                    toggleAccountStatus(account);
                });
            }

            @Override
            protected void updateItem(AccountModel account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setGraphic(null);
                } else {
                    button.setText("Đã khóa".equals(account.getStatus()) ? "Mở khóa" : "Khóa");
                    button.setDisable(account.isLoggedIn());
                    setGraphic(button);
                }
            }
        });
    }

    private void loadAccounts() {
        try {
            List<AccountModel> loaded = AccountDAO.getAll();
            if (loaded.isEmpty()) {
                employeeChoices = AccountDAO.getAvailableEmployeeLabels();
                if (employeeChoices.isEmpty()) {
                    demoMode = true;
                    accounts.setAll(createDemoAccounts());
                    employeeChoices = demoEmployeeChoices();
                    lblDataNote.setText("Dữ liệu minh họa — thay đổi chỉ lưu trong phiên hiện tại.");
                } else {
                    demoMode = false;
                    accounts.clear();
                    lblDataNote.setText("");
                }
            } else {
                demoMode = false;
                String currentUsername = UserContext.getInstance().getUsername();
                loaded.forEach(account -> account.setLoggedIn(
                        currentUsername != null && currentUsername.equals(account.getUsername())
                ));
                accounts.setAll(loaded);
                employeeChoices = AccountDAO.getAvailableEmployeeLabels();
                lblDataNote.setText("");
            }
        } catch (SQLException exception) {
            demoMode = true;
            accounts.setAll(createDemoAccounts());
            employeeChoices = demoEmployeeChoices();
            lblDataNote.setText("Dữ liệu minh họa — không kết nối được CSDL tài khoản.");
            System.err.println("Không thể tải tài khoản: " + exception.getMessage());
        }
        applyFilter();
        updateTotal();
    }

    private List<AccountModel> createDemoAccounts() {
        return List.of(
                new AccountModel("admin01", "NV0001", "Nguyễn Văn A", "ADMIN", "Hoạt động", true),
                new AccountModel("bacs01", "NV0002", "Trần Thị B", "DOCTOR", "Hoạt động", false),
                new AccountModel("yta01", "NV0003", "Lê Văn C", "NURSE", "Hoạt động", false),
                new AccountModel("qlnv01", "NV0004", "Phạm Thị D", "MANAGER", "Hoạt động", false),
                new AccountModel("admin02", "NV0005", "Hoàng Văn E", "ADMIN", "Hoạt động", false),
                new AccountModel("bacs02", "NV0006", "Võ Thị H", "DOCTOR", "Hoạt động", false)
        );
    }

    private List<String> demoEmployeeChoices() {
        Set<String> assigned = new HashSet<>();
        accounts.forEach(account -> assigned.add(account.getEmployeeId()));
        List<String> choices = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            String id = String.format("NV%04d", i);
            if (!assigned.contains(id)) {
                choices.add(id + " - Nhân viên mẫu " + String.format("%02d", i));
            }
        }
        return choices;
    }

    private void applyFilter() {
        if (filteredAccounts == null) {
            return;
        }
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String role = cbRoleFilter.getValue();
        String status = cbStatusFilter.getValue();
        filteredAccounts.setPredicate(account -> {
            boolean matchesText = keyword.isEmpty()
                    || contains(account.getUsername(), keyword)
                    || contains(account.getEmployeeId(), keyword)
                    || contains(account.getEmployeeName(), keyword);
            boolean matchesRole = role == null || "Tất cả".equals(role)
                    || role.equals(account.getRoleLabel());
            boolean matchesStatus = status == null || "Tất cả".equals(status)
                    || status.equals(account.getDisplayStatus())
                    || ("Hoạt động".equals(status) && "Đang đăng nhập".equals(account.getDisplayStatus()));
            return matchesText && matchesRole && matchesStatus;
        });
        updateTotal();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void updateTotal() {
        if (lblTotal != null) {
            lblTotal.setText("Tổng số tài khoản: " + accounts.size());
        }
    }

    private void updateSelectionActions(AccountModel account) {
        boolean selected = account != null;
        btnEdit.setDisable(!selected);
        btnResetPassword.setDisable(!selected);
        btnToggleStatus.setDisable(!selected || account.isLoggedIn());
        if (selected) {
            btnToggleStatus.setText("Đã khóa".equals(account.getStatus()) ? "Mở khóa" : "Khóa/Mở khóa");
        }
    }

    @FXML
    private void handleCreate() {
        if (employeeChoices.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không còn nhân viên chưa có tài khoản.");
            return;
        }
        openDialog(controller -> controller.configureCreate(employeeChoices, this::createAccount));
    }

    @FXML
    private void handleEdit() {
        AccountModel account = tvAccounts.getSelectionModel().getSelectedItem();
        if (account != null) {
            openDialog(controller -> controller.configureEdit(account, this::updateAccount));
        }
    }

    @FXML
    private void handleResetPassword() {
        AccountModel account = tvAccounts.getSelectionModel().getSelectedItem();
        if (account != null) {
            openDialog(controller -> controller.configureResetPassword(account, this::resetPassword));
        }
    }

    @FXML
    private void handleToggleStatus() {
        AccountModel account = tvAccounts.getSelectionModel().getSelectedItem();
        if (account != null) {
            toggleAccountStatus(account);
        }
    }

    private void openDialog(java.util.function.Consumer<AccountDialogController> configure) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/account_dialog.fxml"));
            Parent root = loader.load();
            configure.accept(loader.getController());
            Stage dialog = new Stage();
            dialog.setTitle("Tài khoản");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tvAccounts.getScene() != null && tvAccounts.getScene().getWindow() != null) {
                dialog.initOwner(tvAccounts.getScene().getWindow());
            }
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException exception) {
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Không thể mở hộp thoại tài khoản: " + exception.getMessage());
        }
    }

    private boolean createAccount(AccountDialogController.AccountForm form) {
        if (accounts.stream().anyMatch(account ->
                account.getUsername().equalsIgnoreCase(form.username())
                        || account.getEmployeeId().equals(form.employeeId()))) {
            showAlert(Alert.AlertType.WARNING, "Tên đăng nhập hoặc nhân viên đã có tài khoản.");
            return false;
        }
        if (!demoMode) {
            try {
                if (!AccountDAO.create(
                        form.employeeId(), form.username(), form.password(), form.roleId(), form.status()
                )) {
                    showAlert(Alert.AlertType.ERROR, "Không thể tạo tài khoản cho nhân viên đã chọn.");
                    return false;
                }
                loadAccounts();
                return true;
            } catch (SQLException exception) {
                showAlert(Alert.AlertType.ERROR, "Tạo tài khoản thất bại: " + exception.getMessage());
                return false;
            }
        }
        accounts.add(new AccountModel(
                form.username(), form.employeeId(), form.employeeName(), form.roleId(), form.status(), false
        ));
        employeeChoices = demoEmployeeChoices();
        applyFilter();
        return true;
    }

    private boolean updateAccount(AccountDialogController.AccountForm form) {
        AccountModel account = findAccount(form.employeeId());
        if (account == null) {
            return false;
        }
        if (!demoMode) {
            try {
                if (!AccountDAO.update(form.employeeId(), form.roleId(), form.status())) {
                    showAlert(Alert.AlertType.ERROR, "Không tìm thấy tài khoản để cập nhật.");
                    return false;
                }
                loadAccounts();
                return true;
            } catch (SQLException exception) {
                showAlert(Alert.AlertType.ERROR, "Cập nhật tài khoản thất bại: " + exception.getMessage());
                return false;
            }
        }
        account.setRoleId(form.roleId());
        account.setStatus(form.status());
        tvAccounts.refresh();
        applyFilter();
        return true;
    }

    private boolean resetPassword(AccountDialogController.AccountForm form) {
        if (!demoMode) {
            try {
                if (!AccountDAO.resetPassword(form.employeeId(), form.password())) {
                    showAlert(Alert.AlertType.ERROR, "Không tìm thấy tài khoản để đặt lại mật khẩu.");
                    return false;
                }
            } catch (SQLException exception) {
                showAlert(Alert.AlertType.ERROR, "Đặt lại mật khẩu thất bại: " + exception.getMessage());
                return false;
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Đã đặt lại mật khẩu cho " + form.username() + ".");
        return true;
    }

    private void toggleAccountStatus(AccountModel account) {
        if (account.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Không thể khóa tài khoản đang đăng nhập.");
            return;
        }
        String newStatus = "Đã khóa".equals(account.getStatus()) ? "Hoạt động" : "Đã khóa";
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Thay đổi trạng thái tài khoản");
        confirmation.setHeaderText(null);
        confirmation.setContentText(
                ("Đã khóa".equals(newStatus) ? "Khóa" : "Mở khóa")
                        + " tài khoản \"" + account.getUsername() + "\"?"
        );
        confirmation.showAndWait().filter(ButtonType.OK::equals).ifPresent(button -> {
            if (!demoMode) {
                try {
                    if (!AccountDAO.setStatus(account.getEmployeeId(), newStatus)) {
                        showAlert(Alert.AlertType.ERROR, "Không tìm thấy tài khoản để cập nhật.");
                        return;
                    }
                } catch (SQLException exception) {
                    showAlert(Alert.AlertType.ERROR, "Không thể cập nhật trạng thái: " + exception.getMessage());
                    return;
                }
            }
            account.setStatus(newStatus);
            tvAccounts.refresh();
            applyFilter();
            updateSelectionActions(account);
        });
    }

    private AccountModel findAccount(String employeeId) {
        return accounts.stream()
                .filter(account -> account.getEmployeeId().equals(employeeId))
                .findFirst()
                .orElse(null);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
