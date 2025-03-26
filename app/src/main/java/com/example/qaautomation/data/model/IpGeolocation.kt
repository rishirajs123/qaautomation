package com.example.qaautomation.data.model

data class IpGeolocation(
    val ip: String,
    val city: String,
    val region: String,
    val countryName: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val isp: String,
    val org: String,
    val timestamp: Long = System.currentTimeMillis(),
    val vendor: String = "",
    val responseTimeMs: Long = 0,
    val rawResponse: String = "",
    val requestUrl: String = "",
    val cityVendor: String = "",
    val regionVendor: String = "",
    val countryVendor: String = "",
    val countryCodeVendor: String = "",
    val coordinatesVendor: String = "",
    val timezoneVendor: String = "",
    val ispVendor: String = "",
    val orgVendor: String = ""
) 