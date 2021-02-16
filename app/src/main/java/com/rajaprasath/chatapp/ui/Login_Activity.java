package com.rajaprasath.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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

    private Button login, create_account,google_signin;
    private AutoCompleteTextView email;
    private EditText password;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private final DocumentReference documentReference= collectionReference.document();
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN=999;
    private String personName;
    private String currentuserid;
    private String personId;
    private Uri personPhoto;
    private final String google_signin_mode="GOOGLE_SIGNIN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);
        //getSupportActionBar().hide();
        login = findViewById(R.id.login);
        create_account = findViewById(R.id.create_account);
        SignInButton googlesignin = findViewById(R.id.sign_in_button);
        googlesignin.setSize(SignInButton.SIZE_STANDARD);
        TextView google= (TextView) googlesignin.getChildAt(0);
        google.setText("Google");
        email = findViewById(R.id._email);
        password = findViewById(R.id._password);
        firebaseAuth=FirebaseAuth.getInstance();



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
                // Log.d("email", "onClick: " + email.getText().toString().trim());
                String emailid = email.getText().toString().trim();
                String pwd = password.getText().toString().trim();
                loginwithEmailAndPassword(emailid, pwd);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                             currentuserid= user.getUid();


                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(Login_Activity.this);

                            if (acct != null) {

                                 personName = acct.getDisplayName();
                                 String personGivenName = acct.getGivenName();
                                 String personFamilyName = acct.getFamilyName();
                                 String personEmail = acct.getEmail();
                                 personPhoto= acct.getPhotoUrl();
                                 personId = acct.getId();

                            }


                             collectionReference.document(currentuserid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                     finish();

                                     }
                                     else {
                                         Map<String, String> userobj = new HashMap<>();
                                         userobj.put(Util.userid, currentuserid);
                                         userobj.put(Util.username, personName);
                                         userobj.put(Util.imageurl,"default");
                                         collectionReference.document(currentuserid).set(userobj).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 Intent intent= new Intent(Login_Activity.this,ProfileActivity.class);
                                                 intent.putExtra(Util.userid,currentuserid);
                                                 intent.putExtra(Util.username,personName);
                                                 intent.putExtra(Util.imageurl,personPhoto.toString());
                                                 startActivity(intent);
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


}


