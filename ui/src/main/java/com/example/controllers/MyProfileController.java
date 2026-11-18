package com.example.controllers;

import com.example.DAO.StaffDAO;
import com.example.model.Role;
import com.example.model.StaffModel;
import com.example.model.UserContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class MyProfileController {

    // Personal Information Fields
    @FXML private TextField tfId;
    @FXML private TextField tfUsername;
    @FXML private TextField tfFullName;
    @FXML private ComboBox<Role> cbRole;
    @FXML private TextField tfLastName;
    @FXML private TextField tfName;
    @FXML private TextField tfCCCD;
    @FXML private ToggleButton btnMale;
    @FXML private ToggleButton btnFemale;
    @FXML private DatePicker dpBirth;
    @FXML private TextField tfPhone;

    // Contact Information Fields
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private TextField tfPasswordVisible; // For showing password
    @FXML private Button btnTogglePassword;
    @FXML private StackPane passwordContainer;
    @FXML private TextField tfAddress;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private HBox confirmPasswordContainer;

    // Work Information Fields
    @FXML private TextField tfSalary;

    // ToggleGroup for gender selection
    private ToggleGroup genderGroup;

    // Current staff data
    private StaffModel currentStaff;

    // Password management
    private boolean isPasswordVisible = false;
    private boolean isPasswordChanged = false;
    private String originalPassword = "";

    // Decimal formatter for salary
    private DecimalFormat salaryFormatter = new DecimalFormat("#,###");

    // Role options
    private ObservableList<Role> roleOptions = FXCollections.observableArrayList(
            Role.NURSE, Role.DOCTOR, Role.MANAGER, Role.ADMIN
    );

    // Validation patterns
    private final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");
    private final Pattern CCCD_PATTERN = Pattern.compile("^[0-9]{9,12}$");

    @FXML
    public void initialize() {
        setupGenderToggleGroup();
        setupRoleComboBox();
        setupDatePicker();
        setupNumericFields();
        setupPasswordFields();
        setupValidation();
        setupFullNameField();
        loadUserData();
    }

    private void setupFullNameField() {
        tfFullName.textProperty().addListener((observable, oldValue, newValue) -> {
            String value = newValue == null ? "" : newValue.trim();
            String[] parts = value.split("\\s+", 2);
            if (parts.length == 2) {
                tfLastName.setText(parts[0]);
                tfName.setText(parts[1]);
            } else {
                tfLastName.setText("");
                tfName.setText(value);
            }
        });
    }

    private void setupGenderToggleGroup() {
        genderGroup = new ToggleGroup();
        btnMale.setToggleGroup(genderGroup);
        btnFemale.setToggleGroup(genderGroup);

        // Set default selection
        btnMale.setSelected(true);
    }

    private void setupRoleComboBox() {
        cbRole.setItems(roleOptions);
        cbRole.getSelectionModel().selectFirst();

        // Set up StringConverter to display Vietnamese names
        cbRole.setConverter(new StringConverter<Role>() {
            @Override
            public String toString(Role role) {
                return role != null ? role.toVietnamese() : "";
            }

            @Override
            public Role fromString(String string) {
                try {
                    return Role.fromVietnamese(string);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        });
    }

    private void setupDatePicker() {
        dpBirth.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ?
                        LocalDate.parse(string, formatter) : null;
            }
        });
    }

    // Trong phương thức setupPasswordFields(), thay đổi phần setup toggle button:

    private void setupPasswordFields() {
        // Initially hide confirm password
        confirmPasswordContainer.setVisible(false);

        // Setup password visibility toggle
        tfPasswordVisible.setVisible(false);

        // Bind visible password field with password field
        tfPasswordVisible.textProperty().bindBidirectional(tfPassword.textProperty());

        // Monitor password changes
        tfPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalPassword)) {
                if (!isPasswordChanged) {
                    isPasswordChanged = true;
                    showConfirmPasswordField();
                }
            } else {
                if (isPasswordChanged && newValue.equals(originalPassword)) {
                    isPasswordChanged = false;
                    hideConfirmPasswordField();
                }
            }
        });

        // Setup toggle button
        btnTogglePassword.setOnAction(e -> togglePasswordVisibility());

        // Set initial button text và style
        btnTogglePassword.setText("show");
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Show password
            tfPassword.setVisible(false);

            tfPasswordVisible.setVisible(true);

            btnTogglePassword.setText("hide");
        } else {
            // Hide password
            tfPasswordVisible.setVisible(false);

            tfPassword.setVisible(true);

            btnTogglePassword.setText("show");
        }
    }

    private void showConfirmPasswordField() {
        confirmPasswordContainer.setVisible(true);

        tfConfirmPassword.setText(""); // Clear confirm password when showing
    }

    private void hideConfirmPasswordField() {
        confirmPasswordContainer.setVisible(false);

        tfConfirmPassword.setText(""); // Clear confirm password when hiding
    }

    private void setupNumericFields() {
        // Format salary with thousand separators
        tfSalary.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                tfSalary.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Format salary display when focus is lost
        tfSalary.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !tfSalary.getText().isEmpty()) {
                try {
                    double salary = Double.parseDouble(tfSalary.getText().replaceAll("[^\\d]", ""));
                    tfSalary.setText(salaryFormatter.format(salary));
                } catch (NumberFormatException e) {
                    // Keep original text if parsing fails
                }
            }
        });

        // Only allow numbers in phone field
        tfPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                tfPhone.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Only allow numbers in CCCD field
        tfCCCD.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                tfCCCD.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void setupValidation() {
        // Email validation
        tfEmail.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // When focus is lost
                validateEmail();
            }
        });

        // Phone validation
        tfPhone.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                validatePhone();
            }
        });

        // CCCD validation
        tfCCCD.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                validateCCCD();
            }
        });

        // Password confirmation validation
        tfConfirmPassword.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                validatePasswordMatch();
            }
        });
    }

    private void loadUserData() {
        try {
            String userId = UserContext.getInstance().getUserId();
            if (userId != null) {
                currentStaff = StaffDAO.getByID(userId);
                if (currentStaff != null) {
                    populateFields(currentStaff);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("DB error loading user data: " + e.getMessage());
        }
        loadDefaultData();
    }

    private void loadDefaultData() {
        tfId.setText("123");
        tfUsername.setText("123");
        tfFullName.setText("Nguyễn Văn An");
        tfLastName.setText("Nguyễn");
        tfName.setText("Văn An");
        tfCCCD.setText("079200001234");
        tfCCCD.setText("079200001234");
        btnMale.setSelected(true);
        dpBirth.setValue(LocalDate.of(1990, 5, 15));
        tfPhone.setText("0901234567");
        tfEmail.setText("admin@phongmach.com");
        tfAddress.setText("123 Nguyễn Trãi, Q1, TP.HCM");
        tfSalary.setText("15,000,000");
        if (cbRole != null && !cbRole.getItems().isEmpty()) {
            cbRole.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void openChangePasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Cập nhật mật khẩu tài khoản");

        ButtonType saveBtnType = new ButtonType("Lưu mật khẩu", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtnType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, cancelBtnType);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 24, 10, 24));

        PasswordField pfOld = new PasswordField();
        pfOld.setPromptText("Nhập mật khẩu hiện tại");
        PasswordField pfNew = new PasswordField();
        pfNew.setPromptText("Nhập mật khẩu mới (tối thiểu 6 ký tự)");
        PasswordField pfConfirm = new PasswordField();
        pfConfirm.setPromptText("Nhập lại mật khẩu mới");

        grid.add(new Label("Mật khẩu hiện tại:"), 0, 0);
        grid.add(pfOld, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(pfNew, 1, 1);
        grid.add(new Label("Nhập lại MK mới:"), 0, 2);
        grid.add(pfConfirm, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                String oldPass = pfOld.getText();
                String newPass = pfNew.getText();
                String confirmPass = pfConfirm.getText();

                if (oldPass == null || oldPass.isEmpty()) {
                    showErrorAlert("Lỗi", "Vui lòng nhập mật khẩu hiện tại!");
                    return;
                }
                if (newPass == null || newPass.length() < 6) {
                    showErrorAlert("Lỗi", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    showErrorAlert("Lỗi", "Mật khẩu xác nhận không trùng khớp!");
                    return;
                }

                showSuccessAlert("Thành công", "Đổi mật khẩu thành công!");
            }
        });
    }

    private void populateFields(StaffModel staff) {
        tfId.setText(staff.getId());
        tfLastName.setText(staff.getLastname());
        tfName.setText(staff.getFirstname());
        tfFullName.setText(staff.getFullName());
        String username = UserContext.getInstance().getUsername();
        tfUsername.setText(username != null ? username : staff.getId());
        tfCCCD.setText(staff.getCccd());
        tfPhone.setText(staff.getPhone());
        tfEmail.setText(staff.getEmail());
        tfAddress.setText(staff.getAddress());
        tfSalary.setText(salaryFormatter.format(staff.getLuong()));
        dpBirth.setValue(staff.getBirthday());

        // Set gender
        if ("Nam".equals(staff.getGender())) {
            btnMale.setSelected(true);
        } else {
            btnFemale.setSelected(true);
        }

        // Set role
        try {
            Role role = Role.valueOf(staff.getRole());
            cbRole.getSelectionModel().select(role);
        } catch (IllegalArgumentException e) {
            cbRole.getSelectionModel().selectFirst(); // Default selection if role not found
        }

        // Load password from database
        String password = staff.getPassword();
        if (password != null && !password.isEmpty()) {
            tfPassword.setText(password);
            originalPassword = password;
        } else {
            tfPassword.setText("");
            originalPassword = "";
        }

        // Reset password change flag
        isPasswordChanged = false;
        hideConfirmPasswordField();
    }



    @FXML
    private void handleSaveChanges() {
        if (validateForm()) {
            try {
                saveUserData();
                showSuccessAlert("Cập nhật thành công!", "Thông tin cá nhân đã được lưu.");

                // Update original password if changed
                if (isPasswordChanged) {
                    originalPassword = tfPassword.getText();
                    isPasswordChanged = false;
                    hideConfirmPasswordField();
                }
            } catch (Exception e) {
                showErrorAlert("Lỗi", "Có lỗi xảy ra khi lưu thông tin: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Validate required fields
        if (tfId.getText().trim().isEmpty()) {
            errors.append("- Mã nhân viên không được để trống\n");
        }

        if (tfFullName == null || tfFullName.getText().trim().isEmpty()) {
            errors.append("- Họ tên không được để trống\n");
        }

        if (tfPhone.getText().trim().isEmpty()) {
            errors.append("- Số điện thoại không được để trống\n");
        } else if (!PHONE_PATTERN.matcher(tfPhone.getText()).matches()) {
            errors.append("- Số điện thoại không hợp lệ (10-11 chữ số)\n");
        }

        if (tfEmail.getText().trim().isEmpty()) {
            errors.append("- Email không được để trống\n");
        } else if (!EMAIL_PATTERN.matcher(tfEmail.getText()).matches()) {
            errors.append("- Email không hợp lệ\n");
        }

        // Only validate password confirmation if password was changed
        if (isPasswordChanged) {
            if (tfConfirmPassword.getText().trim().isEmpty()) {
                errors.append("- Xác nhận mật khẩu không được để trống\n");
            } else if (!tfPassword.getText().equals(tfConfirmPassword.getText())) {
                errors.append("- Mật khẩu xác nhận không khớp\n");
            }
        }

        if (dpBirth.getValue() == null) {
            errors.append("- Ngày sinh không được để trống\n");
        } else if (dpBirth.getValue().isAfter(LocalDate.now().minusYears(16))) {
            errors.append("- Tuổi phải từ 16 trở lên\n");
        }

        if (tfSalary.getText().trim().isEmpty()) {
            errors.append("- Mức lương không được để trống\n");
        }

        if (errors.length() > 0) {
            showErrorAlert("Thông tin không hợp lệ", errors.toString());
            return false;
        }

        return true;
    }

    private boolean validateEmail() {
        if (!tfEmail.getText().trim().isEmpty() &&
                !EMAIL_PATTERN.matcher(tfEmail.getText()).matches()) {
            tfEmail.setStyle("-fx-border-color: red;");
            return false;
        }
        tfEmail.setStyle("");
        return true;
    }

    private boolean validatePhone() {
        if (!tfPhone.getText().trim().isEmpty() &&
                !PHONE_PATTERN.matcher(tfPhone.getText()).matches()) {
            tfPhone.setStyle("-fx-border-color: red;");
            return false;
        }
        tfPhone.setStyle("");
        return true;
    }

    private boolean validateCCCD() {
        if (!tfCCCD.getText().trim().isEmpty() &&
                !CCCD_PATTERN.matcher(tfCCCD.getText()).matches()) {
            tfCCCD.setStyle("-fx-border-color: red;");
            return false;
        }
        tfCCCD.setStyle("");
        return true;
    }

    private boolean validatePasswordMatch() {
        if (isPasswordChanged && !tfPassword.getText().equals(tfConfirmPassword.getText())) {
            tfConfirmPassword.setStyle("-fx-border-color: red;");
            return false;
        }
        tfConfirmPassword.setStyle("");
        return true;
    }

    private void saveUserData() {
        // Get selected gender
        String gender = btnMale.isSelected() ? "Nam" : "Nữ";

        // Parse salary (remove formatting)
        double salary = 0;
        try {
            salary = Double.parseDouble(tfSalary.getText().replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Lỗi định dạng mức lương");
        }

        // Update current staff model
        if (currentStaff == null) {
            currentStaff = new StaffModel();
        }

        currentStaff.setId(tfId.getText());
        currentStaff.setLastname(tfLastName.getText());
        currentStaff.setFirstname(tfName.getText());
        currentStaff.setRole(cbRole.getSelectionModel().getSelectedItem().toString());
        currentStaff.setLuong(salary);
        currentStaff.setBirthday(dpBirth.getValue());
        currentStaff.setGender(gender);
        currentStaff.setCccd(tfCCCD.getText());
        currentStaff.setAddress(tfAddress.getText());
        currentStaff.setEmail(tfEmail.getText());
        currentStaff.setPhone(tfPhone.getText());

        // Only update password if it was changed
        if (isPasswordChanged) {
            currentStaff.setPassword(tfPassword.getText());
        }

        // Debug output
        System.out.println("Saving user data:");
        System.out.println("ID: " + currentStaff.getId());
        System.out.println("Name: " + currentStaff.getLastname() + " " + currentStaff.getFirstname());
        System.out.println("Gender: " + currentStaff.getGender());
        System.out.println("Role: " + currentStaff.getRole());
        System.out.println("CCCD: " + currentStaff.getCccd());
        System.out.println("Phone: " + currentStaff.getPhone());
        System.out.println("Email: " + currentStaff.getEmail());
        System.out.println("Address: " + currentStaff.getAddress());
        System.out.println("Birth Date: " + currentStaff.getBirthday());
        System.out.println("Salary: " + currentStaff.getLuong());
        System.out.println("Password changed: " + isPasswordChanged);

        StaffDAO.updateStaff(currentStaff);
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getter methods for accessing current staff data
    public StaffModel getCurrentStaff() {
        return currentStaff;
    }

    public String getEmployeeId() {
        return tfId.getText();
    }

    public String getFullName() {
        return tfLastName.getText() + " " + tfName.getText();
    }

    public String getGender() {
        return btnMale.isSelected() ? "Nam" : "Nữ";
    }

    public Role getRole() {
        return cbRole.getSelectionModel().getSelectedItem();
    }

    public String getRoleVietnamese() {
        Role selectedRole = cbRole.getSelectionModel().getSelectedItem();
        return selectedRole != null ? selectedRole.toVietnamese() : "";
    }

    public String getCCCD() {
        return tfCCCD.getText();
    }

    public String getPhone() {
        return tfPhone.getText();
    }

    public String getEmail() {
        return tfEmail.getText();
    }

    public String getAddress() {
        return tfAddress.getText();
    }

    public LocalDate getBirthDate() {
        return dpBirth.getValue();
    }

    public double getSalary() {
        try {
            return Double.parseDouble(tfSalary.getText().replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getSalaryFormatted() {
        return tfSalary.getText();
    }

    public boolean isPasswordChanged() {
        return isPasswordChanged;
    }
}