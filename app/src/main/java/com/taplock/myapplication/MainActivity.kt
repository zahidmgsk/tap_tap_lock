package com.taplock.myapplication

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
    private var showDisclosure by mutableStateOf(false)

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
            var transparency by remember { mutableStateOf(prefs.getInt("widget_transparency", 255)) }

            MyApplicationTheme(seedColor = currentThemeColor, themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Dashboard(
                        isEnabled = isServiceEnabled,
                        currentThemeColor = currentThemeColor,
                        themeMode = themeMode,
                        transparency = transparency,
                        onThemeChange = { newColor ->
                            currentThemeColor = newColor
                            prefs.edit().putInt("app_theme_color", newColor.toArgb()).apply()
                            updateAllWidgets(context)
                        },
                        onModeChange = { newMode ->
                            themeMode = newMode
                            prefs.edit().putString("theme_mode", newMode).apply()
                        },
                        onTransparencyChange = { newTransparency ->
                            transparency = newTransparency
                            prefs.edit().putInt("widget_transparency", newTransparency).apply()
                            updateAllWidgets(context)
                        },
                        onToggleClick = { 
                            if (!isServiceEnabled) {
                                showDisclosure = true 
                            } else {
                                toggleAccessibilityService()
                            }
                        },
                        onTestClick = { testLock() }
                    )

                    if (showDisclosure) {
                        AlertDialog(
                            onDismissRequest = { showDisclosure = false },
                            title = { Text("Accessibility Permission") },
                            text = { 
                                Text("This app requires Accessibility Service permission to perform the 'Screen Lock' action. \n\n" +
                                     "• This is used ONLY to lock the screen.\n" +
                                     "• No personal data is collected.\n\n" +
                                     "⚠️ NOTE: If the setting is 'Restricted' (greyed out), go to Phone Settings > Apps > tap tap lock > tap 3 dots (⋮) > 'Allow restricted settings'.")
                            },
                            confirmButton = {
                                Button(onClick = {
                                    showDisclosure = false
                                    toggleAccessibilityService()
                                }) {
                                    Text("Continue to Settings")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDisclosure = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isServiceEnabled = isAccessibilityServiceEnabled(this, LockAccessibilityService::class.java)
    }

    private fun toggleAccessibilityService() {
        if (isServiceEnabled) {
            // Service is enabled, disable it directly with one tap
            val intent = Intent(this, LockAccessibilityService::class.java).apply {
                action = LockAccessibilityService.ACTION_DISABLE
            }
            startService(intent)
            android.widget.Toast.makeText(this, "❌ Accessibility Disabled", android.widget.Toast.LENGTH_SHORT).show()
            // The service will call disableSelf(), we update UI after a tiny delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isServiceEnabled = isAccessibilityServiceEnabled(this, LockAccessibilityService::class.java)
            }, 500)
        } else {
            // Service is disabled, must go to settings to enable (Android security requirement)
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                android.widget.Toast.makeText(this, "Find 'tap tap lock' and toggle it ON", android.widget.Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }

    private fun testLock() {
        val intent = Intent(this, LockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, service).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(expectedComponentName) == true
    }

    private fun updateAllWidgets(context: Context) {
        val widgetClasses = listOf(
            LockWidget::class.java,
            LockWidgetMin::class.java,
            LockWidgetText::class.java,
            LockWidgetTile::class.java,
            NothingWidget::class.java,
            DisableWidget::class.java,
            CombinedWidget::class.java,
            CombinedWidgetVertical::class.java
        )
        for (widgetClass in widgetClasses) {
            val intent = Intent(context, widgetClass).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, widgetClass))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    isEnabled: Boolean,
    currentThemeColor: Color,
    themeMode: String,
    transparency: Int,
    onThemeChange: (Color) -> Unit,
    onModeChange: (String) -> Unit,
    onTransparencyChange: (Int) -> Unit,
    onToggleClick: () -> Unit,
    onTestClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE) }
    var isDoubleTap by remember { mutableStateOf(prefs.getBoolean("double_tap_enabled", false)) }
    val scrollState = rememberScrollState()
    var showThemeSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    val themeSheetState = rememberModalBottomSheetState()
    val aboutSheetState = rememberModalBottomSheetState()

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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // About Button
                SmallFloatingActionButton(
                    onClick = { showAboutSheet = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Text("?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                // Theme Button
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
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showThemeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showThemeSheet = false },
                sheetState = themeSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                ThemeSettingsContent(
                    currentSelectedColor = currentThemeColor,
                    onColorSelected = { onThemeChange(it) },
                    themeMode = themeMode,
                    onModeChange = onModeChange,
                    transparency = transparency,
                    onTransparencyChange = onTransparencyChange
                )
            }
        }

        if (showAboutSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAboutSheet = false },
                sheetState = aboutSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                AboutSheetContent()
            }
        }
    }
}

@Composable
fun AboutSheetContent() {
    var showDonateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val version = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp, top = 16.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "About tap tap lock",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Developed by Zahid Choudhry",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Version $version",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "A lightweight utility to lock your screen with a simple tap while keeping biometrics active. No data collection, fully offline.",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text("Support & Donate", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            SupportButton(
                name = "Payoneer",
                color = Color(0xFFFF9800),
                onClick = { showDonateDialog = true }
            )

            SupportButton(
                name = "JazzCash",
                color = Color(0xFFFFCC00),
                textColor = Color.Black,
                onClick = { showDonateDialog = true }
            )
        }

        if (showDonateDialog) {
            AlertDialog(
                onDismissRequest = { showDonateDialog = false },
                title = { Text("Donate & Support") },
                text = { Text("Thank you for your support! Details for Payoneer and JazzCash will be added in a future update.") },
                confirmButton = {
                    TextButton(onClick = { showDonateDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeSettingsContent(
    currentSelectedColor: Color,
    onColorSelected: (Color) -> Unit,
    themeMode: String,
    onModeChange: (String) -> Unit,
    transparency: Int,
    onTransparencyChange: (Int) -> Unit
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

        // Widget Transparency
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Widget Transparency", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Slider(
                value = transparency.toFloat(),
                onValueChange = { onTransparencyChange(it.toInt()) },
                valueRange = 0f..255f,
                steps = 25
            )
            Text(
                text = "${(transparency / 255f * 100).toInt()}% Opacity",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
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
            Text("Usage Guide & Privacy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "1. Enable the service using the button above.\n" +
                "2. Add the 'tap tap lock' widget to your home screen.\n" +
                "3. ${if (isDoubleTap) "Double tap" else "Single tap"} the widget to lock instantly.\n\n" +
                "🔒 Privacy: This app is offline and does not collect any data. Accessibility is used only to trigger the system lock.",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SupportButton(
    name: String,
    color: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = Modifier.height(48.dp).width(120.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name,
                color = textColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
