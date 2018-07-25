package com.owners.pet.petowners;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.models.User;
import com.squareup.picasso.Picasso;

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
    private FirebaseUser currentUser;
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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void loadUsers() {

        Query query = db.collection("users");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<User, UsersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull final User model) {

                if(model.getProfileImageUri() != null){
                    Picasso.get()
                            .load(model.getProfileImageUri())
                            .placeholder(R.drawable.profile_icon)
                            .into(holder.userProfileImage);
                }

                holder.userFullName.setText(model.getName());
                holder.userBio.setText(model.getBiography());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(currentUser != null){
                            if(currentUser.getUid().equals(model.getUid())){
                                Intent profileActivityIntent =
                                        new Intent(getApplicationContext(), ProfileActivity.class);
                                startActivity(profileActivityIntent);
                            }else{
                                Intent othersProfileActivity =
                                        new Intent(getApplicationContext(), OthersProfileActivity.class);
                                othersProfileActivity.putExtra(getString(R.string.USER_PROFILE_UID), model.getUid());
                                startActivity(othersProfileActivity);
                            }
                        }
                    }
                });
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
