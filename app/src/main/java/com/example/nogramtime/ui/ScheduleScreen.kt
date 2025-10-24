package com.example.nogramtime.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nogramtime.data.BlockRule
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Screen that allows users to view, add, edit and delete blocking rules. Each
 * rule defines a time window and the days of the week on which Instagram
 * should be blocked. A floating action button invokes a dialog to create a
 * new rule.
 */
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    val rules by viewModel.rules.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<BlockRule?>(null) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                ruleToEdit = null
                showDialog = true
            }) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(rules) { rule ->
                RuleRow(rule = rule, onEdit = {
                    ruleToEdit = it
                    showDialog = true
                }, onDelete = {
                    viewModel.deleteRule(it)
                })
            }
        }
        if (showDialog) {
            AddEditRuleDialog(
                initial = ruleToEdit,
                onDismiss = { showDialog = false },
                onSave = { newRule ->
                    if (ruleToEdit == null) {
                        viewModel.insertRule(newRule)
                    } else {
                        viewModel.updateRule(newRule.copy(id = ruleToEdit!!.id))
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun RuleRow(rule: BlockRule, onEdit: (BlockRule) -> Unit, onDelete: (BlockRule) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val daysString = rule.days.joinToString(separator = ",") { dayInt ->
            DayOfWeek.of(dayInt).name.take(3)
        }
        Text(
            text = "$daysString ${twoDigits(rule.startHour)}:${twoDigits(rule.startMinute)} – ${twoDigits(rule.endHour)}:${twoDigits(rule.endMinute)}",
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onEdit(rule) }) {
            Icon(Icons.Filled.Edit, contentDescription = "Modifica")
        }
        IconButton(onClick = { onDelete(rule) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Elimina")
        }
    }
}

private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

@Composable
private fun AddEditRuleDialog(
    initial: BlockRule?,
    onDismiss: () -> Unit,
    onSave: (BlockRule) -> Unit
) {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf(LocalTime.of(initial?.startHour ?: 8, initial?.startMinute ?: 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(initial?.endHour ?: 9, initial?.endMinute ?: 0)) }
    val allDays = DayOfWeek.values().toList()
    val selectedDays = remember { mutableStateListOf<Int>() }
    // Preselect days when editing
    LaunchedEffect(initial) {
        selectedDays.clear()
        initial?.days?.let { selectedDays.addAll(it) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (initial == null) "Nuova regola" else "Modifica regola")
        },
        text = {
            Column {
                // Day of week selection
                Text(text = "Giorni:")
                allDays.forEach { day ->
                    val checked = selectedDays.contains(day.value)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    if (!selectedDays.contains(day.value)) selectedDays.add(day.value)
                                } else {
                                    selectedDays.remove(day.value)
                                }
                            }
                        )
                        Text(text = day.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Start time picker
                Button(onClick = {
                    TimePickerDialog(
                        context,
                        { _, h: Int, m: Int ->
                            startTime = LocalTime.of(h, m)
                        },
                        startTime.hour,
                        startTime.minute,
                        true
                    ).show()
                }) {
                    Text(text = "Ora inizio: ${twoDigits(startTime.hour)}:${twoDigits(startTime.minute)}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                // End time picker
                Button(onClick = {
                    TimePickerDialog(
                        context,
                        { _, h: Int, m: Int ->
                            endTime = LocalTime.of(h, m)
                        },
                        endTime.hour,
                        endTime.minute,
                        true
                    ).show()
                }) {
                    Text(text = "Ora fine: ${twoDigits(endTime.hour)}:${twoDigits(endTime.minute)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedDays.isNotEmpty()) {
                        onSave(
                            BlockRule(
                                startHour = startTime.hour,
                                startMinute = startTime.minute,
                                endHour = endTime.hour,
                                endMinute = endTime.minute,
                                days = selectedDays.sorted()
                            )
                        )
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}