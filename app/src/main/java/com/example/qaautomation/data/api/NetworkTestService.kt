package com.example.qaautomation.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface NetworkTestService {
    @GET
    suspend fun getRequest(@Url url: String): String

    @POST
    suspend fun postRequest(@Url url: String, @Body body: Map<String, Any>): String
} 