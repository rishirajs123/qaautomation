package com.example.qaautomation.data.api

import com.example.qaautomation.data.model.IpGeolocation
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface IpGeolocationService {
    // ipapi.co endpoint (default)
    @GET("json")
    suspend fun getIpGeolocation(): Response<IpGeolocation>
    
    // ipinfo.io endpoint
    @GET("https://ipinfo.io/json")
    suspend fun getIpInfoIo(): Response<Map<String, Any>>
    
    // ipify.org endpoint
    @GET("https://api.ipify.org?format=json")
    suspend fun getIpify(): Response<Map<String, String>>
    
    // Generic method to call any URL
    @GET
    suspend fun getFromUrl(@Url url: String): Response<Map<String, Any>>
} 