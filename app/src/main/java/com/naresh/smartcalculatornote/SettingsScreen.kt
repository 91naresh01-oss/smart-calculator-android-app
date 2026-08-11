package com.naresh.smartcalculatornote

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat

data class AppLanguage(val tag: String, val label: String)

val SupportedLanguages = listOf(
    AppLanguage("", "Phone language"), AppLanguage("en", "English"), AppLanguage("gu", "ગુજરાતી"),
    AppLanguage("hi", "हिन्दी"), AppLanguage("bn", "বাংলা"), AppLanguage("mr", "मराठी"),
    AppLanguage("pa", "ਪੰਜਾਬੀ"), AppLanguage("ta", "தமிழ்"), AppLanguage("te", "తెలుగు"),
    AppLanguage("kn", "ಕನ್ನಡ"), AppLanguage("ml", "മലയാളം"), AppLanguage("es", "Español"),
    AppLanguage("fr", "Français"), AppLanguage("de", "Deutsch"), AppLanguage("ar", "العربية"),
    AppLanguage("pt", "Português"), AppLanguage("zh-Hans", "简体中文"), AppLanguage("ja", "日本語")
)

@Composable
fun SettingsScreen(state: AppState, viewModel: CalculatorViewModel) {
    val context = LocalContext.current
    val activeTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore(',')
    var languageOpen by remember { mutableStateOf(false) }
    val selectedLanguage = SupportedLanguages.firstOrNull { it.tag == activeTag } ?: SupportedLanguages.first()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingCard("App language", "Works offline") {
            OutlinedButton(onClick = { languageOpen = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedLanguage.label, fontWeight = FontWeight.Bold)
            }
            DropdownMenu(expanded = languageOpen, onDismissRequest = { languageOpen = false }) {
                SupportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(if (language.tag == selectedLanguage.tag) "✓ ${language.label}" else language.label) },
                        onClick = {
                            languageOpen = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
                        }
                    )
                }
            }
        }
        SettingCard("Font size", "Applies to the whole app") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(.85f to "85%", 1f to "100%", 1.15f to "115%", 1.3f to "130%").forEach { (scale, label) ->
                    val active = kotlin.math.abs(state.fontScale - scale) < .01f
                    Button(
                        onClick = { viewModel.fontScale(scale) }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (active) Navy else SoftField, contentColor = if (active) androidx.compose.ui.graphics.Color.White else DeepNavy)
                    ) { Text(label, fontSize = 11.sp) }
                }
            }
            Text("Preview: ₹ 1,23,456", fontSize = 18.sp, color = DeepNavy)
        }
        SettingCard("Appearance", "Choose light, dark or phone setting") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (mode, label) ->
                    val active = state.theme == mode
                    Button(
                        onClick = { viewModel.theme(mode) }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (active) Navy else SoftField, contentColor = if (active) androidx.compose.ui.graphics.Color.White else DeepNavy)
                    ) { Text(label, fontSize = 11.sp) }
                }
            }
        }
        val notificationAllowed = Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val exactAllowed = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()
        SettingCard("Reminder access", if (notificationAllowed && exactAllowed) "Ready" else "Permission needed for precise reminders") {
            Text("Notifications: ${if (notificationAllowed) "Allowed" else "Not allowed"}", color = DeepNavy)
            Text("Exact reminders: ${if (exactAllowed) "Allowed" else "Approximate time"}", color = DeepNavy)
            if (!exactAllowed && Build.VERSION.SDK_INT >= 31) {
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                }) { Text("Allow exact reminders") }
            }
        }
    }
}

@Composable
private fun SettingCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = PageWhite)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = DeepNavy)
            Text(subtitle, fontSize = 11.sp, color = Muted)
            content()
        }
    }
}
