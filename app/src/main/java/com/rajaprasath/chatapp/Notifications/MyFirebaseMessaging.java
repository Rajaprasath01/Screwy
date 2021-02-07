package com.rajaprasath.chatapp.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
        int mode= Integer.parseInt(remoteMessage.getData().get("mode"));

        RemoteMessage.Notification notification= remoteMessage.getNotification();
        int j= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=null;
        int friend_Request = 3;
        if (mode==Normal){
            intent = new Intent(this, ChatRoom.class);
        }
        else if (mode==Incognito){
            intent = new Intent(this, IncogChatRoom.class);
        }
        else if (mode== friend_Request){
            intent = new Intent(this, MainActivity.class);
        }
        else if (mode==Category_users_intent){
            intent=new Intent(this, MainActivity.class);
        }

        Bundle bundle= new Bundle();
        bundle.putString("userid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

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
        int mode= Integer.parseInt(remoteMessage.getData().get("mode"));

        RemoteMessage.Notification notification= remoteMessage.getNotification();
        int j= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=null;
        if (mode==Normal){
            intent = new Intent(this, ChatRoom.class);
        }
        else if (mode==Incognito){
            intent = new Intent(this, IncogChatRoom.class);
        }
        Bundle bundle= new Bundle();
        bundle.putString("userid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
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
