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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

private val InternationalLanguages = SupportedLanguages.filter {
    it.tag in setOf("en", "es", "fr", "de", "ar", "pt", "zh-Hans", "ja")
}

private val IndianLanguages = SupportedLanguages.filter {
    it.tag in setOf("gu", "hi", "bn", "mr", "pa", "ta", "te", "kn", "ml")
}

private fun languageFlagResource(tag: String): Int? = when (tag) {
    "en" -> R.drawable.flag_gb
    "es" -> R.drawable.flag_es
    "fr" -> R.drawable.flag_fr
    "de" -> R.drawable.flag_de
    "ar" -> R.drawable.flag_sa
    "pt" -> R.drawable.flag_pt
    "zh-Hans" -> R.drawable.flag_cn
    "ja" -> R.drawable.flag_jp
    "gu", "hi", "bn", "mr", "pa", "ta", "te", "kn", "ml" -> R.drawable.flag_in
    else -> null
}

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
            if (languageOpen) {
                LanguagePickerDialog(
                    selectedLanguage = selectedLanguage,
                    onDismiss = { languageOpen = false },
                    onSelect = { language ->
                        languageOpen = false
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
                    }
                )
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
private fun LanguagePickerDialog(
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(.92f).widthIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            color = PageWhite,
            tonalElevation = 3.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, Line)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(13.dp), color = Navy.copy(alpha = .12f)) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.padding(10.dp).size(23.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("App language", color = DeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("Works offline", color = Muted, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Muted)
                    }
                }

                LanguageChoice(
                    language = SupportedLanguages.first(),
                    selected = selectedLanguage.tag.isEmpty(),
                    onSelect = onSelect,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Line)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LanguageSectionHeader("International Languages")
                    }
                    items(InternationalLanguages, key = { it.tag }) { language ->
                        LanguageChoice(
                            language = language,
                            selected = language.tag == selectedLanguage.tag,
                            onSelect = onSelect,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LanguageSectionHeader("Indian Languages")
                    }
                    items(IndianLanguages, key = { it.tag }) { language ->
                        LanguageChoice(
                            language = language,
                            selected = language.tag == selectedLanguage.tag,
                            onSelect = onSelect,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Navy,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Line)
    }
}

@Composable
private fun LanguageChoice(
    language: AppLanguage,
    selected: Boolean,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val flagResource = languageFlagResource(language.tag)
    Surface(
        onClick = { onSelect(language) },
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Navy.copy(alpha = .12f) else SoftField,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) Navy else Line)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(width = 30.dp, height = 21.dp),
                shape = RoundedCornerShape(4.dp),
                color = androidx.compose.ui.graphics.Color.White,
                border = BorderStroke(1.dp, Line)
            ) {
                if (flagResource != null) {
                    Image(
                        painter = painterResource(flagResource),
                        contentDescription = "${language.label} flag",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.padding(3.dp).fillMaxSize()
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(language.label, color = DeepNavy, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold, maxLines = 1)
                Text(if (language.tag.isEmpty()) "AUTO" else language.tag.substringBefore('-').uppercase(), color = if (selected) Navy else Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            if (selected) {
                Surface(shape = RoundedCornerShape(50), color = Navy) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(4.dp).size(14.dp)
                    )
                }
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
