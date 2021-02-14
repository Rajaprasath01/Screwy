package com.rajaprasath.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.rajaprasath.chatapp.Adapter.MessageAdapter;
import com.rajaprasath.chatapp.Notifications.Client;
import com.rajaprasath.chatapp.Notifications.Data;
import com.rajaprasath.chatapp.Notifications.MyResponse;
import com.rajaprasath.chatapp.Notifications.Sender;
import com.rajaprasath.chatapp.Notifications.Token;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.fragment.APIService;
import com.rajaprasath.chatapp.model.Chat;
import com.rajaprasath.chatapp.model.UserStatus;
import com.rajaprasath.chatapp.ui.stranger.CategoryActivity;
import com.rajaprasath.chatapp.ui.stranger.CategoryUsers;
import com.rajaprasath.chatapp.util.Util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoom extends AppCompatActivity {
private CircleImageView profilepic;
private TextView username,status;
private EditText msg_text;
private ImageView send,remove_contact;
private RelativeLayout  back;
private String sender, receiver,msg;
private RecyclerView recyclerView;
private MessageAdapter messageAdapter;
private final FirebaseFirestore db= FirebaseFirestore.getInstance();
private final CollectionReference collectionReference=db.collection("Users");
private final FirebaseDatabase database = FirebaseDatabase.getInstance();
private FirebaseUser firebaseUser;
private final byte[] encryptionkey={9,-18,27,-36,45,-54,63,-72,81,-90,8,-16,24,-32,40,-48};
private Cipher cipher, decipher;
private SecretKeySpec secretKeySpec;
    private List<Chat> chats;
    private ImageView back_button;
    private String userid;
    private String typing="typing";
    private ValueEventListener seenListener;
    private DatabaseReference reference;
    private Integer Normal=0;
    private boolean notify=false;
    private APIService apiService;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getSupportActionBar().hide();

        String activity=getIntent().getStringExtra("activity");
        if (activity!=null){
            if (activity.trim().equals("notif")){
                setuserinstance();
            }
        }

        apiService = Client.getclient("https://fcm.googleapis.com/").create(APIService.class);
        send=findViewById(R.id.send);
        recyclerView=findViewById(R.id.chat_recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(ChatRoom.this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (User.getInstance().getUserid()!=null){
            sender=User.getInstance().getUserid();
        }
       if (getIntent().getStringExtra("userid")!=null){
           receiver=getIntent().getStringExtra("userid");
           userid=getIntent().getStringExtra("userid");
       }

       msg_text=findViewById(R.id.msg_text);
        profilepic=findViewById(R.id.profile_image_id);
        username=findViewById(R.id.user_id);
        status=findViewById(R.id.status);
        back=findViewById(R.id.back_layout);
        back_button=findViewById(R.id.back);

        remove_contact=findViewById(R.id.remove_trusted);

        remove_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                remove_trusted();
            }
        });


        try {
            cipher=Cipher.getInstance("AES");
            decipher=Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec=new SecretKeySpec(encryptionkey,"AES");

        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        return true;
                    case MotionEvent.ACTION_UP:

                        onBackPressed();
                        finish();
                        return true;
                }
                return false;
            }
        });

   msg_text.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (msg_text.getText().toString().length()==0){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("typing",null);

            DatabaseReference reference= database.getReference("Users").child(User.getInstance().getUserid());

            reference.updateChildren(hashMap);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (msg_text.getText().toString().length()==0){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("typing",null);

            DatabaseReference reference= database.getReference("Users").child(User.getInstance().getUserid());

            reference.updateChildren(hashMap);
        }
        else {
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("typing",typing);
            hashMap.put("receiver",receiver);
            DatabaseReference reference= database.getReference("Users").child(User.getInstance().getUserid());

            reference.updateChildren(hashMap);
        }
        
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (msg_text.getText().toString().length()==0){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("typing",null);
            DatabaseReference reference= database.getReference("Users").child(User.getInstance().getUserid());

            reference.updateChildren(hashMap);
        }
    }
});


   checkstatus_readmsg();



        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                msg= String.valueOf(msg_text.getText());
                msg=msg.trim();
                if (!msg.equals("")) {

                    send_message(sender, receiver, msg);
                }
                else {

                }
                msg_text.setText("");
            }
        });

        if (sender!=null && receiver!=null) {
            seenmsg(sender, receiver);
        }
    }

    private void checkstatus_readmsg() {
        if (userid!=null) {

            collectionReference.document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isComplete() && task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        User user = new User();
                        user.setUsername(snapshot.getString("username"));
                        user.setUserid(snapshot.getString("userid"));
                        user.setImageurl(snapshot.getString("imageurl"));

                        username.setText(user.getUsername());
                        if (user.getImageurl().equals("default")) {
                            profilepic.setImageResource(R.mipmap.chatroom_person_icon_round);

                        } else {
                            Glide.with(ChatRoom.this).load(user.getImageurl()).into(profilepic);
                        }

                        checkstatus(receiver);


                        read_message(sender, receiver, user.getImageurl());
                    }


                }
            });
        }
    }

    private void setuserinstance() {

       String user= FirebaseAuth.getInstance().getCurrentUser().getUid();
       if (user!=null) {
           collectionReference.document(user).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                   if (task.isComplete() && task.isSuccessful()) {
                       DocumentSnapshot snapshot = task.getResult();
                       User user = User.getInstance();
                       user.setUserid(snapshot.getString("userid"));
                       user.setUsername(snapshot.getString("username"));
                       user.setImageurl(snapshot.getString("imageurl"));
                       user.setNickname(snapshot.getString("nickname"));
                       user.setGender(snapshot.getString("gender"));
                       user.setInterest((ArrayList<String>) snapshot.get("interest"));
                       user.setAbout(snapshot.getString("about"));

                       if (User.getInstance().getUserid() != null) {
                           sender = User.getInstance().getUserid();
                       }
                       if (getIntent().getStringExtra("userid") != null) {
                           receiver = getIntent().getStringExtra("userid");
                           userid = getIntent().getStringExtra("userid");
                       }


                       seenmsg(sender,receiver);
                       checkstatus_readmsg();
                       onResume();

                   } else {

                   }
               }
           });
       }
    }

    private void remove_trusted() {
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.remove_trusted_popup, null);
        TextView confirmation=view.findViewById(R.id.confirmation);
        Button yes = view.findViewById(R.id.yes);
        Button no = view.findViewById(R.id.no);
        String text="Do you really want to block this person?";
        confirmation.setText(text);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(Util.trusted, false);


                collectionReference.document(User.getInstance().getUserid()).collection(Util.message).document(userid).set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        collectionReference.document(userid).collection(Util.message).document(User.getInstance().getUserid()).set(hashMap);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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
        dialog = builder.create();
        dialog.show();

    }

    private void seenmsg(final String sender, final String receiver) {
        reference= database.getReference("chats");
       seenListener= reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot datasnapshot : snapshot.getChildren() ){
                    Chat chat = datasnapshot.getValue(Chat.class);
                    chat.setMessage(AESDecryptionMethod(chat.getMessage()));

                    if (chat.getSender()!=null && sender !=null && chat.getReceiver()!=null && receiver !=null) {

                        if ((chat.getSender().equals(receiver)&& chat.getReceiver().equals(sender))) {

                            HashMap<String,Object> hashMap= new HashMap<>();
                            hashMap.put("isseen",true);
                            datasnapshot.getRef().updateChildren(hashMap);


                        }


                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void read_message(final String sender, final String receiver, final String imageurl) {

         chats= new ArrayList<>();

        DatabaseReference reference= database.getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot datasnapshot : snapshot.getChildren() ){
                    Chat chat = datasnapshot.getValue(Chat.class);
                    if (chat.getMessage() != null) {
                        chat.setMessage(AESDecryptionMethod(chat.getMessage()));
                    }

                    if (chat.getSender()!=null && sender!=null && chat.getReceiver()!=null && receiver!=null) {

                    if ((chat.getSender().equals(sender)&& chat.getReceiver().equals(receiver))  ||
                            (chat.getSender().equals(receiver) && chat.getReceiver().equals(sender))) {

                        chat.setMessageid(datasnapshot.getKey());
                        chats.add(chat);
                        
                        
                        
                    }


                    }
                }


                messageAdapter=new MessageAdapter(ChatRoom.this,chats,imageurl,0);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void send_message(String sender, final String receiver, final String msg) {


        DatabaseReference reference = database.getReference("chats");
        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",AESEncryptionMethod(msg));
        hashMap.put("isseen", false);

        reference.push().setValue(hashMap);
        HashMap<String,Object> time = new HashMap<>();
        time.put(receiver,Timestamp.now().toDate());
        HashMap<String,Object> obj=new HashMap<>();
        obj.put("messagetime",Timestamp.now().toDate());
        collectionReference.document(receiver).collection("message").document(sender).set(obj,SetOptions.merge());
        collectionReference.document(sender).collection("message").document(receiver).set(obj,SetOptions.merge());
        collectionReference.document(User.getInstance().getUserid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                User user = new User();
                user.setUserid(snapshot.getString(Util.userid));
                user.setUsername(snapshot.getString(Util.username));


                if (notify) {

                    sendNotification(receiver, user.getUsername(), msg, Normal);
                }

                notify = false;
            }
        });
    }

    private void sendNotification(String receiver, final String username, final String msg, final Integer Normal) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {


                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    final Data data = new Data(User.getInstance().getUserid(), R.mipmap.ic_launcher, username, msg,User.getInstance().getUserid(),Normal);
                    Sender sender = new Sender(data, token.getToken());


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



    private String AESEncryptionMethod(String msg) {

        byte[] stringbyte = msg.getBytes();
        byte[] encryptedbyte= new byte[stringbyte.length];
        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedbyte=cipher.doFinal(stringbyte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        String encryptedmessage= new String(encryptedbyte, StandardCharsets.ISO_8859_1);
        return encryptedmessage;
    }

    private String AESDecryptionMethod(String message) {

        byte[] stringbyte= message.getBytes(StandardCharsets.ISO_8859_1);
        byte[] decryptedbyte= new byte[stringbyte.length];
        try {
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryptedbyte=decipher.doFinal(stringbyte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        String decryptedmessage=new String(decryptedbyte);
        return decryptedmessage;
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        if (userid!=null) {
            currentuser(userid);
        }
        updatelastseen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");

        currentuser("none");
        updatelastseen();
    }


    private void checkstatus(String receiver) {

        DatabaseReference reference= database.getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    UserStatus userStatus= dataSnapshot.getValue(UserStatus.class);


                    if (dataSnapshot.getKey().equals(userid)) {


                        if (userStatus.getStatus().equals("online")) {
                            if (userStatus.getTyping() != null) {
                                if (userStatus.getReceiver().equals(sender)) {
                                    if (userStatus.getTyping().equals("typing")) {
                                        status.setText("typing...");
                                    }
                                }
                            }else {
                                    status.setText("online");
                                }

                        } else  {
                            status.setText("offline");
                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentuser(String userid){

        if (userid!=null) {
            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("currentuser", userid);
            editor.apply();

        }
    }

    private void updatelastseen() {

        if (User.getInstance().getUserid()!=null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Util.lastseen, Timestamp.now());
            collectionReference.document(User.getInstance().getUserid()).set(hashMap, SetOptions.merge());
        }
    }
    private void status(final String status) {

        if (User.getInstance().getUserid()!=null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            DatabaseReference reference = database.getReference("Users").child(User.getInstance().getUserid());

            reference.updateChildren(hashMap);

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String activity=getIntent().getStringExtra("activity");
        if (activity!=null){
            if (activity.trim().equals("notif")){
                startActivity(new Intent(this,MainActivity.class));
                finish();

            }
        }

    }
}