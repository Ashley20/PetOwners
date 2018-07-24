package com.owners.pet.petowners;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.owners.pet.petowners.Glide.GlideApp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class PetProfileActivity extends AppCompatActivity {
    public static final int IMAGE_PICKER = 1;

    @BindView(R.id.pet_profile_picture_image_view)
    CircleImageView petProfilePic;
    @BindView(R.id.pet_name)
    TextView petNameTv;
    @BindView(R.id.type)
    TextView petTypeTv;
    @BindView(R.id.gender)
    TextView petGenderTv;
    @BindView(R.id.pet_bio_text_view)
    TextView petBioTv;
    @BindView(R.id.owner_text_view)
    TextView petOwnerTv;
    @BindView(R.id.pet_adoption_state_text_view)
    TextView petAdoptionStateTv;
    @BindView(R.id.pet_location)
    TextView petLocationTv;
    @BindView(R.id.edit_pet_fab)
    FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private StorageReference petProfilePictureRef;
    private FirebaseUser currentUser;
    private String petUid;
    private String petOwnerUid;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_profile);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            loadPetProfile(extras);
        }


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.edit_pet_fab)
    public void editPetInformation() {

    }

    @OnClick(R.id.pet_profile_picture_image_view)
    public void pickProfilePicture() {
        /*If another person other than the owner of the pet
        is viewing the pet profile then
        don't let a pet profile image upload */
        if(!currentUser.getUid().equals(petOwnerUid)){
            return;
        }
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, IMAGE_PICKER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case IMAGE_PICKER:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = data.getData();

                    // Store the picked image into firebase storage
                    if (imageUri != null) {
                        progressDialog.setMessage("Image uploading");
                        progressDialog.show();

                        UploadTask uploadTask = petProfilePictureRef.putFile(imageUri);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                loadPetProfilePicture();
                            }
                        });
                    }

                }
        }
    }


    /**
     * This function loads all the pet profile page information
     * such as name, bio, profile picture and all..
     * @param extras
     */
    private void loadPetProfile(Bundle extras) {
        petOwnerUid = extras.getString(getString(R.string.EXTRA_PET_OWNER_UID));
        petUid = extras.getString(getString(R.string.EXTRA_PET_UID));
        petNameTv.setText(extras.getString(getString(R.string.EXTRA_PET_NAME)));
        petBioTv.setText(extras.getString(getString(R.string.EXTRA_PET_ABOUT)));
        petLocationTv.setText(extras.getString(getString(R.string.EXTRA_PET_LOCATION)));
        petGenderTv.setText(extras.getString(getString(R.string.EXTRA_PET_GENDER)));
        petTypeTv.setText(extras.getString(getString(R.string.EXTRA_PET_TYPE)));
        petOwnerTv.setText(extras.getString(getString(R.string.EXTRA_PET_OWNER)));

        if(!currentUser.getUid().equals(petOwnerUid)){
            fab.setVisibility(View.INVISIBLE);
        }

        petAdoptionStateTv.setText(extras.getBoolean(
                getString(R.string.EXTRA_PET_ADOPTION_STATE))
                ? getString(R.string.waits_for_adoption_text)
                : getString(R.string.no_adoption));

        petProfilePictureRef = storageRef.child(petOwnerUid)
                .child(petUid)
                .child(getString(R.string.storage_profile_ref));

        loadPetProfilePicture();
    }

    /**
     * This function loads the pet profile picture
     * from firebase storage
     */
    private void loadPetProfilePicture() {
        GlideApp.with(getApplicationContext())
                .load(petProfilePictureRef)
                .placeholder(R.drawable.pets_icon)
                .into(petProfilePic);
    }
}
