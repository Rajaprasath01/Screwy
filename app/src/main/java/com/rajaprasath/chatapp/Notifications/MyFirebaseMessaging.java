package com.rajaprasath.chatapp.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rajaprasath.chatapp.fragment.Category_users;
import com.rajaprasath.chatapp.fragment.FriendRequests;
import com.rajaprasath.chatapp.ui.ChatRoom;
import com.rajaprasath.chatapp.ui.MainActivity;
import com.rajaprasath.chatapp.ui.splashScreen;
import com.rajaprasath.chatapp.ui.stranger.CategoryActivity;
import com.rajaprasath.chatapp.ui.stranger.CategoryUsers;
import com.rajaprasath.chatapp.ui.stranger.IncogChatRoom;

public class MyFirebaseMessaging extends FirebaseMessagingService {


    private final Integer Normal=0;
    private final Integer Incognito=1;
    private Integer Category_users_intent=4;

    public MyFirebaseMessaging() {
    }

    public MyFirebaseMessaging(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    @Override
    public void onNewToken(@NonNull String s) {
        
        super.onNewToken(s);

    }

    @Override
    public void onMessageSent(@NonNull String s) {
        super.onMessageSent(s);

    }

    FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sent= remoteMessage.getData().get("sent");
        String user=remoteMessage.getData().get("user");
        String category=remoteMessage.getData().get("category");

        SharedPreferences preferences= getSharedPreferences("PREFS",MODE_PRIVATE);


       String currentuser= preferences.getString("currentuser","none");
        if (firebaseUser!=null ){

            if (!currentuser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                     sendOreoNotification(remoteMessage);

                } else {
                    sendNotification(remoteMessage);
                }
            }
        }

    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user= remoteMessage.getData().get("user");

        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");
        String category=remoteMessage.getData().get("category");
        int mode= Integer.parseInt(remoteMessage.getData().get("mode"));

        RemoteMessage.Notification notification= remoteMessage.getNotification();
        int j= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=null;
        int requestID = (int) System.currentTimeMillis();

        int friend_Request = 3;
        if (mode==Normal){
            intent = new Intent(this, ChatRoom.class);
        }
        else if (mode==Incognito){
            intent=new Intent(getApplicationContext(),IncogChatRoom.class);
            intent.putExtra("mode","incognito");
        }
        else if (mode== friend_Request){
            intent=new Intent(getApplicationContext(),CategoryUsers.class);
            intent.putExtra("mode","friend_request");
            intent.putExtra("category_name",category);



        }
        else if (mode==Category_users_intent){
            intent=new Intent(getApplicationContext(),CategoryUsers.class);
            intent.putExtra("mode","category_users");
            intent.putExtra("category_name",category);
        }


        Bundle bundle= new Bundle();
        bundle.putString("userid",user);
        intent.putExtras(bundle);
        intent.putExtra("activity","notif");
        intent.putExtra("userid",user);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent=stackBuilder.getPendingIntent(100,PendingIntent.FLAG_UPDATE_CURRENT);
        String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";
        Uri defaultringtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoNotification oreoNotification= new OreoNotification(this);

        Notification.Builder builder= oreoNotification.getOreoNotification(title,body,pendingIntent,defaultringtone,icon);


        int i=0;
        if (j>0){

            i=j;

        }

        oreoNotification.getManager().notify(i,builder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user= remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");
        String category=remoteMessage.getData().get("category");
        int mode= Integer.parseInt(remoteMessage.getData().get("mode"));

        RemoteMessage.Notification notification= remoteMessage.getNotification();
        int j= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(getApplicationContext(), splashScreen.class);
        int friend_Request = 3;
        if (mode==Normal){
            intent = new Intent(this, ChatRoom.class);
        }
        else if (mode==Incognito){
            intent=new Intent(getApplicationContext(),IncogChatRoom.class);
            intent.putExtra("mode","incognito");
        }
        else if (mode== friend_Request){
            intent=new Intent(getApplicationContext(),CategoryUsers.class);
            intent.putExtra("mode","friend_request");
            intent.putExtra("category_name",category);



        }
        else if (mode==Category_users_intent){
            intent=new Intent(getApplicationContext(),CategoryUsers.class);
            intent.putExtra("mode","category_users");
            intent.putExtra("category_name",category);
        }



        intent.putExtra("activity","notif");
        intent.putExtra("userid",user);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(getApplicationContext());

        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent=stackBuilder.getPendingIntent(100,PendingIntent.FLAG_UPDATE_CURRENT);
        String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";
        Uri defaultringtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder= new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultringtone)
                .setContentIntent(pendingIntent)
                .setGroup(GROUP_KEY_WORK_EMAIL);


        NotificationManager noti= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i=0;

        if (j>0){
            i=j;
        }


        noti.notify(i,builder.build());


    }

}
