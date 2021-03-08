package com.rajaprasath.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rajaprasath.chatapp.Adapter.UserAdapter;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.fragment.UsersFragment;
import com.rajaprasath.chatapp.ui.stranger.CategoryActivity;
import com.rajaprasath.chatapp.util.Util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class splashScreen extends AppCompatActivity {

    private Button get_started;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private final FirebaseFirestore db =  FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Objects.requireNonNull(getSupportActionBar()).hide();

        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();

        ImageView background = findViewById(R.id.iv_background);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        background.setAnimation(fadeIn);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                user = firebaseAuth.getCurrentUser();
                firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if(user!=null) {
                            user=firebaseAuth.getCurrentUser();
                            final String currentId = user.getUid();

                            collectionReference.document(currentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                                        check_users();
                                        updatelastseen();
                                    }
                                }
                            });
                        }
                        else {
                            startActivity(new Intent(splashScreen.this,Login_Activity.class));
                            finish();
                        }
                    }
                });
            }
        }, 2000);
    }

    private void check_users() {
         final List<String> ids=new ArrayList<>();
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        collectionReference.document(firebaseUser.getUid()).collection("message").orderBy("messagetime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot value=task.getResult();
                if (value!=null){
                    ids.clear();
                    for (QueryDocumentSnapshot documentSnapshot :value){

                        if (documentSnapshot!=null){
                            String id= documentSnapshot.getId();
                            if (documentSnapshot.get("trusted")!=null) {
                                if (documentSnapshot.getBoolean("trusted")) {

                                    ids.add(id);

                                } else if (!documentSnapshot.getBoolean("trusted")) {
                                    ids.remove(id);

                                }
                            }
                        }

                    }
                    if (ids.size() == 0){
                        Intent intent=new Intent(splashScreen.this, CategoryActivity.class);
                        intent.putExtra("activity","splashscreen");
                        startActivity(intent);
                    }
                    else {
                        startActivity(new Intent(splashScreen.this, MainActivity.class));
                    }
                    finish();
                }
            }
        });

    }

    private void updatelastseen() {
        if (User.getInstance().getUserid()!=null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Util.lastseen, Timestamp.now());
            collectionReference.document(User.getInstance().getUserid()).set(hashMap, SetOptions.merge());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
