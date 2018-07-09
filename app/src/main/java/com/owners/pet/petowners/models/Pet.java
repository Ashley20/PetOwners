package com.owners.pet.petowners.models;

import com.google.firebase.auth.FirebaseUser;

public class Pet {

    private String owner;
    private String about;
    private String name;
    private String gender;
    private String type;
    private boolean wants_to_be_adopted;


    public Pet() {
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWants_to_be_adopted() {
        return wants_to_be_adopted;
    }

    public void setWants_to_be_adopted(boolean wants_to_be_adopted) {
        this.wants_to_be_adopted = wants_to_be_adopted;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
