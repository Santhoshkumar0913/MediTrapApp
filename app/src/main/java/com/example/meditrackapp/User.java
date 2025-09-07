package com.example.meditrackapp;

public class User {
    private String uid;
    private String name;
    private String age;
    private String gender;
    private String familyPhone;
    private String personalPhone;
    private String email;

    // Empty constructor required for Firebase
    public User() {
    }

    // Full constructor
    public User(String uid, String name, String age, String gender,
                String familyPhone, String personalPhone, String email) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.familyPhone = familyPhone;
        this.personalPhone = personalPhone;
        this.email = email;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFamilyPhone() {
        return familyPhone;
    }

    public void setFamilyPhone(String familyPhone) {
        this.familyPhone = familyPhone;
    }

    public String getPersonalPhone() {
        return personalPhone;
    }

    public void setPersonalPhone(String personalPhone) {
        this.personalPhone = personalPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
