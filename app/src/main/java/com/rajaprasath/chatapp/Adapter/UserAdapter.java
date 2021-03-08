package com.rajaprasath.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rajaprasath.chatapp.Notifications.Client;
import com.rajaprasath.chatapp.Notifications.Data;
import com.rajaprasath.chatapp.Notifications.MyResponse;
import com.rajaprasath.chatapp.Notifications.Sender;
import com.rajaprasath.chatapp.Notifications.Token;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.controller.UserInterface;
import com.rajaprasath.chatapp.fragment.APIService;
import com.rajaprasath.chatapp.fragment.UsersFragment;
import com.rajaprasath.chatapp.model.Chat;
import com.rajaprasath.chatapp.model.UserStatus;
import com.rajaprasath.chatapp.ui.ChatRoom;
import com.rajaprasath.chatapp.ui.stranger.IncogChatRoom;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>  {

    Context context;
    List<User> users;
    Integer mode;
    private final FirebaseFirestore db= FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference=db.collection("Users");
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final byte[] encryptionkey={9,-18,27,-36,45,-54,63,-72,81,-90,8,-16,24,-32,40,-48};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;
    private UserInterface userInterface;



    public UserAdapter(Context context, List<User> users, Integer mode, UserInterface userInterface) {
        this.context = context;
        this.users = users;
        this.mode=mode;
        this.userInterface=userInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (mode==0)
             view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        else if (mode==1)
            view = LayoutInflater.from(context).inflate(R.layout.incog_user_item, parent, false);

        try {
            cipher=Cipher.getInstance("AES");
            decipher=Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec=new SecretKeySpec(encryptionkey,"AES");

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
         final User user = users.get(position);
         holder.msg_count.setVisibility(View.GONE);

         if (mode==0){
             holder.username.setText(user.getUsername());
             setlastmsg(User.getInstance().getUserid(),user.getUserid(),holder,"chats");
             holder.msg_count.setBackgroundResource(R.drawable.dark_count_fill);
             holder.msg_count.setTextColor(Color.WHITE);
             setunseencount(User.getInstance().getUserid(),user.getUserid(),holder,"chats");

         }
         else if (mode==1){
             holder.username.setText(user.getNickname());
             setlastmsg(User.getInstance().getUserid(),user.getUserid(),holder,"Incogchats");
             holder.msg_count.setBackgroundResource(R.drawable.white_count_fill);
             holder.msg_count.setTextColor(Color.BLACK);
             setunseencount(User.getInstance().getUserid(),user.getUserid(),holder,"Incogchats");

         }


         if (user.getImageurl()!=null) {
             if (user.getImageurl().equals("default")) {

                 holder.profilepic.setImageResource(R.mipmap.person_icon_round);
             } else {
                 Glide.with(context).load(user.getImageurl()).into(holder.profilepic);
             }
         }

        checkstatus(holder,user.getUserid());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode==0){

                Intent intent = new Intent(context, ChatRoom.class);
                intent.putExtra("userid",user.getUserid());
                intent.putExtra("gender",user.getGender());
                    context.startActivity(intent);}
                else if (mode==1){
                    collectionReference.document(user.getUserid()).collection("requests").document(User.getInstance().getUserid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = null;
                            if (task.getResult()!=null){
                                snapshot=task.getResult();
                            }
                            if (snapshot!=null){
                                if (snapshot.getBoolean("permission")!=null){
                                    if (snapshot.getBoolean("permission")){
                                        Intent intent = new Intent(context, IncogChatRoom.class);
                                        intent.putExtra("userid", user.getUserid());
                                        intent.putExtra("gender",user.getGender());
                                        context.startActivity(intent);
                                    }
                                    else {
                                        userInterface.requestchat(user.getUserid(),user.getNickname());
                                    }
                                }
                                else {
                                    userInterface.requestchat(user.getUserid(),user.getNickname());
                                }
                            }

                        }
                    });



                }



            }
        });
    }

    private void setunseencount(final String sender, final String receiver, final ViewHolder holder, String collection) {

        DatabaseReference reference=null;
if (collection=="chats") {
     reference = database.getReference(collection);
}
else if (collection=="Incogchats"){
    reference=database.getReference(collection).child(User.getInstance().getUserid());
}

    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            int unread_count = 0;
            for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                Chat chat = datasnapshot.getValue(Chat.class);
                if (chat.getMessage() != null) {
                    chat.setMessage(AESDecryptionMethod(chat.getMessage()));
                }
                if (chat.getSender() != null && sender != null && chat.getReceiver() != null && receiver != null) {

                    if (chat.getSender().equals(receiver) && chat.getReceiver().equals(sender)) {

                        if (!chat.isIsseen()) {
                            unread_count++;
                        }


                    }


                }
            }

            if (unread_count > 0) {

                holder.msg_count.setVisibility(View.VISIBLE);
                holder.msg_count.setText(" " + unread_count);
            } else {
                holder.msg_count.setVisibility(View.GONE);
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });


    }

    private void setlastmsg(final String sender, final String receiver, final ViewHolder holder, final String collection) {


        DatabaseReference reference=null;
        if (collection=="chats") {
            reference = database.getReference(collection);
        }
        else if (collection=="Incogchats"){
            reference=database.getReference(collection).child(User.getInstance().getUserid());
        }
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String recent_msg="";
                for (DataSnapshot datasnapshot : snapshot.getChildren() ){
                    Chat chat = datasnapshot.getValue(Chat.class);

                    if (chat != null && chat.getMessage() != null && !chat.getMessage().isEmpty()) {
                          chat.setMessage(AESDecryptionMethod(chat.getMessage()));

                        if (chat.getSender() != null && sender != null && chat.getReceiver() != null && receiver != null) {

                            if ((chat.getSender().equals(sender) && chat.getReceiver().equals(receiver)) ||
                                    (chat.getSender().equals(receiver) && chat.getReceiver().equals(sender))) {


                                   recent_msg = chat.getMessage();


                            }


                        }
                    }
               }

                    holder.lastmessage.setText(recent_msg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void checkstatus(final ViewHolder holder, final String userid) {

        DatabaseReference reference= database.getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    UserStatus userStatus= dataSnapshot.getValue(UserStatus.class);


                    if (dataSnapshot.getKey().equals(userid)) {


                        if (userStatus.getStatus().equals("online")) {
                            holder.status.setImageResource(R.color.android_green);
                        } else  {
                            holder.status.setImageResource(R.color.colorAccent);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
    public int getItemCount() {
        return users.size() ;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profilepic;
        public ImageView status;
        public TextView lastmessage;
        public TextView msg_count;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user);
            profilepic=itemView.findViewById(R.id.profile_image);
            status=itemView.findViewById(R.id.status);
            lastmessage=itemView.findViewById(R.id.lastmessage);
            msg_count=itemView.findViewById(R.id.msg_count);
        }
    }
}
