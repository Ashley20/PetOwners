package com.owners.pet.petowners;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owners.pet.petowners.adapters.UserAdapter;
import com.owners.pet.petowners.models.ChatUser;
import com.owners.pet.petowners.models.Message;
import com.owners.pet.petowners.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatFragment extends Fragment {
    public static final String TAG = ChatFragment.class.getSimpleName();
    @BindView(R.id.conversation_list_list_view)
    ListView convListLv;
    private User user;

    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, rootView);

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        user = dataSnapshot.getValue(User.class);
                        int i = 0;
                        if (user != null) {
                            for (String uid : user.getChatWithUidList()) {
                                if (uid != null) {

                                    final int finalI = i;
                                    mDatabase.child(getString(R.string.COLLECTION_MESSAGES)).child(currentUser.getUid())
                                            .child(uid).orderByKey().limitToLast(1)
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        for(DataSnapshot snap : dataSnapshot.getChildren()){
                                                            Message message = snap.getValue(Message.class);
                                                            if (message != null) {
                                                                Log.d(TAG, message.getContent());
                                                                String lastMessage = message.getContent();
                                                                String lastMessageTimeStamp = message.getTimestamp();

                                                                user.getConversationList().get(finalI).setLastMessage(lastMessage);
                                                                user.getConversationList().get(finalI)
                                                                        .setLastMessageTimeStamp(lastMessageTimeStamp);
                                                            }
                                                        }

                                                        loadConversations(user.getConversationList());
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                }
                                i++;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        return rootView;
    }


    private void loadConversations(ArrayList<ChatUser> conversationList) {
        if (getContext() != null) {
            UserAdapter adapter = new UserAdapter(getContext(), conversationList);
            adapter.notifyDataSetChanged();
            convListLv.setAdapter(adapter);
        }
    }
}
