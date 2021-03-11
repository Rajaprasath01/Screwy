package com.rajaprasath.chatapp.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.util.Assert;
import com.rajaprasath.chatapp.Adapter.requestAdapter;
import com.rajaprasath.chatapp.Notifications.MyFirebaseMessaging;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.util.Util;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FriendRequests extends Fragment {

    private RecyclerView chat_recyclerView, trust_recyclerview;
    private com.rajaprasath.chatapp.Adapter.requestAdapter chat_requestAdapter,trust_requestAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference= db.collection("Users");
    public List<User> chat_users = new ArrayList<>();
    public List<User> trusted_users= new ArrayList<>();
    private final List<String> chat_ids=new ArrayList<>();
    private final List<String> trust_ids=new ArrayList<>();
    private final int permission=130;
    private final  int trusted=1;
    private RelativeLayout chat_request_layout,trust_request_layout;
    private TextView chat_count,trust_count;
    private ConstraintLayout chat_request_root;
    private RelativeLayout trust_request_root;

    private int j=0;

    public FriendRequests() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_friend_requests, container, false);
        chat_recyclerView=view.findViewById(R.id.chat_request_recyclerview);
        chat_recyclerView.setHasFixedSize(true);
        chat_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        trust_recyclerview=view.findViewById(R.id.trust_request_recyclerview);
        trust_recyclerview.setHasFixedSize(true);
        trust_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        String fuser=FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.getInstance().setUserid(fuser);
        getchatrequests();
        gettrustrequest();
        chat_request_layout=view.findViewById(R.id.chat_request_layout);
        trust_request_layout=view.findViewById(R.id.trust_request_layout);
        chat_count=view.findViewById(R.id.chat_count);
        trust_count=view.findViewById(R.id.trust_count);
        chat_request_root=view.findViewById(R.id.chat_request_root);
        trust_request_root=view.findViewById(R.id.trust_request_root);
        chat_request_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(chat_request_root,new AutoTransition());
                TransitionManager.beginDelayedTransition(trust_request_root,new AutoTransition());
                if (chat_recyclerView.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(chat_request_root,new AutoTransition());
                    chat_recyclerView.setVisibility(View.GONE);
                }
                else {
                    TransitionManager.beginDelayedTransition(chat_request_root,new AutoTransition());
                    TransitionManager.beginDelayedTransition(trust_request_root,new AutoTransition());
                    chat_recyclerView.setVisibility(View.VISIBLE);
                    trust_recyclerview.setVisibility(View.GONE);
                }
            }
        });
        trust_request_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(chat_request_root,new AutoTransition());
                TransitionManager.beginDelayedTransition(trust_request_root,new AutoTransition());
                if (trust_recyclerview.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(trust_request_root,new AutoTransition());
                    trust_recyclerview.setVisibility(View.GONE);
                }
                else {
                    TransitionManager.beginDelayedTransition(chat_request_root,new AutoTransition());
                    TransitionManager.beginDelayedTransition(trust_request_root,new AutoTransition());
                    trust_recyclerview.setVisibility(View.VISIBLE);
                    chat_recyclerView.setVisibility(View.GONE);
                }
            }
        });
        return  view;

    }

    private void getchatrequests() {
        j=0;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


        collectionReference.document(firebaseUser.getUid()).collection("requests").orderBy("requesttime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {


                    if (value != null) {
                        chat_ids.clear();

                        for (QueryDocumentSnapshot documentSnapshot : value) {

                            if (documentSnapshot != null) {
                                String id = documentSnapshot.getId();


                                chat_ids.add(id);



                            }

                        }

                            displayrequests(chat_ids);



                    }


            }


        });


    }

    private void gettrustrequest(){


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        collectionReference.document(firebaseUser.getUid()).collection("message").orderBy("requesttrust", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {


            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {



                    if (value != null) {
                        trust_ids.clear();
                        for (QueryDocumentSnapshot documentSnapshot : value) {

                            if (documentSnapshot != null) {
                                String id = documentSnapshot.getId();

                                trust_ids.add(id);
                            }

                        }

                            displaytrustrequests(trust_ids);


                    }



            }
        });


    }

    private void displaytrustrequests(final List<String> trust_ids) {

        int k=0;

        if (k%2==0) {
            int i = 0;
            trusted_users.clear();
            if (!trust_ids.isEmpty()) {
                for (i = 0; i < trust_ids.size(); i++) {
                    final int finalI = i;
                    collectionReference.document(trust_ids.get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {

                            User user = new User();
                            user.setUserid(snapshot.getString(Util.userid));
                            user.setUsername(snapshot.getString(Util.username));
                            user.setImageurl(snapshot.getString(Util.imageurl));
                            user.setNickname(snapshot.getString(Util.nickname));
                            user.setStatus(snapshot.getString(Util.status));
                            trusted_users.add(user);

                            if (finalI == trust_ids.size() - 1) {
                                trust_count.setText(" " + trusted_users.size());
                                trust_requestAdapter = new requestAdapter(getContext(), trusted_users, trusted);
                                trust_recyclerview.setAdapter(trust_requestAdapter);

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("trust_count", trusted_users.size());
                                collectionReference.document(User.getInstance().getUserid()).set(obj, SetOptions.merge());
                                //cancelNotification(trust_ids);

                            }
                        }
                    });
                }
            }

            k++;
        }

        if (trust_ids.isEmpty())
         {
            trusted_users.clear();
            trust_count.setText(" " + trusted_users.size());
            trust_requestAdapter = new requestAdapter(getContext(), trusted_users, trusted);
            trust_recyclerview.setAdapter(trust_requestAdapter);

            HashMap<String, Object> obj = new HashMap<>();
            obj.put("trust_count", trusted_users.size());
            collectionReference.document(User.getInstance().getUserid()).set(obj, SetOptions.merge());
        }


    }

    private void displayrequests(final List<String> chat_ids) {

        int k=0;

        if (k%2==0) {

            int i = 0;

            chat_users.clear();
            if (!chat_ids.isEmpty()) {
                for (i = 0; i < chat_ids.size(); i++) {
                    final int finalI = i;


                    collectionReference.document(chat_ids.get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {

                            User user = new User();
                            user.setUserid(snapshot.getString(Util.userid));
                            user.setUsername(snapshot.getString(Util.username));
                            user.setImageurl(snapshot.getString(Util.imageurl));
                            user.setNickname(snapshot.getString(Util.nickname));
                            user.setStatus(snapshot.getString(Util.status));

                            chat_users.add(user);


                            if (finalI == chat_ids.size() - 1) {
                                chat_count.setText(" " + chat_users.size());

                                chat_requestAdapter = new requestAdapter(getContext(), chat_users, permission);
                                chat_recyclerView.setAdapter(chat_requestAdapter);


                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("chat_count", chat_users.size());
                                collectionReference.document(User.getInstance().getUserid()).set(obj, SetOptions.merge());


                               // cancelNotification(chat_ids);
                            }
                        }
                    });


                }
            }

            k++;
        }

 if (chat_ids.isEmpty())
     {

            chat_users.clear();
            chat_count.setText(" " + chat_users.size());

            chat_requestAdapter = new requestAdapter(getContext(), chat_users, permission);
            chat_recyclerView.setAdapter(chat_requestAdapter);

            HashMap<String, Object> obj = new HashMap<>();
            obj.put("chat_count", chat_users.size());
            collectionReference.document(User.getInstance().getUserid()).set(obj, SetOptions.merge());


        }



        }

    private void cancelNotification(List<String> chat_ids) {
        if (chat_ids!=null && !chat_ids.isEmpty()){
            for (String id : chat_ids){
                int j=Integer.parseInt(id.replaceAll("[\\D]",""));
                if ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)!=null) {
                    NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(j);
                }
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();

    }
}