package com.pravesh.myapplication.entities;

public class Admin {
    String name;
    String phone;
    String houseNo;

    public Admin(String name, String phone, String houseNo) {
        this.name = name;
        this.phone = phone;
        this.houseNo = houseNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }
}
