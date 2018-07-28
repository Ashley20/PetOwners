package com.owners.pet.petowners.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Pet;

import java.util.ArrayList;

public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String TAG = ListRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private ArrayList<Pet> mPetList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private int mAppWidgetId;


    ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mPetList = new ArrayList<Pet>();
        db = FirebaseFirestore.getInstance();
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



        if (currentUser != null) {
            db.collection(mContext.getString(R.string.COLLECTION_PETS))
                    .whereEqualTo("ownerUid", currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Task is successfull.");
                                mPetList.clear();
                                for (QueryDocumentSnapshot snap : task.getResult()) {
                                    Pet pet = snap.toObject(Pet.class);
                                    Log.d(TAG, pet.getAbout());
                                    mPetList.add(pet);
                                }


                            } else {
                                Log.d(TAG, "Task failed.");
                            }
                        }
                    });
        }

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
