package com.naresh.smartcalculatornote

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CompactVisibleHeight = 40.dp
private val CompactFieldShape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)

/**
 * Compact native field with an exact visual frame.
 *
 * Material's DecorationBox adds a minimum interactive height after measurement, which
 * enlarged the source-matched 32dp and 36dp controls. The border is drawn directly so
 * the requested height is also the rendered height.
 */
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    isError: Boolean = false,
    height: Dp = CompactVisibleHeight,
    shape: Shape = CompactFieldShape,
    plainWhenIdle: Boolean = false,
    autoFit: Boolean = true,
    indianNumber: Boolean = keyboardOptions.keyboardType == KeyboardType.Number || keyboardOptions.keyboardType == KeyboardType.Decimal
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val border = when {
        isError -> AppRed
        focused -> Navy
        else -> Line
    }
    val showFrame = !plainWhenIdle || focused
    val boxAlignment = when (textStyle.textAlign) {
        TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
        TextAlign.Center -> Alignment.Center
        else -> Alignment.CenterStart
    }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, selection = TextRange(value.length))) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            val cursor = fieldValue.selection.end.coerceAtMost(value.length)
            fieldValue = TextFieldValue(value, selection = TextRange(cursor))
        }
    }
    val displayLength = if (indianNumber) CalculationEngine.formatTyping(value).length else value.length
    val fittedSize = when {
        !autoFit -> textStyle.fontSize
        displayLength > 20 -> 9.sp
        displayLength > 16 -> 10.sp
        displayLength > 12 -> 11.sp
        else -> textStyle.fontSize
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        BasicTextField(
            value = fieldValue,
            onValueChange = { next -> fieldValue = next; onValueChange(next.text) },
            modifier = Modifier.fillMaxWidth().height(height),
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle.copy(fontFamily = AppFontFamily, fontSize = fittedSize),
            keyboardOptions = keyboardOptions,
            visualTransformation = if (indianNumber) IndianNumberVisualTransformation else visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    Modifier.fillMaxWidth().height(height)
                        .then(if (showFrame) Modifier.modernBoxSurface(shape = shape, borderColor = border) else Modifier)
                        .alpha(if (enabled) 1f else 0.55f)
                ) {
                    if (label != null && value.isNotBlank()) {
                        Box(Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 1.dp)) {
                            ProvideTextStyle(LocalTextStyle.current.copy(fontSize = 9.sp, color = Muted)) { label() }
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().height(height)
                            .padding(horizontal = 7.dp, vertical = if (label != null && value.isNotBlank()) 5.dp else 4.dp),
                        contentAlignment = boxAlignment
                    ) {
                        if (value.isBlank()) {
                            if (label != null) {
                                ProvideTextStyle(LocalTextStyle.current.copy(color = Muted)) { label() }
                            } else {
                                placeholder?.invoke()
                            }
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

private object IndianNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = CalculationEngine.formatTyping(raw)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = CalculationEngine.formatTyping(raw.take(offset.coerceIn(0, raw.length))).length.coerceAtMost(formatted.length)
            override fun transformedToOriginal(offset: Int): Int {
                val target = offset.coerceIn(0, formatted.length)
                return (0..raw.length).lastOrNull { originalToTransformed(it) <= target } ?: 0
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
