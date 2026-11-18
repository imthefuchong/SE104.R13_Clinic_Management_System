package com.example.model;

import java.time.LocalDate;

public class StaffModel {
    private String id;
    private String lastname;
    private String firstname;
    private String role;
    private double luong;
    private LocalDate birthday;
    private String gender;
    private String cccd;
    private String address;
    private String email;
    private String phone;
    private String password;
    private String status = "Đang làm việc";

    public StaffModel() {

    }

    public StaffModel(String id, String lastname, String firstname, String role, double luong, LocalDate birthday,
                      String gender, String cccd, String address, String email, String phone, String password) {
        this.id = id;
        this.lastname = lastname;
        this.firstname = firstname;
        this.role = role;
        this.luong = luong;
        this.birthday = birthday;
        this.gender = gender;
        this.cccd = cccd;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.status = "Đang làm việc";
    }

    public String getStatus() {
        return (status != null && !status.isEmpty()) ? status : "Đang làm việc";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullName() {
        String h = lastname != null ? lastname : "";
        String t = firstname != null ? firstname : "";
        return (h + " " + t).trim();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public double getLuong() {
        return luong;
    }
    public void setLuong(double luong) {
        this.luong = luong;
    }

    public LocalDate getBirthday() {
        return birthday;
    }
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCccd() {
        return cccd;
    }
    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "StaffModel{" +
                "id='" + id + '\'' +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", role='" + role + '\'' +
                ", luong=" + luong +
                ", birthday=" + birthday +
                ", gender='" + gender + '\'' +
                ", cccd='" + cccd + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
