package com.example.qaautomation.data.model

data class NetworkSpeedData(
    val downloadSpeed: Float,
    val uploadSpeed: Float,
    val ping: Int,
    val jitter: Float,
    val packetLoss: Float
) 