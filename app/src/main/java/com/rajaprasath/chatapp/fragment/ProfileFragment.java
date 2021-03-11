package com.rajaprasath.chatapp.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rajaprasath.chatapp.Adapter.InterestAdapter;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.model.Category;
import com.rajaprasath.chatapp.util.Util;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final int GALLERY_CODE = 100;
    private TextView username;
    private CircleImageView display_pic;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser fuser;
    private CollectionReference collectionReference= db.collection("Users");
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private String imageurl;
    private RelativeLayout nickname_layout;
    private TextView nickname;
    private RelativeLayout gender_layout;
    private TextView gender;
    private RelativeLayout interest_layout;
    private TextView interest;

    private RelativeLayout about_layout;
    private TextView about;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_profile, container, false);

        username= view.findViewById(R.id.username_id);
        display_pic=view.findViewById(R.id.display_pic);
        nickname_layout=view.findViewById(R.id.nickname_layout);
        nickname=view.findViewById(R.id.nickname);
        gender_layout= view.findViewById(R.id.gender_layout);
        gender=view.findViewById(R.id.gender);
        interest_layout= view.findViewById(R.id.interest_layout);
        interest=view.findViewById(R.id.interest);
        about_layout=view.findViewById(R.id.about_layout);
        about=view.findViewById(R.id.about);
        nickname_layout.setOnClickListener(this);
        gender_layout.setOnClickListener(this);
        interest_layout.setOnClickListener(this);
        about_layout.setOnClickListener(this);
        User user=User.getInstance();
        nickname.setText(user.getNickname());
        gender.setText(user.getGender());
        if (User.getInstance().getInterest()!=null) {
            interest.setText(User.getInstance().getInterest().toString().trim());
        }
        about.setText(user.getAbout());
        firebaseAuth=FirebaseAuth.getInstance();
        fuser=firebaseAuth.getCurrentUser();


        username.setText(user.getUsername());
        if (user.getImageurl()!=null) {
            if (user.getImageurl().equals("default")) {
                display_pic.setImageResource(R.mipmap.ic_launcher);
            } else {
                Glide.with(getContext()).load(user.getImageurl()).into(display_pic);
            }
        }


        display_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_CODE);

            }


        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_username();
            }
        });

        return view;
    }

    private void set_username() {

        builder= new AlertDialog.Builder(getContext());
        View view1 = getLayoutInflater().inflate(R.layout.nickname_popup,null);
        final EditText editname=view1.findViewById(R.id.name_id);
        if (User.getInstance().getUsername()!=null){
            editname.setText(User.getInstance().getUsername().trim());
        }
        Button save_button=view1.findViewById(R.id.save_id);
        Button cancel_button=view1.findViewById(R.id.cancel_id);
        builder.setView(view1);
        dialog=builder.create();
        dialog.show();

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setText(editname.getText().toString().trim());
                dialog.dismiss();
                Map<String,Object> userobj = new HashMap<>();

                userobj.put(Util.username,editname.getText().toString().trim());
                collectionReference.document(fuser.getUid()).update(userobj);
                User.getInstance().setUsername(editname.getText().toString().trim());
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    private void set_about() {
        builder= new AlertDialog.Builder(getContext());
        View view1 = getLayoutInflater().inflate(R.layout.about_popup,null);
        final EditText editabout=view1.findViewById(R.id.about_id);
        if (User.getInstance().getAbout()!=null){
            editabout.setText(User.getInstance().getAbout().trim());
        }
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
                Map<String,Object> userobj = new HashMap<>();

                userobj.put(Util.about,about.getText().toString().trim());
                collectionReference.document(fuser.getUid()).update(userobj).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        User.getInstance().setAbout(editabout.getText().toString().trim());

                    }
                });
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
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
        builder= new AlertDialog.Builder(getContext());
        View view1 = getLayoutInflater().inflate(R.layout.interest_popup,null);
        Button done = view1.findViewById(R.id.done);
        RecyclerView recyclerView= view1.findViewById(R.id.interest_popup_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        InterestAdapter adapter= new InterestAdapter(getContext(),categories,1);
        recyclerView.setAdapter(adapter);
        builder.setView(view1);
        dialog=builder.create();
        dialog.show();
        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {

            }
        });



        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (User.getInstance().getInterest()!=null && !User.getInstance().getInterest().isEmpty()) {
                    if (User.getInstance().getInterest().toString()!=null && !User.getInstance().getInterest().toString().isEmpty()) {
                        interest.setText(User.getInstance().getInterest().toString().trim());
                    }
                }
                dialog.dismiss();
                Map<String,Object> userobj = new HashMap<>();

                userobj.put(Util.interest,User.getInstance().getInterest());
                collectionReference.document(fuser.getUid()).update(userobj);
            }
        });

    }

    private void set_gender() {
        builder= new AlertDialog.Builder(getContext());
        final View view1 = getLayoutInflater().inflate(R.layout.gender_popup,null);
        RelativeLayout gender_layout=view1.findViewById(R.id.gender_layout);
        final RadioGroup gender_group = view1.findViewById(R.id.gender_group);
               RadioButton male=view1.findViewById(R.id.male_id);
               RadioButton female=view1.findViewById(R.id.female_id);
               RadioButton others=view1.findViewById(R.id.others_id);

               String gender_option=User.getInstance().getGender();
               if (gender_option!=null){
                   gender_option=gender_option.trim();
                   if (gender_option.equals(Util.male)){
                       male.toggle();
                       male.setChecked(true);
                   }
                   else if (gender_option.equals(Util.female)){
                       female.toggle();
                       female.setChecked(true);
                   }
                   else if (gender_option.equals(Util.others)){
                       others.toggle();
                       others.setChecked(true);
                   }
               }

        builder.setView(view1);
        dialog=builder.create();
        dialog.show();



        gender_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.male_id:
                        gender.setText(Util.male);
                        dialog.dismiss();
                        User.getInstance().setGender(Util.male);
                        break;
                    case R.id.female_id:
                        gender.setText(Util.female);
                        dialog.dismiss();
                        User.getInstance().setGender(Util.female);
                        break;
                    case R.id.others_id:
                        gender.setText(Util.others);
                        dialog.dismiss();
                        User.getInstance().setGender(Util.others);
                }
                Map<String,Object> userobj = new HashMap<>();

                userobj.put(Util.gender,gender.getText().toString().trim());
                collectionReference.document(fuser.getUid()).update(userobj);
            }
        });




    }

    private void create_name_setter() {
        builder= new AlertDialog.Builder(getContext());
        View view1 = getLayoutInflater().inflate(R.layout.nickname_popup,null);
        final EditText editname=view1.findViewById(R.id.name_id);
        if (User.getInstance().getNickname()!=null){
            editname.setText(User.getInstance().getNickname().trim());
        }
        Button save_button=view1.findViewById(R.id.save_id);
        Button cancel_button=view1.findViewById(R.id.cancel_id);
        builder.setView(view1);
        dialog=builder.create();
        dialog.show();

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Map<String,Object> userobj = new HashMap<>();

                userobj.put(Util.nickname,editname.getText().toString().trim());
                collectionReference.document(fuser.getUid()).update(userobj).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        nickname.setText(editname.getText().toString().trim());
                        User.getInstance().setNickname(editname.getText().toString().trim());
                    }
                });

            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){

            if (requestCode==GALLERY_CODE) {
                imageUri = data.getData();
                getcroppedimage(imageUri);

            }
            if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

                CropImage.ActivityResult result= CropImage.getActivityResult(data);
                if (resultCode==RESULT_OK){

                    Uri resulturi =result.getUri();
                    upload_dp(resulturi);

                    display_pic.setImageURI(resulturi);
                }
                else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Exception error = result.getError();
                }


            }

        }
    }

    private void getcroppedimage(Uri imageUri) {
        CropImage.activity(imageUri).start(getContext(),this);

    }

    private void upload_dp(Uri uri) {
        final StorageReference filepath= storageReference.child("uploads").child("myImage"+ Timestamp.now().getSeconds());
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                         imageurl = uri.toString();

                        User user= User.getInstance();
                        user.setImageurl(imageurl);
                        Map<String,Object> userobj = new HashMap<>();
                        userobj.put(Util.imageurl,user.getImageurl());
                        collectionReference.document(fuser.getUid()).update(userobj);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nickname_layout:
                create_name_setter();
                break;
            case R.id.gender_layout:
                set_gender();
                break;
            case R.id.interest_layout:
                set_interest();
                break;
            case R.id.about_layout:
                set_about();
                break;
        }
    }
}