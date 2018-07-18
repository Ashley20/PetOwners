package com.owners.pet.petowners;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.owners.pet.petowners.adapters.UserAdapter;
import com.owners.pet.petowners.models.ChatUser;
import com.owners.pet.petowners.models.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {
    @BindView(R.id.conversation_list_list_view)
    ListView convListLv;

    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, rootView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null) {
            db.collection(getString(R.string.COLLECTION_USERS))
                    .document(currentUser.getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if (snapshot != null && snapshot.exists()) {
                                User user = snapshot.toObject(User.class);
                                if (user != null) {
                                    loadConversations(user.getConversationList());
                                }
                            }
                        }
                    });
        }

        return rootView;
    }


    private void loadConversations(ArrayList<ChatUser> conversationList) {
        UserAdapter adapter = new UserAdapter(getContext(), conversationList);
        convListLv.setAdapter(adapter);
    }
}
