package com.example.qaautomation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow<BrowserUiState>(BrowserUiState.Initial)
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()
    
    // Current URL being displayed
    private val _currentUrl = MutableStateFlow<String>("https://ifconfig.me")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()
    
    // Loading progress (0-100)
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()
    
    // Is the page currently loading?
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Can go back/forward in history
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()
    
    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()
    
    // Network speed information
    private val _networkSpeed = MutableStateFlow<String?>(null)
    val networkSpeed: StateFlow<String?> = _networkSpeed.asStateFlow()
    
    // Bookmarks
    private val _bookmarks = MutableStateFlow<List<String>>(emptyList())
    val bookmarks: StateFlow<List<String>> = _bookmarks.asStateFlow()
    
    fun navigateTo(url: String) {
        _currentUrl.value = normalizeUrl(url)
        _isLoading.value = true
        _progress.value = 0
        _uiState.value = BrowserUiState.Loading
    }
    
    fun updateProgress(progress: Int) {
        _progress.value = progress
        if (progress == 100) {
            _isLoading.value = false
            _uiState.value = BrowserUiState.Success
        }
    }
    
    fun updateNetworkSpeed(bytesPerSecond: Long) {
        val speedText = when {
            bytesPerSecond < 1024 -> "${bytesPerSecond} B/s"
            bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024} KB/s"
            else -> "${bytesPerSecond / (1024 * 1024)} MB/s"
        }
        _networkSpeed.value = speedText
    }
    
    fun updateCanGoBackForward(canGoBack: Boolean, canGoForward: Boolean) {
        _canGoBack.value = canGoBack
        _canGoForward.value = canGoForward
    }
    
    fun stopLoading() {
        _isLoading.value = false
        _progress.value = 0
    }
    
    fun addBookmark(url: String) {
        viewModelScope.launch {
            val normalizedUrl = normalizeUrl(url)
            if (!_bookmarks.value.contains(normalizedUrl)) {
                _bookmarks.value = _bookmarks.value + normalizedUrl
            }
        }
    }
    
    fun removeBookmark(url: String) {
        viewModelScope.launch {
            val normalizedUrl = normalizeUrl(url)
            _bookmarks.value = _bookmarks.value.filter { it != normalizedUrl }
        }
    }
    
    fun clearBookmarks() {
        viewModelScope.launch {
            _bookmarks.value = emptyList()
        }
    }
    
    fun refresh() {
        _isLoading.value = true
        _progress.value = 0
        _uiState.value = BrowserUiState.Loading
        // The actual refresh will be handled by the WebView
    }
    
    fun goBack() {
        // WebView handles actual navigation, this just updates state
        _isLoading.value = true
        _progress.value = 0
    }
    
    fun goForward() {
        // WebView handles actual navigation, this just updates state
        _isLoading.value = true
        _progress.value = 0
    }
    
    fun onUrlChanged(url: String) {
        _currentUrl.value = url
    }
    
    fun onPageFinished() {
        _isLoading.value = false
        _progress.value = 100
        _uiState.value = BrowserUiState.Success
    }
    
    fun onError(error: String) {
        _isLoading.value = false
        _uiState.value = BrowserUiState.Error(error)
    }
    
    fun normalizeUrl(url: String): String {
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
    }
    
    fun updateLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
        if (isLoading) {
            _progress.value = 0
            _uiState.value = BrowserUiState.Loading
        } else {
            _progress.value = 100
            _uiState.value = BrowserUiState.Success
        }
    }
    
    fun resetState() {
        _uiState.value = BrowserUiState.Initial
    }
}

sealed class BrowserUiState {
    object Initial : BrowserUiState()
    object Loading : BrowserUiState()
    object Success : BrowserUiState()
    data class Error(val message: String) : BrowserUiState()
} 