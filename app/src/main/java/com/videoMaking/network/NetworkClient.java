package com.videoMaking.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NetworkClient {
    String BASE_URL1 = "https://newsapi.org/v2/";
//    String BASE_URL2 = "https://api.myjson.com/bins/";


    @GET("top-headlines")
    Call<ResponseBody> getNews(@Query("country") String country, @Query("apiKey") String apiKey);

    /*@GET("1bonqw")
    Call<ResponseBody> getVideoData();*/
}
