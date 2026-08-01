package com.dogon.vpn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dogon.vpn.R
import com.dogon.vpn.data.ConfigStore
import com.dogon.vpn.qr.GalleryQrDecoder
import com.dogon.vpn.qr.QrScanScreen
import com.dogon.vpn.ui.theme.*
import kotlinx.coroutines.launch

private enum class SetupTab { SCAN, GALLERY, TEXT }

@Composable
fun SetupScreen(onConfigSaved: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(SetupTab.SCAN) }
    var pastedText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val value = GalleryQrDecoder.decode(context, uri)
                if (value != null) {
                    ConfigStore.save(context, value)
                    onConfigSaved()
                } else {
                    error = "QR kod okunamadı, tekrar dene"
                }
            }
        }
    }

    fun saveAndContinue(text: String) {
        if (text.isBlank()) {
            error = "Bağlantı bilgisi boş olamaz"
            return
        }
        ConfigStore.save(context, text.trim())
        onConfigSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(20.dp)
    ) {
        Text(
            "DogonVPN'e Bağlan",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "QR tara, galeriden seç ya da bağlantı kodunu yapıştır",
            color = TextSecondary
        )
        Spacer(Modifier.height(20.dp))

        // Tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgCard)
                .padding(4.dp)
        ) {
            SetupTabChip("QR Tara", R.drawable.ic_qr, tab == SetupTab.SCAN, Modifier.weight(1f)) {
                tab = SetupTab.SCAN
                if (!hasCameraPermission) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            SetupTabChip("Galeri", R.drawable.ic_gallery, tab == SetupTab.GALLERY, Modifier.weight(1f)) {
                tab = SetupTab.GALLERY
            }
            SetupTabChip("Kod Gir", R.drawable.ic_keyboard, tab == SetupTab.TEXT, Modifier.weight(1f)) {
                tab = SetupTab.TEXT
            }
        }

        Spacer(Modifier.height(20.dp))

        when (tab) {
            SetupTab.SCAN -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                ) {
                    if (hasCameraPermission) {
                        QrScanScreen(modifier = Modifier.fillMaxSize()) { value ->
                            ConfigStore.save(context, value)
                            onConfigSaved()
                        }
                    } else {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Kamera izni gerekli", color = TextSecondary)
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }) { Text("İzin Ver") }
                        }
                    }
                }
            }

            SetupTab.GALLERY -> {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_gallery),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Galeriden QR Seç")
                    }
                }
            }

            SetupTab.TEXT -> {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = { Text("Bağlantı kodu / .conf içeriği") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { saveAndContinue(pastedText) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Kaydet ve Bağlan", fontWeight = FontWeight.SemiBold) }
                }
            }
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = AccentWarn)
        }
    }
}

@Composable
private fun SetupTabChip(
    label: String,
    icon: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AccentLive else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}
