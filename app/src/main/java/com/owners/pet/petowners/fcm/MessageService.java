package com.owners.pet.petowners.fcm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.owners.pet.petowners.R;

public class MessageService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"id")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Notification Title")
                .setContentText("You have a new message!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, mBuilder.build());

    }
}
