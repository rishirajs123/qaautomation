package com.example.qaautomation.di

import com.example.qaautomation.data.api.IpGeolocationService
import com.example.qaautomation.data.api.NetworkLoggingInterceptor
import com.example.qaautomation.data.api.NetworkTestService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(networkLoggingInterceptor: NetworkLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(networkLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://ipapi.co/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideIpGeolocationService(retrofit: Retrofit): IpGeolocationService {
        return retrofit.create(IpGeolocationService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideNetworkTestService(retrofit: Retrofit): NetworkTestService {
        return retrofit.create(NetworkTestService::class.java)
    }
} 