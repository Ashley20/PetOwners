package com.owners.pet.petowners.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.ChatActivity;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.ChatUser;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends ArrayAdapter<ChatUser> {
    public static final String TAG = UserAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ChatUser> chatUserList;
    private StorageReference storageRef;
    private StorageReference profileImageRef;

    public UserAdapter(Context context, ArrayList<ChatUser> chatUserList) {
        super(context, 0, chatUserList);
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

        final CircleImageView profilePic = convertView.findViewById(R.id.user_profile_image_view);
        TextView name = convertView.findViewById(R.id.user_fullname);
        TextView bio = convertView.findViewById(R.id.user_bio);
        TextView lastMessageTime = convertView.findViewById(R.id.last_message_time_text_view);

        // Update UI
        if (chatUser != null) {
            name.setText(chatUser.getName());
            bio.setText(chatUser.getLastMessage());

            profileImageRef = storageRef.child(mContext.getString(R.string.COLLECTION_USERS))
                    .child(chatUser.getUid())
                    .child(mContext.getString(R.string.storage_profile_ref));

            if(chatUser.getProfileImageUrl() != null && !chatUser.getProfileImageUrl().equals("")){
                profileImageRef.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get()
                                        .load(uri)
                                        .placeholder(R.drawable.profile_icon)
                                        .into(profilePic);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
            }


            if (chatUser.getLastMessageTimeStamp() != null) {
                lastMessageTime.setText(chatUser.getLastMessageTimeStamp());
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chatActivityIntent = new Intent(mContext, ChatActivity.class);
                    chatActivityIntent.putExtra(mContext.getString(R.string.USER_PROFILE_UID), chatUser.getUid());
                    chatActivityIntent.putExtra(mContext.getString(R.string.USER_PROFILE_NAME), chatUser.getName());
                    mContext.startActivity(chatActivityIntent);
                }
            });
        }

        return convertView;
    }


    @Override
    public int getCount() {
        if (chatUserList != null) {
            return chatUserList.size();
        } else {
            return 0;
        }
    }

}
