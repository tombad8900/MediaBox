package com.mediabox.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mediabox.core.model.PlatformType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
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
    var urlText by remember { mutableStateOf("") }
    var detectedPlatform by remember { mutableStateOf(PlatformType.GENERIC) }
    Scaffold(topBar = { TopAppBar(title = { Text("ميديا بوكس | MediaBox 📦") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("منزل الوسائط الشامل", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = urlText, onValueChange = { urlText = it; detectedPlatform = PlatformType.detectFromUrl(it) }, modifier = Modifier.fillMaxWidth(), label = { Text("إلصق رابط الفيديو هنا...") }, singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            Text("المنصة المكتشفة: ${detectedPlatform.displayName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("تحليل الرابط وعرض الخيارات") }
            Spacer(modifier = Modifier.height(24.dp))
            Text("مسار التنزيل التلقائي: Downloads/MediaBox/", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
