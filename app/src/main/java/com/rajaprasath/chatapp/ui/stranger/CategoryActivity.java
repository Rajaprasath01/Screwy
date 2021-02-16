package com.rajaprasath.chatapp.ui.stranger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.rajaprasath.chatapp.Adapter.CategoryAdapter;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.model.Category;
import com.rajaprasath.chatapp.ui.MainActivity;
import com.rajaprasath.chatapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");
    private FirebaseDatabase database=FirebaseDatabase.getInstance();
    private RelativeLayout mode;
    private String activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);


        categories = new ArrayList<>();
        getCategories();
        recyclerView=findViewById(R.id.category_recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        categoryAdapter= new CategoryAdapter(getApplicationContext(),categories);
        recyclerView.setAdapter(categoryAdapter);
         activity=getIntent().getStringExtra("activity");
        mode= findViewById(R.id.mode_layout);
        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (activity != null) {
                    if (activity.equals("mainactivity")) {
                        finish();
                    } else if (activity.equals("splashscreen")) {
                        Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
                        intent.putExtra("activity", "categoryactivity");
                        startActivity(intent);
                    }

                }
            }
        });

    }

    private void getCategories() {
        String[] name = new String[]{"MEME","SPORTS","CODING","STARTUP","MUSIC","DANCE","MOVIES","GAMING","BOOKS","TRAVEL"};
        int[] drawables = new int[]{R.drawable.meme,R.drawable.sports,R.drawable.coding,R.drawable.startup,
                R.drawable.music,R.drawable.dance,R.drawable.movies,R.drawable.gaming,R.drawable.books,R.drawable.travel};
        for (int i=0;i<name.length;i++){
            Category category = new Category();
            category.setName(name[i]);
            category.setIcon(getResources().getDrawable(drawables[i]));
            categories.add(category);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        status("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");

    }


    private void status(final String status) {

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        if (User.getInstance().getUserid()!=null) {
            DatabaseReference reference = database.getReference("Users").child(User.getInstance().getUserid());
            reference.updateChildren(hashMap);

        }

    }



}