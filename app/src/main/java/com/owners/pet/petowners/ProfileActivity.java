package com.owners.pet.petowners;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    @BindView(R.id.profile_picture_image_view) ImageView profile_picture;
    @BindView(R.id.phone_text_view) TextView phone;
    @BindView(R.id.email_text_view) TextView email;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setElevation(0);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            profile_picture.setImageURI(currentUser.getPhotoUrl());
        }
        super.onStart();
    }

    @OnClick(R.id.profile_picture_image_view)
    public void changeProfilePicture(){
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = data.getData();
                        InputStream imageStream = null;
                        if (imageUri != null) {
                            imageStream = getContentResolver().openInputStream(imageUri);
                        }
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        profile_picture.setImageBitmap(selectedImage);



                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }


    @OnClick(R.id.add_pet_fab)
    public void addPet(){

    }
}
