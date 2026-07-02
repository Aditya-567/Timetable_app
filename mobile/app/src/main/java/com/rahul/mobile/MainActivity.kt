package com.rahul.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.rahul.mobile.data.AppPreferences
import com.rahul.mobile.data.TimetableEvent
import com.rahul.mobile.data.TimetableItem
import com.rahul.mobile.notifications.ClassReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.compose.material3.Divider
import androidx.compose.foundation.layout.IntrinsicSize
import coil.compose.AsyncImage 
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Emerald900 = Color(0xFF2A221F)
private val Emerald700 = Color(0xFFED153E)
val Emerald800 = Color(0xFFC81D11)
private val Emerald500 = Color(0xFFFB503F)
private val SlateBg = Color(0xFFffffff)
private val CardWhite = Color(0xFFFFFFFF)
private val MutedText = Color(0xFF6B7280)
private val AccentBlack = Color(0xFF0C0C0C)
private val AccentOrange = Color(0xFFE05302)
private val AccentBlue = Color(0xFF3B41BF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppEntry()
            }
        }
    }
}

private enum class AppScreen {
    Onboarding,
    Home,
    Profile,
    ShowAllFilters
}

private enum class HomeTab {
    Classes,
    Upcoming,
    Canceled
}

private data class ToastData(
    val message: String,
    val isError: Boolean,
    val id: Long = System.nanoTime()
)

@Composable
private fun BottomLeftToast(toast: ToastData, onDismiss: () -> Unit) {
    LaunchedEffect(toast.id) {
        delay(2500)
        onDismiss()
    }
    val accentColor = if (toast.isError) Color(0xFFDC2626) else Color(0xFF16A34A)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (toast.isError) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = toast.message,
                color = Color(0xFF1F2937),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDismiss)
            )
        }
    }
}

@Composable
private fun AppEntry(vm: TimetableViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    var profile by remember { mutableStateOf(prefs.getProfile()) }
    var savedFilters by remember { mutableStateOf(prefs.getFilters()) }
    var notificationSettings by remember { mutableStateOf(prefs.getNotificationSettings()) }

    var currentScreen by remember {
        mutableStateOf(if (prefs.isOnboardingCompleted()) AppScreen.Home else AppScreen.Onboarding)
    }

    var toast by remember { mutableStateOf<ToastData?>(null) }
    val onShowToast: (String, Boolean) -> Unit = { message, isError ->
        toast = ToastData(message, isError)
    }

    val state by vm.uiState.collectAsState()

    val sectionOptions = remember(state.items) {
        listOf(AppPreferences.FILTER_ALL) + state.items
            .map { extractSectionFilter(it.section) }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    var workingFilter by remember(sectionOptions, savedFilters.sectionFilter) {
        mutableStateOf(
            if (savedFilters.sectionFilter.uppercase() in sectionOptions) {
                savedFilters.sectionFilter.uppercase()
            } else {
                AppPreferences.FILTER_ALL.uppercase()
            }
        )
    }

    LaunchedEffect(savedFilters.sectionFilter, sectionOptions) {
        val normalized = savedFilters.sectionFilter.uppercase()
        workingFilter = if (normalized in sectionOptions) normalized else AppPreferences.FILTER_ALL.uppercase()
    }

    LaunchedEffect(state.items, notificationSettings) {
        ClassReminderScheduler.syncReminders(context, state.items, notificationSettings)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.Onboarding -> OnboardingScreen(
                initialName = profile.fullName,
                initialPhotoUri = profile.photoUri,
                onSubmit = { fullName, photoUri ->
                    prefs.saveProfile(fullName, photoUri)
                    profile = prefs.getProfile()
                    currentScreen = AppScreen.Home
                }
            )

            AppScreen.Home -> HomeScreen(
                vm = vm,
                profileName = profile.fullName,
                profilePhotoUri = profile.photoUri,
                selectedSection = workingFilter,
                notificationSettings = notificationSettings,
                onUpdateNotificationSettings = { settings ->
                    notificationSettings = settings
                    prefs.saveNotificationSettings(settings)
                },
                onSectionSelected = { section ->
                    workingFilter = section
                    prefs.saveFilters(AppPreferences.FILTER_ALL, section.lowercase())
                    savedFilters = prefs.getFilters()
                },
                onOpenProfile = { currentScreen = AppScreen.Profile },
                // Removed onOpenAllEvents from here
                onShowToast = onShowToast
            )

            AppScreen.Profile -> ProfileScreen(
                initialName = profile.fullName,
                initialPhotoUri = profile.photoUri,
                sectionOptions = sectionOptions,
                notificationSettings = notificationSettings,
                onBack = { currentScreen = AppScreen.Home },
                onSave = { fullName, photoUri ->
                    prefs.saveProfile(fullName, photoUri)
                    profile = prefs.getProfile()
                    currentScreen = AppScreen.Home
                },
                onSaveNotificationSettings = { settings ->
                    notificationSettings = settings
                    prefs.saveNotificationSettings(settings)
                },
                onShowToast = onShowToast
            )

            AppScreen.ShowAllFilters -> ShowAllFilterScreen(
                current = workingFilter,
                allOptions = sectionOptions,
                onBack = { currentScreen = AppScreen.Home },
                onApply = { section ->
                    workingFilter = section
                    prefs.saveFilters(AppPreferences.FILTER_ALL, section.lowercase())
                    savedFilters = prefs.getFilters()
                    currentScreen = AppScreen.Home
                }
            )
        }

        toast?.let { current ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                BottomLeftToast(toast = current, onDismiss = { toast = null })
            }
        }
    }
}

@Composable
private fun WeatherForecastCard(temp: String, rainChance: String, condition: String, message: String) {
    val conditionLower = condition.lowercase(Locale.ENGLISH)
    
    // 1. Map the condition string from the API to your local big image files
    val iconPath = when {
        conditionLower.contains("clear") || conditionLower.contains("sun") -> "file:///android_asset/images/sun.png"
        conditionLower.contains("thunder") || conditionLower.contains("storm") -> "file:///android_asset/images/thunder.png"
        conditionLower.contains("rain") || conditionLower.contains("drizzle") -> "file:///android_asset/images/rainy.png"
        else -> "file:///android_asset/images/cloudy.png"
    }

    // 2. Set dynamic colors and icons for the Condition "Pill" Chip
    val chipBgColor: Color
    val chipTextColor: Color
    val chipIcon: ImageVector

    when {
        conditionLower.contains("clear") || conditionLower.contains("sun") -> {
            chipBgColor = Color(0xFFFFF7ED) // Light Orange
            chipTextColor = Color(0xFFEA580C) // Dark Orange
            chipIcon = Icons.Default.WbSunny
        }
        conditionLower.contains("thunder") || conditionLower.contains("storm") -> {
            chipBgColor = Color(0xFFF3E8FF) // Light Purple
            chipTextColor = Color(0xFF7E22CE) // Dark Purple
            chipIcon = Icons.Default.Warning 
        }
        conditionLower.contains("rain") || conditionLower.contains("drizzle") -> {
            chipBgColor = Color(0xFFEFF6FF) // Light Blue
            chipTextColor = Color(0xFF2563EB) // Dark Blue
            chipIcon = Icons.Default.Cloud 
        }
        else -> {
            chipBgColor = Color(0xFFF0F9FF) // Sky Blue (Matches your screenshot)
            chipTextColor = Color(0xFF0284C7) // Dark Sky Blue
            chipIcon = Icons.Default.Cloud
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
            shape = RoundedCornerShape(16.dp), // 1. Defines how round the corners are
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)), // 2. Adds a subtle light-gray border
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dynamic Big Weather Image (Left side)
            AsyncImage(
                model = iconPath,
                contentDescription = condition,
                modifier = Modifier
                    .size(72.dp)
                    .padding(end = 16.dp)
            )
            
            // Weather Details (Right side)
            Column {
                // Top Row: Temp + Rain Chance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = temp, 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 26.sp, 
                            color = AccentBlack,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50)) // Makes it perfectly pill-shaped
                        .background(chipBgColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = chipIcon,
                        contentDescription = null,
                        tint = chipTextColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = condition, 
                        color = chipTextColor, 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                }
                
                
                Spacer(Modifier.height(2.dp))
                
                // Bottom Row: Message
                Text(
                    text = message, 
                    fontSize = 13.sp, 
                    color = AccentBlack,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    vm: TimetableViewModel,
    profileName: String,
    profilePhotoUri: String?,
    selectedSection: String,
    notificationSettings: AppPreferences.NotificationSettings,
    onUpdateNotificationSettings: (AppPreferences.NotificationSettings) -> Unit,
    onSectionSelected: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onShowToast: (String, Boolean) -> Unit
) {
    val state by vm.uiState.collectAsState()
    var showUpcoming by remember { mutableStateOf(true) }
    var selectedHomeTab by remember { mutableStateOf(HomeTab.Classes) }
    var showNotificationPopup by remember { mutableStateOf(false) }
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    var hasSyncedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(state.loading) {
        if (!state.loading) {
            if (hasSyncedOnce) {
                if (state.error != null) {
                    onShowToast("Error: ${state.error}", true)
                } else {
                    onShowToast("Synced successfully", false)
                }
            }
            hasSyncedOnce = true
        }
    }

    val sectionFilters = remember(state.items) {
        state.items
            .map { extractSectionFilter(it.section) }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    val visibleItems = remember(state.items, selectedSection) {
        state.items.filter { item ->
            // Check if the item is a global event (applies to all sections)
            val isGlobalEvent = item.courseName.equals("Lunch Break", ignoreCase = true) || 
                                item.courseName.contains("Blocked", ignoreCase = true) ||
                                item.originalCode.equals("LUNCH", ignoreCase = true)

            selectedSection == AppPreferences.FILTER_ALL.uppercase() ||
                isGlobalEvent || // Always allow global events through the filter
                extractSectionFilter(item.section).equals(selectedSection, ignoreCase = true)
        }
    }
    val cancelledItems = remember(state.items) { state.items.filter { it.isCancelled } }
    val allUpcomingEvents = remember(state.events, visibleItems) {
        sheetUpcomingEvents(state.events).ifEmpty { fakeUpcomingEvents(visibleItems).ifEmpty { fallbackEvents() } }
    }
    
    val upcomingEvents = remember(allUpcomingEvents) {
        allUpcomingEvents.take(1) // CHANGED: Only take 1 recent upcoming event
    }
    val classDates = remember(state.items) {
        val parsedDates = state.items.mapNotNull { parseDate(it.date) }.distinct().sorted()
        if (parsedDates.isEmpty()) return@remember emptyList<LocalDate>()
        
        val minDate = parsedDates.first()
        val maxDate = parsedDates.last()
        
        val continuousDates = mutableListOf<LocalDate>()
        var current = minDate
        while (!current.isAfter(maxDate)) {
            continuousDates.add(current)
            current = current.plusDays(1)
        }
        continuousDates
    }

    val dateListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedClassDate by remember(classDates) {
        mutableStateOf(classDates.firstOrNull { it == LocalDate.now() } ?: classDates.firstOrNull())
    }
    LaunchedEffect(classDates) {
        if (classDates.isEmpty()) {
            selectedClassDate = null
        } else {
            val todayDate = classDates.firstOrNull { it == LocalDate.now() } ?: classDates.first()
            selectedClassDate = todayDate
            
            // Find index of today and jump to it
            val todayIndex = classDates.indexOf(todayDate)
            if (todayIndex >= 0) {
                dateListState.scrollToItem(todayIndex)
            }
        }
    }
    val selectedDateItems = remember(visibleItems, selectedClassDate) {
        val picked = selectedClassDate ?: return@remember emptyList()
        visibleItems
            .filter { parseDate(it.date) == picked }
            .sortedWith(compareBy({ parseStartTime(it.time) ?: LocalTime.MAX }, { it.courseName }))
    }
    val selectedDateCancelledItems = remember(cancelledItems, selectedClassDate) {
        val picked = selectedClassDate ?: return@remember emptyList()
        cancelledItems.filter { parseDate(it.date) == picked }
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::sync,
                containerColor = Emerald700,
                contentColor = Color.White
            ) {
                RefreshIcon(spinning = state.loading)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        brush = Brush.verticalGradient(listOf(Emerald900, Emerald700)),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = timeBasedGreeting(),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                 
                                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = if (profileName.isBlank()) "User" else profileName,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    shadow = Shadow(
                                        color = Emerald900.copy(alpha = 0.9f),
                                        offset = Offset(0f, 3f),
                                        blurRadius = 8f
                                    )
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 0.4.sp
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { showNotificationPopup = true }
                                )
                                
                            }
                            ProfileImage(photoUri = profilePhotoUri, size = 36, onClick = onOpenProfile, showBorder = true)
                        }
                    }

                    Spacer(Modifier.height(64.dp))

                    HomeTabSwitcher(
                        selectedTab = selectedHomeTab,
                        onTabSelected = { selectedHomeTab = it }
                    )

                    Spacer(Modifier.height(12.dp))

                    when (selectedHomeTab) {
                        HomeTab.Classes -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFffffff))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SECTION AND BATCH",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlack,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Viewing: ${selectedSection.uppercase(Locale.ENGLISH)}",
                                               fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlack,
                                            style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }

                                    // 1. Create a scroll state to control the LazyRow
                                    val filterListState = rememberLazyListState()

                                    // 2. Automatically animate scroll to the far left (index 0) whenever selection changes
                                    LaunchedEffect(selectedSection) {
                                        filterListState.animateScrollToItem(0)
                                    }

                                    val allLabel = AppPreferences.FILTER_ALL.uppercase()
                                    val allSections = listOf(allLabel) + sectionFilters.filter { it != allLabel }
                                    
                                    val displaySections = remember(allSections, selectedSection) {
                                        val selectedItem = allSections.firstOrNull { it.equals(selectedSection, ignoreCase = true) }
                                        if (selectedItem != null) {
                                            listOf(selectedItem) + allSections.filter { it != selectedItem }
                                        } else {
                                            allSections
                                        }
                                    }

                                    LazyRow(
                                        state = filterListState, // 3. ATTACH THE STATE HERE
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        items(displaySections, key = { it }) { label ->
                                            val isSelected = selectedSection.equals(label, ignoreCase = true)
                                            SectionFilterButton(
                                                modifier = Modifier, 
                                                label = label,
                                                selected = isSelected,
                                                onClick = { onSectionSelected(label) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HomeTab.Upcoming -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),

                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "UPCOMING EVENTS",
                                            color = Color.White.copy(alpha = 0.96f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        TextButton(onClick = { showUpcoming = !showUpcoming }) {
                                            Icon(
                                                imageVector = if (showUpcoming) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (showUpcoming) "Hide upcoming events" else "Show upcoming events",
                                                tint = Color.White.copy(alpha = 0.96f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = if (showUpcoming) "Hide" else "Show",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = 0.96f),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    if (showUpcoming) {
                                        upcomingEvents.forEachIndexed { index, event ->
                                            val badgeColor = if (index == 0) Color(0xFF0B7A4D) else AccentOrange
                                            val dayText = event.dateLabel.take(2).trim()
                                            val monthText = event.dateLabel.split(" ").getOrNull(1)?.replace(",", "")?.uppercase() ?: "JUL"

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(18.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFffffff)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFffffff))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(badgeColor.copy(alpha = 0.12f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text(dayText, color = badgeColor, fontWeight = FontWeight.Bold)
                                                            Text(monthText, color = badgeColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(event.title, fontWeight = FontWeight.Bold, color = Color(0xFF333333) ,
                                                            fontSize = 14.sp,
                                                            style = MaterialTheme.typography.titleSmall,)
                                                        Text(event.dateLabel, color = MutedText,
                                                            fontSize = 10.sp,
                                                            style = MaterialTheme.typography.titleSmall,)
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .height(42.dp)
                                                            .width(1.dp)
                                                            .background(Color(0xFFE5E7EB))
                                                    )

                                                    Column(
                                                        modifier = Modifier.padding(end = 13.dp),
                                                        horizontalAlignment = Alignment.End) {
                                                        Text("${event.daysRemaining} days", color = badgeColor, fontWeight = FontWeight.Bold,
                                                         fontSize = 14.sp,
                                                            style = MaterialTheme.typography.titleSmall,)
                                                        Text("remaining", color = MutedText, fontSize = 10.sp,
                                                            style = MaterialTheme.typography.titleSmall,)
                                                    }
                                                }
                                            }
                                        }

                                        
                                    }
                                }
                            }
                        }

                        HomeTab.Canceled -> {}
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedHomeTab) {
                    HomeTab.Classes -> {
                        item {
                            state.weather?.let { weatherData ->
                                WeatherForecastCard(
                                    temp = weatherData.temp,
                                    rainChance = weatherData.rainChance,
                                    condition = weatherData.condition,
                                    message = weatherData.message
                                )
                            }
                        }

                        when {
                            state.error != null -> item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = state.error ?: "Unknown error",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            classDates.isEmpty() -> item { EmptyStateCard() }
                            else -> {
                                item {
                                    DateScrollerWithReset(
                                        dates = classDates,
                                        selectedDate = selectedClassDate,
                                        listState = dateListState,
                                        coroutineScope = coroutineScope,
                                        onSelectDate = { selectedClassDate = it }
                                    )
                                }

                                // 1. Filter out Lunch/Blocked items to see if there are any *actual* classes
                                val regularClasses = selectedDateItems.filter { 
                                    !it.courseName.equals("Lunch Break", ignoreCase = true) && 
                                    !it.courseName.contains("Blocked", ignoreCase = true) 
                                }

                                // 2. Logic: If there are no regular classes, show empty.png
                                // This will hide the Lunch Break on days where there is no actual class data.
                                if (regularClasses.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 34.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            AsyncImage(
                                                model = "file:///android_asset/images/empty.png",
                                                contentDescription = "No Classes",
                                                modifier = Modifier.size(260.dp)
                                            )
                                            Text(
                                                text = "No classes scheduled",
                                                color = MutedText,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                } else {
                                    // 3. Show the timeline only if there are actual classes
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                                        ) {
                                            Column {
                                                selectedDateItems.forEachIndexed { index, item ->
                                                    ClassTimelineRow(
                                                        item = item,
                                                        isLast = index == selectedDateItems.lastIndex
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }   
                            }
                        }
                    }

             HomeTab.Upcoming -> {
                        
                        // Removed the "Upcoming Event / View all" header Row

                        items(allUpcomingEvents) { event ->
                            EventRowCard(event)
                        }
                        
                        // Added graphic footer
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = "file:///android_asset/images/empty.png",
                                    contentDescription = "No more events",
                                    modifier = Modifier.size(200.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Stay tuned for more events!",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlack,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    HomeTab.Canceled -> {
                        item {
                            state.weather?.let { weatherData ->
                                WeatherForecastCard(
                                    temp = weatherData.temp,
                                    rainChance = weatherData.rainChance,
                                    condition = weatherData.condition,
                                    message = weatherData.message
                                )
                            }
                        }

                        if (classDates.isNotEmpty()) {
                            item {
                                DateScrollerWithReset(
                                    dates = classDates,
                                    selectedDate = selectedClassDate,
                                    listState = dateListState,
                                    coroutineScope = coroutineScope,
                                    onSelectDate = { selectedClassDate = it }
                                )
                            }
                        }

                        if (selectedDateCancelledItems.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 34.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    AsyncImage(
                                        model = "file:///android_asset/images/cancel.png",
                                        contentDescription = "No canceled classes",
                                        modifier = Modifier.size(260.dp)
                                    )
                                    Text(
                                        text = "No canceled classes",
                                        color = MutedText,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            items(selectedDateCancelledItems) { item ->
                                TimetableRowCard(item = item)
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.loading,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Emerald700,
                trackColor = Color.Transparent
            )
        }
        }

        if (showNotificationPopup) {
            AlertDialog(
                onDismissRequest = { showNotificationPopup = false },
                title = { Text("Class Notifications") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Remind before")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15, 30).forEach { mins ->
                                val active = notificationSettings.minutesBefore == mins
                                FilterChipButton(
                                    label = "${mins}m",
                                    active = active,
                                    onClick = {
                                        onUpdateNotificationSettings(notificationSettings.copy(minutesBefore = mins))
                                    }
                                )
                            }
                        }
                        Text(
                            "Section reminders are managed in Profile > Notification Settings.",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onUpdateNotificationSettings(notificationSettings.copy(enabled = true))
                        notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        showNotificationPopup = false
                    }) {
                        Text("Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onUpdateNotificationSettings(notificationSettings.copy(enabled = false))
                        showNotificationPopup = false
                    }) {
                        Text("Disable")
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeTabSwitcher(selectedTab: HomeTab, onTabSelected: (HomeTab) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                HomeTab.Classes to "CLASSES",
                HomeTab.Upcoming to "UPCOMING EVENT",
                HomeTab.Canceled to "CANCELED CLASSES"
            ).forEach { (tab, label) ->
                val selected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Color(0xFFdd200f) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.White else Color(0xFFE5E7EB),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionFilterButton(
    modifier: Modifier = Modifier, // ADD THIS
    label: String,
    selected: Boolean,
    topLabel: String = "sec",
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFFdd200f) else Color(0xFFf1f1f1)
    val textColor = if (selected) Color.White else Color(0xFF43434b)
    val secTextColor = if (selected) Color.White.copy(alpha = 0.7f) else Color(0xFF9CA3AF)

    Box(
        // USE THE PASSED MODIFIER HERE
        modifier = modifier
            .height(56.dp)
            .widthIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = topLabel,
                color = secTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = label,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}
@Composable
private fun DateScrollerWithReset(
    dates: List<LocalDate>,
    selectedDate: LocalDate?,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    onSelectDate: (LocalDate) -> Unit
) {
    ClassDateScroller(
        dates = dates,
        selectedDate = selectedDate,
        listState = listState,
        onSelectDate = onSelectDate
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        selectedDate?.let { picked ->
            Text(
                text = picked.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)),
                fontWeight = FontWeight.Bold,
                color = AccentBlack,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
            Text(
                "Reset ",
                color = Emerald800,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.clickable {
                    val today = LocalDate.now()
                    val todayDate = dates.firstOrNull { it == today } ?: dates.firstOrNull()
                    if (todayDate != null) onSelectDate(todayDate)

                    val todayIndex = dates.indexOf(todayDate)
                    if (todayIndex >= 0) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(todayIndex)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ClassDateScroller(
    dates: List<LocalDate>,
    selectedDate: LocalDate?,
    listState: LazyListState, // Added state to control scroll position
    onSelectDate: (LocalDate) -> Unit
) {
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dates) { date ->
            val selected = selectedDate == date
            val bgColor = if (selected) Color(0xFF0acca2) else Color(0xFFf1f1f1)
            val dayTextColor = if (selected) Color.White.copy(alpha = 0.9f) else Color(0xFF6B7280)
            val dateTextColor = if (selected) Color.White else Color(0xFF0C0C0C)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onSelectDate(date) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = date.dayOfWeek.name.take(3).uppercase(Locale.ENGLISH),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dayTextColor
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = dateTextColor,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassTimelineRow(
    item: TimetableItem,
    isLast: Boolean
) {
    val isLunch = item.courseName.equals("Lunch Break", ignoreCase = true)
    val isBlocked = item.courseName.contains("Blocked", ignoreCase = true)
    val dotColor = when ((item.courseName + item.classroom).hashCode().mod(4)) {
        0 -> Color(0xFF10B981) // Green
        1 -> Color(0xFF3B82F6) // Blue
        2 -> Color(0xFFFF0000) 
        else -> Color(0xFFF59E0B) // Orange
    }

    val rowBgColor = when {
        isLunch -> Color(0xFFf0f0f0)
        isBlocked -> Color(0xFFf80b0b) // Light red for blocked
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBgColor)
            .height(IntrinsicSize.Min), // Forces children to match height so the line spans correctly
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Column
        Text(
            text = item.time,
            color = Color(0xFF1F2937),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .width(90.dp)
                .padding(start = 16.dp, end = 8.dp)
        )

        // Timeline Node Column
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // The vertical timeline line
           Box(modifier = Modifier.width(40.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(Color(0xFFcf0404)))
            
            if (isLunch) {
                AsyncImage(model = "file:///android_asset/images/lunch.png", contentDescription = "lunch", modifier = Modifier.size(46.dp))
            } else if (isBlocked) {
                AsyncImage(model = "file:///android_asset/images/block.png", contentDescription = "Blocked", modifier = Modifier.size(34.dp))
            } else {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(dotColor))
            }
        }
        }

        // Details Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text(
                text = item.courseName,
                fontWeight = FontWeight.Bold,
                color = if(isBlocked) Color.White else Color.Black,
                fontSize = 14.sp
            )
            if (!isLunch && !isBlocked) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.classroom}  •  ${item.professor}",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

       
    }

    if (!isLast) {
        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
    }
}

@Composable
private fun HomeHeader(name: String, photoUri: String?, onOpenProfile: () -> Unit) {
    val firstName = name.trim().split(" ").firstOrNull().orEmpty().ifBlank { "User" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Emerald900,
                            Emerald700,
                            Emerald500
                        )
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Good Morning,",
                        color = Color.White.copy(alpha = 0.96f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$firstName 👋",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                            .clickable(onClick = onOpenProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUri.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(Uri.parse(photoUri)),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = AccentBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeQuickActions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "A",
                "B",
                "C",
                "D",
                "E",
                "More"
            ).forEach { label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Emerald700),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (label == "More") "..." else label,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    if (label == "More") {
                        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectBatchCard(sections: List<String>, selected: String, onSelect: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("SELECT BATCH", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sections.forEach { section ->
                    val active = selected.equals(section, ignoreCase = true)
                    if (active) {
                        Button(
                            onClick = { onSelect(section) },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text(section, color = Color.White) }
                    } else {
                        OutlinedButton(
                            onClick = { onSelect(section) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text(section) }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingCard(events: List<UiEvent>, onToggle: () -> Unit, onShowAll: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("UPCOMING EVENTS", fontWeight = FontWeight.Bold)
                TextButton(onClick = onToggle) { Text("Show/Hide") }
            }

            events.take(2).forEach { ev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ev.title, fontWeight = FontWeight.SemiBold)
                            Text(ev.dateLabel, color = MutedText)
                        }
                        Text("${ev.daysRemaining} days", color = Emerald700, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = onShowAll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Text("SHOW ALL", color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun SectionFilterStrip(
    topSections: List<String>,
    selectedSection: String,
    onSelect: (String) -> Unit,
    onMore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipButton(
                label = "ALL",
                active = selectedSection == AppPreferences.FILTER_ALL.uppercase(),
                onClick = { onSelect(AppPreferences.FILTER_ALL.uppercase()) }
            )
            topSections.filter { it != AppPreferences.FILTER_ALL.uppercase() }.forEach { section ->
                FilterChipButton(
                    label = section,
                    active = selectedSection.equals(section, ignoreCase = true),
                    onClick = { onSelect(section) }
                )
            }
            FilterChipButton(label = "MORE", active = false, onClick = onMore)
        }
    }
}

@Composable
private fun FilterChipButton(label: String, active: Boolean, onClick: () -> Unit) {
    val bg = if (active) Emerald700 else Color.White
    val fg = if (active) Color.White else AccentBlack

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowAllFilterScreen(
    current: String,
    allOptions: List<String>,
    onBack: () -> Unit,
    onApply: (String) -> Unit
) {
    val primary = listOf("ALL") + allOptions.filter { it != "ALL" }.take(4)
    val extras = allOptions.filter { it != "ALL" && it !in primary }
    var selected by remember(current, allOptions) { mutableStateOf(current) }
    var expandMore by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SlateBg,
        topBar = {
            TopAppBar(
                title = { Text("Show All", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Select Filter", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    primary.forEach { label ->
                        FilterChipButton(
                            label = label,
                            active = selected.equals(label, ignoreCase = true),
                            onClick = { selected = label }
                        )
                    }
                    FilterChipButton(
                        label = if (expandMore) "LESS" else "MORE",
                        active = false,
                        onClick = { expandMore = !expandMore }
                    )
                }

                Text("More Options", fontWeight = FontWeight.Bold)
                if (expandMore) {
                    val extrasState = remember { mutableStateListOf<String>().apply { addAll(extras) } }
                    for (i in extrasState.indices step 4) {
                        val rowItems = extrasState.subList(i, minOf(i + 4, extrasState.size))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { label ->
                                Box(modifier = Modifier.weight(1f)) {
                                    FilterChipButton(
                                        label = label,
                                        active = selected.equals(label, ignoreCase = true),
                                        onClick = { selected = label }
                                    )
                                }
                            }
                            repeat(4 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { onApply(selected) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filter", color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllEventsScreen(items: List<TimetableItem>, sheetEvents: List<TimetableEvent>, onBack: () -> Unit) {
    val events = remember(items, sheetEvents) {
        sheetUpcomingEvents(sheetEvents).ifEmpty { fakeUpcomingEvents(items).ifEmpty { fallbackEvents() } }
    }

    Scaffold(
        containerColor = SlateBg,
        topBar = {
            TopAppBar(
                title = { Text("All Events", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventRowCard(event)
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Stay tuned for more events!", color = MutedText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRowCard(event: UiEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFececec)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Left Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(event.badgeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = event.icon,
                        contentDescription = null,
                        tint = event.badgeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Center Title & Date
                Column {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = event.dateLabel,
                        color = MutedText,
                        fontSize = 13.sp
                    )
                }
            }
            
            // Right Days Remaining Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(event.badgeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${event.daysRemaining} days",
                        color = event.badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "remaining",
                        color = event.badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TimetableRowCard(item: TimetableItem) {
    val avatarLetter = item.courseName.firstOrNull()?.uppercase() ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Emerald700),
            contentAlignment = Alignment.Center
        ) {
            Text(avatarLetter, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(item.courseName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.date, color = MutedText, style = MaterialTheme.typography.bodySmall)
            Text("${item.time}  |  ${item.classroom}", color = MutedText, style = MaterialTheme.typography.bodySmall)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedText)
    }
}

@Composable
private fun OnboardingScreen(
    initialName: String,
    initialPhotoUri: String?,
    onSubmit: (String, String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var photoUri by remember { mutableStateOf(initialPhotoUri) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            photoUri = uri.toString()
        }
    }

    Scaffold(containerColor = SlateBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(AccentBlue.copy(alpha = 0f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "file:///android_asset/images/timetable.png",
                        contentDescription = "CampusSync logo",
                        modifier = Modifier
                            .size(65.dp)
                            .clip(CircleShape)
                    )
                }

                Column {
                    Text(
                        text = "WELCOME TO",
                        color = MutedText,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        Text(
                            text = "Campus",
                            color = AccentBlack,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 42.sp
                        )
                        Text(
                            text = "Sync",
                            color = AccentBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 42.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(124.dp))

            ProfileImage(
                photoUri = photoUri,
                size = 140,
                onClick = { launcher.launch(arrayOf("image/*")) }
            )

            Spacer(Modifier.height(12.dp))
            Text("Tap image to add profile photo")

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Enter full name") }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = name.trim().isNotEmpty(),
                onClick = { onSubmit(name.trim(), photoUri) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Text("Continue", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    initialName: String,
    initialPhotoUri: String?,
    sectionOptions: List<String>,
    notificationSettings: AppPreferences.NotificationSettings,
    onBack: () -> Unit,
    onSave: (String, String?) -> Unit,
    onSaveNotificationSettings: (AppPreferences.NotificationSettings) -> Unit,
    onShowToast: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var photoUri by remember { mutableStateOf(initialPhotoUri) }
    var notifEnabled by remember(notificationSettings) { mutableStateOf(notificationSettings.enabled) }
    var notifSection by remember(notificationSettings) { mutableStateOf(notificationSettings.section.uppercase(Locale.ENGLISH)) }
    var notifMinutes by remember(notificationSettings) { mutableStateOf(notificationSettings.minutesBefore) }
    var showCustomMinutesDialog by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            photoUri = uri.toString()
        }
    }

    Scaffold(containerColor = SlateBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        brush = Brush.verticalGradient(listOf(Emerald900, Emerald700)),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Spacer(Modifier.height(4.dp))

                    Box(contentAlignment = Alignment.BottomEnd) {
                        ProfileImage(
                            photoUri = photoUri,
                            size = 134,
                            onClick = { launcher.launch(arrayOf("image/*")) },
                            showBorder = true
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFfc1b1b), CircleShape)
                                .clickable { launcher.launch(arrayOf("image/*")) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Change photo",
                                tint = Emerald700,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (name.isBlank()) "User" else name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )

                    if (notifSection.isNotBlank() && !notifSection.equals(AppPreferences.FILTER_ALL, ignoreCase = true)) {
                        Text(
                            text = "Notification for Section : $notifSection",
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edit name", color = MutedText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Full name") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                   Spacer(Modifier.height(10.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable class reminders")
                        Switch(checked = notifEnabled, onCheckedChange = { notifEnabled = it })
                    }

                    Text("Select Section For Notifications", color = MutedText, fontSize = 14.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sectionOptions.filter { !it.equals(AppPreferences.FILTER_ALL, ignoreCase = true) }.forEach { section ->
                            SectionFilterButton(
                                label = section,
                                selected = notifSection.equals(section, ignoreCase = true),
                                onClick = { notifSection = section.uppercase(Locale.ENGLISH) }
                            )
                        }
                    }

                    Text("Remind before", color = MutedText, fontSize = 13.sp)
                    val presetMinutes = listOf(5, 10, 15, 30)
                    val isCustomMinutes = notifMinutes !in presetMinutes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetMinutes.forEach { mins ->
                            SectionFilterButton(
                                label = "${mins}m",
                                selected = !isCustomMinutes && notifMinutes == mins,
                                topLabel = "min",
                                onClick = { notifMinutes = mins }
                            )
                        }
                        SectionFilterButton(
                            label = if (isCustomMinutes) "${notifMinutes}m" else "Custom",
                            selected = isCustomMinutes,
                            topLabel = "min",
                            onClick = {
                                customMinutesInput = if (isCustomMinutes) notifMinutes.toString() else ""
                                showCustomMinutesDialog = true
                            }
                        )
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp),
                enabled = name.trim().isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                onClick = {
                    try {
                        onSave(name.trim(), photoUri)
                        onSaveNotificationSettings(
                            AppPreferences.NotificationSettings(
                                enabled = notifEnabled,
                                section = notifSection.lowercase(Locale.ENGLISH),
                                minutesBefore = notifMinutes
                            )
                        )
                        onShowToast("Notification saved successfully", false)
                    } catch (e: Exception) {
                        onShowToast("Error in saving", true)
                    }
                }
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        if (showCustomMinutesDialog) {
            AlertDialog(
                onDismissRequest = { showCustomMinutesDialog = false },
                title = { Text("Custom reminder time") },
                text = {
                    OutlinedTextField(
                        value = customMinutesInput,
                        onValueChange = { input -> customMinutesInput = input.filter { it.isDigit() }.take(3) },
                        singleLine = true,
                        label = { Text("Minutes before class") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = (customMinutesInput.toIntOrNull() ?: 0) > 0,
                        onClick = {
                            val value = customMinutesInput.toIntOrNull()
                            if (value != null && value > 0) {
                                notifMinutes = value
                                showCustomMinutesDialog = false
                            }
                        }
                    ) {
                        Text("Set")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomMinutesDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileImage(photoUri: String?, size: Int, onClick: () -> Unit, showBorder: Boolean = false) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFFfcfcfc)) // You can change this to Color.White if your logo has a transparent background!
            .then(
                if (showBorder) {
                    Modifier.border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(photoUri)),
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // LOAD CUSTOM LOGO INSTEAD OF DEFAULT ICON
            AsyncImage(
                model = "file:///android_asset/images/logo.png",
                contentDescription = "Default Profile Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Ensures the logo fills the circular box nicely
            )
        }
    }
}

@Composable
private fun RefreshIcon(spinning: Boolean) {
    if (spinning) {
        val infiniteTransition = rememberInfiniteTransition(label = "refresh-spin")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearEasing)),
            label = "angle"
        )
        Icon(
            Icons.Default.Refresh,
            contentDescription = "Refreshing",
            modifier = Modifier.rotate(angle)
        )
    } else {
        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
    }
}

@Composable
private fun EmptyStateCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("No classes for selected filter")
            Text("Open Show All and apply another filter", style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
    }
}

private data class UiEvent(
    val title: String,
    val dateLabel: String,
    val daysRemaining: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeColor: Color
)

private fun sheetUpcomingEvents(events: List<TimetableEvent>): List<UiEvent> {
    val today = LocalDate.now()
    val palette = listOf(
        Color(0xFF10B981),
        Color(0xFFF97316),
        Color(0xFFF59E0B),
        Color(0xFF3B82F6),
        Color(0xFF16A34A)
    )

    val normalized = events
        .mapNotNull { ev ->
            val startDate = parseDate(ev.startDate) ?: return@mapNotNull null
            val endDate = parseDate(ev.endDate.ifBlank { ev.startDate }) ?: startDate
            if (endDate.isBefore(today)) return@mapNotNull null
            Triple(startDate, endDate, ev.title.trim())
        }
        .distinctBy { "${it.first}|${it.second}|${it.third.uppercase(Locale.ENGLISH)}" }
        .sortedBy { it.first }

    return normalized.mapIndexed { index, (startDate, endDate, title) ->
        val lower = title.lowercase(Locale.ENGLISH)
        val icon = when {
            "independence" in lower -> Icons.Default.Flag
            "sports" in lower -> Icons.Default.SportsSoccer
            "exam" in lower || "term" in lower -> Icons.Default.School
            else -> Icons.Default.Event
        }

        val dateLabel = if (startDate == endDate) {
            startDate.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH))
        } else {
            "${startDate.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))} - ${endDate.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH))}"
        }

        UiEvent(
            title = title,
            dateLabel = dateLabel,
            daysRemaining = ChronoUnit.DAYS.between(today, startDate).toInt(),
            icon = icon,
            badgeColor = palette[index % palette.size]
        )
    }
}

private fun fakeUpcomingEvents(items: List<TimetableItem>): List<UiEvent> {
    val uniqueDates = items.mapNotNull { parseDate(it.date) }.distinct().sorted()

    val events = uniqueDates.take(5).mapIndexed { index, date ->
        val day = kotlin.math.abs(LocalDate.now().until(date).days)
        UiEvent(
            title = when (index) {
                0 -> "Mid Term"
                1 -> "Independence Day"
                2 -> "End Term"
                3 -> "Sports Day"
                else -> "College Fest"
            },
            dateLabel = date.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)),
            daysRemaining = if (day == 0) index + 7 else day,
            icon = when (index) {
                0 -> Icons.Default.Event
                1 -> Icons.Default.Flag
                2 -> Icons.Default.School
                3 -> Icons.Default.SportsSoccer
                else -> Icons.Default.Search
            },
            badgeColor = when (index) {
                0 -> Color(0xFF10B981)
                1 -> Color(0xFFF97316)
                2 -> Color(0xFFF59E0B)
                3 -> Color(0xFF3B82F6)
                else -> Color(0xFF16A34A)
            }
        )
    }

    return if (events.isEmpty()) fallbackEvents() else events
}

private fun fallbackEvents(): List<UiEvent> {
    return listOf(
        UiEvent("Mid Term", "31 Jul, 2026", 32, Icons.Default.Event, Color(0xFF10B981)),
        UiEvent("Independence Day", "15 Aug, 2026", 47, Icons.Default.Flag, Color(0xFFF97316)),
        UiEvent("End Term", "20 Sep, 2026", 83, Icons.Default.School, Color(0xFFF59E0B)),
        UiEvent("Sports Day", "05 Oct, 2026", 98, Icons.Default.SportsSoccer, Color(0xFF3B82F6)),
        UiEvent("College Fest", "18 Oct, 2026", 111, Icons.Default.Search, Color(0xFF16A34A))
    )
}

private fun parseDate(raw: String): LocalDate? {
    val inputs = listOf(
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH),  // "Saturday, August 15, 2026"
        DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy", Locale.ENGLISH),  // "Friday, 31 July, 2026"
        DateTimeFormatter.ofPattern("EEEE, dd MMMM, yyyy", Locale.ENGLISH), // "Friday, 31 July, 2026" (zero-padded)
        DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH), // "Saturday, August 15, 2026" (zero-padded)
        DateTimeFormatter.ISO_LOCAL_DATE
    )
    for (fmt in inputs) {
        val parsed = runCatching { LocalDate.parse(raw.trim(), fmt) }.getOrNull()
        if (parsed != null) return parsed
    }
    return null
}

private fun parseStartTime(timeRange: String): LocalTime? {
    // This regex handles various dash types better than substringBefore
    val head = timeRange
        .split(Regex("\\s*[-–—]\\s*"))
        .firstOrNull()
        .orEmpty()
        .trim()
        .replace(".", ":") // Converts "13.40" to "13:40" automatically
        .replace(" ", "")

    val formats = listOf(
        DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH)
    )
    
    formats.forEach { fmt ->
        val parsed = runCatching { LocalTime.parse(head.uppercase(Locale.ENGLISH), fmt) }.getOrNull()
        if (parsed != null) return parsed
    }
    return null
}

private fun extractSectionFilter(raw: String): String {
    val value = raw.trim()
    if (value.isBlank()) return ""
    return if ("-" in value) {
        value.substringAfterLast("-").trim().uppercase(Locale.ENGLISH)
    } else {
        value.uppercase(Locale.ENGLISH)
    }
}

private fun timeBasedGreeting(): String {
    return when (LocalTime.now().hour) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        in 17..20 -> "Good Evening 🫩"
        else -> "Good Night 🌚"
    }
}
