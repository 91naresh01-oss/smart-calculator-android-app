package com.naresh.smartcalculatornote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * The reference page uses 360px as its normal phone canvas, with compact, large-phone
 * and wide breakpoints.  Applying the scale through LocalDensity keeps every existing
 * dp/sp measurement in sync, including Material controls that are composed deeper down.
 */
enum class ReferenceScreenSize { COMPACT, PHONE, LARGE_PHONE, WIDE }

data class ReferenceLayoutMetrics(
    val screenSize: ReferenceScreenSize,
    val densityScale: Float,
    val moreColumns: Int
) {
    companion object {
        fun from(width: Dp): ReferenceLayoutMetrics = when {
            width <= Dp(360f) -> ReferenceLayoutMetrics(ReferenceScreenSize.COMPACT, 0.92f, 2)
            width <= Dp(519f) -> ReferenceLayoutMetrics(ReferenceScreenSize.PHONE, 1.00f, 2)
            width <= Dp(768f) -> ReferenceLayoutMetrics(ReferenceScreenSize.LARGE_PHONE, 1.02f, 3)
            else -> ReferenceLayoutMetrics(ReferenceScreenSize.WIDE, 1.05f, 4)
        }
    }
}

val LocalReferenceLayoutMetrics = staticCompositionLocalOf {
    ReferenceLayoutMetrics(ReferenceScreenSize.PHONE, 1f, 2)
}

@Composable
fun ResponsiveReferenceLayout(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sourceDensity = LocalDensity.current
        val metrics = remember(maxWidth) { ReferenceLayoutMetrics.from(maxWidth) }
        val adjustedDensity = remember(sourceDensity.density, sourceDensity.fontScale, metrics.densityScale) {
            Density(sourceDensity.density * metrics.densityScale, sourceDensity.fontScale)
        }
        CompositionLocalProvider(
            LocalReferenceLayoutMetrics provides metrics,
            LocalDensity provides adjustedDensity
        ) {
            Box(Modifier.fillMaxSize()) { content() }
        }
    }
}
