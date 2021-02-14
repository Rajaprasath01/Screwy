package com.rajaprasath.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.model.Chat;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    private  int mode;
    private Context context;
   private List<Chat> chats;
   private String imageurl;
   private int LEFT_MSG=0;
   private int RIGHT_MSG=1;
   private int who;
    private User fuser;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    public MessageAdapter(Context context, List<Chat> chats,String imageurl, int mode) {
        this.context = context;
        this.chats= chats;
        this.imageurl=imageurl;
        this.mode=mode;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (mode==0){
            if (viewType==LEFT_MSG) {
                 view = LayoutInflater.from(context).inflate(R.layout.left_msg, parent, false);
                who=LEFT_MSG;

            }
            else {
                 view = LayoutInflater.from(context).inflate(R.layout.right_msg, parent, false);
                who = RIGHT_MSG;
            }
        }

        else if (mode==1){
            if (viewType==LEFT_MSG) {
                view = LayoutInflater.from(context).inflate(R.layout.incog_left_msg, parent, false);
                who=LEFT_MSG;

            }
            else {
                view = LayoutInflater.from(context).inflate(R.layout.incog_right_msg, parent, false);
                who = RIGHT_MSG;
            }
        }

        return new MessageAdapter.ViewHolder(view);
        }




    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position ) {

        Chat chat= chats.get(position);
        if (holder.show_message!=null) {
            holder.show_message.setText(chat.getMessage().trim());
        }
        if (holder.profile_pic!=null) {
            holder.profile_pic.setBackgroundResource(R.color.transparent);
        }
        holder.show_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Chat chat1=chats.get(position);
               deletechat(holder,position,chat1);
               return false;
            }
        });

        if (who==LEFT_MSG){
            if (imageurl.equals("default")){
                if (mode==0) {
                    holder.profile_pic.setImageResource(R.mipmap.person_icon_round);
                }
                else {
                    holder.profile_pic.setImageResource(R.mipmap.chatroom_person_icon_round);
                }
            }
            else {


                Glide.with(context).load(imageurl).into(holder.profile_pic);
            }
        }
        else if (who==RIGHT_MSG){
            User user= User.getInstance();
            if (user.getImageurl().equals("default")){
                if (mode==0) {
                    holder.profile_pic.setImageResource(R.mipmap.person_icon_round);
                }
                else {
                    holder.profile_pic.setImageResource(R.mipmap.chatroom_person_icon_round);
                }            }
            else {
                Glide.with(context).load(user.getImageurl()).into(holder.profile_pic);
            }

            if (position==chats.size()-1){
                if (holder.seen_msg!=null) {
                    holder.seen_msg.setVisibility(View.VISIBLE);
                    if (chat.isIsseen()) {
                        holder.seen_msg.setText("Seen");
                    } else {
                        holder.seen_msg.setText("Delivered");
                    }
                }
            }
            else {
                if (holder.seen_msg!=null) {
                    holder.seen_msg.setVisibility(View.GONE);
                }
            }

        }




    }

    private void deletechat(ViewHolder holder, final int position, final Chat localchat) {
        builder=new AlertDialog.Builder(context);
        View view=LayoutInflater.from(context).inflate(R.layout.delete_popup,null);
        TextView delete=view.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DatabaseReference databaseReference = database.getReference("Incogchats").child(localchat.getSender());
                DatabaseReference reference2=database.getReference("Incogchats").child(localchat.getReceiver());
                databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        DataSnapshot snapshot = task.getResult();
                        Chat chat = new Chat();
                        if (snapshot != null){
                            for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                                chat = datasnapshot.getValue(Chat.class);

                                if (datasnapshot.getKey() != null) {
                                    if (datasnapshot.getKey().equals(localchat.getMessageid())) {
                                        if (chat.getSender().equals(User.getInstance().getUserid())) {

                                            datasnapshot.getRef().setValue(null);
                                        }
                                    }
                                }

                            }
                    }
                    }
                });
                reference2.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        DataSnapshot snapshot = task.getResult();
                        Chat chat = new Chat();
                        if (snapshot != null){
                            for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                                chat = datasnapshot.getValue(Chat.class);
                                if (datasnapshot.getKey() != null) {
                                    if (datasnapshot.getKey().equals(localchat.getMessageid())) {
                                            datasnapshot.getRef().setValue(null);

                                    }
                                }

                            }
                        }
                    }
                });


                dialog.dismiss();
            }


        });
        builder.setView(view);

        dialog=builder.create();

        dialog.show();
        dialog.getWindow().setLayout(400,130);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.delete_popup_background);
    }


    @Override
    public int getItemCount() {
        return chats.size() ;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profile_pic;
        private TextView show_message;
        private TextView seen_msg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
           profile_pic=itemView.findViewById(R.id.profile_pic);
           show_message=itemView.findViewById(R.id.show_message);
           seen_msg=itemView.findViewById(R.id.seen_msg);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser= User.getInstance();
        if (chats.get(position).getSender().equals(fuser.getUserid())){
            return RIGHT_MSG;
        }
        else {
            return LEFT_MSG;
        }
    }
}
