package com.owners.pet.petowners.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private StorageReference profileImagesRef;
    private Context mContext;

    public MessagesAdapter(List<Message> messageList) {

        this.messageList = messageList;
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_item, parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.message_text_view)
        TextView messageText;
        @BindView(R.id.custom_message_profile_pic)
        CircleImageView profilePic;
        @BindView(R.id.date)
        TextView date;

        MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && message != null) {
            if (message.getSender().equals(currentUser.getUid())) {
                holder.messageText.setBackgroundColor(Color.WHITE);
                holder.messageText.setTextColor(Color.BLACK);

                // Set profile pic
                profileImagesRef = storageRef.child("users")
                        .child(message.getSender())
                        .child("profile.jpg");

                GlideApp.with(mContext)
                        .load(profileImagesRef)
                        .placeholder(R.drawable.profile_icon)
                        .into(holder.profilePic);

            } else {
                holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                holder.messageText.setTextColor(Color.WHITE);

                // Set profile pic
                profileImagesRef = storageRef.child("users")
                        .child(message.getSender())
                        .child("profile.jpg");

                GlideApp.with(mContext)
                        .load(profileImagesRef)
                        .placeholder(R.drawable.profile_icon)
                        .into(holder.profilePic);

            }
            holder.messageText.setText(message.getContent());
            // Set date
            Locale l = Locale.US;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a",l );
            holder.date.setText(simpleDateFormat.format(message.getDate()));


        }




    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


}
