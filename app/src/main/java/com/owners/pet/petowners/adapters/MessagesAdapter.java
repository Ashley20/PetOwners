package com.owners.pet.petowners.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Message;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private FirebaseAuth mAuth;

    public MessagesAdapter(List<Message> messageList) {

        this.messageList = messageList;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_item, parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.message_text_view)
        TextView messageText;
        @BindView(R.id.custom_message_profile_pic)
        CircleImageView profilePic;


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
            } else {
                holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                holder.messageText.setTextColor(Color.WHITE);
            }
            holder.messageText.setText(message.getContent());
        }


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


}
