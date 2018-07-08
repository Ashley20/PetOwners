package com.owners.pet.petowners.models;

import java.util.List;

public class User {
    private String name;
    private String email;
    private String phoneNumber;
    private String biography;
    private List<Pet> petList;
    private enum state {
        WANTS_TO_ADOPT,
        WANTS_TO_POST_FOR_ADOPTION,
        NONE
    }


    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
