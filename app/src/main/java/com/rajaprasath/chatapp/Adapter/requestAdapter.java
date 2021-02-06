package com.rajaprasath.chatapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class requestAdapter  extends RecyclerView.Adapter<requestAdapter.ViewHolder> {
   private Context context;
   private List<User> users=null;
    private final int permission=130;
    private final  int trusted=1;
    private final int request_type;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference= db.collection("Users");
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private User user;

    public requestAdapter(Context context, List<User> users, int request_type) {
        this.context=context;
        this.users=users;
        this.request_type=request_type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.request_item,parent,false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (request_type==permission) {
            user = users.get(position);
            String request = user.getNickname() + " wants to chat with you.";

            if (user.getImageurl().equals("default")) {

                holder.profile.setImageResource(R.mipmap.person_icon_round);
            } else {
                Glide.with(context).load(user.getImageurl()).into(holder.profile);
            }
            checkstatus(holder, user.getStatus());
            holder.username.setText(request);
            holder.agree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("permission", true);
                    collectionReference.document(User.getInstance().getUserid())
                            .collection("requests").document(user.getUserid()).set(obj);
                    collectionReference.document(user.getUserid()).collection("requests").document(User.getInstance().getUserid()).set(obj);
                }
            });
            holder.decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("permission", false);
                    collectionReference.document(User.getInstance().getUserid())
                            .collection("requests").document(user.getUserid()).set(obj);

                }
            });
        }

        else if (request_type==trusted){
            user = users.get(position);

            String request = user.getNickname() + " wants to add you to his contacts.";

            if (user.getImageurl().equals("default")) {

                holder.profile.setImageResource(R.mipmap.person_icon_round);
            } else {
                Glide.with(context).load(user.getImageurl()).into(holder.profile);
            }

            checkstatus(holder, user.getStatus());
            holder.username.setText(request);
            holder.agree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("messagetime", Timestamp.now().toDate());
                    obj.put("trusted", true);
                    HashMap<String,Object> del_obj=new HashMap<>();
                    del_obj.put("permission",true);
                    collectionReference.document(User.getInstance().getUserid())
                            .collection("message").document(user.getUserid()).set(obj);
                    collectionReference.document(user.getUserid()).collection("message").document(User.getInstance().getUserid()).set(obj);
                    collectionReference.document(User.getInstance().getUserid()).collection("requests").document(user.getUserid()).set(del_obj);
                    collectionReference.document(user.getUserid()).collection("requests").document(User.getInstance().getUserid()).set(del_obj);
                }
            });
            holder.decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("trusted", false);
                    HashMap<String,Object> del_obj=new HashMap<>();
                    del_obj.put("permission",true);
                    collectionReference.document(User.getInstance().getUserid())
                            .collection("message").document(user.getUserid()).set(obj);
                    collectionReference.document(User.getInstance().getUserid())
                            .collection("requests").document(user.getUserid()).set(del_obj);

                }
            });

        }

    }

    private void checkstatus(final ViewHolder holder, final String status) {
if (status!=null) {
    if (status.equals("online")) {
        holder.status.setImageResource(R.color.android_green);
    } else {
        holder.status.setImageResource(R.color.colorAccent);
    }
}
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private ImageButton agree,decline;
        private CircleImageView profile,status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user);
            agree=itemView.findViewById(R.id.agree);
            decline=itemView.findViewById(R.id.decline);
            profile=itemView.findViewById(R.id.profile_image);
            status=itemView.findViewById(R.id.status);

        }
    }
}
