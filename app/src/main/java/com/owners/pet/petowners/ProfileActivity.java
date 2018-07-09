package com.owners.pet.petowners;

import android.app.Dialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.owners.pet.petowners.models.Pet;
import com.owners.pet.petowners.models.User;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import adapters.PetsAdapter;
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
    @BindView(R.id.pets_list_view) ListView pets_list_view;



    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private User user;

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
                            if (doc.exists()){
                                user = doc.toObject(User.class);
                                if (user != null) {
                                    phone.setText(user.getPhoneNumber());
                                    bio.setText(user.getBiography());
                                    loadPets(user.getPetList());
                                }
                            }
                        }
                    }
                });

        super.onStart();
    }

    private void loadPets(ArrayList<Pet> petList) {
        PetsAdapter petsAdapter = new PetsAdapter(this, petList);
        pets_list_view.setAdapter(petsAdapter);

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

    /**
     * Updates email for firebaseUser and also calls updateEmail function
     * to update the firestore user's phone information.
     */
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

    /**
     * Updates user's phone and saves it into firestore database.
     * @param newPhone
     */
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
    public void displayAddPetDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_pet_custom_dialog, null);
        builder.setTitle(getString(R.string.add_a_new_pet_title));

        ImageView petProfilePicture = (ImageView) view.findViewById(R.id.pet_profile_pic);
        final EditText petName = (EditText) view.findViewById(R.id.pet_name_edit_text);
        final EditText petAbout = (EditText) view.findViewById(R.id.about_pet_edit_text);
        final CheckBox petAdoptionState = (CheckBox) view.findViewById(R.id.pet_adoption_state);
        final Spinner genderSpinner = (Spinner) view.findViewById(R.id.pet_gender_spinner);
        final Spinner typeSpinner = (Spinner) view.findViewById(R.id.pet_type_spinner);

        // Create adapter for the genderSpinner and set it
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.genderList));
        genderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        genderSpinner.setAdapter(genderAdapter);


        // Create adapter for the typeSpinner and set it
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.petTypeList));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        typeSpinner.setAdapter(typeAdapter);

        // Set positive and negative buttons
        builder.setPositiveButton(getString(R.string.add_pet_dialog_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!TextUtils.isEmpty(petName.getText().toString())){
                    // With the given information create a new pet.
                    Pet pet = new Pet();
                    pet.setOwner(currentUser.getDisplayName());
                    pet.setName(petName.getText().toString());
                    pet.setAbout(petAbout.getText().toString());
                    pet.setGender(genderSpinner.getSelectedItem().toString());
                    pet.setType(typeSpinner.getSelectedItem().toString());
                    pet.setWants_to_be_adopted(petAdoptionState.isChecked());

                 addAnewPet(pet);

                }else {
                    Toast.makeText(getApplicationContext(), getString(R.string.fill_in_required_fields_text),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton(getString(R.string.add_pet_cancel_dialog_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        // Set the custom dialog view
        builder.setView(view);

        // Display the custom dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void addAnewPet(Pet pet) {

        user.getPetList().add(pet);

        db.collection(getString(R.string.COLLECTION_USERS))
                .document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), getString(R.string.successful_pet_addition_message),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @OnClick(R.id.bio_text_view)
    public void showDialogAndUpdateBiography(){
        showDialog();
    }



    /**
     * Shows a dialog which has an edit text for adding an about me information
     * Finally this method calls updateBio to update (biography - about me) information.
     */
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


    /**
     * Update user's (biography - about me).
     * @param newBiography
     */
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
