package com.example.controllers;

import com.example.DAO.QuiDinhDAO;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class ClinicRulesController {
    private enum CatalogType {
        DISEASE,
        UNIT,
        USAGE
    }

    private record RuleEntry(String code, String name, String details) {}

    @FXML private Button btnParameters;
    @FXML private Button btnDiseases;
    @FXML private Button btnUnits;
    @FXML private Button btnUsage;
    @FXML private javafx.scene.layout.VBox parametersPane;
    @FXML private javafx.scene.layout.VBox catalogPane;
    @FXML private Spinner<Integer> spMaxPatients;
    @FXML private Spinner<Integer> spExamFee;
    @FXML private Label lblParametersNote;
    @FXML private Label lblCatalogTitle;
    @FXML private Label lblCode;
    @FXML private Label lblName;
    @FXML private Label lblDetails;
    @FXML private Label lblCatalogNote;
    @FXML private TextField tfSearch;
    @FXML private TextField tfCode;
    @FXML private TextField tfName;
    @FXML private TextField tfDetails;
    @FXML private TableView<RuleEntry> tvCatalog;
    @FXML private TableColumn<RuleEntry, String> colCode;
    @FXML private TableColumn<RuleEntry, String> colName;
    @FXML private TableColumn<RuleEntry, String> colDetails;
    @FXML private Button btnEditEntry;
    @FXML private Button btnDeleteEntry;
    @FXML private Button btnSaveEntry;

    private final Map<CatalogType, ObservableList<RuleEntry>> catalogs =
            new EnumMap<>(CatalogType.class);
    private CatalogType currentType;
    private FilteredList<RuleEntry> filteredEntries;
    private String editingCode;
    private boolean addingEntry;
    private boolean demoParameters;

    @FXML
    private void initialize() {
        spMaxPatients.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 40));
        spMaxPatients.setEditable(true);
        spExamFee.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000000, 30000, 1000));
        spExamFee.setEditable(true);
        configureFeeFormatter();
        setupCatalogs();
        setupTable();
        loadParameters();
        showParameters();
        btnEditEntry.setDisable(true);
        btnDeleteEntry.setDisable(true);
        tvCatalog.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, selected) -> {
                    btnEditEntry.setDisable(selected == null);
                    btnDeleteEntry.setDisable(selected == null);
                }
        );
        tfSearch.textProperty().addListener((obs, oldValue, newValue) -> applyCatalogFilter());
    }

    private void configureFeeFormatter() {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        spExamFee.getValueFactory().setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer value) {
                return value == null ? "" : format.format(value);
            }

            @Override
            public Integer fromString(String value) {
                if (value == null || value.isBlank()) {
                    return 0;
                }
                return Integer.parseInt(value.replace(",", "").trim());
            }
        });
        spExamFee.getEditor().setText(format.format(spExamFee.getValue()));
    }

    private void setupCatalogs() {
        catalogs.put(CatalogType.DISEASE, FXCollections.observableArrayList(
                new RuleEntry("LB01", "Loại bệnh 1", "Nội dung minh họa")
        ));
        catalogs.put(CatalogType.UNIT, FXCollections.observableArrayList(
                new RuleEntry("DV01", "Viên", "Đơn vị tính thuốc mẫu")
        ));
        catalogs.put(CatalogType.USAGE, FXCollections.observableArrayList(
                new RuleEntry("CD01", "Uống sau ăn", "Dùng sau bữa ăn")
        ));
    }

    private void setupTable() {
        colCode.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().code()));
        colName.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().name()));
        colDetails.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().details()));
    }

    private void loadParameters() {
        BigDecimal maxPatients = QuiDinhDAO.getGiaTri("MAX_PATIENT_PER_DAY");
        BigDecimal examFee = QuiDinhDAO.getGiaTri("DEFAULT_TIEN_KHAM");
        demoParameters = maxPatients == null || examFee == null;
        spMaxPatients.getValueFactory().setValue(
                maxPatients == null ? 40 : Math.max(1, maxPatients.intValue())
        );
        spExamFee.getValueFactory().setValue(
                examFee == null ? 30000 : Math.max(0, examFee.intValue())
        );
        lblParametersNote.setText(demoParameters
                ? "Đang hiển thị giá trị minh họa khi chưa đọc được quy định từ CSDL."
                : "Quy định mới áp dụng cho nghiệp vụ phát sinh sau khi lưu.");
    }

    @FXML
    private void showParameters() {
        currentType = null;
        parametersPane.setVisible(true);
        parametersPane.setManaged(true);
        catalogPane.setVisible(false);
        catalogPane.setManaged(false);
        setActiveTab(btnParameters);
    }

    @FXML
    private void showDiseases() {
        showCatalog(CatalogType.DISEASE, btnDiseases);
    }

    @FXML
    private void showUnits() {
        showCatalog(CatalogType.UNIT, btnUnits);
    }

    @FXML
    private void showUsage() {
        showCatalog(CatalogType.USAGE, btnUsage);
    }

    private void showCatalog(CatalogType type, Button tabButton) {
        currentType = type;
        parametersPane.setVisible(false);
        parametersPane.setManaged(false);
        catalogPane.setVisible(true);
        catalogPane.setManaged(true);
        setActiveTab(tabButton);
        switch (type) {
            case DISEASE -> {
                lblCatalogTitle.setText("Danh mục loại bệnh");
                lblCode.setText("Mã loại bệnh");
                lblName.setText("Tên loại bệnh");
                lblDetails.setText("Triệu chứng đặc trưng");
                tfDetails.setPromptText("Nhập triệu chứng đặc trưng");
                lblCatalogNote.setText("Dữ liệu minh họa, chỉ lưu trong phiên này. Không xóa loại bệnh đang dùng.");
            }
            case UNIT -> {
                lblCatalogTitle.setText("Danh mục đơn vị tính");
                lblCode.setText("Mã đơn vị");
                lblName.setText("Tên đơn vị");
                lblDetails.setText("Ghi chú");
                tfDetails.setPromptText("Nhập ghi chú đơn vị");
                lblCatalogNote.setText("Dữ liệu minh họa, chỉ lưu trong phiên này. Dùng trong danh mục thuốc.");
            }
            case USAGE -> {
                lblCatalogTitle.setText("Danh mục cách dùng");
                lblCode.setText("Mã cách dùng");
                lblName.setText("Tên cách dùng");
                lblDetails.setText("Hướng dẫn");
                tfDetails.setPromptText("Nhập hướng dẫn sử dụng");
                lblCatalogNote.setText("Dữ liệu minh họa, chỉ lưu trong phiên này. Dùng khi lập đơn thuốc.");
            }
        }
        filteredEntries = new FilteredList<>(catalogs.get(type), entry -> true);
        tvCatalog.setItems(filteredEntries);
        clearEditor();
        applyCatalogFilter();
    }

    private void setActiveTab(Button active) {
        for (Button button : new Button[]{btnParameters, btnDiseases, btnUnits, btnUsage}) {
            button.setStyle(button == active
                    ? "-fx-background-color: #C8E6C9; -fx-text-fill: #166534; -fx-font-weight: bold;"
                    : "-fx-background-color: #F1F5F9; -fx-text-fill: #475569;");
        }
    }

    @FXML
    private void handleSaveParameters() {
        try {
            int enteredMax = spMaxPatients.getValueFactory().getConverter()
                    .fromString(spMaxPatients.getEditor().getText());
            int enteredFee = spExamFee.getValueFactory().getConverter()
                    .fromString(spExamFee.getEditor().getText());
            if (enteredMax < 1 || enteredMax > 10000 || enteredFee < 0 || enteredFee > 10000000) {
                showAlert(Alert.AlertType.WARNING, "Giá trị quy định không hợp lệ.");
                return;
            }
            spMaxPatients.getValueFactory().setValue(enteredMax);
            spExamFee.getValueFactory().setValue(enteredFee);
            int maxPatients = enteredMax;
            int examFee = enteredFee;
            if (demoParameters) {
                lblParametersNote.setText("Đã cập nhật giá trị minh họa trong phiên hiện tại.");
                showAlert(Alert.AlertType.INFORMATION, "Đã lưu giá trị minh họa.");
                return;
            }
            boolean maxSaved = QuiDinhDAO.updateGiaTri(
                    "MAX_PATIENT_PER_DAY", BigDecimal.valueOf(maxPatients)
            );
            boolean feeSaved = QuiDinhDAO.updateGiaTri(
                    "DEFAULT_TIEN_KHAM", BigDecimal.valueOf(examFee)
            );
            if (!maxSaved || !feeSaved) {
                showAlert(Alert.AlertType.ERROR,
                        "Không lưu được đầy đủ quy định. Kiểm tra CSDL và các dòng quy định hiện có.");
                return;
            }
            lblParametersNote.setText("Quy định mới áp dụng cho nghiệp vụ phát sinh sau khi lưu.");
            showAlert(Alert.AlertType.INFORMATION, "Đã lưu quy định phòng mạch.");
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Giá trị phải là số nguyên hợp lệ.");
        }
    }

    @FXML
    private void handleCancelParameters() {
        loadParameters();
    }

    @FXML
    private void handleAddEntry() {
        clearEditor();
        addingEntry = true;
        editingCode = null;
        tfCode.setEditable(true);
        tfCode.requestFocus();
    }

    @FXML
    private void handleEditEntry() {
        RuleEntry selected = tvCatalog.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        addingEntry = false;
        editingCode = selected.code();
        tfCode.setText(selected.code());
        tfCode.setEditable(false);
        tfName.setText(selected.name());
        tfDetails.setText(selected.details());
    }

    @FXML
    private void handleDeleteEntry() {
        RuleEntry selected = tvCatalog.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xóa danh mục");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Xóa \"" + selected.name() + "\" khỏi danh mục?");
        confirmation.showAndWait().filter(javafx.scene.control.ButtonType.OK::equals)
                .ifPresent(button -> catalogs.get(currentType).remove(selected));
        applyCatalogFilter();
        clearEditor();
    }

    @FXML
    private void handleSaveEntry() {
        String code = tfCode.getText() == null ? "" : tfCode.getText().trim();
        String name = tfName.getText() == null ? "" : tfName.getText().trim();
        String details = tfDetails.getText() == null ? "" : tfDetails.getText().trim();
        if (currentType == null || code.isBlank() || name.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập mã và tên danh mục.");
            return;
        }
        ObservableList<RuleEntry> entries = catalogs.get(currentType);
        if (addingEntry && entries.stream().anyMatch(entry -> entry.code().equalsIgnoreCase(code))) {
            showAlert(Alert.AlertType.WARNING, "Mã danh mục đã tồn tại.");
            return;
        }
        RuleEntry updated = new RuleEntry(code, name, details);
        if (addingEntry) {
            entries.add(updated);
        } else {
            int index = -1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).code().equals(editingCode)) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                showAlert(Alert.AlertType.ERROR, "Không tìm thấy danh mục cần cập nhật.");
                return;
            }
            entries.set(index, updated);
        }
        applyCatalogFilter();
        tvCatalog.getSelectionModel().select(updated);
        clearEditor();
    }

    @FXML
    private void handleCancelEntry() {
        clearEditor();
    }

    private void applyCatalogFilter() {
        if (filteredEntries == null) {
            return;
        }
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        filteredEntries.setPredicate(entry -> keyword.isEmpty()
                || entry.code().toLowerCase().contains(keyword)
                || entry.name().toLowerCase().contains(keyword)
                || entry.details().toLowerCase().contains(keyword));
    }

    private void clearEditor() {
        if (tfCode == null) {
            return;
        }
        tfCode.clear();
        tfName.clear();
        tfDetails.clear();
        tfCode.setEditable(true);
        editingCode = null;
        addingEntry = false;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
