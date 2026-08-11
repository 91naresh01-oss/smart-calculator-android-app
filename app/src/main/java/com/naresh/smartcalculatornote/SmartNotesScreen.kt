package com.naresh.smartcalculatornote

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.UUID

@Composable
fun CalHubScreen(state: AppState, viewModel: CalculatorViewModel, openNoteId: String? = null) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            CalSection.entries.forEach { section ->
                val selected = state.calSection == section
                Button(
                    onClick = { viewModel.calSection(section) }, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selected) Navy else SoftField, contentColor = if (selected) Color.White else DeepNavy)
                ) {
                    Text(if (section == CalSection.CALCULATOR) "Note + Cal" else "Smart Note", fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.calSection == CalSection.CALCULATOR) CalScreen(state, viewModel)
        else SmartNotesScreen(state, viewModel, openNoteId)
    }
}

@Composable
fun SmartNotesScreen(state: AppState, viewModel: CalculatorViewModel, openNoteId: String? = null) {
    val context = LocalContext.current
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(openNoteId) { if (openNoteId != null) editingId = openNoteId }
    val editing = state.notes.firstOrNull { it.id == editingId }
    if (editingId != null) {
        NoteEditor(editingId!!, editing, onCancel = { editingId = null }, onSave = { note ->
            viewModel.upsertNote(note)
            if (note.reminderAt != null && !note.completed) ReminderScheduler.schedule(context, note) else ReminderScheduler.cancel(context, note.id)
            editingId = null
        }, onDelete = editing?.let { note ->
            {
                ReminderScheduler.cancel(context, note.id)
                viewModel.deleteNote(note.id)
                editingId = null
            }
        })
        return
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Smart Notes", fontSize = 20.sp, fontWeight = FontWeight.Black, color = DeepNavy)
            Button(onClick = { editingId = UUID.randomUUID().toString() }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("New note")
            }
        }
        if (state.notes.isEmpty()) {
            ReferenceCard {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Navy)
                    Text("No notes yet", fontWeight = FontWeight.Bold, color = DeepNavy)
                    Text("Create a note and add an optional reminder.", color = Muted)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.notes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { editingId = note.id },
                        colors = CardDefaults.cardColors(containerColor = PageWhite)
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.Black, color = if (note.completed) Muted else DeepNavy, modifier = Modifier.weight(1f))
                                Text(if (note.completed) "Done" else "Edit", color = if (note.completed) Muted else Navy, fontWeight = FontWeight.Bold)
                            }
                            if (note.details.isNotBlank()) Text(note.details, maxLines = 2, color = Muted)
                            note.reminderAt?.let { Text("🔔 ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it))} · ${repeatLabel(note.repeat)}", fontSize = 11.sp, color = Navy) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditor(draftId: String, note: SmartNote?, onCancel: () -> Unit, onSave: (SmartNote) -> Unit, onDelete: (() -> Unit)?) {
    val context = LocalContext.current
    val id = note?.id ?: draftId
    var title by rememberSaveable(id) { mutableStateOf(note?.title.orEmpty()) }
    var details by rememberSaveable(id) { mutableStateOf(note?.details.orEmpty()) }
    var completed by rememberSaveable(id) { mutableStateOf(note?.completed ?: false) }
    var reminderEnabled by rememberSaveable(id) { mutableStateOf(note?.reminderAt != null) }
    var reminderAt by rememberSaveable(id) { mutableStateOf(note?.reminderAt ?: (System.currentTimeMillis() + 60 * 60_000L)) }
    var repeat by rememberSaveable(id) { mutableStateOf(note?.repeat ?: ReminderRepeat.NONE) }
    var error by rememberSaveable { mutableStateOf("") }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun chooseDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = reminderAt }
        DatePickerDialog(context, { _, year, month, day ->
            val updated = Calendar.getInstance().apply { timeInMillis = reminderAt; set(year, month, day) }
            reminderAt = updated.timeInMillis
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
    fun chooseTime() {
        val cal = Calendar.getInstance().apply { timeInMillis = reminderAt }
        TimePickerDialog(context, { _, hour, minute ->
            val updated = Calendar.getInstance().apply { timeInMillis = reminderAt; set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0) }
            reminderAt = updated.timeInMillis
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 11.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (note == null) "New note" else "Edit note", fontSize = 20.sp, fontWeight = FontWeight.Black, color = DeepNavy)
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
        item { OutlinedTextField(value = title, onValueChange = { title = it.take(120) }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { OutlinedTextField(value = details, onValueChange = { details = it.take(4000) }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth(), minLines = 5) }
        item {
            ReferenceCard {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text("Reminder", fontWeight = FontWeight.Black, color = DeepNavy); Text("Show a phone notification", fontSize = 11.sp, color = Muted) }
                        Switch(checked = reminderEnabled, onCheckedChange = {
                            reminderEnabled = it
                            if (it && Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        })
                    }
                    if (reminderEnabled) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            OutlinedButton(onClick = ::chooseDate, modifier = Modifier.weight(1f)) { Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(reminderAt))) }
                            OutlinedButton(onClick = ::chooseTime, modifier = Modifier.weight(1f)) { Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(reminderAt))) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReminderRepeat.entries.forEach { option ->
                                Button(
                                    onClick = { repeat = option }, modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (repeat == option) Navy else SoftField, contentColor = if (repeat == option) Color.White else DeepNavy)
                                ) { Text(repeatLabel(option), fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }
        if (note != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mark as done", fontWeight = FontWeight.Bold, color = DeepNavy)
                    Switch(checked = completed, onCheckedChange = { completed = it; if (it) reminderEnabled = false })
                }
            }
        }
        if (error.isNotBlank()) item { Text(error, color = AppRed) }
        item {
            Button(onClick = {
                if (title.isBlank() && details.isBlank()) { error = "Enter a title or note details."; return@Button }
                if (reminderEnabled && reminderAt <= System.currentTimeMillis()) { error = "Choose a future reminder time."; return@Button }
                onSave(SmartNote(id, title.trim(), details.trim(), note?.createdAt ?: System.currentTimeMillis(), System.currentTimeMillis(), completed, if (reminderEnabled && !completed) reminderAt else null, if (reminderEnabled) repeat else ReminderRepeat.NONE))
            }, modifier = Modifier.fillMaxWidth()) { Text("Save note", fontWeight = FontWeight.Bold) }
        }
        if (onDelete != null) item {
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = AppRed)
                Text("Delete note", color = AppRed)
            }
        }
    }
}

private fun repeatLabel(value: ReminderRepeat) = when (value) {
    ReminderRepeat.NONE -> "Once"
    ReminderRepeat.DAILY -> "Daily"
    ReminderRepeat.WEEKLY -> "Weekly"
}
