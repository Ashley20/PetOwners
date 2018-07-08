package com.owners.pet.petowners;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final String FIREBASE_STORAGE_IMAGES_REFERENCE_URL = "gs://petowners.appspot.com";

    @BindView(R.id.profile_picture_image_view) ImageView profile_picture;
    @BindView(R.id.phone_edit_text) EditText phone;
    @BindView(R.id.email_edit_text) EditText email;
    @BindView(R.id.bio_text_view) TextView bio;
    @BindView(R.id.name) TextView name;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

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
            actionBar.setLogo(R.drawable.ic_done);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onStart() {
        currentUser = mAuth.getCurrentUser();
        // If the user is not null then update the UI with current user's profile information
        if (currentUser != null) {
            profile_picture.setImageURI(currentUser.getPhotoUrl());
            name.setText(currentUser.getDisplayName());
            email.setText(currentUser.getEmail());
        }

        db.collection(getString(R.string.COLLECTION_USERS))
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            if(doc.get(getString(R.string.PHONE_NUMBER_KEY)) != null){
                                phone.setText((doc.get(getString(R.string.PHONE_NUMBER_KEY))).toString());
                            }
                            if(doc.get(getString(R.string.BIO_KEY)) != null){
                                bio.setText(doc.get(getString(R.string.BIO_KEY)).toString());
                            }
                        }
                    }
                });

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.done:
                saveUserProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveUserProfile() {
        final String phoneUpdate = phone.getText().toString();
        final String emailUpdate = email.getText().toString();

        if(TextUtils.isEmpty(phoneUpdate) || TextUtils.isEmpty(emailUpdate)){
            Toast.makeText(this, getString(R.string.fill_in_required_fields_text),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.updateEmail(emailUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    email.setText(emailUpdate);
                }
            }
        });

        // Update the firestore user's phone number.
        updateFirestoreUser(phoneUpdate);

        Toast.makeText(this, getString(R.string.successful_profile_update_message),
                Toast.LENGTH_SHORT).show();


    }

    private void updateFirestoreUser(String newPhone) {
        DocumentReference user = db.collection(getString(R.string.COLLECTION_USERS)).document(currentUser.getUid());
        user.update(getString(R.string.PHONE_NUMBER_KEY), newPhone);
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
                    final Uri imageUri = data.getData();

                    // Update user photo
                    final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(imageUri)
                            .build();

                    currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            profile_picture.setImageURI(currentUser.getPhotoUrl());
                        }
                    });

                }
        }
    }

    private void uploadImageToFirebaseStorage(InputStream imageStream) {
        storageRef = storage.getReference();
        StorageReference profileImagesRef = storageRef.child("profile.jpg");

        UploadTask uploadTask = profileImagesRef.putStream(imageStream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

    }


    @OnClick(R.id.add_pet_fab)
    public void addPet(){

    }

    @OnClick(R.id.bio_text_view)
    public void showDialogAndUpdateBiography(){
        showDialog();
    }

    private void showDialog() {
        final EditText editText = new EditText(this);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.edit_your_bio_title))
                .setView(editText)
                .setPositiveButton(getString(R.string.bio_dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String biography = editText.getText().toString();

                        if(TextUtils.isEmpty(biography)){
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.fill_in_bio_text), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        updateBio(biography);


                    }
                })
                .setNegativeButton(getString(R.string.bio_dialog_negative_button), null)
                .create();

        alertDialog.show();
    }

    private void updateBio(final String newBiography) {
        DocumentReference user = db.collection(getString(R.string.COLLECTION_USERS)).document(currentUser.getUid());
        user.update(getString(R.string.BIO_KEY), newBiography).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                bio.setText(newBiography);
            }
        });
    }
}
