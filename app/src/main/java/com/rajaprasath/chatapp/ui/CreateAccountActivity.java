package com.rajaprasath.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText username_Account;
    private EditText email_Account;
    private EditText password_Account;
    private Button create_account;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        getSupportActionBar().hide();

        username_Account=findViewById(R.id.username);
        email_Account=findViewById(R.id.email_account);
        password_Account=findViewById(R.id.password_account);
        create_account=findViewById(R.id.create_account_);
       User.getInstance().setInterest(null);

        firebaseAuth= FirebaseAuth.getInstance();

        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if(currentUser!=null){

                }
                else{

                }
            }
        };

        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(email_Account.getText().toString())
                        &&!TextUtils.isEmpty(username_Account.getText().toString())
                        &&!TextUtils.isEmpty(password_Account.getText().toString())){
                    String email=email_Account.getText().toString();
                    String username=username_Account.getText().toString();
                    String password=password_Account.getText().toString();
                     createUserEmailAccount(email,password,username);
                }
                else {

                }
            }
        });
    }
    public void createUserEmailAccount(final String email, final String password, final String username){
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {

            Intent intent = new Intent(CreateAccountActivity.this, ProfileActivity.class);
            intent.putExtra("username",username);
            intent.putExtra("email",email);
            intent.putExtra("password",password);

            Toast.makeText(this, "I know this app is kinda lame!", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();

        }
        else {

        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}
