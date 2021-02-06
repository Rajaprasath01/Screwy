package com.rajaprasath.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.fragment.ProfileFragment;
import com.rajaprasath.chatapp.fragment.UsersFragment;
import com.rajaprasath.chatapp.ui.stranger.CategoryActivity;
import com.rajaprasath.chatapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Cipher;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference=db.collection("Users");
    private final FirebaseDatabase database= FirebaseDatabase.getInstance();
    private StorageReference storageReference;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private RelativeLayout mode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar= findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        mode=findViewById(R.id.mode_layout);
        UsersFragment usersFragment = new UsersFragment();
        ProfileFragment profileFragment = new ProfileFragment();

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        tabLayout=findViewById(R.id.tab_layout);
        viewPager=findViewById(R.id.view_pager);


        ViewpagerAdapter viewpagerAdapter= new ViewpagerAdapter(getSupportFragmentManager());

        tabLayout.setElevation(0);
        viewpagerAdapter.addFragments(usersFragment,getString(R.string.chats_fragment));
        viewpagerAdapter.addFragments(profileFragment,getString(R.string.profile_fragment));
        viewPager.setAdapter(viewpagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CategoryActivity.class));

            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sign_out:
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();

                    User user= User.getInstance();
                    user=null;

                    startActivity(new Intent(MainActivity.this, splashScreen.class));
                    finish();
                }
                else {

                }
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    public class ViewpagerAdapter extends FragmentPagerAdapter{
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
        updatelastseen();
    }

    private void updatelastseen() {

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put(Util.lastseen, Timestamp.now());
        collectionReference.document(User.getInstance().getUserid()).set(hashMap,SetOptions.merge());
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        updatelastseen();
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





