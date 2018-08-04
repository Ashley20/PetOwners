package com.owners.pet.petowners;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.owners.pet.petowners.adapters.MessagesAdapter;
import com.owners.pet.petowners.models.ChatUser;
import com.owners.pet.petowners.models.Message;
import com.owners.pet.petowners.models.Notification;
import com.owners.pet.petowners.models.User;
import com.owners.pet.petowners.utils.Time;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int GALLERY_PICK_REQUEST = 1;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String uid;
    private String name;
    private StorageReference storageRef;
    private StorageReference chatUserProfileImageRef;
    private StorageReference imageMessagesRef;
    private FirebaseUser currentUser;
    private MessagesAdapter messagesAdapter;
    private ProgressDialog progressDialog;

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

        progressDialog = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        imageMessagesRef = storageRef.child("IMAGE_MESSAGES");

        // Get the current user
        currentUser = mAuth.getCurrentUser();

        Intent intent = getIntent();
        if (intent != null) {
            uid = intent.getStringExtra(getString(R.string.USER_PROFILE_UID));
            name = intent.getStringExtra(getString(R.string.USER_PROFILE_NAME));

            Log.d(TAG, uid + " " + name);
            chatUserProfileImageRef = storageRef.child(getString(R.string.COLLECTION_USERS))
                    .child(uid).child(getString(R.string.storage_profile_ref));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View custom_bar_view = inflater.inflate(R.layout.custom_chat_actionbar, null);

            TextView chatUserNameTv = custom_bar_view.findViewById(R.id.chat_user_name);
            final CircleImageView chatUserProfileImageIv = custom_bar_view.findViewById(R.id.chat_profile_image);

            chatUserNameTv.setText(name);
            mDatabase.child(getString(R.string.COLLECTION_USERS))
                    .child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                User chatUser = dataSnapshot.getValue(User.class);
                                if (chatUser != null) {
                                    if (chatUser.getProfileImageUri() != null) {
                                        Picasso.get()
                                                .load(chatUser.getProfileImageUri())
                                                .placeholder(R.drawable.profile_icon)
                                                .into(chatUserProfileImageIv);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            actionBar.setCustomView(custom_bar_view);


            messageListRv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (bottom < oldBottom) {
                        messageListRv.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                messageListRv.smoothScrollToPosition(
                                        messageListRv.getAdapter().getItemCount() - 1);
                            }
                        }, 100);
                    }
                }
            });
        }

        messagesAdapter = new MessagesAdapter(messageList);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        messageListRv.setHasFixedSize(true);
        messageListRv.setLayoutManager(mLinearLayout);
        messageListRv.setAdapter(messagesAdapter);


        loadMessages();


    }

    private void loadMessages() {
        mDatabase.child(getString(R.string.COLLECTION_MESSAGES))
                .child(currentUser.getUid())
                .child(uid)
                //.orderByChild(getString(R.string.DATE_KEY))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if (messageList != null) {
                                messageList.clear();
                            }
                            for (DataSnapshot s : dataSnapshot.getChildren()) {

                                Message message = s.getValue(Message.class);
                                messageList.add(message);
                            }

                            messagesAdapter.notifyDataSetChanged();

                            messageListRv.getLayoutManager().scrollToPosition(messagesAdapter.getItemCount() - 1);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void checkIfChatExistsWithTheUser() {
        mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                if (!user.getChatWithUidList().contains(uid)) {
                                    updateConversationList(user);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * This function is updating the chatWithUidList and ConversationList
     * fields of both the current user and the other user having chat
     *
     * @param currentUser
     */
    private void updateConversationList(final User currentUser) {
        mDatabase.child(getString(R.string.COLLECTION_USERS))
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            User otherUser = dataSnapshot.getValue(User.class);
                            if (otherUser != null) {
                                updateCurrentUserFields(currentUser, otherUser);
                                updateOtherUserFields(currentUser, otherUser);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Function which updates the other user's ChatWithUidList and ConversationList fields
     * This function simply packs up the current user's uid, name and bio information as a chatUser object
     * and stores it into its own conversationList for later use
     *
     * @param currentUser
     * @param otherUser
     */
    private void updateOtherUserFields(User currentUser, User otherUser) {
        ChatUser chatUserTwo = new ChatUser();

        chatUserTwo.setUid(currentUser.getUid());
        chatUserTwo.setName(currentUser.getName());
        chatUserTwo.setBiography(currentUser.getBiography());
        chatUserTwo.setProfileImageUrl(currentUser.getProfileImageUri());

        otherUser.getChatWithUidList().add(currentUser.getUid());
        otherUser.getConversationList().add(chatUserTwo);

        mDatabase.child(getString(R.string.COLLECTION_USERS)).child(otherUser.getUid())
                .setValue(otherUser);
    }


    /**
     * Function which updates the current user's ChatWithUidList and ConversationList fields
     * This function simply packs up the other user's uid, name and bio information as a chatUser object
     * and stores it into its own conversationList for later use
     *
     * @param currentUser
     * @param otherUser
     */
    private void updateCurrentUserFields(User currentUser, User otherUser) {
        ChatUser chatUserOne = new ChatUser();

        chatUserOne.setUid(otherUser.getUid());
        chatUserOne.setName(otherUser.getName());
        chatUserOne.setBiography(otherUser.getBiography());
        chatUserOne.setProfileImageUrl(otherUser.getProfileImageUri());


        // Update our current user
        currentUser.getChatWithUidList().add(chatUserOne.getUid());
        currentUser.getConversationList().add(chatUserOne);

        mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                .setValue(currentUser);
    }


    @OnClick(R.id.send_btn)
    public void sendMessage() {

        if (currentUser != null) {
            checkIfChatExistsWithTheUser();
        }

        final String content = messageEditText.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            messageEditText.setText("");


            // Create a new message and set the content, receiver and sender information
            final Message newMessage = new Message();
            newMessage.setSender(currentUser.getUid());
            newMessage.setReceiver(uid);
            newMessage.setType("text");
            newMessage.setContent(content);
            newMessage.setTimestamp(Time.getTime());

            mDatabase.child(getString(R.string.COLLECTION_MESSAGES)).child(uid)
                    .child(currentUser.getUid()).push().setValue(newMessage);


            mDatabase.child(getString(R.string.COLLECTION_MESSAGES)).child(currentUser.getUid())
                    .child(uid).push().setValue(newMessage);

            Notification notification = new Notification();
            notification.setFromUid(currentUser.getUid());
            notification.setFrom(currentUser.getDisplayName());
            notification.setTo(uid);
            notification.setContent(content);
            notification.setTimestamp(Time.getTime());


            // Update notification database
            mDatabase.child(getString(R.string.COLLECTION_NOTIFICATIONS))
                    .push()
                    .setValue(notification);
        }

    }


    @OnClick(R.id.upload_image_btn)
    public void uploadImage() {

        if (currentUser != null) {
            checkIfChatExistsWithTheUser();
        }

        Intent openGalleryIntent = new Intent();
        openGalleryIntent.setType("image/*");
        openGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(openGalleryIntent, "Complete action using"),
                GALLERY_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_REQUEST && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            if (imageUri != null) {
                progressDialog.setMessage("Image uploading");
                progressDialog.show();

                UploadTask uploadTask = imageMessagesRef
                        .child(imageUri.getLastPathSegment()).putFile(imageUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // Continue with the task to get the download URL
                        return imageMessagesRef.child(imageUri.getLastPathSegment()).getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        progressDialog.dismiss();

                        Uri downloadUri = task.getResult();

                        Message imageMessage = new Message();
                        imageMessage.setSender(currentUser.getUid());
                        imageMessage.setReceiver(uid);
                        imageMessage.setType("image");
                        imageMessage.setContent(downloadUri.toString());
                        imageMessage.setTimestamp(Time.getTime());


                        mDatabase.child(getString(R.string.COLLECTION_MESSAGES)).child(currentUser.getUid())
                                .child(uid).push().setValue(imageMessage);

                        mDatabase.child(getString(R.string.COLLECTION_MESSAGES)).child(uid)
                                .child(currentUser.getUid()).push().setValue(imageMessage);

                    }
                });
            }
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
