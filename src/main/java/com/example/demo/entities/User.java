package com.example.demo.entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.searchbox.annotations.JestId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JestId
    private Long userId ;
    private String name ;
    private String password ;
    private String emailId ;
    private Long phoneNumber ;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


}
