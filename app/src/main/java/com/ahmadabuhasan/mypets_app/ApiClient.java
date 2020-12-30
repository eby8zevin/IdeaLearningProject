package com.ahmadabuhasan.mypets_app;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
 * Created by Ahmad Abu Hasan on 30/12/2020
 */

public class ApiClient {

    private static final String BASE_URL = "http://ipaddressServer/demo_pets/";
    private static Retrofit retrofit;

    static Retrofit getApiClient() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }
        return retrofit;
    }
}
