package com.owners.pet.petowners;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.all_users_title));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ButterKnife.bind(this);

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        
        loadUsers();
    }

    private void loadUsers() {

        Query query = FirebaseFirestore.getInstance()
                .collection(getString(R.string.COLLECTION_USERS))
                .limit(50);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<User, UsersViewHolder>(options){

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull User model) {
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
        };

        adapter.notifyDataSetChanged();
        // Finally set the adapter
        mUsersList.setAdapter(adapter);
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_profile_image_view)
        CircleImageView userProfileImage;
        @BindView(R.id.user_fullname)
        TextView userFullName;
        @BindView(R.id.user_bio)
        TextView userBio;

        public UsersViewHolder(View itemView) {
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
