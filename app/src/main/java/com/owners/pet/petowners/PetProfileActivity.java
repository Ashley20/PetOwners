package com.owners.pet.petowners;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.owners.pet.petowners.models.Pet;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

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
    FloatingActionButton editFab;
    @BindView(R.id.send_message_to_owner_btn)
    Button sendMessageBtn;
    @BindView(R.id.display_owner_profile_btn)
    Button displayProfileBtn;
    @BindView(R.id.delete_pet_container)
    RelativeLayout deletePetContainer;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private StorageReference petProfilePictureRef;
    private FirebaseUser currentUser;
    private String petUid;
    private String petOwnerUid;
    private String petOwnerName;
    private ProgressDialog progressDialog;
    private Pet currentPet;


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

        petUid = getIntent().getStringExtra(getString(R.string.EXTRA_PET_UID));

        if (petUid != null) {
            db.collection(getString(R.string.COLLECTION_PETS))
                    .document(petUid)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snap, @Nullable FirebaseFirestoreException e) {
                            if (snap != null && snap.exists()) {
                                currentPet = snap.toObject(Pet.class);
                                loadPetProfile();
                            }
                        }
                    });
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
    public void editPetProfile() {
        if (!currentUser.getUid().equals(petOwnerUid)) {
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_pet_custom_dialog, null);
        builder.setTitle(getString(R.string.edit_pet_title));

        final EditText petNameEditText = (EditText) view.findViewById(R.id.pet_name_edit_text);
        final EditText petAboutEditText = (EditText) view.findViewById(R.id.about_pet_edit_text);
        final CheckBox petAdoptionStateCheckBox = (CheckBox) view.findViewById(R.id.pet_adoption_state);
        final Spinner genderSpinner = (Spinner) view.findViewById(R.id.pet_gender_spinner);
        final Spinner typeSpinner = (Spinner) view.findViewById(R.id.pet_type_spinner);

        petNameEditText.setText(currentPet.getName());
        petAboutEditText.setText(currentPet.getAbout());
        petAdoptionStateCheckBox.setChecked(currentPet.getAdoptionState());

        // Create adapter for the genderSpinner and set it
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.genderList));
        genderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setSelection(currentPet.getGender().equals("Girl") ? 0 : 1);


        // Create adapter for the typeSpinner and set it
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.petTypeList));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setSelection(currentPet.getType().equals("Cat") ? 0 : 1);


        // Set positive and negative buttons
        builder.setPositiveButton(getString(R.string.edit_pet_dialog_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!TextUtils.isEmpty(petNameEditText.getText().toString())
                        && !TextUtils.isEmpty(petAboutEditText.getText().toString())) {
                    final DocumentReference petDocumentRef = db.collection(getString(R.string.COLLECTION_PETS))
                            .document(petUid);

                    currentPet.setName(petNameEditText.getText().toString());
                    currentPet.setAbout(petAboutEditText.getText().toString());
                    currentPet.setGender(genderSpinner.getSelectedItem().toString());
                    currentPet.setType(typeSpinner.getSelectedItem().toString());
                    currentPet.setAdoptionState(petAdoptionStateCheckBox.isChecked());

                    petDocumentRef.set(currentPet).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.successful_pet_edition_message),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.name_and_about_field_cannot_be_empty),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton(getString(R.string.cancel_dialog_button), new DialogInterface.OnClickListener() {
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

    @OnClick(R.id.display_owner_profile_btn)
    public void displayOwnerProfile() {
        if (currentUser.getUid().equals(petOwnerUid)) {
            return;
        }
        Intent othersProfileActivity =
                new Intent(getApplicationContext(), OthersProfileActivity.class);
        othersProfileActivity.putExtra(getString(R.string.USER_PROFILE_UID), petOwnerUid);
        startActivity(othersProfileActivity);
    }

    @OnClick(R.id.send_message_to_owner_btn)
    public void sendMessageToOwner() {
        if (currentUser.getUid().equals(petOwnerUid)) {
            return;
        }

        if (petOwnerUid != null && petOwnerName != null) {
            Intent chatActivity =
                    new Intent(getApplicationContext(), ChatActivity.class);
            chatActivity.putExtra(getString(R.string.USER_PROFILE_UID), petOwnerUid);
            chatActivity.putExtra(getString(R.string.USER_PROFILE_NAME), petOwnerName);
            startActivity(chatActivity);
        }
    }

    @OnClick(R.id.delete_pet_container)
    public void deletePet() {
        if (!currentUser.getUid().equals(petOwnerUid)) {
            return;
        }
        db.collection(getString(R.string.COLLECTION_PETS))
                .document(petUid)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.successful_pet_deletion_message),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_pet_deletion_message),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @OnClick(R.id.pet_profile_picture_image_view)
    public void pickProfilePicture() {
        if (!currentUser.getUid().equals(petOwnerUid)) {
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
                    petProfilePictureRef = storageRef.child(getString(R.string.COLLECTION_PETS))
                            .child(petOwnerUid)
                            .child(petUid)
                            .child(getString(R.string.storage_profile_ref));

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
                                petProfilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        DocumentReference pet = db.collection(getString(R.string.COLLECTION_PETS))
                                                .document(petUid);
                                        pet.update(getString(R.string.PROFILE_IMAGE_URI_KEY), uri.toString());

                                        Picasso.get()
                                                .load(uri)
                                                .placeholder(R.drawable.pets_icon)
                                                .into(petProfilePic);
                                    }
                                });
                            }
                        });
                    }

                }
        }
    }


    /**
     * This function loads all the pet profile page information
     * such as name, bio, profile picture and all..
     */
    private void loadPetProfile() {

        petOwnerUid = currentPet.getOwnerUid();
        petOwnerName = currentPet.getOwner();
        petNameTv.setText(currentPet.getName());
        petBioTv.setText(currentPet.getAbout());
        petLocationTv.setText(currentPet.getLocation());
        petGenderTv.setText(currentPet.getGender());
        petTypeTv.setText(currentPet.getType());
        petOwnerTv.setText(currentPet.getOwner());

        String petProfileImageUri = currentPet.getProfileImageUri();
        if (petProfileImageUri != null) {
            Picasso.get()
                    .load(petProfileImageUri)
                    .placeholder(R.drawable.pets_icon)
                    .into(petProfilePic);
        }

        petAdoptionStateTv.setText(currentPet.getAdoptionState()
                ? getString(R.string.waits_for_adoption_text)
                : getString(R.string.no_adoption));

        setVisibilityOptions();

    }


    /**
     * This function sets the visibility options
     * based on the rights of the
     * user displaying the profile..
     */
    private void setVisibilityOptions() {
        if (!currentUser.getUid().equals(petOwnerUid)) {
            /* If the profile is being viewed by someone other than the owner of the pet
            then dont show the delete pet and edit pet fields */
            editFab.setVisibility(View.INVISIBLE);
            deletePetContainer.setVisibility(View.INVISIBLE);
        } else {
            /* Don't show the send message and display owner profile buttons
            to the owner fab to the owner of the pet */
            sendMessageBtn.setVisibility(View.INVISIBLE);
            displayProfileBtn.setVisibility(View.INVISIBLE);
        }
    }


}
