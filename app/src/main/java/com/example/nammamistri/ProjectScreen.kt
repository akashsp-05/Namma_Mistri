package com.example.nammamistri

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProjectScreen(
    db: AppDatabase,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onProjectSelected: (Int) -> Unit
) {
    var projects by remember { mutableStateOf(listOf<Project>()) }
    var name by remember { mutableStateOf("") }
    var totalDays by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var selectedProjectId by remember { mutableStateOf<Int?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editProject by remember { mutableStateOf<Project?>(null) }
    var editName by remember { mutableStateOf("") }

    val enterAllFieldsMessage = stringResource(R.string.enter_all_fields)
    val enterValidNumberMessage = stringResource(R.string.enter_valid_number)
    val projectAddedMessage = stringResource(R.string.project_added)
    val projectDeletedMessage = stringResource(R.string.project_deleted)

    LaunchedEffect(Unit) {
        projects = db.projectDao().getAllProjects()
    }

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
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 26.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = stringResource(R.string.logo),
                        modifier = Modifier.size(62.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.brand_name),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(R.string.tagline),
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LanguageSwitch(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    contentColor = Color.White.copy(alpha = 0.82f)
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.project_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = totalDays,
                        onValueChange = { totalDays = it },
                        label = { Text(stringResource(R.string.estimated_days)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (name.isEmpty() || totalDays.isEmpty()) {
                                message = enterAllFieldsMessage
                                return@Button
                            }

                            val days = totalDays.toIntOrNull()
                            if (days == null) {
                                message = enterValidNumberMessage
                                return@Button
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                db.projectDao().insert(
                                    Project(
                                        name = name,
                                        createdDate = System.currentTimeMillis(),
                                        totalDays = days
                                    )
                                )

                                val updated = db.projectDao().getAllProjects()

                                withContext(Dispatchers.Main) {
                                    projects = updated
                                    name = ""
                                    totalDays = ""
                                    message = projectAddedMessage
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.add_project))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(message)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.your_projects),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text(stringResource(R.string.search_project)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            projects
                .filter { it.name.contains(searchText, ignoreCase = true) }
                .forEach { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onProjectSelected(project.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.open))
                                }

                                OutlinedButton(
                                    onClick = {
                                        editProject = project
                                        editName = project.name
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.edit))
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedProjectId = project.id
                                        showDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text(stringResource(R.string.delete))
                                }
                            }
                        }
                    }
                }
        }
    }

    if (showDialog && selectedProjectId != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.delete_project)) },
            text = { Text(stringResource(R.string.delete_project_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val projectId = selectedProjectId!!

                            db.workerDao().deleteWorkersByProject(projectId)
                            db.expenseDao().deleteExpensesByProject(projectId)
                            db.photoDao().deletePhotosByProject(projectId)
                            db.projectDao().deleteProject(projectId)

                            val updated = db.projectDao().getAllProjects()

                            withContext(Dispatchers.Main) {
                                projects = updated
                                message = projectDeletedMessage
                                showDialog = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showEditDialog && editProject != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_project)) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(stringResource(R.string.project_name)) }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val updated = editProject!!.copy(name = editName)
                            db.projectDao().update(updated)
                            val newList = db.projectDao().getAllProjects()

                            withContext(Dispatchers.Main) {
                                projects = newList
                                showEditDialog = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.update))
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
