package com.example.qaautomation.data.repository

import com.example.qaautomation.data.api.IpGeolocationService
import com.example.qaautomation.data.model.IpGeolocation
import com.example.qaautomation.data.model.VendorResponse
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IpGeolocationRepository @Inject constructor(
    private val service: IpGeolocationService
) {
    private val gson = Gson()
    
    // Store all vendor responses for tracking
    private val _vendorResponses = MutableStateFlow<List<VendorResponse>>(emptyList())
    val vendorResponses: StateFlow<List<VendorResponse>> = _vendorResponses.asStateFlow()
    
    // Store the selected/best vendor response
    private val _selectedVendor = MutableStateFlow<VendorResponse?>(null)
    val selectedVendor: StateFlow<VendorResponse?> = _selectedVendor.asStateFlow()
    
    suspend fun getIpGeolocation(): Result<IpGeolocation> {
        return try {
            coroutineScope {
                // Start all API calls in parallel
                val ipApiCoDeferred = async { tryIpApiCo() }
                val ipInfoIoDeferred = async { tryIpInfoIo() }
                val ipifyDeferred = async { tryIpify() }
                // Removed ip-api.com call as it's failing with 403
                
                // Wait for all to complete
                val ipApiCoResponse = ipApiCoDeferred.await()
                val ipInfoIoResponse = ipInfoIoDeferred.await()
                val ipifyResponse = ipifyDeferred.await()
                
                // Collect all responses
                val allResponses = listOf(
                    ipApiCoResponse, 
                    ipInfoIoResponse,
                    ipifyResponse
                ).filterNotNull()
                
                // Update the vendor responses
                _vendorResponses.value = allResponses
                
                // Find the fastest valid IPv4 response for IP address
                val fastestIpResponse = allResponses
                    .filter { it.successful && it.isIpv4 }
                    .minByOrNull { it.responseTimeMs }
                
                if (fastestIpResponse != null) {
                    _selectedVendor.value = fastestIpResponse
                    
                    // Create a composite IpGeolocation with the best data from all responses
                    val compositeInfo = createCompositeIpGeolocation(allResponses, fastestIpResponse)
                    Result.success(compositeInfo)
                } else {
                    Result.failure(Exception("No valid IPv4 responses from any vendors"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun tryIpApiCo(): VendorResponse? {
        return try {
            val startTime = System.currentTimeMillis()
            val response = service.getIpGeolocation()
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            
            if (response.isSuccessful && response.body() != null) {
                val ipData = response.body()!!
                val ipAddress = ipData.ip
                val isIpv4 = isIpv4Address(ipAddress)
                val rawResponse = gson.toJson(response.body())
                
                // Format JSON for better readability
                val formattedResponse = try {
                    val jsonObject = gson.fromJson(rawResponse, Any::class.java)
                    gson.newBuilder().setPrettyPrinting().create().toJson(jsonObject)
                } catch (e: Exception) {
                    rawResponse
                }
                
                VendorResponse(
                    vendor = "ipapi.co",
                    ipAddress = ipAddress,
                    isIpv4 = isIpv4,
                    responseTimeMs = responseTime,
                    timestamp = endTime,
                    rawResponse = rawResponse,
                    formattedResponse = formattedResponse,
                    requestUrl = "https://ipapi.co/json",
                    successful = true,
                    status = response.code()
                )
            } else {
                VendorResponse(
                    vendor = "ipapi.co",
                    ipAddress = "",
                    isIpv4 = false,
                    responseTimeMs = responseTime,
                    timestamp = endTime,
                    rawResponse = response.errorBody()?.string() ?: "No error body",
                    requestUrl = "https://ipapi.co/json",
                    successful = false,
                    status = response.code()
                )
            }
        } catch (e: Exception) {
            VendorResponse(
                vendor = "ipapi.co",
                ipAddress = "",
                isIpv4 = false,
                responseTimeMs = 0,
                timestamp = System.currentTimeMillis(),
                rawResponse = "Exception: ${e.message}",
                requestUrl = "https://ipapi.co/json",
                successful = false
            )
        }
    }
    
    private suspend fun tryIpInfoIo(): VendorResponse? {
        return try {
            val startTime = System.currentTimeMillis()
            val response = service.getIpInfoIo()
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val ipAddress = data["ip"]?.toString() ?: ""
                val isIpv4 = isIpv4Address(ipAddress)
                val rawResponse = gson.toJson(data)
                
                // Format JSON for better readability
                val formattedResponse = try {
                    gson.newBuilder().setPrettyPrinting().create().toJson(data)
                } catch (e: Exception) {
                    rawResponse
                }
                
                VendorResponse(
                    vendor = "ipinfo.io",
                    ipAddress = ipAddress,
                    isIpv4 = isIpv4,
                    responseTimeMs = responseTime,
                    timestamp = endTime,
                    rawResponse = rawResponse,
                    formattedResponse = formattedResponse,
                    requestUrl = "https://ipinfo.io/json",
                    successful = true,
                    status = response.code()
                )
            } else {
                VendorResponse(
                    vendor = "ipinfo.io",
                    ipAddress = "",
                    isIpv4 = false,
                    responseTimeMs = responseTime,
                    timestamp = endTime,
                    rawResponse = response.errorBody()?.string() ?: "No error body",
                    requestUrl = "https://ipinfo.io/json",
                    successful = false,
                    status = response.code()
                )
            }
        } catch (e: Exception) {
            VendorResponse(
                vendor = "ipinfo.io",
                ipAddress = "",
                isIpv4 = false,
                responseTimeMs = 0,
                timestamp = System.currentTimeMillis(),
                rawResponse = "Exception: ${e.message}",
                requestUrl = "https://ipinfo.io/json",
                successful = false
            )
        }
    }
    
    private suspend fun tryIpify(): VendorResponse? {
        return try {
            val startTime = System.currentTimeMillis()
            val response = service.getIpify()
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val ipAddress = data["ip"] ?: ""
                val isIpv4 = isIpv4Address(ipAddress)
                val rawResponse = gson.toJson(data)
                
                // Format JSON for better readability
                val formattedResponse = try {
                    gson.newBuilder().setPrettyPrinting().create().toJson(data)
                } catch (e: Exception) {
                    rawResponse
                }
                
                VendorResponse(
                    vendor = "ipify.org",
                    ipAddress = ipAddress,
                    isIpv4 = isIpv4,
                    responseTimeMs = responseTime,
                    timestamp = endTime,
                    rawResponse = rawResponse,
                    formattedResponse = formattedResponse,
                    requestUrl = "https://api.ipify.org?format=json",
                    successful = true,
                    status = response.code()
                )
            } else {
                VendorResponse(
                    vendor = "ipify.org",
                    ipAddress = "",
                    isIpv4 = false,
                    responseTimeMs = responseTime,
                    timestamp = endTime,
                    rawResponse = response.errorBody()?.string() ?: "No error body",
                    requestUrl = "https://api.ipify.org?format=json",
                    successful = false,
                    status = response.code()
                )
            }
        } catch (e: Exception) {
            VendorResponse(
                vendor = "ipify.org",
                ipAddress = "",
                isIpv4 = false,
                responseTimeMs = 0,
                timestamp = System.currentTimeMillis(),
                rawResponse = "Exception: ${e.message}",
                requestUrl = "https://api.ipify.org?format=json",
                successful = false
            )
        }
    }
    
    // Create a composite IpGeolocation with the best data from all responses
    private fun createCompositeIpGeolocation(
        allResponses: List<VendorResponse>,
        fastestIpResponse: VendorResponse
    ): IpGeolocation {
        // Start with basic info from the fastest response
        var city = "Unknown"
        var cityVendor = ""
        var region = "Unknown"
        var regionVendor = ""
        var countryName = "Unknown"
        var countryVendor = ""
        var countryCode = "Unknown" 
        var countryCodeVendor = ""
        var latitude = 0.0
        var longitude = 0.0
        var coordinatesVendor = ""
        var timezone = "Unknown"
        var timezoneVendor = ""
        var isp = "Unknown"
        var ispVendor = ""
        var org = "Unknown"
        var orgVendor = ""
        
        // Try to extract information from each vendor response
        for (response in allResponses.filter { it.successful }) {
            try {
                when (response.vendor) {
                    "ipapi.co" -> {
                        // Parse ipapi.co response
                        val data = gson.fromJson(response.rawResponse, IpApiCoResponse::class.java)
                        if (data != null) {
                            if (city == "Unknown" && data.city != null) {
                                city = data.city
                                cityVendor = "ipapi.co"
                            }
                            if (region == "Unknown" && data.region != null) {
                                region = data.region
                                regionVendor = "ipapi.co"
                            }
                            if (countryName == "Unknown" && data.country_name != null) {
                                countryName = data.country_name
                                countryVendor = "ipapi.co"
                            }
                            if (countryCode == "Unknown" && data.country_code != null) {
                                countryCode = data.country_code
                                countryCodeVendor = "ipapi.co"
                            }
                            if (latitude == 0.0 && data.latitude != null) {
                                latitude = data.latitude
                                longitude = data.longitude ?: 0.0
                                coordinatesVendor = "ipapi.co"
                            }
                            if (timezone == "Unknown" && data.timezone != null) {
                                timezone = data.timezone
                                timezoneVendor = "ipapi.co"
                            }
                            if (isp == "Unknown" && data.org != null) {
                                isp = data.org
                                ispVendor = "ipapi.co"
                            }
                        }
                    }
                    "ipinfo.io" -> {
                        // Parse ipinfo.io response
                        val dataMap = gson.fromJson(response.rawResponse, Map::class.java) as? Map<String, Any>
                        if (dataMap != null) {
                            if (city == "Unknown" && dataMap["city"] != null) {
                                city = dataMap["city"].toString()
                                cityVendor = "ipinfo.io"
                            }
                            if (region == "Unknown" && dataMap["region"] != null) {
                                region = dataMap["region"].toString()
                                regionVendor = "ipinfo.io"
                            }
                            if (countryName == "Unknown" && dataMap["country"] != null) {
                                countryName = dataMap["country"].toString()
                                countryVendor = "ipinfo.io"
                            }
                            if (timezone == "Unknown" && dataMap["timezone"] != null) {
                                timezone = dataMap["timezone"].toString()
                                timezoneVendor = "ipinfo.io"
                            }
                            if (org == "Unknown" && dataMap["org"] != null) {
                                org = dataMap["org"].toString()
                                orgVendor = "ipinfo.io"
                            }
                            // Parse location if available
                            if (latitude == 0.0 && dataMap["loc"] != null) {
                                val loc = dataMap["loc"].toString()
                                val parts = loc.split(",")
                                if (parts.size == 2) {
                                    try {
                                        latitude = parts[0].toDouble()
                                        longitude = parts[1].toDouble()
                                        coordinatesVendor = "ipinfo.io"
                                    } catch (e: Exception) {
                                        // Ignore parsing errors
                                    }
                                }
                            }
                        }
                    }
                    "ipify.org" -> {
                        // ipify.org only provides IP, which we already have
                        // No additional data to extract
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors for individual vendors
            }
        }
        
        return IpGeolocation(
            ip = fastestIpResponse.ipAddress,
            city = city,
            region = region,
            countryName = countryName,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
            timezone = timezone,
            isp = isp,
            org = org,
            timestamp = System.currentTimeMillis(),
            vendor = fastestIpResponse.vendor,
            responseTimeMs = fastestIpResponse.responseTimeMs,
            rawResponse = fastestIpResponse.rawResponse,
            requestUrl = fastestIpResponse.requestUrl,
            cityVendor = cityVendor,
            regionVendor = regionVendor,
            countryVendor = countryVendor,
            countryCodeVendor = countryCodeVendor,
            coordinatesVendor = coordinatesVendor,
            timezoneVendor = timezoneVendor,
            ispVendor = ispVendor,
            orgVendor = orgVendor
        )
    }
    
    // Data class to help parse ipapi.co responses
    private data class IpApiCoResponse(
        val ip: String?,
        val city: String?,
        val region: String?,
        val country_name: String?,
        val country_code: String?,
        val latitude: Double?,
        val longitude: Double?,
        val timezone: String?,
        val org: String?
    )
    
    // Add this at the class level
    private object MapTypeToken {
        val type = com.google.gson.reflect.TypeToken.getParameterized(Map::class.java, String::class.java, Any::class.java).type
    }
    
    // Simple check for IPv4 address format
    private fun isIpv4Address(ip: String): Boolean {
        return ip.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"))
    }
} 