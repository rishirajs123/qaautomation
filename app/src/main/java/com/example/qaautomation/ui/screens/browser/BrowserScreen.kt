package com.example.qaautomation.ui.screens.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qaautomation.data.model.NetworkLog
import com.example.qaautomation.data.repository.NetworkLogRepository
import com.example.qaautomation.ui.viewmodel.BrowserViewModel
import com.example.qaautomation.ui.viewmodel.NetworkLogViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.focus.onFocusChanged

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onBackClick: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel(),
    networkLogViewModel: NetworkLogViewModel = hiltViewModel()
) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val networkSpeed by viewModel.networkSpeed.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()
    
    var urlTextFieldValue by remember { mutableStateOf(TextFieldValue(currentUrl)) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    
    // Track URL textfield focus state
    var isUrlFocused by remember { mutableStateOf(false) }
    
    // Create focus requester at the composable level
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Keep URL text field value updated with current URL
    LaunchedEffect(currentUrl) {
        if (!isUrlFocused) {
            urlTextFieldValue = TextFieldValue(
                text = currentUrl,
                selection = TextRange(0, 0)
            )
        }
    }
    
    // Simulated network speed calculation
    LaunchedEffect(isLoading) {
        if (isLoading) {
            // Simulate network speed monitoring
            while (isLoading) {
                val bytesPerSecond = (10000..500000).random().toLong()
                viewModel.updateNetworkSpeed(bytesPerSecond)
                delay(500)
            }
        } else {
            viewModel.updateNetworkSpeed(0)
        }
    }
    
    // Select all text when URL TextField is focused
    LaunchedEffect(isUrlFocused) {
        if (isUrlFocused) {
            delay(50) // Short delay to ensure selection works
            urlTextFieldValue = TextFieldValue(
                text = urlTextFieldValue.text,
                selection = TextRange(0, urlTextFieldValue.text.length)
            )
        }
    }
    
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !isUrlFocused,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    title = { Text("Browser") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = "Navigate back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showBookmarks = !showBookmarks }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Bookmarks")
                        }
                        IconButton(onClick = { showOptions = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Clear focus when tapping outside the URL bar (like in Safari)
                    if (isUrlFocused) {
                        focusManager.clearFocus()
                    }
                }
        ) {
            // WebView content
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            javaScriptCanOpenWindowsAutomatically = true
                            setSupportMultipleWindows(true)
                            loadsImagesAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let { viewModel.onUrlChanged(it) }
                                viewModel.updateCanGoBackForward(view?.canGoBack() ?: false, view?.canGoForward() ?: false)
                                // Ensure we set loading state to true
                                viewModel.updateLoadingState(true)
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                viewModel.onPageFinished()
                                viewModel.updateCanGoBackForward(view?.canGoBack() ?: false, view?.canGoForward() ?: false)
                                // Ensure we set loading state to false
                                viewModel.updateLoadingState(false)
                            }
                            
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                viewModel.onError("Error loading: ${error?.description}")
                            }
                            
                            // Track network requests
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                request?.url?.toString()?.let { url ->
                                    // Create a network log for the request
                                    val networkLog = NetworkLog(
                                        method = request.method ?: "GET",
                                        url = url,
                                        requestHeaders = request.requestHeaders ?: emptyMap(),
                                        requestBody = null,
                                        responseTime = 0, // Will be updated when we get the response
                                        statusCode = 200, // Default, will be updated if we get an error
                                        responseHeaders = emptyMap(),
                                        responseBody = null,
                                        timestamp = System.currentTimeMillis(),
                                        source = "Browser"
                                    )
                                    
                                    networkLogViewModel.addNetworkLog(networkLog)
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                        }
                        
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                viewModel.updateProgress(newProgress)
                            }
                        }
                        
                        loadUrl(currentUrl)

                        // Add click handler to dismiss keyboard
                        setOnTouchListener { _, _ ->
                            // If URL bar is focused, clear focus
                            if (isUrlFocused) {
                                focusManager.clearFocus()
                            }
                            false // Return false to allow other touch events to be processed
                        }
                    }.also { webView = it }
                },
                update = { _ ->
                    // No need to update the existing WebView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp) // Space for bottom navigation bar
                    .testTag("web_view")
            )
            
            // Loading progress bar
            if (isLoading && progress < 100) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
            
            // Safari-style URL bar at the bottom with navigation controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                // URL input bar with navigation controls all in one row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = { 
                            if (canGoBack) {
                                webView?.goBack()
                            }
                        },
                        enabled = canGoBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = if (canGoBack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    
                    // Forward button
                    IconButton(
                        onClick = { 
                            if (canGoForward) {
                                webView?.goForward()
                            }
                        },
                        enabled = canGoForward,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward, 
                            contentDescription = "Forward",
                            tint = if (canGoForward) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    
                    // URL TextField with selection on tap
                    OutlinedTextField(
                        value = urlTextFieldValue,
                        onValueChange = { urlTextFieldValue = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { 
                                isUrlFocused = it.isFocused
                                // Select all text when focused
                                if (it.isFocused) {
                                    urlTextFieldValue = TextFieldValue(
                                        text = urlTextFieldValue.text,
                                        selection = TextRange(0, urlTextFieldValue.text.length)
                                    )
                                }
                            }
                            .testTag("browser_url_field"),
                        placeholder = { Text("Enter URL") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                var finalUrl = urlTextFieldValue.text
                                // Add http:// if not present
                                if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                                    finalUrl = "https://$finalUrl"
                                }
                                webView?.loadUrl(finalUrl)
                                focusManager.clearFocus()
                            }
                        ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    
                    // Adaptive loading/cancel button
                    IconButton(
                        onClick = {
                            if (isLoading) {
                                webView?.stopLoading()
                                viewModel.stopLoading()
                            } else {
                                webView?.reload()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = if (isLoading) "Stop Loading" else "Refresh",
                            tint = if (isLoading) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Bookmark button
                    IconButton(
                        onClick = { 
                            currentUrl?.let { 
                                if (bookmarks.contains(it)) {
                                    viewModel.removeBookmark(it)
                                } else {
                                    viewModel.addBookmark(it) 
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        val isFavorite = bookmarks.contains(currentUrl)
                        Icon(
                            if (isFavorite) Icons.Default.Star else Icons.Outlined.Star, 
                            contentDescription = if (isFavorite) "Remove Bookmark" else "Add Bookmark",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Bookmarks panel
            AnimatedVisibility(
                visible = showBookmarks,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bookmarks",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { showBookmarks = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (bookmarks.isEmpty()) {
                            Text(
                                text = "No bookmarks yet",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                bookmarks.forEach { bookmark ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                webView?.loadUrl(bookmark)
                                                showBookmarks = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = bookmark,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        IconButton(
                                            onClick = { viewModel.removeBookmark(bookmark) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove bookmark",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
            
            // Options menu
            if (showOptions) {
                AlertDialog(
                    onDismissRequest = { showOptions = false },
                    title = { Text("Options") },
                    text = {
                        Column {
                            ListItem(
                                headlineContent = { Text("Add Bookmark") },
                                leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    currentUrl?.let { viewModel.addBookmark(it) }
                                    showOptions = false
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Clear Bookmarks") },
                                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    viewModel.clearBookmarks()
                                    showOptions = false
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Share") },
                                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    // Share implementation
                                    showOptions = false
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showOptions = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
} 