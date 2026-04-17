package com.dreamdevelopersone.trackmytimepro

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import com.dreamdevelopersone.trackmytimepro.ui.TimeViewModel
import com.dreamdevelopersone.trackmytimepro.ui.FilterType
import com.dreamdevelopersone.trackmytimepro.util.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: TimeViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Timer", "Analytics")
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("TrackMyTime ", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("PRO", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn(animationSpec = tween(300))) togetherWith 
                        (slideOutHorizontally { -it } + fadeOut(animationSpec = tween(300)))
                    } else {
                        (slideInHorizontally { -it } + fadeIn(animationSpec = tween(300))) togetherWith 
                        (slideOutHorizontally { it } + fadeOut(animationSpec = tween(300)))
                    }.using(SizeTransform(clip = false))
                },
                label = "TabTransition"
            ) { targetTab ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (targetTab) {
                        0 -> TimerScreen(viewModel)
                        1 -> AnalyticsScreen(viewModel, snackbarHostState)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(viewModel: TimeViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val taskName by viewModel.taskName.collectAsState()
    val activeStart by viewModel.activeStartTime.collectAsState()
    val currentTime by viewModel.currentTimeMs.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Focus Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .graphicsLayer {
                        val scale = if (activeStart != null) 1.02f else 1.0f
                        scaleX = scale
                        scaleY = scale
                    },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeStart != null) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (activeStart != null) 20.dp else 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (activeStart != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    Modifier.padding(vertical = 40.dp, horizontal = 16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (activeStart != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "PulseAlpha"
                            )
                            Box(Modifier.size(10.dp).graphicsLayer(alpha = alpha).background(Color.Red, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("FOCUS MODE ACTIVE", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                        }
                    } else {
                        Text("READY TO FOCUS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    val displayTime = if (activeStart != null) {
                        formatDuration(currentTime - activeStart!!)
                    } else {
                        "00:00:00"
                    }
                    
                    Text(
                        displayTime,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                        color = if (activeStart != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    AnimatedVisibility(visible = activeStart != null) {
                        Text(
                            taskName.uppercase(), 
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // 2. Controls & Input
        item {
            if (activeStart == null) {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { viewModel.onTaskNameChange(it) },
                    label = { Text("Enter task for focus...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (suggestions.isNotEmpty()) {
                    Text("QUICK START:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.take(10).forEach { 
                            AssistChip(onClick = { viewModel.onTaskNameChange(it) }, label = { Text(it) })
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.startTask() },
                    modifier = Modifier.weight(1f),
                    enabled = activeStart == null && taskName.isNotBlank()
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Text("Start Focus")
                }

                Button(
                    onClick = { viewModel.stopTask() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = activeStart != null
                ) {
                    Icon(Icons.Default.Stop, null)
                    Text("End Session")
                }
            }
        }

        item { HorizontalDivider() }

        // 3. Recent Activities (Explicitly Limited)
        item {
            Text("Recent Activity (Last 10 sessions)", style = MaterialTheme.typography.titleSmall)
        }

        items(sessions.take(10), key = { it.id }) { s ->
            SessionItem(
                session = s,
                onDelete = { viewModel.deleteSession(s) },
                onEditEnd = { newTime -> viewModel.updateSessionEndTime(s, newTime) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: TimeViewModel, snackbarHostState: SnackbarHostState) {
    val stats by viewModel.taskStats.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val currentFilter by viewModel.selectedFilter.collectAsState()
    val totalTime = stats.values.sum()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterType.values().forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.onFilterChange(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Productivity (${currentFilter.label})", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatDuration(totalTime),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        item { Text("Task Allocation", style = MaterialTheme.typography.titleMedium) }

        if (stats.isEmpty()) {
            item { Text("No data for this period", color = MaterialTheme.colorScheme.outline) }
        } else {
            items(stats.toList().sortedByDescending { it.second }) { (name, duration) ->
                val proportion = if (totalTime > 0) duration.toFloat() / totalTime else 0f
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                        Text(formatDuration(duration), style = MaterialTheme.typography.bodySmall)
                    }
                    LinearProgressIndicator(
                        progress = { proportion },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item { HorizontalDivider() }
        
        item {
            var isExporting by remember { mutableStateOf(false) }

            val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let {
                    viewModel.exportAllData(context, it) { success ->
                        scope.launch { snackbarHostState.showSnackbar(if (success) "Backup saved" else "Backup failed") }
                    }
                }
            }

            val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let {
                    viewModel.importAllData(context, it) { success ->
                        scope.launch { snackbarHostState.showSnackbar(if (success) "Data restored" else "Import failed") }
                    }
                }
            }

            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            scope.launch { 
                                isExporting = true
                                if (exportPDF(context, sessions, currentFilter.label) != null) {
                                    snackbarHostState.showSnackbar("PDF saved")
                                }
                                isExporting = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isExporting
                    ) {
                        if (isExporting) CircularProgressIndicator(Modifier.size(16.dp)) else Text("Export PDF")
                    }
                    OutlinedButton(
                        onClick = { exportLauncher.launch("TrackMyTime_Backup.json") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Backup Data") }
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Restore Data") }
            }
        }
    }
}

@Composable
fun TimeFlowTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFF4F46E5), // Deep Indigo
        onPrimary = Color.White,
        primaryContainer = Color(0xFF3730A3),
        onPrimaryContainer = Color(0xFFE0E7FF),
        surface = Color(0xFF1E1E22), // Surface Charcoal
        onSurface = Color(0xFFE2E8F0), // Silver White
        background = Color(0xFF121214), // Midnight Charcoal
        onBackground = Color(0xFFE2E8F0),
        surfaceVariant = Color(0xFF2D2D32),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF475569),
        outlineVariant = Color(0xFF334155),
        error = Color(0xFFEF4444)
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography(), // In a real app we'd define custom Outfit/Inter fonts here
        content = content
    )
}

@Composable
fun SessionItem(
    session: TaskSession,
    onDelete: () -> Unit,
    onEditEnd: (Long) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(session.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${formatTime(session.startTime)} - ${formatTime(session.endTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    formatDuration(session.duration()),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            IconButton(onClick = {
                val cal = Calendar.getInstance()
                cal.timeInMillis = session.endTime
                TimePickerDialog(context, { _, h, m ->
                    val editCal = Calendar.getInstance()
                    editCal.timeInMillis = session.endTime
                    editCal.set(Calendar.HOUR_OF_DAY, h)
                    editCal.set(Calendar.MINUTE, m)
                    onEditEnd(editCal.timeInMillis)
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }) {
                Text("Edit", style = MaterialTheme.typography.labelSmall)
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
