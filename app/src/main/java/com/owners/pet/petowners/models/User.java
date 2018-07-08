package com.owners.pet.petowners.models;

import android.content.res.Resources;

import com.owners.pet.petowners.R;

import java.util.List;

public class User {
    public static final int WANTS_TO_ADOPT = 0;
    public static final int WANTS_TO_POST_FOR_ADOPTION = 1;
    public static final int NONE = 2;

    private String uid;
    private String name;
    private String phoneNumber;
    private String biography;
    private List<Pet> petList;
    private int userState;

    public User() {
        this.userState = NONE;
    }

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

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Pet> getPetList() {
        return petList;
    }

    public void setPetList(List<Pet> petList) {
        this.petList = petList;
    }

    public int getUserState() {
        return userState;
    }

    public void setUserState(int userState) {
        this.userState = userState;
    }
}
