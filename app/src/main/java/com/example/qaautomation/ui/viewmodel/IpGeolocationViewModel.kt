package com.example.qaautomation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qaautomation.data.model.IpGeolocation
import com.example.qaautomation.data.model.VendorResponse
import com.example.qaautomation.data.repository.IpGeolocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class IpGeolocationViewModel @Inject constructor(
    private val repository: IpGeolocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IpGeolocationUiState>(IpGeolocationUiState.Initial)
    val uiState: StateFlow<IpGeolocationUiState> = _uiState.asStateFlow()
    
    private val _currentIpLocation = MutableStateFlow<IpGeolocation?>(null)
    val currentIpLocation: StateFlow<IpGeolocation?> = _currentIpLocation.asStateFlow()
    
    // Expose the vendor responses from the repository
    val vendorResponses: StateFlow<List<VendorResponse>> = repository.vendorResponses
    
    // Expose the selected vendor
    val selectedVendor: StateFlow<VendorResponse?> = repository.selectedVendor
    
    // Flag to show vendor details dialog
    private val _showVendorDetails = MutableStateFlow(false)
    val showVendorDetails: StateFlow<Boolean> = _showVendorDetails.asStateFlow()
    
    // Currently selected vendor for details view
    private val _selectedVendorForDetails = MutableStateFlow<VendorResponse?>(null)
    val selectedVendorForDetails: StateFlow<VendorResponse?> = _selectedVendorForDetails.asStateFlow()
    
    init {
        // Auto-refresh on init so the IP is available on the main screen
        refreshIpGeolocation()
    }
    
    fun refreshIpGeolocation() {
        viewModelScope.launch {
            _uiState.value = IpGeolocationUiState.Loading
            try {
                val result = repository.getIpGeolocation()
                result.onSuccess { ipInfo ->
                    _currentIpLocation.value = ipInfo
                    _uiState.value = IpGeolocationUiState.Success(ipInfo)
                }.onFailure { error ->
                    _uiState.value = IpGeolocationUiState.Error(error.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = IpGeolocationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun showVendorDetails(vendor: VendorResponse?) {
        _selectedVendorForDetails.value = vendor
        _showVendorDetails.value = true
    }
    
    fun hideVendorDetails() {
        _showVendorDetails.value = false
    }
    
    fun getFormattedTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

sealed class IpGeolocationUiState {
    object Initial : IpGeolocationUiState()
    object Loading : IpGeolocationUiState()
    data class Success(val ipGeolocation: IpGeolocation) : IpGeolocationUiState()
    data class Error(val message: String) : IpGeolocationUiState()
} 