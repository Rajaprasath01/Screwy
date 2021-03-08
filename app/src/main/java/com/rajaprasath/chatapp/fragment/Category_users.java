package com.rajaprasath.chatapp.fragment;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rajaprasath.chatapp.Adapter.UserAdapter;
import com.rajaprasath.chatapp.Notifications.Client;
import com.rajaprasath.chatapp.Notifications.Data;
import com.rajaprasath.chatapp.Notifications.MyResponse;
import com.rajaprasath.chatapp.Notifications.Sender;
import com.rajaprasath.chatapp.Notifications.Token;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.controller.UserInterface;
import com.rajaprasath.chatapp.ui.stranger.CategoryUsers;
import com.rajaprasath.chatapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Category_users extends Fragment implements UserInterface {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference= db.collection("Users");
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private final APIService apiService = Client.getclient("https://fcm.googleapis.com/").create(APIService.class);
    private Integer Category_users_intent=4;
    private String category;
    private Integer friend_request=3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_category_users, container, false);
        recyclerView=view.findViewById(R.id.category_user_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        users=new ArrayList<>();

        String fuser=FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.getInstance().setUserid(fuser);

        if (getArguments() != null) {
            category=getArguments().getString("category");

        }
        return view;
    }

    private void getusers() {
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();



        collectionReference.orderBy(Util.lastseen, Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshots) {
                if (querySnapshots!=null){
                    users.clear();
                    for (QueryDocumentSnapshot snapshot: querySnapshots){
                        if (snapshot!=null){
                            if (!firebaseUser.getUid().equals(snapshot.get(Util.userid))) {
                                User user = new User();
                                user.setUserid(snapshot.getString(Util.userid));
                                user.setUsername(snapshot.getString(Util.username));
                                user.setGender(snapshot.getString(Util.gender));
                                user.setImageurl(snapshot.getString(Util.imageurl));
                                user.setNickname(snapshot.getString(Util.nickname));
                                user.setInterest((ArrayList<String>) snapshot.get(Util.interest));

                                if (user.getInterest() != null) {
                                    if (category!=null) {
                                        if (user.getInterest().toString().contains(category)) {
                                            users.add(user);
                                        }
                                    }
                                }

                            }
                        }
                    }




                    userAdapter = new UserAdapter(getContext(), users, 1,Category_users.this);
                    recyclerView.setAdapter(userAdapter);
                }
            }
        });



    }



    @Override
    public void onStart() {
        super.onStart();
        getusers();



    }


    @Override
    public void requestchat(final String userid, final String username) {
        builder=new AlertDialog.Builder(getContext());
        View view= getLayoutInflater().inflate(R.layout.request_popup,null);
        TextView request=view.findViewById(R.id.request);
        final String request_text= getResources().getString(R.string.request_text)+" "+username+"?";
        request.setText(request_text);
        Button yes=view.findViewById(R.id.yes);
        Button no=view.findViewById(R.id.no);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,Object> hashMap= new HashMap<>();

                hashMap.put("requesttime", Timestamp.now().toDate());
             collectionReference.document(userid).collection("requests").document(User.getInstance().getUserid()).set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                 @Override
                 public void onComplete(@NonNull Task<Void> task) {
                     Toast.makeText(getContext(), "Request Sent", Toast.LENGTH_SHORT).show();
                     String notif_text=User.getInstance().getNickname()+" wants to chat with you";
                     String category=User.getInstance().getInterest().get(0);
                     sendNotification(userid,User.getInstance().getNickname(),notif_text,friend_request,category);

                 }
             });

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
        dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_background);
    }


    private void sendNotification(final String receiver, final String nickname, final String msg, final Integer Incognito, final String category) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        com.google.firebase.database.Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {


                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    final Data data = new Data(User.getInstance().getUserid(), R.mipmap.ic_launcher, nickname, msg,User.getInstance().getUserid(),Incognito,category);
                    Sender sender = null;
                    if (token != null) {
                        sender = new Sender(data, token.getToken());
                    }


                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.code() == 200) {

                                        if (response.body().success != 1) {

                                        }
                                        else {

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }





}