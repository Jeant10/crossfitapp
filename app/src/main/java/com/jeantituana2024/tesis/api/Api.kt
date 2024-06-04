package com.jeantituana2024.tesis.api

import com.jeantituana2024.tesis.models.DefaultResponse
import com.jeantituana2024.tesis.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Api {

    @FormUrlEncoded
    @POST("createuser")
    fun createUser(
        @Field("name") name:String,
        @Field("email") email:String,
        @Field("password") password:String
    ):Call<DefaultResponse>

    @FormUrlEncoded
    @POST("userLogin")
    fun userLogin(
        @Field("email") email:String,
        @Field("password") password:String
    ):Call<LoginResponse>
}