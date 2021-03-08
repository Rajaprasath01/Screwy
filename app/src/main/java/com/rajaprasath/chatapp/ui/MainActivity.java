package com.rajaprasath.chatapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.fragment.ProfileFragment;
import com.rajaprasath.chatapp.fragment.UsersFragment;
import com.rajaprasath.chatapp.ui.stranger.CategoryActivity;
import java.util.ArrayList;
import java.util.HashMap;

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
    private AlertDialog dialog;
    private AlertDialog.Builder builder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences preferences= getSharedPreferences("ENTRY",MODE_PRIVATE);
        int nthTime=preferences.getInt("entry_time",0);
        if (nthTime!=0){


            TapTargetView.showFor(this, TapTarget.forView(findViewById(R.id.mode_layout),"Go Incognito","Screw some random assholes")
            .targetCircleColor(R.color.transparent)
            .textColor(R.color.colorAccent)
            .transparentTarget(true)
            .targetRadius((int) getResources().getDimension(R.dimen._10sdp))
            .titleTextSize((int) getResources().getDimension(R.dimen._7sdp))
            ,new TapTargetView.Listener(){
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            onclickMode();
                        }
                    });


            entry(1);
        }

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

                onclickMode();


            }
        });



    }

    private void onclickMode() {
        String activity=getIntent().getStringExtra("activity");
        if (activity!=null) {
            if (activity.equals("categoryactivity")) {
                finish();
            }
        }
        else {
            Intent intent=new Intent(MainActivity.this, CategoryActivity.class);
            intent.putExtra("activity","mainactivity");
            startActivity(intent);
        }

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
                    signout();


                }
                else {

                }
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void signout() {
        builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.signout_popup,null);
        Button yes = view.findViewById(R.id.yes);
        Button no = view.findViewById(R.id.no);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser()!=null) {
                    firebaseAuth.signOut();
                    User user = User.getInstance();
                    user = null;

                }
                if (firebaseAuth.getCurrentUser()==null) {
                    firebaseAuth=null;
                    startActivity(new Intent(MainActivity.this, Login_Activity.class));
                    finish();
                }

                dialog.dismiss();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.signout_popup_background);
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
        if (User.getInstance().getUserid()!=null){

             }
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

    public void entry(int i){
        SharedPreferences.Editor editor= getSharedPreferences("ENTRY",MODE_PRIVATE).edit();
        editor.putInt("entry_time",i);
        editor.apply();

    }
}





