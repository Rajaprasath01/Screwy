package com.rajaprasath.chatapp.Notifications;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

public class Client {

    private static Retrofit retrofit=null;
    public  static Retrofit getclient(String url){
        if (retrofit==null){
            retrofit= new Retrofit.Builder()
                    .baseUrl(url).addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
