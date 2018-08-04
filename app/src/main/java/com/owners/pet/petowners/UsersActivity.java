package com.owners.pet.petowners;

import android.content.Intent;
import android.os.Parcelable;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.adapters.AllUsersAdapter;
import com.owners.pet.petowners.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    public static final String TAG = UsersActivity.class.getSimpleName();

    private static final String LIST_STATE_KEY = "KEY";
    @BindView(R.id.user_list_rv)
    RecyclerView mUsersListRv;
    private DatabaseReference mDatabase;
    private StorageReference storageReference;
    private StorageReference profileImagesRef;
    private FirebaseUser currentUser;
    private LinearLayoutManager linearLayoutManager;
    private AllUsersAdapter adapter;
    private Parcelable mListState;
    private ArrayList<User> mUserList = new ArrayList<>();


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

        if(savedInstanceState != null){
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }

        init();
        loadUsers();


    }

    private void init() {
        mUsersListRv.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        mUsersListRv.setLayoutManager(linearLayoutManager);

        adapter = new AllUsersAdapter(this, mUserList);
        mUsersListRv.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(LIST_STATE_KEY, mUsersListRv.getLayoutManager().onSaveInstanceState());
    }

    private void loadUsers() {

        mDatabase.child(getString(R.string.COLLECTION_USERS))
                .limitToLast(50).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(!mUserList.isEmpty()){
                        mUserList.clear();
                    }
                    for(DataSnapshot s : dataSnapshot.getChildren()){
                        User user = s.getValue(User.class);
                        mUserList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                    mUsersListRv.getLayoutManager().onRestoreInstanceState(mListState);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




}
