package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BlackoutEyeShield
import com.example.ui.screens.ContentFilterScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ParentSettingsScreen
import com.example.ui.screens.ScreenTimeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KidSafeViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Bảo Vệ Mắt", Icons.Filled.Visibility, Icons.Outlined.Visibility, "tab_home"),
    SCREEN_TIME("Thời Gian", Icons.Filled.Timer, Icons.Outlined.Timer, "tab_time"),
    CONTENT_FILTER("Lọc Web", Icons.Filled.Security, Icons.Outlined.Security, "tab_filter"),
    PARENT_SETTINGS("Phụ Huynh", Icons.Filled.Lock, Icons.Outlined.Lock, "tab_parent")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KidSafeApp()
            }
        }
    }
}

@Composable
fun KidSafeApp(viewModel: KidSafeViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val distanceState by viewModel.distanceState.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val isParentUnlocked by viewModel.isParentUnlocked.collectAsState()
    val isInAppBlackout by viewModel.isInAppBlackout.collectAsState()
    val blackoutReason by viewModel.blackoutReason.collectAsState()
    val currentDistanceForBlackout by viewModel.currentDistanceForBlackout.collectAsState()
    val eyeRestSeconds by viewModel.eyeRestTimerSeconds.collectAsState()

    val activity = LocalActivity.current
    var selectedTabIndex by remember {
        val initialTab = if (activity?.intent?.getStringExtra("OPEN_TAB") == "SCREEN_TIME") 1 else 0
        mutableIntStateOf(initialTab)
    }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission granted or denied */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // If terms not yet accepted, show onboarding & consent screen first
    if (!settings.isTermsAccepted) {
        OnboardingScreen(
            onComplete = { pin ->
                viewModel.acceptTermsAndStartGuard(pin)
            }
        )
        return
    }

    // Handle back button on secondary tabs
    if (selectedTabIndex != 0) {
        BackHandler {
            selectedTabIndex = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    MainTab.values().forEachIndexed { index, tab ->
                        val isSelected = selectedTabIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTabIndex) {
                    0 -> HomeScreen(
                        settings = settings,
                        distanceState = distanceState,
                        logs = logs,
                        onToggleGuard = { viewModel.toggleGuard(it) },
                        onSimulateDistance = { viewModel.simulateDistance(it) },
                        onTriggerTestBlackout = { viewModel.triggerTestBlackout() },
                        onUpdateSettings = { viewModel.updateSettings(it) }
                    )
                    1 -> ScreenTimeScreen(
                        settings = settings,
                        eyeRestSeconds = eyeRestSeconds,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onResetDailyTime = { viewModel.resetDailyTime() },
                        onVerifyPin = { viewModel.verifyPin(it) },
                        onTest80PercentAlert = { viewModel.testSend80PercentAlert() },
                        onSimulateScreenTime = { viewModel.simulateScreenTime(it) }
                    )
                    2 -> ContentFilterScreen(
                        settings = settings,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onCheckSafety = { viewModel.checkUrlOrContent(it) },
                        onAddBlockedUrl = { viewModel.addBlockedUrl(it) },
                        onRemoveBlockedUrl = { viewModel.removeBlockedUrl(it) },
                        onAddBlockedKeyword = { viewModel.addBlockedKeyword(it) },
                        onRemoveBlockedKeyword = { viewModel.removeBlockedKeyword(it) }
                    )
                    3 -> ParentSettingsScreen(
                        settings = settings,
                        logs = logs,
                        isParentUnlocked = isParentUnlocked,
                        onUnlockParent = { viewModel.unlockParent(it) },
                        onLockParent = { viewModel.lockParent() },
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onClearLogs = { viewModel.clearAllLogs() }
                    )
                }
            }
        }

        // Full Screen Blackout Shield when child is viewing too close or screen time runs out
        AnimatedVisibility(
            visible = isInAppBlackout,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BlackoutEyeShield(
                reason = blackoutReason,
                currentDistanceCm = currentDistanceForBlackout,
                safeThresholdCm = settings.safeDistanceCm,
                onDismissWithPin = { pin ->
                    viewModel.dismissBlackoutWithPin(pin)
                },
                onSimulatePushAway = {
                    viewModel.simulateDistance(38)
                }
            )
        }
    }
}
