package com.example.nammamistri

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.nammamistri.ui.theme.NammamistriTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applySavedLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nammamistri-db"
        ).build()

        setContent {
            NammamistriTheme {
                var selectedProjectId by remember { mutableStateOf<Int?>(null) }
                val context = LocalContext.current
                var currentLanguage by remember {
                    mutableStateOf(LocaleManager.getSavedLanguage(context))
                }
                val onLanguageChange: (String) -> Unit = { languageCode ->
                    LocaleManager.setLocale(context, languageCode)
                    currentLanguage = languageCode
                    (context as? Activity)?.recreate()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (selectedProjectId == null) {
                        ProjectScreen(
                            db = db,
                            currentLanguage = currentLanguage,
                            onLanguageChange = onLanguageChange,
                            onProjectSelected = { id -> selectedProjectId = id }
                        )
                    } else {
                        AppScreen(
                            projectId = selectedProjectId!!,
                            db = db,
                            currentLanguage = currentLanguage,
                            onLanguageChange = onLanguageChange,
                            onBack = { selectedProjectId = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSwitch(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.language),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
        FilterChip(
            selected = currentLanguage == LocaleManager.ENGLISH,
            onClick = { onLanguageChange(LocaleManager.ENGLISH) },
            label = { Text(stringResource(R.string.english)) }
        )
        FilterChip(
            selected = currentLanguage == LocaleManager.KANNADA,
            onClick = { onLanguageChange(LocaleManager.KANNADA) },
            label = { Text(stringResource(R.string.kannada)) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppScreen(
    projectId: Int,
    db: AppDatabase,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var projectName by remember { mutableStateOf("") }

    LaunchedEffect(projectId) {
        val projectList = db.projectDao().getAllProjects()
        val project = projectList.find { it.id == projectId }
        projectName = project?.name ?: ""
    }

    val tabs = listOf(
        stringResource(R.string.tab_calculator),
        stringResource(R.string.tab_team),
        stringResource(R.string.tab_photos),
        stringResource(R.string.tab_rates),
        stringResource(R.string.tab_expenses),
        stringResource(R.string.tab_summary)
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.project),
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = projectName.ifBlank { stringResource(R.string.loading) },
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.back_to_projects))
                }

                Spacer(modifier = Modifier.height(12.dp))

                LanguageSwitch(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    contentColor = Color.White.copy(alpha = 0.82f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                fontWeight = if (pagerState.currentPage == index) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> MaterialCalculator(projectId, db)
                    1 -> LabourDiary(projectId, db)
                    2 -> PhotosScreen(projectId, db)
                    3 -> RatesScreen(projectId, db)
                    4 -> ExpenseScreen(projectId, db)
                    5 -> SummaryScreen(projectId, db)
                }
            }
        }
    }
}

@Composable
fun MaterialCalculator(projectId: Int, db: AppDatabase) {
    var brickRate by remember { mutableStateOf("") }
    var cementRate by remember { mutableStateOf("") }
    var sandRate by remember { mutableStateOf("") }

    var length by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var thickness by remember { mutableStateOf("") }
    var doorCount by remember { mutableStateOf("") }
    var doorWidth by remember { mutableStateOf("") }
    var doorHeight by remember { mutableStateOf("") }
    var doorRate by remember { mutableStateOf("") }
    var windowCount by remember { mutableStateOf("") }
    var windowWidth by remember { mutableStateOf("") }
    var windowHeight by remember { mutableStateOf("") }
    var windowRate by remember { mutableStateOf("") }

    var result by remember { mutableStateOf("") }
    val invalidDataMessage = stringResource(R.string.enter_valid_data)
    val openingTooLargeMessage = stringResource(R.string.opening_area_too_large)
    val grossVolumeLabel = stringResource(R.string.gross_wall_volume)
    val openingVolumeLabel = stringResource(R.string.door_window_deduction)
    val netVolumeLabel = stringResource(R.string.net_wall_volume)
    val bricksLabel = stringResource(R.string.bricks)
    val cementLabel = stringResource(R.string.cement)
    val sandLabel = stringResource(R.string.sand)
    val bagsLabel = stringResource(R.string.bags)
    val tonsLabel = stringResource(R.string.tons)
    val doorsLabel = stringResource(R.string.doors)
    val windowsLabel = stringResource(R.string.windows)
    val countLabel = stringResource(R.string.count)
    val sizeLabel = stringResource(R.string.size)
    val rateLabel = stringResource(R.string.rate)
    val materialCostLabel = stringResource(R.string.material_cost)
    val totalCostLabel = stringResource(R.string.total_cost)
    val currencyTemplate = stringResource(R.string.currency_amount)

    LaunchedEffect(projectId) {
        val rates = db.ratesDao().getRates(projectId)
        if (rates != null) {
            brickRate = rates.brickRate.toString()
            cementRate = rates.cementRate.toString()
            sandRate = rates.sandRate.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.material_calculator), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = length,
            onValueChange = { length = it },
            label = { Text(stringResource(R.string.length_ft)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text(stringResource(R.string.height_ft)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = thickness,
            onValueChange = { thickness = it },
            label = { Text(stringResource(R.string.thickness_inches)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.doors), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = doorCount,
            onValueChange = { doorCount = it },
            label = { Text(stringResource(R.string.door_count)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = doorWidth,
                onValueChange = { doorWidth = it },
                label = { Text(stringResource(R.string.door_width_ft)) },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = doorHeight,
                onValueChange = { doorHeight = it },
                label = { Text(stringResource(R.string.door_height_ft)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = doorRate,
            onValueChange = { doorRate = it },
            label = { Text(stringResource(R.string.door_rate)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.windows), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = windowCount,
            onValueChange = { windowCount = it },
            label = { Text(stringResource(R.string.window_count)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = windowWidth,
                onValueChange = { windowWidth = it },
                label = { Text(stringResource(R.string.window_width_ft)) },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = windowHeight,
                onValueChange = { windowHeight = it },
                label = { Text(stringResource(R.string.window_height_ft)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = windowRate,
            onValueChange = { windowRate = it },
            label = { Text(stringResource(R.string.window_rate)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = brickRate,
            onValueChange = { brickRate = it },
            label = { Text(stringResource(R.string.brick_rate)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = cementRate,
            onValueChange = { cementRate = it },
            label = { Text(stringResource(R.string.cement_rate)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = sandRate,
            onValueChange = { sandRate = it },
            label = { Text(stringResource(R.string.sand_rate)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val l = length.toDoubleOrNull()
                val h = height.toDoubleOrNull()
                val tInch = thickness.toDoubleOrNull()
                val br = brickRate.toDoubleOrNull()
                val cr = cementRate.toDoubleOrNull()
                val sr = sandRate.toDoubleOrNull()
                val dc = doorCount.toIntOrNull() ?: 0
                val dw = doorWidth.toDoubleOrNull() ?: 0.0
                val dh = doorHeight.toDoubleOrNull() ?: 0.0
                val dr = doorRate.toDoubleOrNull() ?: 0.0
                val wc = windowCount.toIntOrNull() ?: 0
                val ww = windowWidth.toDoubleOrNull() ?: 0.0
                val wh = windowHeight.toDoubleOrNull() ?: 0.0
                val wr = windowRate.toDoubleOrNull() ?: 0.0

                if (
                    l == null || h == null || tInch == null || br == null || cr == null || sr == null ||
                    l <= 0 || h <= 0 || tInch <= 0 || br < 0 || cr < 0 || sr < 0 ||
                    dc < 0 || dw < 0 || dh < 0 || dr < 0 || wc < 0 || ww < 0 || wh < 0 || wr < 0 ||
                    (dc > 0 && (dw <= 0 || dh <= 0)) || (wc > 0 && (ww <= 0 || wh <= 0))
                ) {
                    result = invalidDataMessage
                    return@Button
                }

                val t = tInch / 12
                val grossVolume = l * h * t
                val doorArea = dc * dw * dh
                val windowArea = wc * ww * wh
                val openingVolume = (doorArea + windowArea) * t

                if (openingVolume > grossVolume) {
                    result = openingTooLargeMessage
                    return@Button
                }

                val volume = grossVolume - openingVolume
                val bricks = (volume * 500).toInt()
                val cement = (volume * 8).toInt()
                val sand = volume * 0.3
                val brickCost = bricks * br
                val cementCost = cement * cr
                val sandCost = sand * sr
                val materialCost = brickCost + cementCost + sandCost
                val doorCost = dc * dr
                val windowCost = wc * wr
                val totalCost = materialCost + doorCost + windowCost

                result = """
$grossVolumeLabel: ${"%.2f".format(grossVolume)}
$openingVolumeLabel: ${"%.2f".format(openingVolume)}
$netVolumeLabel: ${"%.2f".format(volume)}

$bricksLabel: $bricks - ${String.format(currencyTemplate, "%.2f".format(brickCost))}
$cementLabel: $cement $bagsLabel - ${String.format(currencyTemplate, "%.2f".format(cementCost))}
$sandLabel: ${"%.2f".format(sand)} $tonsLabel - ${String.format(currencyTemplate, "%.2f".format(sandCost))}

$doorsLabel: $dc $countLabel, $sizeLabel ${"%.2f".format(dw)} x ${"%.2f".format(dh)} ft, $rateLabel ${String.format(currencyTemplate, "%.2f".format(dr))} - ${String.format(currencyTemplate, "%.2f".format(doorCost))}
$windowsLabel: $wc $countLabel, $sizeLabel ${"%.2f".format(ww)} x ${"%.2f".format(wh)} ft, $rateLabel ${String.format(currencyTemplate, "%.2f".format(wr))} - ${String.format(currencyTemplate, "%.2f".format(windowCost))}

$materialCostLabel: ${String.format(currencyTemplate, "%.2f".format(materialCost))}
$totalCostLabel: ${String.format(currencyTemplate, "%.2f".format(totalCost))}
""".trimIndent()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(stringResource(R.string.calculate))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(result)
    }
}

@Composable
fun LabourDiary(projectId: Int, db: AppDatabase) {
    var editingId by remember { mutableStateOf<Int?>(null) }
    var workers by remember { mutableStateOf(listOf<Worker>()) }
    var name by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    var advance by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var attendanceMap by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    val enterValidDataMessage = stringResource(R.string.enter_valid_data)
    val savedMessage = stringResource(R.string.saved)
    val attendanceSavedMessage = stringResource(R.string.attendance_saved)
    val currencyTemplate = stringResource(R.string.currency_amount)
    val totalWorkers = workers.size
    val totalWage = workers.sumOf { it.wage }
    val totalAdvance = workers.sumOf { it.advance }
    val totalBalance = totalWage - totalAdvance

    LaunchedEffect(Unit) {
        val workerList = db.workerDao().getWorkersByProject(projectId)
        val map = mutableMapOf<Int, Int>()
        workerList.forEach { worker ->
            val att = db.attendanceDao().getAttendance(projectId, worker.id)
            map[worker.id] = att?.daysWorked ?: 0
        }
        workers = workerList
        attendanceMap = map
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.labour_diary), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.summary), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${stringResource(R.string.workers)}: $totalWorkers")
                Text("${stringResource(R.string.total_wage)}: ${String.format(currencyTemplate, totalWage.toString())}")
                Text("${stringResource(R.string.advance)}: ${String.format(currencyTemplate, totalAdvance.toString())}")
                Text("${stringResource(R.string.balance)}: ${String.format(currencyTemplate, totalBalance.toString())}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.worker_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = wage,
            onValueChange = { wage = it },
            label = { Text(stringResource(R.string.daily_wage)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = advance,
            onValueChange = { advance = it },
            label = { Text(stringResource(R.string.advance)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val w = wage.toDoubleOrNull()
                val a = advance.toDoubleOrNull()

                if (name.isEmpty() || w == null || a == null) {
                    result = enterValidDataMessage
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    if (editingId == null) {
                        db.workerDao().insert(
                            Worker(name = name, wage = w, advance = a, projectId = projectId)
                        )
                    } else {
                        db.workerDao().updateWorker(
                            Worker(id = editingId!!, name = name, wage = w, advance = a, projectId = projectId)
                        )
                    }

                    val updated = db.workerDao().getWorkersByProject(projectId)

                    withContext(Dispatchers.Main) {
                        workers = updated
                        name = ""
                        wage = ""
                        advance = ""
                        editingId = null
                        result = savedMessage
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_worker))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(result)
        Spacer(modifier = Modifier.height(20.dp))

        workers.forEach { worker ->
            val daysWorked = attendanceMap[worker.id] ?: 0
            val total = daysWorked * worker.wage

            Card(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(worker.name)
                    Text("${stringResource(R.string.daily_wage)}: ${String.format(currencyTemplate, worker.wage.toString())}")
                    Text("${stringResource(R.string.advance)}: ${String.format(currencyTemplate, worker.advance.toString())}")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = daysWorked.toString(),
                        onValueChange = { value ->
                            val d = value.toIntOrNull() ?: 0
                            attendanceMap = attendanceMap.toMutableMap().apply {
                                put(worker.id, d)
                            }
                        },
                        label = { Text(stringResource(R.string.days_worked_week)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${stringResource(R.string.weekly_salary)}: ${String.format(currencyTemplate, total.toString())}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val days = attendanceMap[worker.id] ?: 0

                            CoroutineScope(Dispatchers.IO).launch {
                                val existing = db.attendanceDao().getAttendance(projectId, worker.id)

                                if (existing == null) {
                                    db.attendanceDao().insert(
                                        Attendance(
                                            workerId = worker.id,
                                            projectId = projectId,
                                            daysWorked = days
                                        )
                                    )
                                } else {
                                    db.attendanceDao().update(existing.copy(daysWorked = days))
                                }

                                withContext(Dispatchers.Main) {
                                    result = attendanceSavedMessage
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.save_attendance))
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.workerDao().deleteWorker(worker.id)
                                val updated = db.workerDao().getWorkersByProject(projectId)
                                withContext(Dispatchers.Main) {
                                    workers = updated
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.delete_worker))
                    }
                }
            }
        }
    }
}

@Composable
fun PhotosScreen(projectId: Int, db: AppDatabase) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }
    var photoList by remember { mutableStateOf<List<Photo>>(emptyList()) }
    val selectPhotoFirstMessage = stringResource(R.string.select_photo_first)
    val photoSavedMessage = stringResource(R.string.photo_saved)
    val photoDeletedMessage = stringResource(R.string.photo_deleted)

    LaunchedEffect(projectId) {
        val photos = db.photoDao().getPhotos(projectId)
        photoList = photos
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.site_photos),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(stringResource(R.string.select_photo))
        }

        Spacer(modifier = Modifier.height(20.dp))

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = stringResource(R.string.selected_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (imageUri == null) {
                    message = selectPhotoFirstMessage
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    db.photoDao().insert(
                        Photo(
                            projectId = projectId,
                            imageUri = imageUri.toString()
                        )
                    )

                    val updated = db.photoDao().getPhotos(projectId)

                    withContext(Dispatchers.Main) {
                        photoList = updated
                        message = photoSavedMessage
                        imageUri = null
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(stringResource(R.string.save_photo))
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (photoList.isEmpty()) {
            Text(stringResource(R.string.no_photos_saved))
        } else {
            Text(stringResource(R.string.saved_photos), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))

            photoList.forEach { photo ->
                Column {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(photo.imageUri)),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(5.dp),
                        contentScale = ContentScale.Crop
                    )

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.photoDao().deletePhoto(photo.id)
                                val updated = db.photoDao().getPhotos(projectId)
                                withContext(Dispatchers.Main) {
                                    photoList = updated
                                    message = photoDeletedMessage
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.delete_photo))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}

@Composable
fun RatesScreen(projectId: Int, db: AppDatabase) {
    var cementPrice by remember { mutableStateOf("") }
    var brickPrice by remember { mutableStateOf("") }
    var sandPrice by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val invalidRatesMessage = stringResource(R.string.enter_valid_rates)
    val ratesSavedMessage = stringResource(R.string.rates_saved)

    LaunchedEffect(projectId) {
        val rates = db.ratesDao().getRates(projectId)
        if (rates != null) {
            cementPrice = rates.cementRate.toString()
            brickPrice = rates.brickRate.toString()
            sandPrice = rates.sandRate.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.standard_rates),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = cementPrice,
            onValueChange = { cementPrice = it },
            label = { Text(stringResource(R.string.cement_price)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = brickPrice,
            onValueChange = { brickPrice = it },
            label = { Text(stringResource(R.string.brick_price)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = sandPrice,
            onValueChange = { sandPrice = it },
            label = { Text(stringResource(R.string.sand_price)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val c = cementPrice.toDoubleOrNull()
                val b = brickPrice.toDoubleOrNull()
                val s = sandPrice.toDoubleOrNull()

                if (c == null || b == null || s == null) {
                    message = invalidRatesMessage
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    db.ratesDao().deleteRates(projectId)
                    db.ratesDao().insert(
                        Rates(
                            projectId = projectId,
                            brickRate = b,
                            cementRate = c,
                            sandRate = s
                        )
                    )

                    withContext(Dispatchers.Main) {
                        message = ratesSavedMessage
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(stringResource(R.string.save_rates))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(message)
    }
}
