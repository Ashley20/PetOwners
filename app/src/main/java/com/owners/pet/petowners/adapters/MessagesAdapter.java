package com.owners.pet.petowners.adapters;

import android.content.Context;
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
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> messageList;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private Context mContext;

    public MessagesAdapter(List<Message> messageList) {

        this.messageList = messageList;
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);
                return new MessageViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_me_list_item, parent, false);
                return new MeMessageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (message != null && currentUser != null) {
            StorageReference profileImagesRef;
            if (message.getSender().equals(currentUser.getUid())) {
                ((MeMessageViewHolder) holder).messageText.setText(message.getContent());

                // Set date
                Locale l = Locale.US;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", l);
                if(message.getDate() != null){
                    ((MeMessageViewHolder) holder).date.setText(simpleDateFormat.format(message.getDate()));
                }

            } else {

                ((MessageViewHolder) holder).messageText.setText(message.getContent());

                // Set profile pic
                profileImagesRef = storageRef.child("users")
                        .child(message.getSender())
                        .child("profile.jpg");

                GlideApp.with(mContext)
                        .load(profileImagesRef)
                        .placeholder(R.drawable.profile_icon)
                        .into(((MessageViewHolder) holder).profilePic);

                // Set date
                Locale l = Locale.US;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", l);
                ((MessageViewHolder) holder).date.setText(simpleDateFormat.format(message.getDate()));

            }

        }
    }


public static class MessageViewHolder extends RecyclerView.ViewHolder {
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

public static class MeMessageViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.message_text_view)
    TextView messageText;
    @BindView(R.id.date)
    TextView date;

    MeMessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}


    @Override
    public int getItemCount() {
        if(messageList == null){
            return 0;
        }else{
            return messageList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (messageList != null) {
            Message message = messageList.get(position);
            if (message != null && currentUser != null) {
                return message.getSender().equals(currentUser.getUid()) ? 1 : 0;
            }
        }
        return 0;
    }


}
