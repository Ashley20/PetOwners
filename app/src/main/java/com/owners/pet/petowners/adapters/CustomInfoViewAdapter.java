package com.owners.pet.petowners.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.OthersProfileActivity;
import com.owners.pet.petowners.ProfileActivity;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomInfoViewAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private StorageReference storageRef;
    private StorageReference userProfileImageRef;

    public CustomInfoViewAdapter(Context context) {
        this.context = context;
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View getInfoWindow(Marker marker) {

        View view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);

        CircleImageView infoWindowProfileImage = view.findViewById(R.id.info_window_profile_image);
        TextView infoWindowTitle = (TextView) view.findViewById(R.id.info_window_title);
        TextView infoWindowBody = (TextView) view.findViewById(R.id.info_window_body);
        TextView infoWindowINFO= (TextView) view.findViewById(R.id.info_window_tv);

        infoWindowTitle.setText(marker.getTitle());
        infoWindowBody.setText(marker.getSnippet());


        if(marker.getTag() != null){
            HashMap<String, String> tag = (HashMap<String, String>) marker.getTag();
            String uid = tag.get(context.getString(R.string.UID_KEY));
            String color = tag.get(context.getString(R.string.COLOR_KEY));

            userProfileImageRef = storageRef.child(context.getString(R.string.COLLECTION_USERS))
                    .child(uid).child("profile.jpg");

            GlideApp.with(context)
                    .load(userProfileImageRef)
                    .placeholder(R.drawable.profile_icon)
                    .into(infoWindowProfileImage);



            switch (color) {
                case "HUE_ORANGE":
                    view.setBackgroundColor(context.getResources().getColor(R.color.colorOrange900));
                    break;
                case "HUE_CYAN":
                    view.setBackgroundColor(context.getResources().getColor(R.color.colorBlue900));
                    break;
                case "HUE_GREEN":
                    view.setBackgroundColor(context.getResources().getColor(R.color.colorGreen900));
                    break;
            }

        }






        return view;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        return null;
    }
}
