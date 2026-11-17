package com.example.model;

public class AccountModel {
    private String username;
    private String employeeId;
    private String employeeName;
    private String roleId;
    private String status;
    private boolean loggedIn;

    public AccountModel(
            String username,
            String employeeId,
            String employeeName,
            String roleId,
            String status,
            boolean loggedIn
    ) {
        this.username = username;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.roleId = roleId;
        this.status = status;
        this.loggedIn = loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getRoleLabel() {
        return switch (roleId == null ? "" : roleId.toUpperCase()) {
            case "ADMIN" -> "Quản trị viên";
            case "DOCTOR" -> "Bác sĩ";
            case "NURSE" -> "Y tá";
            case "MANAGER" -> "Quản lý";
            default -> roleId == null ? "" : roleId;
        };
    }

    public String getStatus() {
        return status;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getDisplayStatus() {
        if ("Đã khóa".equals(status)) {
            return status;
        }
        return loggedIn ? "Đang đăng nhập" : status;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
