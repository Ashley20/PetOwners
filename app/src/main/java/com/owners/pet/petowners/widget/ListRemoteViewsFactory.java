package com.owners.pet.petowners.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.ChatUser;
import com.owners.pet.petowners.models.Message;
import com.owners.pet.petowners.models.User;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = ListRemoteViewsFactory.class.getSimpleName();
    private Context mContext;
    private ArrayList<ChatUser> mChatList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    ListRemoteViewsFactory(Context context) {
        mContext = context;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onCreate() {
        mChatList = new ArrayList<ChatUser>();
    }

    @Override
    public void onDataSetChanged() {
        currentUser = mAuth.getCurrentUser();


        if(!mChatList.isEmpty()){
            Log.d(TAG, "mChatList is not empty we are returning");
            return;
        }


        if (currentUser != null) {
            db.collection(mContext.getString(R.string.COLLECTION_USERS)).document(currentUser.getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snap, @Nullable FirebaseFirestoreException e) {
                    if (snap != null && snap.exists()) {
                        final User user = snap.toObject(User.class);
                        int i = 0;
                    if (user != null) {
                        for (String uid : user.getChatWithUidList()) {
                            if (uid != null) {
                                final int finalI = i;
                                db.collection(mContext.getString(R.string.COLLECTION_MESSAGES))
                                        .document(currentUser.getUid())
                                        .collection(uid)
                                        .orderBy(mContext.getString(R.string.DATE_KEY), Query.Direction.DESCENDING)
                                        .limit(1)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable QuerySnapshot snap, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                if (snap != null) {
                                                    Message m = snap.getDocuments().get(0).toObject(Message.class);
                                                    if (m != null) {
                                                        String lastMessage = m.getContent();
                                                        Date lastMessageDate = m.getDate();
                                                        user.getConversationList().get(finalI).setLastMessage(lastMessage);
                                                        user.getConversationList().get(finalI).setLastMessageDate(lastMessageDate);
                                                    }

                                                }
                                            }
                                        });
                            }
                            i++;
                        }
                        mChatList.addAll(user.getConversationList());

                    }
                    }
                }
            });
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName thisWidget = new ComponentName(mContext, PetOwnersWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
    }

    @Override
    public void onDestroy() {
        mChatList.clear();
    }

    @Override
    public int getCount() {
        if (mChatList == null) return 0;
        return mChatList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        final RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.pet_owners_widget_provider);
        ChatUser chatUser = mChatList.get(i);

        views.setTextViewText(R.id.user_fullname, chatUser.getName());
        views.setTextViewText(R.id.last_message, chatUser.getLastMessage());

        // Set date
        Locale l = Locale.US;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", l);
        if(chatUser.getLastMessageDate() != null){
            views.setTextViewText(R.id.last_message_time_text_view,
                    simpleDateFormat.format(chatUser.getLastMessageDate()));
        }


        Bundle extras = new Bundle();

        extras.putString(mContext.getString(R.string.USER_PROFILE_UID), chatUser.getUid());
        extras.putString(mContext.getString(R.string.USER_PROFILE_NAME), chatUser.getName());

        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_item_layout, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
