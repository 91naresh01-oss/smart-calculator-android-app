package com.naresh.smartcalculatornote

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat

val Navy: Color @Composable get() = MaterialTheme.colorScheme.primary
val DeepNavy: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val Cyan: Color @Composable get() = MaterialTheme.colorScheme.secondary
val AppRed: Color @Composable get() = MaterialTheme.colorScheme.error
val PageWhite: Color @Composable get() = MaterialTheme.colorScheme.surface
val SoftField: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
val Line: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val IsDarkMode: Boolean @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

// Adaptive semantic colors for tags, status indicators and adjustment cards
val TagGreenBg: Color @Composable get() = if (IsDarkMode) Color(0xFF064E3B).copy(alpha = 0.65f) else Color(0xFFECFDF5)
val TagGreenBorder: Color @Composable get() = if (IsDarkMode) Color(0xFF059669).copy(alpha = 0.6f) else Color(0xFFA7F3D0)
val TagGreenText: Color @Composable get() = if (IsDarkMode) Color(0xFF34D399) else Color(0xFF047857)

val TagRedBg: Color @Composable get() = if (IsDarkMode) Color(0xFF7F1D1D).copy(alpha = 0.55f) else Color(0xFFFEF2F2)
val TagRedBorder: Color @Composable get() = if (IsDarkMode) Color(0xFFDC2626).copy(alpha = 0.5f) else Color(0xFFFECACA)
val TagRedText: Color @Composable get() = if (IsDarkMode) Color(0xFFF87171) else Color(0xFFB91C1C)

val TagAmberBg: Color @Composable get() = if (IsDarkMode) Color(0xFF78350F).copy(alpha = 0.55f) else Color(0xFFFFFBEB)
val TagAmberBorder: Color @Composable get() = if (IsDarkMode) Color(0xFFD97706).copy(alpha = 0.5f) else Color(0xFFFDE68A)
val TagAmberText: Color @Composable get() = if (IsDarkMode) Color(0xFFFBBF24) else Color(0xFFB45309)

val TagBlueBg: Color @Composable get() = if (IsDarkMode) Color(0xFF1E3A8A).copy(alpha = 0.55f) else Color(0xFFEFF6FF)
val TagBlueBorder: Color @Composable get() = if (IsDarkMode) Color(0xFF2563EB).copy(alpha = 0.5f) else Color(0xFFBFDBFE)
val TagBlueText: Color @Composable get() = if (IsDarkMode) Color(0xFF60A5FA) else Color(0xFF1D4ED8)

val TagPurpleBg: Color @Composable get() = if (IsDarkMode) Color(0xFF581C87).copy(alpha = 0.55f) else Color(0xFFFAF5FF)
val TagPurpleBorder: Color @Composable get() = if (IsDarkMode) Color(0xFF7C3AED).copy(alpha = 0.5f) else Color(0xFFE9D5FF)
val TagPurpleText: Color @Composable get() = if (IsDarkMode) Color(0xFFA78BFA) else Color(0xFF6D28D9)

private val LightScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF0F766E), // Deep Teal
    secondary = Color(0xFFD97706), // Warm Amber
    background = Color(0xFFF6F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626)
)
private val DarkScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF2DD4BF), // Radiant Cyan-Teal
    secondary = Color(0xFFFBBF24), // Radiant Amber
    background = Color(0xFF0F172A), // Slate Navy 900
    surface = Color(0xFF1E293B), // Slate Surface 800
    surfaceContainerLow = Color(0xFF0D1525), // Inset Field Surface
    onSurface = Color(0xFFF8FAFC), // Crisp Slate 50
    onSurfaceVariant = Color(0xFF94A3B8), // Slate 400
    outlineVariant = Color(0xFF334155), // Slate Border 700
    error = Color(0xFFF87171)
)

@Composable
fun AppGlossyBackgroundBrush(): Brush {
    val isDark = IsDarkMode
    return if (isDark) {
        Brush.verticalGradient(
            0.0f to Color(0xFF0B2545), // Top: Rich glowing sapphire ambient
            0.35f to Color(0xFF0C192C), // Upper mid: Slate midnight navy
            0.75f to Color(0xFF070F1B), // Lower mid: Deep glossy obsidian
            1.0f to Color(0xFF040810)   // Bottom: Rich jet foundation
        )
    } else {
        Brush.verticalGradient(
            0.0f to Color(0xFFE0F2FE), // Top: Crisp sky-blue gloss tint
            0.25f to Color(0xFFF0FDF4), // Upper mid: Light emerald fresh sheen
            0.65f to Color(0xFFF8FAFC), // Lower mid: Pristine pearl porcelain
            1.0f to Color(0xFFE2E8F0)  // Bottom: Soft slate contoured base
        )
    }
}

@Composable
fun SmartCalculatorApp(viewModel: CalculatorViewModel, openNoteId: String? = null) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(openNoteId, state.notes) {
        if (openNoteId != null && state.notes.any { it.id == openNoteId }) viewModel.openNotes()
    }
    val dark = when (state.theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val view = LocalView.current
    val systemDensity = LocalDensity.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme) {
        CompositionLocalProvider(LocalDensity provides Density(systemDensity.density, systemDensity.fontScale * state.fontScale)) {
          Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppGlossyBackgroundBrush())
            ) {
              ProvideTextStyle(LocalTextStyle.current.copy(fontFamily = AppFontFamily)) {
                ResponsiveReferenceLayout {
                    Column(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                            AppNavigation(state.activeTab, viewModel::select)
                            Box(Modifier.fillMaxWidth().weight(1f)) {
                                when (state.activeTab) {
                                    MainTab.CAL -> CalHubScreen(state, viewModel, openNoteId)
                                    MainTab.FOUR_VALUE -> FourValueScreen(state, viewModel)
                                    MainTab.CASH -> CashScreen(state, viewModel)
                                    MainTab.ORIGINAL -> OriginalScreen(state.originalHistory, viewModel::originalHistory)
                                    MainTab.MORE -> MoreScreen(state, viewModel) {
                                        val calResult = CalculationEngine.rows(state.rows)
                                        val cashResult = CalculationEngine.cash(state.cash, state.toolInputs["addCash"].number(), state.toolInputs["lessCash"].number())
                                        val cal = calResult.error ?: calResult.display
                                        val cash = cashResult.error ?: cashResult.display
                                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Smart Calculator Summary\nCAL Total: $cal\nCash Total: $cash")
                                        }, "Share summary"))
                                    }
                                }
                            }
                        }
                    }
                }
              }
            }
          }
        }
    }
}

@Composable
private fun AppNavigation(selected: MainTab, select: (MainTab) -> Unit) {
    val navIcons = mapOf(
        MainTab.CAL to R.drawable.ic_nav_cal,
        MainTab.FOUR_VALUE to R.drawable.ic_nav_four,
        MainTab.CASH to R.drawable.ic_nav_cash
    )
    val navigationOrder = listOf(
        MainTab.ORIGINAL,
        MainTab.FOUR_VALUE,
        MainTab.CASH,
        MainTab.CAL,
        MainTab.MORE
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (IsDarkMode) Color(0xFF0F172A).copy(alpha = 0.90f) else Color(0xFFFFFFFF).copy(alpha = 0.92f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navigationOrder.forEach { tab ->
            val active = selected == tab
            Box(
                modifier = Modifier.weight(1f).height(56.dp)
                    .pointerInput(tab) { detectTapGestures(onTap = { select(tab) }) },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val navVector = when (tab) {
                        MainTab.ORIGINAL -> Icons.Default.Calculate
                        MainTab.MORE -> Icons.Default.Apps
                        else -> null
                    }
                    if (navVector != null) {
                        val navVectorColor = when (tab) {
                            MainTab.ORIGINAL -> if (IsDarkMode) Color(0xFFA78BFA) else Color(0xFF7C3AED)
                            MainTab.MORE -> if (IsDarkMode) Color(0xFF2DD4BF) else Color(0xFF0D9488)
                            else -> Navy
                        }
                        Icon(
                            imageVector = navVector,
                            contentDescription = tab.label,
                            tint = navVectorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        androidx.compose.foundation.Image(
                            painter = painterResource(navIcons.getValue(tab)),
                            contentDescription = tab.label,
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (active) Navy else DeepNavy.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
                if (active) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth(0.52f)
                            .height(3.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = Navy.copy(alpha = 0.7f),
                                spotColor = Navy.copy(alpha = 0.7f)
                            )
                            .background(Navy, RoundedCornerShape(50))
                    )
                }
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Line.copy(alpha = 0.7f)))
}

@Composable
fun ReferenceCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val isDark = IsDarkMode
    Card(
        modifier = modifier.fillMaxWidth().then(
            if (!isDark) {
                Modifier.shadow(
                    elevation = 4.dp,
                    shape = shape,
                    spotColor = Color(0x18000000),
                    ambientColor = Color(0x0C000000)
                )
            } else {
                Modifier.shadow(
                    elevation = 2.dp,
                    shape = shape,
                    spotColor = Color(0x33000000),
                    ambientColor = Color(0x1A000000)
                )
            }
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.88f) else Color(0xFFFFFFFF).copy(alpha = 0.94f)
        ),
        border = BorderStroke(1.dp, Line.copy(alpha = if (isDark) 0.7f else 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { content() }
}

/** Shared white-box treatment used by calculator fields and compact controls. */
@Composable
fun Modifier.modernBoxSurface(
    shape: Shape = RoundedCornerShape(10.dp),
    elevation: Dp = 2.dp,
    borderColor: Color = Line
): Modifier {
    val isDark = IsDarkMode
    return this
        .then(
            if (!isDark) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    spotColor = Color(0x18000000),
                    ambientColor = Color(0x0A000000)
                )
            } else Modifier
        )
        .clip(shape)
        .background(if (isDark) SoftField else PageWhite)
        .border(1.dp, borderColor, shape)
}

@Composable
fun ResetButton(onClick: () -> Unit, modifier: Modifier = Modifier.height(36.dp)) {
    val shape = RoundedCornerShape(10.dp)
    val gradientBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(Color(0xFFF87171), Color(0xFFE11D48))
    )
    Box(
        modifier = modifier
            .shadow(3.dp, shape, spotColor = Color(0x30E11D48), ambientColor = Color(0x10000000))
            .clip(shape)
            .background(gradientBrush)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text("RESET", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp)
        }
    }
}

@Composable
fun PageHeader(title: String, reset: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, color = DeepNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        reset?.let { ResetButton(it) }
    }
}

@Composable
fun HeroCard(eyebrow: String, result: String, details: String, action: (@Composable () -> Unit)? = null) {
    ReferenceCard {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Muted)
                Text(result, fontSize = 23.sp, fontWeight = FontWeight.Black, color = Navy)
                Text(details, fontSize = 12.sp, color = Muted)
            }
            action?.invoke()
        }
    }
}

@Composable
fun ScreenList(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 11.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

fun String?.number(): Double = this?.replace(",", "")?.toDoubleOrNull() ?: 0.0
