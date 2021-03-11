package com.rajaprasath.chatapp.ui.stranger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.rajaprasath.chatapp.Adapter.UserAdapter;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.fragment.Category_users;
import com.rajaprasath.chatapp.fragment.FriendRequests;
import com.rajaprasath.chatapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryUsers extends AppCompatActivity  {


    private List<User> users;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference= db.collection("Users");
    private FirebaseDatabase database=FirebaseDatabase.getInstance();
    private String category;
    private ImageButton back;
    private TextView category_name;


    private String request_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_users);

      String fuser=  FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.getInstance().setUserid(fuser);

        String activity=getIntent().getStringExtra("activity");
        if (activity!=null){
            if (activity.trim().equals("notif")){
                updatelastseen();

            }
        }


        Toolbar toolbar= findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        category=getIntent().getStringExtra("category_name");

        Bundle bundle=new Bundle();
        bundle.putString("category",category);
        bundle.putString("activity","notif");
        final Category_users category_users= new Category_users();
        category_users.setArguments(bundle);
        final FriendRequests friendRequests= new FriendRequests();

        friendRequests.setArguments(bundle);

      final TabLayout  tabLayout=findViewById(R.id.tab_layout);
      final ViewPager  viewPager=findViewById(R.id.view_pager);




        tabLayout.setElevation(0);

        ViewpagerAdapter viewpagerAdapter= new ViewpagerAdapter(getSupportFragmentManager());
        viewpagerAdapter.addFragments(category_users,getString(R.string.chats_fragment));
        viewpagerAdapter.addFragments(friendRequests,getString(R.string.request));

        viewPager.setAdapter(viewpagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

    collectionReference.document(firebaseUser.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

            if (snapshot!=null) {

                if (snapshot.getLong("trust_count") != null && snapshot.getLong("chat_count") != null) {
                    Long trust_count = snapshot.getLong("trust_count");
                    Long chat_count = snapshot.getLong("chat_count");
                    long total_count = trust_count + chat_count;
                    request_count = "  (" + total_count + ")";

                    tabLayout.getTabAt(1).setText("Friend Requests" + request_count);

                }
            }

        }


    });





        back=findViewById(R.id.back);
        category_name=findViewById(R.id.category_name);
        users = new ArrayList<>();

       category_name.setText(category);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });


    }





    public class ViewpagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewpagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void  addFragments(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
            

        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        cancelNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");

    }
    private void updatelastseen() {


        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put(Util.lastseen, Timestamp.now());
        collectionReference.document(User.getInstance().getUserid()).set(hashMap,SetOptions.merge());
    }
    private void status(final String status) {

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        DatabaseReference reference= database.getReference("Users").child(User.getInstance().getUserid());

        reference.updateChildren(hashMap);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String activity=getIntent().getStringExtra("activity");
        if (activity!=null){
            if (activity.trim().equals("notif")){
                startActivity(new Intent(this,CategoryActivity.class));
                finish();

            }
        }
        else {
            finish();
        }
    }


    private void cancelNotification() {
        if ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)!=null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}