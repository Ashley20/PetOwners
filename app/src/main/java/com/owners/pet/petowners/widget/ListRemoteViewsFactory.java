package com.owners.pet.petowners.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Pet;

import java.util.ArrayList;

public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String TAG = ListRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private ArrayList<Pet> mPetList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private int mAppWidgetId;


    ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mPetList = new ArrayList<Pet>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {

        currentUser = mAuth.getCurrentUser();

        if(!mPetList.isEmpty()){
            return;
        }

        mDatabase.child(mContext.getString(R.string.COLLECTION_PETS)).child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(!mPetList.isEmpty()){
                        mPetList.clear();
                    }

                    for(DataSnapshot s : dataSnapshot.getChildren()){
                        Pet pet = s.getValue(Pet.class);
                        mPetList.add(pet);
                    }
                }


                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                ComponentName thisWidget = new ComponentName(mContext, PetOwnersWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        mPetList.clear();
    }

    @Override
    public int getCount() {
        if (mPetList == null) return 0;
        return mPetList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.pet_owners_widget_provider);
        Pet pet = mPetList.get(i);
        views.setTextViewText(R.id.pet_name, pet.getName());
        views.setTextViewText(R.id.pet_bio, pet.getAbout());


        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(mContext.getString(R.string.EXTRA_PET_UID), pet.getPetUid());
        fillInIntent.putExtra(mContext.getString(R.string.EXTRA_PET_OWNER_UID), pet.getOwnerUid());
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
