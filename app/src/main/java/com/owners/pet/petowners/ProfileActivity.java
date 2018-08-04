package com.owners.pet.petowners;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.owners.pet.petowners.models.Pet;
import com.owners.pet.petowners.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.owners.pet.petowners.adapters.PetsAdapter;
import com.owners.pet.petowners.widget.PetOwnersWidgetProvider;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND;

public class ProfileActivity extends AppCompatActivity implements OnFailureListener {
    private static final int PICK_IMAGE = 1;
    public static final int PICK_PET_IMAGE = 2;
    private static final String TAG = ProfileActivity.class.getSimpleName();

    @BindView(R.id.profile_picture_image_view)
    CircleImageView profile_picture;
    @BindView(R.id.phone_edit_text)
    EditText phone;
    @BindView(R.id.email_edit_text)
    EditText email;
    @BindView(R.id.bio_text_view)
    TextView bio;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.pets_list_view)
    ListView pets_list_view;


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference profileImagesRef;
    private StorageReference petProfilePictureRef;
    private User user;
    CircleImageView petPic = null;
    private Uri petSelectedImageUri;
    private ProgressDialog progressDialog;
    private ArrayList<Pet> petList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setLogo(R.drawable.ic_done);
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        storageRef = FirebaseStorage.getInstance().getReference();

        currentUser = mAuth.getCurrentUser();
        profileImagesRef = storageRef.child("users")
                .child(currentUser.getUid()).child("profile.jpg");
    }

    @Override
    protected void onStart() {
        loadUserProfileInformation();
        super.onStart();
    }

    /**
     * When the profile activity first starts
     * this method will fetch and set the user profile information
     * like profile picture, email, name and others..
     */
    private void loadUserProfileInformation() {
        // If the user is not null then update the UI with current user's profile information
        if (currentUser != null) {
            mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                user = dataSnapshot.getValue(User.class);

                                if (user != null) {
                                    name.setText(user.getName());
                                    email.setText(user.getEmail());
                                    phone.setText(user.getPhoneNumber());
                                    bio.setText(user.getBiography());

                                    if (user.getProfileImageUri() != null) {
                                        Picasso.get()
                                                .load(user.getProfileImageUri())
                                                .resize(150, 150)
                                                .centerCrop()
                                                .into(profile_picture);
                                    }

                                    loadPets();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }


    private void loadPets() {
        final PetsAdapter petsAdapter = new PetsAdapter(this, petList);
        pets_list_view.setAdapter(petsAdapter);

        mDatabase.child(getString(R.string.COLLECTION_PETS)).child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if (petList != null) {
                                petList.clear();
                            }

                            for(DataSnapshot s : dataSnapshot.getChildren()){
                                Pet pet = s.getValue(Pet.class);
                                petList.add(pet);
                            }

                            petsAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(pets_list_view);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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

        if (TextUtils.isEmpty(phoneUpdate) || TextUtils.isEmpty(emailUpdate)) {
            Toast.makeText(this, getString(R.string.fill_in_required_fields_text),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.updateEmail(emailUpdate);

        // Update the  user's email and phone number.
        updateUser(phoneUpdate, emailUpdate);

        Toast.makeText(this, getString(R.string.successful_profile_update_message),
                Toast.LENGTH_SHORT).show();

    }

    /**
     * Updates user's phone and saves it into firebase realtime database.
     *
     * @param newPhone
     */
    private void updateUser(final String newPhone, final String newEmail) {

        Map<String, Object> updates = new HashMap<>();
        updates.put(getString(R.string.EMAIL_KEY), newEmail);
        updates.put(getString(R.string.PHONE_NUMBER_KEY), newPhone);

        mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                .updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                email.setText(newEmail);
                phone.setText(newPhone);
            }
        });
    }

    @OnClick(R.id.profile_picture_image_view)
    public void changeProfilePicture() {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    progressDialog.setMessage("Image uploading..");
                    progressDialog.show();
                    final Uri imageUri = data.getData();

                    // Update user photo
                    if (imageUri != null) {
                        UploadTask uploadTask = profileImagesRef.putFile(imageUri);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.IMAGE_UPLOAD_ERROR), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                profileImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        progressDialog.dismiss();
                                        Picasso.get()
                                                .load(uri)
                                                .placeholder(R.drawable.profile_icon)
                                                .into(profile_picture);


                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put(getString(R.string.USER_PROFILE_IMAGE_URI_KEY), uri.toString());

                                        mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                                                .updateChildren(updates);

                                    }
                                });

                            }
                        });
                    }

                }
                break;

            case PICK_PET_IMAGE:
                if (resultCode == RESULT_OK) {
                    petSelectedImageUri = data.getData();
                    // Update user photo
                    if (petSelectedImageUri != null && petPic != null) {
                        Picasso.get()
                                .load(petSelectedImageUri)
                                .into(petPic);
                    }
                }
                break;

        }
    }


    @OnClick(R.id.add_pet_fab)
    public void displayAddPetDialog() {
        petSelectedImageUri = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_pet_custom_dialog, null);
        builder.setTitle(getString(R.string.add_a_new_pet_title));

        petPic = (CircleImageView) view.findViewById(R.id.pet_profile_pic);
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

        petPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
                imagePickerIntent.setType("image/*");
                startActivityForResult(imagePickerIntent, PICK_PET_IMAGE);
            }
        });


        // Set positive and negative buttons
        builder.setPositiveButton(getString(R.string.add_pet_dialog_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!TextUtils.isEmpty(petName.getText().toString())) {
                    // With the given information create a new pet.
                    final Pet pet = new Pet();
                    pet.setOwnerUid(currentUser.getUid());
                    pet.setOwner(currentUser.getDisplayName());
                    pet.setAdminArea(user.getAdminArea());
                    pet.setCountry(user.getCountry());
                    pet.setName(petName.getText().toString());
                    pet.setAbout(petAbout.getText().toString());
                    pet.setGender(genderSpinner.getSelectedItem().toString());
                    pet.setType(typeSpinner.getSelectedItem().toString());
                    pet.setAdoptionState(petAdoptionState.isChecked());

                    petProfilePictureRef = storageRef.child(getString(R.string.COLLECTION_PETS))
                            .child(user.getUid())
                            .child(pet.getPetUid())
                            .child(getString(R.string.storage_profile_ref));

                    // Store pet profile picture into firebase storage
                    if (petSelectedImageUri != null) {
                        UploadTask uploadTask = petProfilePictureRef.putFile(petSelectedImageUri);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                petProfilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        pet.setProfileImageUri(uri.toString());
                                        addAnewPet(pet);
                                    }
                                });
                            }
                        });

                    } else {
                        addAnewPet(pet);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.fill_in_required_fields_text),
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


    /**
     * Creates a new pet and
     * stores it into pets section in the database
     *
     * @param pet
     */
    private void addAnewPet(final Pet pet) {

        mDatabase.child(getString(R.string.COLLECTION_PETS)).child(pet.getOwnerUid()).child(pet.getPetUid())
                .setValue(pet).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), getString(R.string.successful_pet_addition_message),
                        Toast.LENGTH_SHORT).show();

            }
        });

    }

    @OnClick(R.id.bio_text_view)
    public void showDialogAndUpdateBiography() {
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

                        if (TextUtils.isEmpty(biography)) {
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
     *
     * @param newBiography
     */
    private void updateBio(final String newBiography) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(getString(R.string.BIO_KEY), newBiography);

        mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                .updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                bio.setText(newBiography);
            }
        });
    }


    @OnClick(R.id.logout)
    public void logOut() {
        // Simply sign out the user and redirect the user to the login page
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    /**
     * Solves listview inside a NestedScrollView problem
     */
    public static void setListViewHeightBasedOnChildren
    (ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) view.setLayoutParams(new
                    ViewGroup.LayoutParams(desiredWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight + (listView.getDividerHeight() *
                (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        int errorCode = ((StorageException) e).getErrorCode();
        String errorMessage = e.getMessage();

        if (errorCode == ERROR_OBJECT_NOT_FOUND) {
            Log.d(TAG, errorMessage);
            profile_picture.setImageResource(R.drawable.profile_icon);
        }
    }
}
