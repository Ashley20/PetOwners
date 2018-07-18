package com.owners.pet.petowners;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owners.pet.petowners.Glide.GlideApp;
import com.owners.pet.petowners.adapters.MessagesAdapter;
import com.owners.pet.petowners.models.ChatUser;
import com.owners.pet.petowners.models.Message;
import com.owners.pet.petowners.models.User;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uid;
    private String name;
    private StorageReference storageRef;
    private StorageReference chatUserProfileImageRef;
    private FirebaseUser currentUser;
    private MessagesAdapter messagesAdapter;
    private boolean chatAlreadyExists;

    @BindView(R.id.message_edit_text)
    EditText messageEditText;
    @BindView(R.id.message_list_recycler_view)
    RecyclerView messageListRv;

    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.bind(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Get the current user
        currentUser = mAuth.getCurrentUser();

        Intent intent = getIntent();
        if (intent != null) {
            uid = intent.getStringExtra(getString(R.string.USER_PROFILE_UID));
            name = intent.getStringExtra(getString(R.string.USER_PROFILE_NAME));

            chatUserProfileImageRef = storageRef.child(getString(R.string.COLLECTION_USERS))
                    .child(uid).child("profile.jpg");
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View custom_bar_view = inflater.inflate(R.layout.custom_chat_actionbar, null);

            TextView chatUserNameTv = custom_bar_view.findViewById(R.id.chat_user_name);
            CircleImageView chatUserProfileImageIv = custom_bar_view.findViewById(R.id.chat_profile_image);

            chatUserNameTv.setText(name);
            GlideApp.with(getApplicationContext())
                    .load(chatUserProfileImageRef)
                    .placeholder(R.drawable.profile_icon)
                    .into(chatUserProfileImageIv);

            actionBar.setCustomView(custom_bar_view);
        }

        messagesAdapter = new MessagesAdapter(messageList);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        messageListRv.setHasFixedSize(true);
        messageListRv.setLayoutManager(mLinearLayout);
        messageListRv.setAdapter(messagesAdapter);


        if (currentUser != null) {
            checkIfChatExistsWithTheUser();
        }

        loadMessages();


    }

    private void loadMessages() {
        db.collection(getString(R.string.COLLECTION_MESSAGES))
                .document(currentUser.getUid())
                .collection(uid)
                .orderBy(getString(R.string.DATE_KEY), Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snap, @Nullable FirebaseFirestoreException e) {
                        if (snap != null) {
                            if (messageList != null) {
                                messageList.clear();
                            }
                            for (DocumentSnapshot s : snap.getDocuments()) {
                                Message message = s.toObject(Message.class);
                                messageList.add(message);
                            }
                            messagesAdapter.notifyDataSetChanged();

                            messageListRv.getLayoutManager().scrollToPosition(messagesAdapter.getItemCount() - 4);


                        }
                    }
                });
    }

    private void checkIfChatExistsWithTheUser() {
        db.collection(getString(R.string.COLLECTION_USERS)).document(currentUser.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (snapshot != null && snapshot.exists()) {
                            User user = snapshot.toObject(User.class);
                            if (user != null) {
                                if (!user.getChatWithUidList().contains(uid)) {
                                    updateConversationList(user);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Updates the user conversation list by adding a new conversation
     *
     * @param user
     */
    private void updateConversationList(final User user) {
        db.collection(getString(R.string.COLLECTION_USERS))
                .document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        User u = snapshot.toObject(User.class);
                        ChatUser chatUser = new ChatUser();
                        if (u != null) {
                            // Create a chat user with the fetched user uid , bio and name
                            chatUser.setUid(u.getUid());
                            chatUser.setName(u.getName());
                            chatUser.setBiography(u.getBiography());

                        }

                        user.getChatWithUidList().add(uid);
                        user.getConversationList().add(chatUser);

                        db.collection(getString(R.string.COLLECTION_USERS))
                                .document(currentUser.getUid())
                                .set(user);
                    }
                });

    }

    @OnClick(R.id.send_btn)
    public void sendMessage() {
        String content = messageEditText.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            messageEditText.setText("");
            // Create a new message and set the content, receiver and sender information
            Message newMessage = new Message();
            newMessage.setSender(currentUser.getUid());
            newMessage.setReceiver(uid);
            newMessage.setContent(content);


            // Store the message in firestore
            db.collection(getString(R.string.COLLECTION_MESSAGES))
                    .document(currentUser.getUid())
                    .collection(uid).document().set(newMessage);

            db.collection(getString(R.string.COLLECTION_MESSAGES))
                    .document(uid)
                    .collection(currentUser.getUid())
                    .document().set(newMessage);


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
