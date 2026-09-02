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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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

private val LightScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF1C735E),
    secondary = Color(0xFFB98218),
    background = Color(0xFFF7FAF9),
    surface = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF6B7280),
    outlineVariant = Color(0xFFD7DEDB),
    error = Color(0xFFB3261E)
)
private val DarkScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF9EB8FF),
    secondary = Color(0xFF67E0D4),
    background = Color(0xFF101827),
    surface = Color(0xFF172338),
    onSurface = Color(0xFFF5F7FF),
    onSurfaceVariant = Color(0xFFBDCAE1),
    surfaceContainerLow = Color(0xFF1D2A40),
    outlineVariant = Color(0xFF33445E),
    error = Color(0xFFFFB4AB)
)

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
          Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            ProvideTextStyle(LocalTextStyle.current.copy(fontFamily = AppFontFamily)) {
                ResponsiveReferenceLayout {
                    Column(Modifier.fillMaxSize()) {
                        // Apply safe drawing padding only to the content area if needed, 
                        // but let's manage it more precisely to avoid double padding if the layout already handles it.
                        // However, standardizing on a top-level padding for the entire screen content is safer.
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
        modifier = Modifier.fillMaxWidth().height(54.dp).background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navigationOrder.forEach { tab ->
            val active = selected == tab
            Box(
                modifier = Modifier.weight(1f).height(54.dp)
                    .pointerInput(tab) { detectTapGestures(onTap = { select(tab) }) },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 5.dp),
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
                            MainTab.ORIGINAL -> Color(0xFF7655B5)
                            MainTab.MORE -> Color(0xFF008A75)
                            else -> Navy
                        }
                        Icon(
                            imageVector = navVector,
                            contentDescription = tab.label,
                            tint = navVectorColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        androidx.compose.foundation.Image(
                            painter = painterResource(navIcons.getValue(tab)),
                            contentDescription = tab.label,
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (active) Navy else DeepNavy,
                        textAlign = TextAlign.Center
                    )
                }
                if (active) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth(0.5f)
                            .height(3.dp)
                            .shadow(
                                elevation = 7.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = Color(0xFF72D8AC).copy(alpha = 0.8f),
                                spotColor = Color(0xFF72D8AC).copy(alpha = 0.8f)
                            )
                            .background(Color(0xFF63C99B), RoundedCornerShape(50))
                    )
                }
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
}

@Composable
fun ReferenceCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Card(
        modifier = modifier.fillMaxWidth().shadow(
            elevation = 5.dp,
            shape = shape,
            spotColor = Color(0x26000000),
            ambientColor = Color(0x12000000)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { content() }
}

/** Shared white-box treatment used by calculator fields and compact controls. */
@Composable
fun Modifier.modernBoxSurface(
    shape: Shape = RoundedCornerShape(10.dp),
    elevation: Dp = 2.5.dp,
    borderColor: Color = Line
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        spotColor = Color(0x24000000),
        ambientColor = Color(0x10000000)
    )
    .clip(shape)
    .background(PageWhite)
    .border(1.dp, borderColor, shape)

@Composable
fun ResetButton(onClick: () -> Unit, modifier: Modifier = Modifier.height(40.dp)) {
    val shape = RoundedCornerShape(12.dp)
    val gradientBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(Color(0xFFEF5350), Color(0xFFC62828))
    )
    Box(
        modifier = modifier
            .shadow(4.dp, shape, spotColor = Color(0x40C62828), ambientColor = Color(0x18000000))
            .clip(shape)
            .background(gradientBrush)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text("RESET", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
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
