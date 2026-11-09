package com.mediabox.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mediabox.core.extractor.CompositeExtractor
import com.mediabox.core.model.MediaInfo
import com.mediabox.core.model.PlatformType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MediaBoxHomeScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaBoxHomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val compositeExtractor = remember { CompositeExtractor() }

    var urlText by remember { mutableStateOf("") }
    var detectedPlatform by remember { mutableStateOf(PlatformType.GENERIC) }
    var isLoading by remember { mutableStateOf(false) }
    var mediaInfoResult by remember { mutableStateOf<MediaInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ميديا بوكس | MediaBox 📦") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "منزل الوسائط الشامل",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = urlText,
                onValueChange = { input ->
                    urlText = input
                    detectedPlatform = PlatformType.detectFromUrl(input)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("إلصق رابط الفيديو من (YouTube, TikTok, FB, Telegram)...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "المنصة المكتشفة: ${detectedPlatform.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (urlText.isBlank()) {
                        Toast.makeText(context, "الرجاء إدخال رابط أولاً", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    mediaInfoResult = null

                    scope.launch {
                        val result = compositeExtractor.extract(urlText)
                        isLoading = false
                        result.onSuccess { info ->
                            mediaInfoResult = info
                        }.onFailure { err ->
                            errorMessage = err.localizedMessage ?: "حدث خطأ أثناء التحليل"
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري تحليل الرابط...")
                } else {
                    Text("تحليل الرابط وعرض خيارات التنزيل")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❌ $error",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            mediaInfoResult?.let { info ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "📌 ${info.title}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "👤 الناشر: ${info.author}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "الخيارات والجودات المتاحة:", style = MaterialTheme.typography.labelLarge)

                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(info.options) { option ->
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "جاري تنزيل: ${option.qualityLabel} في Downloads/MediaBox", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Text("⬇️ تنزيل ${option.qualityLabel} (${option.format.uppercase()})")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "مسار الحفظ التلقائي: Downloads/MediaBox/",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
