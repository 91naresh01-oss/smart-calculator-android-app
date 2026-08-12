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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private val SettingsGreen = Color(0xFF078A4D)
private val SettingsBlue = Color(0xFF1769E8)
private val SettingsPurple = Color(0xFF7047EB)
private val SettingsOrange = Color(0xFFF06A00)
private val SettingsCoral = Color(0xFFE43D30)
private val SettingsIndigo = Color(0xFF4F5B89)

@Composable
fun SettingsScreen(state: AppState, viewModel: CalculatorViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val activeTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore(',')
    var languageOpen by remember { mutableStateOf(false) }
    val selectedLanguage = SupportedLanguages.firstOrNull { it.tag == activeTag } ?: SupportedLanguages.first()
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("Settings", color = DeepNavy, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("Customize your calculator experience", color = Muted, fontSize = 14.sp)
            }
            Surface(
                onClick = onBack,
                modifier = Modifier.size(42.dp).shadow(3.dp, RoundedCornerShape(13.dp), spotColor = Color(0x24000000)),
                shape = RoundedCornerShape(13.dp),
                color = PageWhite,
                border = BorderStroke(1.dp, Line)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "All tools", tint = DeepNavy, modifier = Modifier.size(21.dp))
                }
            }
        }

        PremiumSettingsCard {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsIcon(Icons.Rounded.CheckCircle, SettingsGreen, Color(0xFFE4F7E4), 44.dp)
                Column(Modifier.weight(1f)) {
                    Text("All systems ready", color = DeepNavy, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("Your preferences are active", color = Muted, fontSize = 12.sp)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = SettingsGreen, modifier = Modifier.size(25.dp))
            }
        }

        SettingCard(
            title = "App language",
            subtitle = "Works offline",
            icon = Icons.Rounded.Language,
            iconTint = SettingsBlue,
            iconBackground = Color(0xFFE6F0FF)
        ) {
                Surface(
                    onClick = { languageOpen = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.5.dp, RoundedCornerShape(11.dp), spotColor = Color(0x26000000)),
                    shape = RoundedCornerShape(11.dp),
                    color = PageWhite,
                    border = BorderStroke(1.dp, Line)
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(selectedLanguage.label, modifier = Modifier.weight(1f), color = DeepNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                    }
                }
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

        SettingCard(
            title = "Font size",
            subtitle = "Applies to the whole app",
            icon = Icons.Rounded.FormatSize,
            iconTint = SettingsPurple,
            iconBackground = Color(0xFFF0EAFF)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(.85f to "85%", 1f to "100%", 1.15f to "115%", 1.3f to "130%").forEach { (scale, label) ->
                    val active = kotlin.math.abs(state.fontScale - scale) < .01f
                    CompactSettingChoice(
                        label = label,
                        selected = active,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.fontScale(scale) }
                    )
                }
            }
            HorizontalDivider(color = Line)
            Text("Preview: ₹ 1,23,456", fontSize = 16.sp, color = DeepNavy, fontWeight = FontWeight.Medium, maxLines = 1)
        }

        SettingCard(
            title = "Appearance",
            subtitle = "Choose light, dark or phone setting",
            icon = Icons.Rounded.Palette,
            iconTint = SettingsOrange,
            iconBackground = Color(0xFFFFEBDD)
        ) {
            Row(
                Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(11.dp), spotColor = Color(0x22000000)),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                AppearanceChoice("System", Icons.Rounded.Settings, state.theme == ThemeMode.SYSTEM, Color(0xFF5C6477), Modifier.weight(1f)) { viewModel.theme(ThemeMode.SYSTEM) }
                AppearanceChoice("Light", Icons.Rounded.LightMode, state.theme == ThemeMode.LIGHT, Color(0xFFFFA12F), Modifier.weight(1f)) { viewModel.theme(ThemeMode.LIGHT) }
                AppearanceChoice("Dark", Icons.Rounded.DarkMode, state.theme == ThemeMode.DARK, SettingsIndigo, Modifier.weight(1f)) { viewModel.theme(ThemeMode.DARK) }
            }
        }

        val notificationAllowed = Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val exactAllowed = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()
        SettingCard(
            title = "Reminder access",
            subtitle = if (notificationAllowed && exactAllowed) "Ready" else "Permission needed for precise reminders",
            icon = Icons.Rounded.NotificationsActive,
            iconTint = SettingsCoral,
            iconBackground = Color(0xFFFFE5E5)
        ) {
            HorizontalDivider(color = Line)
            PermissionRow(
                icon = Icons.Rounded.NotificationsActive,
                iconTint = SettingsCoral,
                iconBackground = Color(0xFFFFE5E5),
                label = "Notifications: ${if (notificationAllowed) "Allowed" else "Not allowed"}",
                allowed = notificationAllowed
            )
            HorizontalDivider(color = Line)
            PermissionRow(
                icon = Icons.Rounded.AccessTime,
                iconTint = SettingsBlue,
                iconBackground = Color(0xFFE6F0FF),
                label = "Exact reminders: ${if (exactAllowed) "Allowed" else "Approximate time"}",
                allowed = exactAllowed
            )
            if (!exactAllowed && Build.VERSION.SDK_INT >= 31) {
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                }) { Text("Allow exact reminders") }
            }
        }
    }
}

@Composable
private fun PremiumSettingsCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(15.dp)
    Card(
        modifier = modifier.fillMaxWidth().shadow(
            elevation = 7.dp,
            shape = shape,
            spotColor = Color(0x38000000),
            ambientColor = Color(0x16000000)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = PageWhite),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { content() }
}

@Composable
private fun SettingsIcon(icon: ImageVector, tint: Color, background: Color, boxSize: androidx.compose.ui.unit.Dp = 42.dp) {
    Surface(modifier = Modifier.size(boxSize), shape = RoundedCornerShape(13.dp), color = background) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun CompactSettingChoice(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp).shadow(2.dp, shape, spotColor = Color(0x24000000)),
        shape = shape,
        color = if (selected) SettingsGreen else PageWhite,
        border = BorderStroke(1.dp, if (selected) SettingsGreen else Line)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) Color.White else DeepNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun AppearanceChoice(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    iconTint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(68.dp),
        color = if (selected) SettingsGreen else PageWhite,
        border = BorderStroke(1.dp, if (selected) SettingsGreen else Line)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (selected) Color(0xFFFFB24C) else iconTint, modifier = Modifier.size(22.dp))
            Text(label, color = if (selected) Color.White else DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PermissionRow(icon: ImageVector, iconTint: Color, iconBackground: Color, label: String, allowed: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingsIcon(icon, iconTint, iconBackground, 36.dp)
        Text(label, modifier = Modifier.weight(1f), color = DeepNavy, fontSize = 13.sp, maxLines = 2)
        Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = if (allowed) SettingsGreen else Muted,
            modifier = Modifier.size(23.dp)
        )
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
        color = if (selected) Navy.copy(alpha = .12f) else PageWhite,
        shadowElevation = if (selected) 1.dp else 2.dp,
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
private fun SettingCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    PremiumSettingsCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsIcon(icon, iconTint, iconBackground)
            Column {
                Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = DeepNavy, maxLines = 2)
                Text(subtitle, fontSize = 11.sp, color = Muted, maxLines = 2)
            }
            content()
        }
    }
}
