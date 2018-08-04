package com.owners.pet.petowners.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Time {

    public static String getTime() {
        return new SimpleDateFormat("h:mm a", Locale.US).format(new Date());
    }
}
