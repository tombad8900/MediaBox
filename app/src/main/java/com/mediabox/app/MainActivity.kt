package com.mediabox.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediabox.core.downloader.FileDownloader
import com.mediabox.core.extractor.CompositeExtractor
import com.mediabox.core.model.MediaInfo
import com.mediabox.core.model.PlatformType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFF6366F1),
                        secondary = Color(0xFF10B981),
                        background = Color(0xFF0F172A),
                        surface = Color(0xFF1E293B)
                    )
                ) {
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
    val fileDownloader = remember { FileDownloader(context) }

    var urlText by remember { mutableStateOf("") }
    var detectedPlatform by remember { mutableStateOf(PlatformType.GENERIC) }
    var isLoading by remember { mutableStateOf(false) }
    var mediaInfoResult by remember { mutableStateOf<MediaInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var downloadingOptionId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadStatusText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📦 ميديا بوكس", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("| MediaBox", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
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
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تنزيل الوسائط المتعددة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { input ->
                            urlText = input
                            detectedPlatform = PlatformType.detectFromUrl(input)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("إلصق رابط الفيديو (YouTube, TikTok, FB, Telegram)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("المنصة المكتشفة: ", fontSize = 13.sp, color = Color.Gray)
                        AssistChip(
                            onClick = {},
                            label = { Text(detectedPlatform.displayName, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (detectedPlatform) {
                                    PlatformType.YOUTUBE -> Color(0xFFFF0000).copy(alpha = 0.2f)
                                    PlatformType.TIKTOK -> Color(0xFF00F2FE).copy(alpha = 0.2f)
                                    PlatformType.FACEBOOK -> Color(0xFF1877F2).copy(alpha = 0.2f)
                                    PlatformType.TELEGRAM -> Color(0xFF229ED9).copy(alpha = 0.2f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (urlText.isBlank()) {
                                Toast.makeText(context, "الرجاء إلصاق رابط أولاً", Toast.LENGTH_SHORT).show()
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
                                    errorMessage = err.localizedMessage ?: "فشل تحليل الرابط، تأكد من صحته"
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري فحص المقطع...")
                        } else {
                            Text("🔍 تحليل الرابط وعرض الخيارات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "❌ $error",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            mediaInfoResult?.let { info ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = info.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "👤 ${info.author}", fontSize = 13.sp, color = Color.LightGray)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "الجودات المتوفرة للتنزيل:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(info.options) { option ->
                                val isThisDownloading = downloadingOptionId == option.id

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Button(
                                        onClick = {
                                            if (downloadingOptionId != null) return@Button
                                            downloadingOptionId = option.id
                                            downloadProgress = 0
                                            downloadStatusText = "جاري الاتصال والتحميل..."

                                            scope.launch {
                                                val targetName = "MediaBox_${System.currentTimeMillis()}.${option.format}"
                                                val downloadRes = fileDownloader.downloadMediaFile(
                                                    mediaUrl = option.downloadUrl,
                                                    fileName = targetName,
                                                    mimeType = if (option.format == "mp3") "audio/mpeg" else "video/mp4"
                                                ) { progress ->
                                                    downloadProgress = progress
                                                    downloadStatusText = "تم تنزيل $progress%"
                                                }

                                                downloadingOptionId = null
                                                downloadRes.onSuccess { savedPath ->
                                                    downloadStatusText = "✅ تم حفظ الملف بنجاح في: $savedPath"
                                                    Toast.makeText(context, "✅ تم حفظ الفيديو بنجاح في Downloads/MediaBox", Toast.LENGTH_LONG).show()
                                                }.onFailure { err ->
                                                    downloadStatusText = "❌ فشل التنزيل: ${err.localizedMessage}"
                                                }
                                            }
                                        },
                                        enabled = downloadingOptionId == null,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⬇️ ${option.qualityLabel}", color = Color.White)
                                            Text(option.format.uppercase(), color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (isThisDownloading) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = downloadProgress / 100f,
                                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            downloadStatusText?.let { status ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "مسار الحفظ التلقائي عند التنزيل: Storage/Downloads/MediaBox/",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
