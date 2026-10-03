package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CyanElectric
import com.example.ui.theme.LossRed
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GalacticTycoonsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebGameScreen(
    viewModel: GalacticTycoonsViewModel,
    isFullscreen: Boolean = false,
    onToggleFullscreen: (() -> Unit)? = null
) {
    val webGameUrl by viewModel.webGameUrl.collectAsState()
    val isDesktopMode by viewModel.isDesktopMode.collectAsState()
    val tycoonNotes by viewModel.tycoonNotes.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showNotesSheet by remember { mutableStateOf(false) }
    val notesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto-reload when app is minimized (paused/stopped) and brought back to foreground (resumed)
    val lifecycleOwner = LocalLifecycleOwner.current
    var wasBackgrounded by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    wasBackgrounded = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (wasBackgrounded) {
                        wasBackgrounded = false
                        webViewInstance?.reload()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle device back gesture inside WebView
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    // Edge-to-edge game container with zero blank space at the top
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        // Fullscreen WebView
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

                    // Enable cookies & storage for persistent player login session
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // Performance & game-optimized viewport settings
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
                webViewInstance = wv
            }
        )

        // Thin loading indicator flush with top edge
        if (isLoading && webProgress < 1.0f) {
            LinearProgressIndicator(
                progress = { webProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter),
                color = CyanElectric,
                trackColor = Color.Transparent
            )
        }

        // Connection Error Recovery View
        if (hasError) {
            Surface(
                color = SpaceDark,
                modifier = Modifier.fillMaxSize()
            ) {
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
                        textAlign = TextAlign.Center
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
        }

        // Unobtrusive floating HUD overlay in the top-right corner
        // (Does NOT push the game down or cause any blank space)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = Color(0xB30B101D),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Notes drawer button
                    IconButton(
                        onClick = { showNotesSheet = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Quick Notes",
                            tint = CyanElectric,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Fullscreen toggle button
                    if (onToggleFullscreen != null) {
                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                tint = CyanElectric,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
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
                            imageVector = Icons.Default.Delete,
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
