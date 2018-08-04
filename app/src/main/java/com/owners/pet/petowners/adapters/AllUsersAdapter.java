package com.owners.pet.petowners.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.OthersProfileActivity;
import com.owners.pet.petowners.ProfileActivity;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.ViewHolder> {
    private ArrayList<User> mUserList;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private StorageReference profileImagesRef;
    private FirebaseUser currentUser;
    private Context mContext;

    public AllUsersAdapter(Context context, ArrayList<User> userList) {
        this.mUserList = userList;
        this.mContext = context;
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_profile_image_view)
        CircleImageView userProfileImage;
        @BindView(R.id.user_fullname)
        TextView userFullName;
        @BindView(R.id.user_bio)
        TextView userBio;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUserList.get(position);

        if(user.getProfileImageUri() != null){
            Picasso.get()
                    .load(user.getProfileImageUri())
                    .placeholder(R.drawable.profile_icon)
                    .into(holder.userProfileImage);
        }

        holder.userFullName.setText(user.getName());
        holder.userBio.setText(user.getBiography());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentUser != null){
                    if(currentUser.getUid().equals(user.getUid())){
                        Intent profileActivityIntent =
                                new Intent(mContext, ProfileActivity.class);
                        mContext.startActivity(profileActivityIntent);
                    }else{
                        Intent othersProfileActivity =
                                new Intent(mContext, OthersProfileActivity.class);
                        othersProfileActivity.putExtra(mContext.getString(R.string.USER_PROFILE_UID), user.getUid());
                        mContext.startActivity(othersProfileActivity);
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }
}
