package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CyanElectric
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SpaceSurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GalacticTycoonsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebGameScreen(
    viewModel: GalacticTycoonsViewModel
) {
    val context = LocalContext.current
    val webGameUrl by viewModel.webGameUrl.collectAsState()
    val isDesktopMode by viewModel.isDesktopMode.collectAsState()
    val tycoonNotes by viewModel.tycoonNotes.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isFullscreen by remember { mutableStateOf(false) }
    var showNotesSheet by remember { mutableStateOf(false) }
    val notesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle back button inside WebView
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        // Compact Game Control Toolbar
        AnimatedVisibility(visible = !isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceCardBg)
                    .border(1.dp, SpaceCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left navigation buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = webViewInstance?.canGoBack() == true,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (webViewInstance?.canGoBack() == true) CyanElectric else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = webViewInstance?.canGoForward() == true,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (webViewInstance?.canGoForward() == true) CyanElectric else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                hasError = false
                                webViewInstance?.reload()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = CyanElectric,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                hasError = false
                                webViewInstance?.loadUrl("https://g2.galactictycoons.com/")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Home",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Center URL pill
                    Surface(
                        color = SpaceSurfaceLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = CyanElectric,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "g2.galactictycoons.com",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    // Right utility actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Desktop / Mobile Mode Toggle
                        IconButton(
                            onClick = {
                                val newMode = !isDesktopMode
                                viewModel.setDesktopMode(newMode)
                                webViewInstance?.let { wv ->
                                    val desktopUA =
                                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                    val defaultUA = WebSettings.getDefaultUserAgent(context)
                                    wv.settings.userAgentString = if (newMode) desktopUA else defaultUA
                                    wv.reload()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isDesktopMode) Icons.Default.PhoneAndroid else Icons.Default.DesktopWindows,
                                contentDescription = "Toggle Viewport",
                                tint = if (isDesktopMode) GoldAccent else TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Tycoon Notes Overlay
                        IconButton(
                            onClick = { showNotesSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Quick Notes",
                                tint = CyanElectric,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Fullscreen
                        IconButton(
                            onClick = { isFullscreen = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Open External
                        IconButton(
                            onClick = {
                                val currentUrl = webViewInstance?.url ?: "https://g2.galactictycoons.com/"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.OpenInBrowser,
                                contentDescription = "Open in Chrome",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Quick Navigation Shortcuts
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val shortcuts = listOf(
                        "Overview" to "https://g2.galactictycoons.com/",
                        "Exchange Market" to "https://g2.galactictycoons.com/",
                        "Logistics Fleet" to "https://g2.galactictycoons.com/",
                        "Planetary Bases" to "https://g2.galactictycoons.com/",
                        "Guild & Alliance" to "https://g2.galactictycoons.com/"
                    )

                    shortcuts.forEach { (label, url) ->
                        Surface(
                            color = SpaceSurfaceLight,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = label,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating exit fullscreen button when fullscreen
        if (isFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    color = Color(0xCC090D16),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanElectric),
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    IconButton(
                        onClick = { isFullscreen = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.FullscreenExit,
                            contentDescription = "Exit Fullscreen",
                            tint = CyanElectric,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Web Loading Progress
        if (isLoading && webProgress < 1.0f) {
            LinearProgressIndicator(
                progress = { webProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = CyanElectric,
                trackColor = SpaceCardBg
            )
        }

        // WebView or Error State
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasError) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = LossRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Galactic Uplink Interrupted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Unable to connect to https://g2.galactictycoons.com/. Please verify your internet connection.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            hasError = false
                            webViewInstance?.loadUrl("https://g2.galactictycoons.com/")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanElectric)
                    ) {
                        Text("Retry Connection", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("galactic_webview"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Enable cookies
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // Game-optimized settings
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            mediaPlaybackRequiresUserGesture = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                hasError = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    errorMessage = error?.description?.toString() ?: "Connection failed"
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                webProgress = newProgress / 100f
                            }
                        }

                        loadUrl(webGameUrl)
                        webViewInstance = this
                    }
                },
                update = { wv ->
                    // Keep instance reference
                    webViewInstance = wv
                }
            )
        }
    }

    // Quick Notes & Coordinates Drawer
    if (showNotesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotesSheet = false },
            sheetState = notesSheetState,
            containerColor = SpaceDark
        ) {
            TycoonQuickNotesContent(
                notes = tycoonNotes,
                onAddNote = { title, content, cat ->
                    viewModel.addNote(title, content, cat)
                },
                onDeleteNote = { id ->
                    viewModel.deleteNote(id)
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}

@Composable
fun TycoonQuickNotesContent(
    notes: List<com.example.data.local.entity.TycoonNote>,
    onAddNote: (String, String, String) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("BASE_COORDS") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "TYCOON MISSION LOG & COORDINATES",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CyanElectric
        )
        Text(
            text = "Quickly reference base coordinates and alliance trade deals while managing your empire",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Add Form
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title (e.g. Iron Smelter Outpost)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanElectric,
                unfocusedBorderColor = SpaceCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Details / Coordinates / Alliance Notes") },
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanElectric,
                unfocusedBorderColor = SpaceCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && content.isNotBlank()) {
                    onAddNote(title, content, category)
                    title = ""
                    content = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanElectric),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Coordinate Note", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing Notes List
        Text("SAVED LOGS (${notes.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Spacer(modifier = Modifier.height(6.dp))

        notes.forEach { note ->
            Surface(
                color = SpaceCardBg,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(note.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Text(note.content, color = TextSecondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = { onDeleteNote(note.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Delete",
                            tint = LossRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
