package com.rajaprasath.chatapp.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Login_Activity extends AppCompatActivity {

    private Button login;
    private TextView create_account;
    private AutoCompleteTextView email;
    private EditText password;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 999;
    private String personName;
    private String currentUserId;
    private Uri personPhoto;

    private BottomSheetBehavior behavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        fullScreenCall();
        setContentView(R.layout.activity_login_);
        login = findViewById(R.id.login);
        create_account = findViewById(R.id.create_account);
        SignInButton googlesignin = findViewById(R.id.sign_in_button);
        googlesignin.setSize(SignInButton.SIZE_STANDARD);
        TextView google= (TextView) googlesignin.getChildAt(0);
        google.setText("Sign in with Google");
        email = findViewById(R.id._email);
        password = findViewById(R.id._password);
        firebaseAuth=FirebaseAuth.getInstance();

        View bottomSheetDialog = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheetDialog);

        LinearLayout showBottomSheet = findViewById(R.id.ll_show_login_options);
        showBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login_Activity.this, CreateAccountActivity.class));
                 finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailId = email.getText().toString().trim();
                String pwd = password.getText().toString().trim();
                loginwithEmailAndPassword(emailId, pwd);
            }
        });
        googlesignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }
    

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.d("TAG", "onActivityResult: " + e.toString());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                            currentUserId = user.getUid();

                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(Login_Activity.this);

                            if (acct != null) {
                                 personName = acct.getDisplayName();
                                 personPhoto= acct.getPhotoUrl();
                            }

                             collectionReference.document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                 @Override
                                 public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                     if ((task.isComplete() && task.isSuccessful()) && Objects.requireNonNull(task.getResult()).exists()){
                                     DocumentSnapshot snapshot= task.getResult();
                                     User.getInstance().setUsername(snapshot.getString(Util.username));
                                     User.getInstance().setUserid(snapshot.getString(Util.userid));
                                     User.getInstance().setImageurl(snapshot.getString(Util.imageurl));
                                     User.getInstance().setNickname(snapshot.getString(Util.nickname));
                                     User.getInstance().setGender(snapshot.getString(Util.gender));
                                     User.getInstance().setInterest((ArrayList<String>) snapshot.get(Util.interest));
                                     User.getInstance().setAbout(snapshot.getString(Util.about));
                                     startActivity(new Intent(Login_Activity.this, MainActivity.class));

                                     Toast.makeText(Login_Activity.this, "I know this app is kinda lame!", Toast.LENGTH_SHORT).show();

                                     finish();

                                     }
                                     else {
                                         Map<String, String> userobj = new HashMap<>();
                                         userobj.put(Util.userid, currentUserId);
                                         userobj.put(Util.username, personName);
                                         userobj.put(Util.imageurl,"default");
                                         collectionReference.document(currentUserId).set(userobj).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 Intent intent= new Intent(Login_Activity.this,ProfileActivity.class);
                                                 intent.putExtra(Util.userid, currentUserId);
                                                 intent.putExtra(Util.username,personName);
                                                 intent.putExtra(Util.imageurl,personPhoto.toString());
                                                 startActivity(intent);
                                                 Toast.makeText(Login_Activity.this, "I know this app is kinda lame!", Toast.LENGTH_SHORT).show();
                                                 finish();
                                             }
                                         });


                                     }
                                 }
                             }).addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {

                                 }
                             });


                        } else {
                            // If sign in fails, display a message to the user.



                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }


        });


    }



    private void loginwithEmailAndPassword(final String email, final String password) {
        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                   if (task.isSuccessful() && task.isComplete()) {
                       user = firebaseAuth.getCurrentUser();
                       assert user != null;

                       String currentuserid = user.getUid();

                       collectionReference.document(currentuserid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                               if ((task.isComplete() && task.isSuccessful()) && Objects.requireNonNull(task.getResult()).exists()) {
                                   DocumentSnapshot snapshot = task.getResult();

                                   User.getInstance().setUsername(snapshot.getString(Util.username));
                                   User.getInstance().setUserid(snapshot.getString(Util.userid));
                                   User.getInstance().setImageurl(snapshot.getString(Util.imageurl));
                                   User.getInstance().setNickname(snapshot.getString(Util.nickname));
                                   User.getInstance().setGender(snapshot.getString(Util.gender));
                                   User.getInstance().setInterest((ArrayList<String>) snapshot.get(Util.interest));
                                   User.getInstance().setAbout(snapshot.getString(Util.about));

                                   startActivity(new Intent(Login_Activity.this, MainActivity.class));
                                   finish();
                               }
                           }
                       });
                   }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(Login_Activity.this, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public void fullScreenCall() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}


