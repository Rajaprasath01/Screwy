package com.rajaprasath.chatapp.fragment;

import com.rajaprasath.chatapp.Notifications.MyResponse;
import com.rajaprasath.chatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA2TeJMBg:APA91bHHMEphij2xviRwf9o31cwFlrPI2YLpRLuYigTEgUoTYOJS1WRBc8r-B2mvdermi_e1ulN4KaWuXEVPVTfc0OX3Wo-pOowp4RFK-cS0hFqBcwCCJxe4CfHRiD7Zb4uAOQCWcRTR"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
