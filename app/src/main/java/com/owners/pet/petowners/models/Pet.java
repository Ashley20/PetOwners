package com.owners.pet.petowners.models;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.UUID;

public class Pet {

    private String petUid;
    private String ownerUid;
    private String owner;
    private String about;
    private String name;
    private String gender;
    private String type;
    private boolean adoptionState;


    public Pet() {
        if(this.getPetUid() == null){
            this.petUid = UUID.randomUUID().toString();
        }
    }

    public String getPetUid() {
        return petUid;
    }

    public void setPetUid(String petUid) {
        this.petUid = petUid;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
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

    public boolean getAdoptionState() {
        return adoptionState;
    }

    public void setAdoptionState(boolean adoptionState) {
        this.adoptionState = adoptionState;
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
