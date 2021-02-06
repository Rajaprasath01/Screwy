package com.rajaprasath.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class splashScreen extends AppCompatActivity {

    private Button get_started;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db=  FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

     // startActivity(new Intent(splashScreen.this,ProfileActivity.class));
        //finish();
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();

        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(user!=null){

                    user=firebaseAuth.getCurrentUser();
                    final String currentid= user.getUid();

                    collectionReference.document(currentid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isComplete() && task.isSuccessful()){
                                DocumentSnapshot snapshot = task.getResult();
                                User user = User.getInstance();
                                user.setUserid(snapshot.getString("userid"));
                                user.setUsername(snapshot.getString("username"));
                                user.setImageurl(snapshot.getString("imageurl"));
                                user.setNickname(snapshot.getString("nickname"));
                                user.setGender(snapshot.getString("gender"));
                                user.setInterest((ArrayList<String>) snapshot.get("interest"));
                                user.setAbout(snapshot.getString("about"));

                                startActivity(new Intent(splashScreen.this, MainActivity.class));
                                finish();
                            }
                            else {

                            }
                        }
                    });

                }
                else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(splashScreen.this,Login_Activity.class));
                             finish();
                        }
                    }, 1000);

                }
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
