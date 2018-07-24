package com.owners.pet.petowners.models;

import android.content.res.Resources;

import com.google.android.gms.maps.model.LatLng;
import com.owners.pet.petowners.R;

import java.util.ArrayList;
import java.util.List;

public class User {
    public static final int WANTS_TO_ADOPT = 0;
    public static final int WANTS_TO_POST_FOR_ADOPTION = 1;
    public static final int NONE = 2;

    private String uid;
    private String deviceToken;
    private String name;
    private String phoneNumber;
    private String biography;
    private String email;
    private Double latitude;
    private Double longtitude;
    private String location;
    private ArrayList<Pet> petList;
    private ArrayList<ChatUser> conversationList;
    private ArrayList<String> chatWithUidList;
    private int userState;


    public User() {
        this.userState = NONE;
        this.petList = new ArrayList<>();
        this.conversationList = new ArrayList<>();
        this.chatWithUidList = new ArrayList<>();
        this.phoneNumber = "(123) 456 78 90";
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

    public ArrayList<Pet> getPetList() {
        return petList;
    }

    public void setPetList(ArrayList<Pet> petList) {
        this.petList = petList;
    }

    public int getUserState() {
        return userState;
    }

    public void setUserState(int userState) {
        this.userState = userState;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(Double longtitude) {
        this.longtitude = longtitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public ArrayList<ChatUser> getConversationList() {
        return conversationList;
    }

    public void setConversationList(ArrayList<ChatUser> conversationList) {
        this.conversationList = conversationList;
    }

    public ArrayList<String> getChatWithUidList() {
        return chatWithUidList;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setChatWithUidList(ArrayList<String> chatWithUidList) {
        this.chatWithUidList = chatWithUidList;


    }

}
