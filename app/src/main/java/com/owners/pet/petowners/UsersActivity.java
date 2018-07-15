package com.owners.pet.petowners;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    @BindView(R.id.user_list_rv)
    RecyclerView mUsersList;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private StorageReference storageReference;
    private StorageReference profileImagesRef;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.all_users_title));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        init();
        loadUsers();

    }

    private void init() {
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUsersList.setLayoutManager(linearLayoutManager);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void loadUsers() {

        Query query = db.collection("users");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<User, UsersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull User model) {
                profileImagesRef = storageReference.child("users").child(model.getUid()).child("profile.jpg");

                GlideApp.with(getApplicationContext())
                        .load(profileImagesRef)
                        .placeholder(R.drawable.profile_icon)
                        .into(holder.userProfileImage);

                holder.userFullName.setText(model.getName());
                holder.userBio.setText(model.getBiography());
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_list_item, parent, false);

                return new UsersViewHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Log.e("errorrrrrrrrrrrr", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        // Finally set the adapter
        mUsersList.setAdapter(adapter);

    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_profile_image_view)
        CircleImageView userProfileImage;
        @BindView(R.id.user_fullname)
        TextView userFullName;
        @BindView(R.id.user_bio)
        TextView userBio;

        UsersViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
