package com.jeantituana2024.tesis.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

//    private val AUTH = "Basic"+Base64.encodeToString("".toByteArray(),Base64.NO_WRAP)

    private const val BASE_URL="https://api-crossfit.vercel.app/api/"

//    private const val BASE_URL="http://192.168.1.15:3000/api/"

//    private val okHttpClient = OkHttpClient.Builder()
//        .addInterceptor { chain ->
//            val original = chain.request()
//
//            val requestBuilder = original.newBuilder()
//                .addHeader("Authorization",AUTH)
//                .method(original.method(),original.body())
//
//            val request = requestBuilder.build()
//            chain.proceed(request)
//        }.build()


    private var _instance: Api? = null

    val instance: Api
        get() {
            return _instance ?: createInstance()
        }

    private fun createInstance(): Api {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        _instance = retrofit.create(Api::class.java)
        return _instance!!
    }

    // Método para cambiar temporalmente la instancia (solo para pruebas)
    fun setInstanceForTesting(api: Api) {
        _instance = api
    }

    // Método para restaurar la instancia original (solo para pruebas)
    fun resetInstanceForTesting() {
        _instance = null
    }
}