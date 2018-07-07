package com.owners.pet.petowners;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.fullname_edit_text) EditText fullName;
    @BindView(R.id.email_edit_text) EditText email;
    @BindView(R.id.password_edit_text) EditText password;
    @BindView(R.id.login_link) TextView login_link;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
    }


    @OnClick(R.id.sign_up_btn)
    public void validateAndSignup(){

        String nameField = fullName.getText().toString();
        String emailField = email.getText().toString();
        String passwordField = password.getText().toString();

        // If signup form validation is successfull sign the user up
        if(validate(nameField, emailField, passwordField)){
            progressBar.setVisibility(View.VISIBLE);
            signup(nameField, emailField, passwordField);
        }

    }

    @OnClick(R.id.login_link)
    public void openLoginActivity(){
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);

    }


    /**
     * Function for creating a new firebase user
     * @param name
     * @param email
     * @param password
     */
    private void signup(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.INVISIBLE);

                        if (task.isSuccessful()) {

                            // Sign up success, set display name as the full name of the user
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            Toast.makeText(getApplicationContext(), "You've successfully signed up", Toast.LENGTH_LONG).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_LONG).show();

                        }

                    }
                });
    }


    /**
     *  Validate signup form fields
     * @param name
     * @param email
     * @param password
     */
    private boolean validate(String name, String email, String password) {


        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if((password.length() < 6)) {
            Toast.makeText(this, "Password has to be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
