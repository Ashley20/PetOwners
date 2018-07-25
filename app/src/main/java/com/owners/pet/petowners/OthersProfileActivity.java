package com.owners.pet.petowners;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.models.Message;
import com.owners.pet.petowners.models.Pet;
import com.owners.pet.petowners.models.User;

import java.util.ArrayList;

import com.owners.pet.petowners.adapters.PetsAdapter;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND;

public class OthersProfileActivity extends AppCompatActivity{
    private static final int PICK_IMAGE = 1;
    private static final String FIREBASE_STORAGE_IMAGES_REFERENCE_URL = "gs://petowners.appspot.com";
    public static final String TAG = OthersProfileActivity.class.getSimpleName();

    @BindView(R.id.profile_picture_image_view)
    CircleImageView profile_picture;
    @BindView(R.id.phone_text_view) TextView phone;
    @BindView(R.id.email_text_view) TextView email;
    @BindView(R.id.bio_text_view) TextView bio;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.pets_list_view) ListView pets_list_view;



    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference profileImagesRef;
    private User user;
    private String uid;
    private ArrayList<Pet> petList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

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
        storageRef = FirebaseStorage.getInstance().getReference();

        // Get the user profile uid extra from the intent
        Intent intent = getIntent();
        if(intent != null){
            uid = intent.getStringExtra(getString(R.string.USER_PROFILE_UID));
            profileImagesRef = storageRef.child(getString(R.string.COLLECTION_USERS))
                    .child(uid).child(getString(R.string.storage_profile_ref));
        }
    }

    @Override
    protected void onStart() {
        loadUserProfileInformation();
        super.onStart();
    }

    private void loadUserProfileInformation() {
        // If the uid is not null then update the UI profile information
        if (uid != null) {
            db.collection(getString(R.string.COLLECTION_USERS))
                    .document(uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()) {
                                    user = doc.toObject(User.class);
                                    if (user != null) {
                                        name.setText(user.getName());
                                        email.setText(user.getEmail());
                                        phone.setText(user.getPhoneNumber());
                                        bio.setText(user.getBiography());

                                        if(user.getProfileImageUri() != null){
                                            Picasso.get()
                                                    .load(user.getProfileImageUri())
                                                    .placeholder(R.drawable.profile_icon)
                                                    .into(profile_picture);
                                        }

                                        loadPets();
                                    }
                                }
                            }
                        }
                    });
        }
    }


    private void loadPets() {
        final PetsAdapter petsAdapter = new PetsAdapter(this, petList);
        pets_list_view.setAdapter(petsAdapter);

        db.collection(getString(R.string.COLLECTION_PETS))
                .whereEqualTo(getString(R.string.OWNER_UID_KEY), uid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snap, @Nullable FirebaseFirestoreException e) {
                        if(snap != null){
                            if (petList != null) {
                                petList.clear();
                            }
                            for (DocumentSnapshot s : snap.getDocuments()) {
                                Pet pet = s.toObject(Pet.class);
                                petList.add(pet);
                            }
                            petsAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(pets_list_view);
                        }
                    }
                });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.message_fab)
    public void sendMessage(){
        Intent chatActivityIntent = new Intent(this, ChatActivity.class);
        chatActivityIntent.putExtra(getString(R.string.USER_PROFILE_UID), uid);
        chatActivityIntent.putExtra(getString(R.string.USER_PROFILE_NAME), user.getName());
        startActivity(chatActivityIntent);
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


}
