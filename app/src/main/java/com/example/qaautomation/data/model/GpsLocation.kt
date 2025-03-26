package com.example.qaautomation.data.model

import java.util.*

data class GpsLocation(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float,
    val speed: Float?,
    val bearing: Float?,
    val provider: String,
    val isMocked: Boolean = false
) 