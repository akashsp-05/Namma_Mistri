package com.example.nammamistri

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SummaryScreen(projectId: Int, db: AppDatabase) {
    val context = LocalContext.current

    var project by remember { mutableStateOf<Project?>(null) }
    var workerList by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var expenseList by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var completedInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val proj = withContext(Dispatchers.IO) {
            db.projectDao().getAllProjects().find { it.id == projectId }
        }

        val workers = withContext(Dispatchers.IO) {
            db.workerDao().getWorkersByProject(projectId)
        }

        val expenses = withContext(Dispatchers.IO) {
            db.expenseDao().getExpenses(projectId)
        }

        project = proj
        workerList = workers
        expenseList = expenses
    }

    val formattedDate = project?.createdDate?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: stringResource(R.string.not_available)

    val totalWorkers = workerList.size
    val labourCost = workerList.sumOf { it.wage } - workerList.sumOf { it.advance }
    val totalExpense = expenseList.sumOf { it.amount }
    val finalTotal = labourCost + totalExpense
    val currencyTemplate = stringResource(R.string.currency_amount)

    val totalDays = project?.totalDays ?: 0
    val completedDays = project?.completedDays ?: 0
    val remainingDays = (totalDays - completedDays).coerceAtLeast(0)

    val progress = if (totalDays > 0) {
        ((completedDays * 100) / totalDays).coerceAtMost(100)
    } else {
        0
    }

    val progressColor = when {
        progress < 40 -> MaterialTheme.colorScheme.error
        progress < 70 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val progressText = when {
        progress < 40 -> stringResource(R.string.work_just_started)
        progress < 70 -> stringResource(R.string.work_in_progress)
        else -> stringResource(R.string.almost_completed)
    }

    val reportText = buildString {
        append("${context.getString(R.string.project_summary)}\n\n")
        append("${context.getString(R.string.report_created)}: $formattedDate\n")
        append("${context.getString(R.string.report_progress)}: $progress%\n\n")
        append("${context.getString(R.string.report_workers)}:\n")
        workerList.forEach {
            append("${it.name} - ${String.format(currencyTemplate, it.wage.toString())}\n")
        }
        append("\n${context.getString(R.string.report_expenses)}:\n")
        expenseList.forEach {
            append("${it.title} - ${String.format(currencyTemplate, it.amount.toString())}\n")
        }
        append("\n${context.getString(R.string.report_labour_cost)}: ${String.format(currencyTemplate, labourCost.toString())}\n")
        append("${context.getString(R.string.report_expenses)}: ${String.format(currencyTemplate, totalExpense.toString())}\n")
        append("${context.getString(R.string.report_final_total)}: ${String.format(currencyTemplate, finalTotal.toString())}\n")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.project_dashboard), style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${stringResource(R.string.created)}: $formattedDate")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = completedInput,
                    onValueChange = { completedInput = it },
                    label = { Text(stringResource(R.string.completed_days)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val days = completedInput.toIntOrNull() ?: return@Button

                        CoroutineScope(Dispatchers.IO).launch {
                            val updatedProject = project?.copy(completedDays = days)

                            if (updatedProject != null) {
                                db.projectDao().update(updatedProject)
                            }

                            val newProject = db.projectDao()
                                .getAllProjects()
                                .find { it.id == projectId }

                            withContext(Dispatchers.Main) {
                                project = newProject
                                completedInput = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.update_progress))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.progress))

                    Text(
                        text = "$progress%",
                        color = progressColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = progressColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(progressText)

                Spacer(modifier = Modifier.height(8.dp))

                Text(stringResource(R.string.completed_days_format, completedDays, totalDays))
                Text(stringResource(R.string.remaining_days_format, remainingDays))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.workers))
                    Text("$totalWorkers", style = MaterialTheme.typography.titleLarge)
                }
            }

            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.expenses))
                    Text(String.format(currencyTemplate, totalExpense.toString()), style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.labour))
                    Text(String.format(currencyTemplate, labourCost.toString()), style = MaterialTheme.typography.titleLarge)
                }
            }

            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.total))
                    Text(String.format(currencyTemplate, finalTotal.toString()), style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, reportText)
                }
                context.startActivity(
                    Intent.createChooser(intent, context.getString(R.string.share_report_title))
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.share_report))
        }
    }
}
