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
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.rajaprasath.chatapp.Notifications.Client;
import com.rajaprasath.chatapp.Notifications.Data;
import com.rajaprasath.chatapp.Notifications.MyResponse;
import com.rajaprasath.chatapp.Notifications.Sender;
import com.rajaprasath.chatapp.Notifications.Token;
import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.fragment.APIService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class requestAdapter  extends RecyclerView.Adapter<requestAdapter.ViewHolder> {
   private final Context context;
   private List<User> users=null;
    private final int permission=130;
    private final  int trusted=1;
    private final int request_type;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference= db.collection("Users");
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private User user;
    private final APIService apiService = Client.getclient("https://fcm.googleapis.com/").create(APIService.class);
    private final Integer Friend_Request=3;
    private Integer Inconito=1;
    private Integer Normal=0;

    public requestAdapter(Context context, List<User> users, int request_type) {
        this.context=context;
        this.users=users;
        this.request_type=request_type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.request_item,parent,false);

        FirebaseUser fuser= FirebaseAuth.getInstance().getCurrentUser();
        User.getInstance().setUserid(fuser.getUid());


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (request_type==permission) {
            user = users.get(position);

            String request = user.getNickname() + " wants to chat with you.";

            if (user.getImageurl()!=null && !user.getImageurl().isEmpty()) {
                if (user.getImageurl().equals("default")) {

                    holder.profile.setImageResource(R.mipmap.person_icon_round);
                } else {
                    Glide.with(context).load(user.getImageurl()).into(holder.profile);
                }
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
                    collectionReference.document(user.getUserid()).collection("requests").document(User.getInstance().getUserid()).set(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg=User.getInstance().getNickname()+" accepted your friend request";

                            sendNotification(user.getUserid(),User.getInstance().getNickname(),msg,Inconito);
                        }
                    });
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

                    collectionReference.document(User.getInstance().getUserid())
                            .collection("message").document(user.getUserid()).set(obj);
                    collectionReference.document(user.getUserid()).collection("message").document(User.getInstance().getUserid()).set(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg=User.getInstance().getNickname()+" added you as a friend";
                            sendNotification(user.getUserid(),User.getInstance().getNickname(),msg,Normal);
                        }
                    });

                }
            });
            holder.decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("trusted", false);

                    collectionReference.document(User.getInstance().getUserid())
                            .collection("message").document(user.getUserid()).set(obj);


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


    private void sendNotification(final String receiver, final String nickname, final String msg, final Integer mode) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                String category="";
                if (User.getInstance().getInterest()!=null && !User.getInstance().getInterest().isEmpty()) {
                    if ( User.getInstance().getInterest().get(0)!=null) {
                        category = User.getInstance().getInterest().get(0);
                    }
                }

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);

                    final Data data = new Data(User.getInstance().getUserid(), R.mipmap.ic_launcher, nickname, msg,User.getInstance().getUserid(),mode,category);
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


    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView username;
        private final ImageButton agree;
        private final ImageButton decline;
        private final CircleImageView profile;
        private final CircleImageView status;
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
