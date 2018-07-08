package com.owners.pet.petowners;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.forgot_password_link) TextView forgot_password_link;
    @BindView(R.id.password_edit_text) EditText password;
    @BindView(R.id.email_edit_text) EditText email;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If the user is already logged in then redirect the user to the main page
        if(currentUser != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.login_btn)
    public void validateAndLogin(){
        String emailField = email.getText().toString();
        String passwordField = password.getText().toString();

        if(TextUtils.isEmpty(emailField) || TextUtils.isEmpty(passwordField)){
            Toast.makeText(this, getString(R.string.fill_in_required_fields_text),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        login(emailField, passwordField);

    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.INVISIBLE);

                        if(task.isSuccessful()){
                            // User is successfully logged in, open the main page
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }else {
                            // Display an error message indicating that the authentication failed
                            Toast.makeText(getApplicationContext(),getString(R.string.auth_fail_text),
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    @OnClick(R.id.forgot_password_link)
    public void remindPassword(){
        final EditText editText = new EditText(this);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.reset_your_password_title))
                .setMessage(getString(R.string.reset_your_password_message))
                .setView(editText)
                .setPositiveButton(getString(R.string.pw_reset_dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = editText.getText().toString();

                        if(TextUtils.isEmpty(email)){
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.fill_in_email_field_text), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        sendPasswordResetEmail(email);
                    }
                })
                .setNegativeButton(getString(R.string.pw_reset_dialog_negative_button), null)
                .create();

        alertDialog.show();

    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.successful_password_reset_email_link_send),
                                    Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(getApplicationContext(), getString(R.string.fail_password_reset_email_link_send),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    @OnClick(R.id.signup_link)
    public void openSignupActivity(){
        Intent signupActivityIntent = new Intent(this, SignupActivity.class);
        startActivity(signupActivityIntent);
    }
}
