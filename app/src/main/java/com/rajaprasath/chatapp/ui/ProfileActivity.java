package com.rajaprasath.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.rajaprasath.chatapp.Adapter.InterestAdapter;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.model.Category;
import com.rajaprasath.chatapp.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nickname;
    private TextView gender,interest,about;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private Button finish;
    private String userid,email,password;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private String username;
    private RelativeLayout viewgroup;
    private String personPhoto;
    private final String google_signin_mode="GOOGLE_SIGNIN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nickname=findViewById(R.id.nickname);
        gender=findViewById(R.id.gender);
        interest=findViewById(R.id.interest);
        about=findViewById(R.id.about);
        finish=findViewById(R.id.finish_button);
        viewgroup=findViewById(R.id.profile_activity_viewgroup);
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

        User.getInstance().setInterest(null);


       username=getIntent().getStringExtra(Util.username);
       personPhoto=getIntent().getStringExtra(Util.imageurl);
       email=getIntent().getStringExtra(Util.email);
       password=getIntent().getStringExtra(Util.password);



        nickname.setOnClickListener(this);
        gender.setOnClickListener(this);
        interest.setOnClickListener(this);
        about.setOnClickListener(this);
        finish.setOnClickListener(this);
         viewgroup.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nickname:
                set_nickname();
                break;
            case R.id.gender:
                InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(viewgroup.getWindowToken(), 0);
                set_gender();
                break;
            case R.id.interest:
                InputMethodManager x = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
                x.hideSoftInputFromWindow(viewgroup.getWindowToken(), 0);

                set_interest();
                break;
            case R.id.about:
                set_about();
                break;
            case R.id.finish_button:
                email=getIntent().getStringExtra(Util.email);
                password=getIntent().getStringExtra(Util.password);
                username=getIntent().getStringExtra(Util.username);
                finish_profile(email,password,username);
                break;

            case R.id.profile_activity_viewgroup:
                InputMethodManager y = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
                y.hideSoftInputFromWindow(viewgroup.getWindowToken(), 0);

        }
    }

    private void finish_profile(String email, String password, String username) {
        Log.d("maara", "finish_profile: "+nickname.getText().toString()+"  "+gender.getText().toString()+"  "+interest.getText().toString()+"  "+about.getText().toString() );
        if (!nickname.getText().toString().isEmpty()  &&  !gender.getText().toString().isEmpty()
                && !interest.getText().toString().isEmpty()  &&  !about.getText().toString().isEmpty())
        {
            String nickname = this.nickname.getText().toString().trim();
            String gender = this.gender.getText().toString().trim();

            ArrayList<String> interest = User.getInstance().getInterest();
            String about = this.about.getText().toString().trim();

            createUserEmailAccount(email, password, username,nickname,gender,interest,about);

        }

    }

    private void create_profile(String userid, final String nickname, final String gender, final ArrayList<String> interest, final String about) {


        Map<String,Object> userobj = new HashMap<>();
        userobj.put(Util.userid, userid);
        userobj.put(Util.username,username);
        userobj.put(Util.imageurl,"default");
        if (personPhoto!=null){
            userobj.put(Util.imageurl,personPhoto);
        }
        userobj.put(Util.nickname,nickname);
        userobj.put(Util.gender,gender);
        userobj.put(Util.interest,interest);
        userobj.put(Util.about,about);

        collectionReference.document(userid).set(userobj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful() && task.isComplete()){

                    User user =User.getInstance();
                    user.setUserid(ProfileActivity.this.userid);
                    user.setUsername(username);
                    user.setImageurl("default");
                    if (personPhoto!=null){
                        user.setImageurl(personPhoto);
                    }
                    user.setNickname(nickname);
                    user.setGender(gender);
                    user.setInterest(interest);
                    user.setAbout(about);

                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.putExtra(Util.username,username);
                    intent.putExtra(Util.userid, ProfileActivity.this.userid);
                    intent.putExtra(Util.imageurl,personPhoto);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });



    }

    private void set_nickname() {

    }

    private void set_interest() {

        String[] name = new String[]{"MEME","SPORTS","CODING","STARTUP","MUSIC","DANCE","MOVIES","GAMING","BOOKS","TRAVEL"};
        int[] drawables = new int[]{R.drawable.meme,R.drawable.sports,R.drawable.coding,R.drawable.startup,
                R.drawable.music,R.drawable.dance,R.drawable.movies,R.drawable.gaming,R.drawable.books,R.drawable.travel};
        List<Category> categories = new ArrayList<>();
        for (int i=0;i<name.length;i++){
            Category category = new Category();
            category.setName(name[i]);
            category.setIcon(getResources().getDrawable(drawables[i]));
            categories.add(category);
        }

        builder= new AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.interest_popup,null);
        RecyclerView recyclerView= view1.findViewById(R.id.interest_popup_rv);
        Button done=view1.findViewById(R.id.done);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        InterestAdapter adapter= new InterestAdapter(this,categories,0);
        recyclerView.setAdapter(adapter);
        builder.setView(view1);
        dialog=builder.create();
        dialog.show();


        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {




                if (User.getInstance().getInterest()!=null){
                interest.setText(User.getInstance().getInterest().toString().trim());}
                dialog.dismiss();
            }
        });

    }

    private void set_about() {
        builder= new AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.about_popup,null);
        final EditText editabout=view1.findViewById(R.id.about_id);
        Button save_button=view1.findViewById(R.id.save_id);
        Button cancel_button=view1.findViewById(R.id.cancel_id);
        builder.setView(view1);
        dialog=builder.create();
        dialog.show();

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about.setText(editabout.getText().toString().trim());
                dialog.dismiss();
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void set_gender() {
        builder= new AlertDialog.Builder(this);
        final View view1 = getLayoutInflater().inflate(R.layout.gender_popup,null);
        RelativeLayout gender_layout=view1.findViewById(R.id.gender_layout);
        final RadioGroup gender_group = view1.findViewById(R.id.gender_group);


        builder.setView(view1);
        dialog=builder.create();
        dialog.show();

        gender_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.male_id:
                        gender.setText("Male");
                        dialog.dismiss();
                        break;
                    case R.id.female_id:
                        gender.setText("Female");
                        dialog.dismiss();
                        break;
                    case R.id.others_id:
                        gender.setText("Others");
                        dialog.dismiss();

                }
            }
        });




    }

    public void createUserEmailAccount(String email, String password, String username, final String nickname, final String gender, final ArrayList<String> interest, final String about){
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {



            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        currentUser=firebaseAuth.getCurrentUser();

                        userid=currentUser.getUid();
                        create_profile(userid,nickname, gender, interest, about);


                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                }
            });
        }
        else {

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }


}