package com.owners.pet.petowners.fcm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.owners.pet.petowners.ChatActivity;
import com.owners.pet.petowners.R;

public class MessageService extends FirebaseMessagingService {
    public static final String TAG = FirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, remoteMessage.getData().toString());

        if (remoteMessage.getNotification() != null) {
            String notificationTitle = remoteMessage.getNotification().getTitle();
            String notificationBody = remoteMessage.getNotification().getBody();

            String user_profile_uid = remoteMessage.getData().get(getString(R.string.USER_PROFILE_UID));
            String user_profile_name = remoteMessage.getData().get(getString(R.string.USER_PROFILE_NAME));

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(this, ChatActivity.class);

            intent.putExtra(getString(R.string.USER_PROFILE_UID), user_profile_uid);
            intent.putExtra(getString(R.string.USER_PROFILE_NAME), user_profile_name);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            sendNotification(notificationTitle, notificationBody, pendingIntent);
        }

    }

    private void sendNotification(String notificationTitle, String notificationBody, PendingIntent pendingIntent) {

        Uri defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "id")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSound(defaultNotificationSound)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, mBuilder.build());
    }
}
