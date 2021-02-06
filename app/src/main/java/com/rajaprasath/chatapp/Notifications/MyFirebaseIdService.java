package com.rajaprasath.chatapp.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db= FirebaseFirestore.getInstance();

    

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        String refreshtoken= FirebaseInstanceId.getInstance().getToken();
        if (user!=null){
            updatetoken(refreshtoken);
        }
    }

    private void updatetoken(String refreshtoken) {

        Token token= new Token(refreshtoken);

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens");
        reference.child(user.getUid()).setValue(token);


    }
}
