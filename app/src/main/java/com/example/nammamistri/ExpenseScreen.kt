package com.example.nammamistri

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExpenseScreen(projectId: Int, db: AppDatabase) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var expenseList by remember { mutableStateOf<List<Expense>>(emptyList()) }

    val enterValidDataMessage = stringResource(R.string.enter_valid_data)
    val expenseSavedMessage = stringResource(R.string.expense_saved)
    val currencyTemplate = stringResource(R.string.currency_amount)

    LaunchedEffect(Unit) {
        expenseList = db.expenseDao().getExpenses(projectId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.expenses), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.expense_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.amount)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val a = amount.toDoubleOrNull()

                if (title.isEmpty() || a == null) {
                    message = enterValidDataMessage
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    db.expenseDao().insert(
                        Expense(projectId = projectId, title = title, amount = a)
                    )

                    val updated = db.expenseDao().getExpenses(projectId)

                    withContext(Dispatchers.Main) {
                        expenseList = updated
                        title = ""
                        amount = ""
                        message = expenseSavedMessage
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_expense))
        }

        Spacer(modifier = Modifier.height(20.dp))

        val total = expenseList.sumOf { it.amount }
        Text(
            "${stringResource(R.string.total_expense)}: ${String.format(currencyTemplate, total.toString())}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        expenseList.forEach { exp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("${exp.title} - ${String.format(currencyTemplate, exp.amount.toString())}")

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.expenseDao().deleteExpense(exp.id)
                                val updated = db.expenseDao().getExpenses(projectId)

                                withContext(Dispatchers.Main) {
                                    expenseList = updated
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}
