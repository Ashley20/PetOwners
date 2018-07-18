package com.owners.pet.petowners.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.ChatUser;
import com.owners.pet.petowners.models.Pet;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends ArrayAdapter<ChatUser> {
    private Context mContext;
    private ArrayList<ChatUser> chatUserList;
    private StorageReference storageRef;

    public UserAdapter (Context context, ArrayList<ChatUser> chatUserList) {
        super(context,0, chatUserList);
        this.mContext = context;
        this.chatUserList = chatUserList;

        storageRef = FirebaseStorage.getInstance().getReference();

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
        }

        final ChatUser chatUser = chatUserList.get(position);

        CircleImageView profilePic = convertView.findViewById(R.id.user_profile_image_view);
        TextView name = convertView.findViewById(R.id.user_fullname);
        TextView bio = convertView.findViewById(R.id.user_bio);

        // Update UI
        if (chatUser != null) {
            name.setText(chatUser.getName());
            bio.setText(chatUser.getBiography());


            StorageReference profileImagesRef = storageRef.child(mContext.getString(R.string.storage_users_ref))
                    .child(chatUser.getUid()).child(mContext.getString(R.string.storage_profile_ref));

            GlideApp.with(mContext)
                    .load(profileImagesRef)
                    .placeholder(R.drawable.profile_icon)
                    .into(profilePic);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        if(chatUserList != null){
            return chatUserList.size();
        }else {
            return 0;
        }
    }
}
