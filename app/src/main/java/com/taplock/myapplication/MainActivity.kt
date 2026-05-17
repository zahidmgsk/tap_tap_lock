package com.taplock.myapplication

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taplock.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private var isServiceEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE) }
            var currentThemeColor by remember { 
                val savedColor = prefs.getInt("app_theme_color", Color(0xFF6750A4).toArgb())
                mutableStateOf(Color(savedColor))
            }
            var themeMode by remember { mutableStateOf(prefs.getString("theme_mode", "System") ?: "System") }

            MyApplicationTheme(seedColor = currentThemeColor, themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Dashboard(
                        isEnabled = isServiceEnabled,
                        currentThemeColor = currentThemeColor,
                        themeMode = themeMode,
                        onThemeChange = { newColor ->
                            currentThemeColor = newColor
                            prefs.edit().putInt("app_theme_color", newColor.toArgb()).apply()
                        },
                        onModeChange = { newMode ->
                            themeMode = newMode
                            prefs.edit().putString("theme_mode", newMode).apply()
                        },
                        onToggleClick = { openAccessibilitySettings() },
                        onTestClick = { testLock() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isServiceEnabled = isAccessibilityServiceEnabled(this, LockAccessibilityService::class.java)
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            android.widget.Toast.makeText(this, "Find 'tap tap lock' and toggle it", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun testLock() {
        val intent = Intent(this, LockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in enabledServices) {
            if (info.resolveInfo.serviceInfo.packageName == context.packageName &&
                info.resolveInfo.serviceInfo.name == service.name) {
                return true
            }
        }
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    isEnabled: Boolean,
    currentThemeColor: Color,
    themeMode: String,
    onThemeChange: (Color) -> Unit,
    onModeChange: (String) -> Unit,
    onToggleClick: () -> Unit,
    onTestClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE) }
    var isDoubleTap by remember { mutableStateOf(prefs.getBoolean("double_tap_enabled", false)) }
    val scrollState = rememberScrollState()
    var showThemeSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Small "Tap" Icon
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Transparent, CircleShape)
                                    .padding(2.dp)
                                    .then(Modifier.background(Color.Transparent, CircleShape)) // Just decorative
                                    .padding(2.dp)
                            )
                            // Outer ring
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary)
                            ) {}
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            "tap tap lock", 
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showThemeSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    Box(modifier = Modifier.size(10.dp).align(Alignment.TopStart).background(Color.Red, CircleShape))
                    Box(modifier = Modifier.size(10.dp).align(Alignment.TopEnd).background(Color.Green, CircleShape))
                    Box(modifier = Modifier.size(10.dp).align(Alignment.BottomCenter).background(Color.Blue, CircleShape))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Status Section
            StatusCard(isEnabled)

            // Quick Actions
            ActionButtons(isEnabled, onToggleClick, onTestClick)

            // Settings Section
            SettingsSection(
                isDoubleTap = isDoubleTap,
                onDoubleTapChange = { 
                    isDoubleTap = it
                    prefs.edit().putBoolean("double_tap_enabled", it).apply()
                }
            )

            // Help Section
            HelpCard(isDoubleTap)
            
            // Padding to avoid FAB overlap
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showThemeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showThemeSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                ThemeSettingsContent(
                    currentSelectedColor = currentThemeColor,
                    onColorSelected = { onThemeChange(it) },
                    themeMode = themeMode,
                    onModeChange = onModeChange
                )
            }
        }
    }
}

@Composable
fun ThemeSettingsContent(
    currentSelectedColor: Color,
    onColorSelected: (Color) -> Unit,
    themeMode: String,
    onModeChange: (String) -> Unit
) {
    val colors = listOf(
        Color(0xFF6750A4), Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
        Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4),
        Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A),
        Color(0xFFCDDC39), Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800),
        Color(0xFFFF5722), Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B),
        Color(0xFF000000), Color(0xFF333333), Color(0xFF666666), Color(0xFF999999)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp, top = 16.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Theme Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Display Mode Selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Display Mode", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ModeOption("Light", themeMode == "Light") { onModeChange("Light") }
                ModeOption("Dark", themeMode == "Dark") { onModeChange("Dark") }
                ModeOption("System", themeMode == "System") { onModeChange("System") }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Color Palette
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("App Color", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 56.dp),
                modifier = Modifier.height(220.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(color) }
                            .padding(4.dp)
                            .then(
                                if (currentSelectedColor == color) {
                                    Modifier.background(Color.White.copy(alpha = 0.4f), CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentSelectedColor == color) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(isEnabled: Boolean) {
    val statusColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336),
        label = "statusColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Service Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnabled) "Active & Running" else "Currently Disabled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(statusColor, CircleShape)
            )
        }
    }
}

@Composable
fun ActionButtons(isEnabled: Boolean, onToggleClick: () -> Unit, onTestClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onToggleClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isEnabled) "DISABLE FOR BANKING" else "ENABLE POWER KEY")
        }

        OutlinedButton(
            onClick = onTestClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = isEnabled
        ) {
            Text("TEST LOCK NOW")
        }
    }
}

@Composable
fun SettingsSection(
    isDoubleTap: Boolean,
    onDoubleTapChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("App Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            // Double Tap Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Double Tap to Lock", fontWeight = FontWeight.SemiBold)
                    Text("Prevent accidental locks", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isDoubleTap, onCheckedChange = onDoubleTapChange)
            }
        }
    }
}

@Composable
fun ModeOption(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.height(40.dp).width(80.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(name, color = textColor, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun HelpCard(isDoubleTap: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Usage Guide", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "1. Enable the service using the button above.\n" +
                "2. Add the 'tap tap lock' widget to your home screen.\n" +
                "3. ${if (isDoubleTap) "Double tap" else "Single tap"} the widget to lock instantly.",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
    }
}
