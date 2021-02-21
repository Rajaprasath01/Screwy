package com.rajaprasath.chatapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rajaprasath.chatapp.Adapter.UserAdapter;
import com.rajaprasath.chatapp.Notifications.Token;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.controller.UserInterface;
import com.rajaprasath.chatapp.ui.MainActivity;
import com.rajaprasath.chatapp.ui.stranger.CategoryActivity;
import com.rajaprasath.chatapp.ui.stranger.CategoryUsers;
import com.rajaprasath.chatapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public class UsersFragment extends Fragment implements UserInterface {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference= db.collection("Users");
    public List<User> users = new ArrayList<>();
    private List<String> ids=new ArrayList<>();
    private ListenerRegistration registration;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView=view.findViewById(R.id.user_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getusers();


        return view;
    }

    private void getusers() {

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

      Query query= collectionReference.document(firebaseUser.getUid()).collection("message").orderBy("messagetime", Query.Direction.DESCENDING);
               registration= query.addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
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


                }

                    displayusers(ids);




            }
        });


       updatetoken(FirebaseInstanceId.getInstance().getToken());





    }

/*
    private void displayusers() {

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value!=null){
                    users.clear();

                    QueryDocumentSnapshot snapshot;

                    for (int i=0;i<ids.size();i++){

                        for (int j=0;j<value.size();j++){
                            snapshot= (QueryDocumentSnapshot) value.getDocuments().get(j);
                            if (snapshot!=null) {
                                if (snapshot.getString(Util.userid).equals(ids.get(i))) {
                                    User user = new User();
                                    user.setUserid(snapshot.getString(Util.userid));
                                    user.setUsername(snapshot.getString(Util.username));
                                    user.setImageurl(snapshot.getString(Util.imageurl));

                                    users.add(user);
                                }
                            }
                        }
                    }


                    userAdapter = new UserAdapter(getContext(),users, 0, UsersFragment.this);

                    recyclerView.setAdapter(userAdapter);

                }
            }
        });
    }


 */


    private void displayusers(final List<String> ids) {


        if (!ids.isEmpty()) {
            users.clear();
            int i = 0;
            for (i = 0; i < ids.size(); i++) {

                final int finalI = i;
                collectionReference.document(ids.get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        User user = new User();
                        user.setUserid(snapshot.getString(Util.userid));
                        user.setUsername(snapshot.getString(Util.username));
                        user.setImageurl(snapshot.getString(Util.imageurl));
                        user.setGender(snapshot.getString(Util.gender));

                        users.add(user);

                        if (finalI == ids.size() - 1) {
                            userAdapter = new UserAdapter(getContext(), users, 0, UsersFragment.this);

                            recyclerView.setAdapter(userAdapter);
                        }
                    }
                });
            }

        }
        if (ids.isEmpty()){

            users.clear();

            userAdapter = new UserAdapter(getContext(), users, 0, UsersFragment.this);

            recyclerView.setAdapter(userAdapter);
        }

    }




    private void updatetoken(String refreshtoken) {

        Token token= new Token(refreshtoken);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        HashMap<String,Object> obj=new HashMap<>();
        obj.put(user.getUid(),token);
        tokens.updateChildren(obj);

    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onPause() {
        super.onPause();



    }

    @Override
    public void onStop() {
        super.onStop();

    }



    @Override
    public void requestchat(String userid, String username) {

    }
}