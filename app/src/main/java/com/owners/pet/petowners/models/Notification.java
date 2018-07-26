package com.owners.pet.petowners.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Notification {
    private String content;
    private String from;
    private String fromUid;
    @ServerTimestamp
    private Date date;

}
