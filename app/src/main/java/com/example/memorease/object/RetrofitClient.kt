package com.example.memorease.network

import QuestionApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://web-production-2ba0.up.railway.app/") // Railway URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: QuestionApi = retrofit.create(QuestionApi::class.java)
}
