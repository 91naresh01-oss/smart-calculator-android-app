package com.naresh.smartcalculatornote

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun CalScreen(state: AppState, viewModel: CalculatorViewModel) {
    val result = CalculationEngine.rows(state.rows)
    var operatorPickerFor by rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { PageHeader("My Calculation") { viewModel.rows(listOf(CalcRow(UUID.randomUUID().toString()))) } }
            items(state.rows, key = { it.id }) { row ->
                val index = state.rows.indexOfFirst { it.id == row.id } + 1
                Row(
                    Modifier.fillMaxWidth().height(54.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier.width(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(index.toString(), color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    CompactTextField(
                        value = row.label,
                        onValueChange = { text ->
                            val capitalized = text.replaceFirstChar { char ->
                                if (char.isLowerCase()) char.titlecase() else char.toString()
                            }
                            viewModel.rows(state.rows.map { if (it.id == row.id) it.copy(label = capitalized) else it })
                        },
                        placeholder = { Text("Short info...", fontWeight = FontWeight.Normal, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        plainWhenIdle = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = DeepNavy, fontWeight = FontWeight.Normal)
                    )
                    CompactTextField(
                        value = row.amount,
                        onValueChange = { text ->
                            val formatted = CalculationEngine.rawTyping(text)
                            viewModel.rows(state.rows.map { if (it.id == row.id) it.copy(amount = formatted) else it })
                        },
                        placeholder = { Text("0") },
                        modifier = Modifier.width(84.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        height = 41.dp,
                        plainWhenIdle = true,
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold, color = DeepNavy)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { operatorPickerFor = row.id },
                            modifier = Modifier.size(39.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            shape = RoundedCornerShape(11.dp),
                            border = BorderStroke(1.dp, Line),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftField, contentColor = DeepNavy)
                        ) { Text(row.operator.symbol, color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 17.sp) }
                        DropdownMenu(
                            expanded = operatorPickerFor == row.id,
                            onDismissRequest = { operatorPickerFor = null },
                            modifier = Modifier.widthIn(min = 150.dp),
                            shape = RoundedCornerShape(14.dp),
                            containerColor = PageWhite,
                            tonalElevation = 0.dp,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Line)
                        ) {
                            Operator.entries.forEach { operation ->
                                DropdownMenuItem(
                                    text = { Text(operation.symbol, color = DeepNavy, fontSize = 19.sp, fontWeight = FontWeight.Black) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    onClick = {
                                        viewModel.rows(state.rows.map { if (it.id == row.id) it.copy(operator = operation) else it })
                                        operatorPickerFor = null
                                    }
                                )
                            }
                        }
                    }
                    if (state.rows.size > 1) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove row",
                            tint = AppRed,
                            modifier = Modifier.size(30.dp).clickable { viewModel.rows(state.rows.filterNot { it.id == row.id }) }.padding(5.dp)
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { viewModel.rows(state.rows + CalcRow(UUID.randomUUID().toString())) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    border = BorderStroke(1.dp, Navy.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftField, contentColor = Navy)
                ) {
                    Icon(Icons.Default.Add, null, tint = Navy, modifier = Modifier.size(16.dp))
                    Text("Add Row", color = Navy, fontWeight = FontWeight.Bold)
                }
            }
        }
        ReferenceCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Calculation · ${state.rows.count { it.amount.isNotBlank() }} Items", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(
                        result.error ?: result.details.firstOrNull() ?: result.display,
                        color = if (result.error == null) Muted else AppRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2
                    )
                }
                val isDark = IsDarkMode
                Button(
                    onClick = { if (result.error == null) viewModel.saveHistory("CAL Total", result.display) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White)
                ) { Text("= TOTAL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${state.rows.count { it.amount.isNotBlank() }} Items", color = Navy, fontWeight = FontWeight.SemiBold)
                    Text(
                        result.display,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 21.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .widthIn(min = 76.dp)
                            .background(Navy, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun CashScreen(state: AppState, viewModel: CalculatorViewModel) {
    val add = state.toolInputs["addCash"].number()
    val less = state.toolInputs["lessCash"].number()
    val result = CalculationEngine.cash(state.cash, add, less)
    val count = state.cash.values.sumOf { (it.toIntOrNull() ?: 0).coerceAtLeast(0).toLong() }
    val reset = {
        viewModel.cash(listOf(500, 200, 100, 50, 20, 10).associateWith { "" })
        viewModel.input("addCash", "")
        viewModel.input("lessCash", "")
    }
    ScreenList {
        item { PageHeader("Cash Counter", reset) }
        item {
            ReferenceCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.cash.entries.sortedByDescending { it.key }.forEach { (denomination, quantity) ->
                        CashRow(denomination, quantity) { text -> viewModel.cash(state.cash + (denomination to text.filter(Char::isDigit))) }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                CashStat("Total Notes", Icons.Default.Payments, count.toString(), Modifier.weight(1f))
                CashStat("Notes Value", Icons.Default.CurrencyRupee, result.display, Modifier.weight(1f))
            }
        }
        item {
            ReferenceCard {
                Column(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("MANUAL ADJUSTMENT", color = Muted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Adjustment("+ Add Cash", TagGreenBg, TagGreenBorder, TagGreenText, state.toolInputs["addCash"].orEmpty(), { viewModel.input("addCash", it) }, Modifier.weight(1f))
                        Adjustment("− Less Cash", TagRedBg, TagRedBorder, TagRedText, state.toolInputs["lessCash"].orEmpty(), { viewModel.input("lessCash", it) }, Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                    HorizontalDivider(color = Line, modifier = Modifier.fillMaxWidth())
                    Text(
                        "SUMMARY",
                        color = Muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .background(SoftField, RoundedCornerShape(12.dp))
                            .border(1.dp, Line, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    )
                }
            }
        }
        item {
            val isDark = IsDarkMode
            val cashCardGradient = if (isDark) {
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF134E4A), Color(0xFF0F3633))
                )
            } else {
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F766E), Color(0xFF065F46))
                )
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2DD4BF).copy(alpha = 0.35f) else Color.Transparent),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cashCardGradient)
            ) {
                Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL AMOUNT", color = Color(0xFFCCFBF1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(result.error ?: result.display, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(1.dp).fillMaxWidth().background(Color(0xFF2DD4BF).copy(alpha = 0.3f)))
                    Text(result.error ?: CalculationEngine.indianWords(result.value ?: 0.0), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("${count} notes · ${result.details.getOrNull(2).orEmpty()}", color = Color(0xFFCCFBF1), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CashRow(denomination: Int, quantity: String, onChange: (String) -> Unit) {
    val image = mapOf(500 to R.drawable.note_500, 200 to R.drawable.note_200, 100 to R.drawable.note_100, 50 to R.drawable.note_50, 20 to R.drawable.note_20, 10 to R.drawable.note_10).getValue(denomination)
    val lineTotal = CalculationEngine.format(denomination.toDouble() * (quantity.replace(",", "").toIntOrNull() ?: 0).coerceAtLeast(0))
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        androidx.compose.foundation.Image(
            painter = painterResource(image),
            contentDescription = "$denomination rupee note",
            modifier = Modifier
                .width(48.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(3.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(8.dp))
        Text(
            denomination.toString(),
            color = DeepNavy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.widthIn(min = 34.dp)
        )
        Text("×", color = DeepNavy)
        CompactTextField(
            value = quantity,
            onValueChange = { onChange(CalculationEngine.rawTyping(it)) },
            placeholder = { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            modifier = Modifier.width(58.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            height = 36.dp,
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, color = DeepNavy, fontWeight = FontWeight.Bold)
        )
        Text("=", color = Muted)
        Text(lineTotal, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CashStat(title: String, icon: ImageVector, value: String, modifier: Modifier) {
    ReferenceCard(modifier.heightIn(min = 55.dp)) {
        Column(Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
            Text(title, fontSize = 10.sp, color = Muted, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                Text(value, color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
        }
    }
}

@Composable
private fun Adjustment(title: String, background: Color, border: Color, color: Color, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = background), border = BorderStroke(1.dp, border)) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
            CompactTextField(
                value = value,
                onValueChange = { onChange(CalculationEngine.rawTyping(it)) },
                placeholder = { Text("0") },
                modifier = Modifier.width(64.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                height = 30.dp,
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End, fontSize = 12.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun OriginalScreen(history: List<HistoryEntry>, onHistory: (List<HistoryEntry>) -> Unit) {
    var rawDisplay by rememberSaveable { mutableStateOf("0") }
    var error by rememberSaveable { mutableStateOf("") }
    val keys = listOf("C", "⌫", "/", "*", "7", "8", "9", "-", "4", "5", "6", "+", "1", "2", "3", "%", "0", "00", ".", "=")
    fun press(key: String) {
        error = ""
        when (key) {
            "C" -> rawDisplay = "0"
            "⌫" -> rawDisplay = if (rawDisplay.length > 1) rawDisplay.dropLast(1) else "0"
            "=" -> {
                val expression = rawDisplay.replace(Regex("[+\\-*/]+$"), "")
                if (expression.isBlank() || !expression.any(Char::isDigit)) {
                    error = "Invalid expression."
                } else {
                    runCatching { CalculationEngine.evaluate(expression) }
                        .onSuccess { value ->
                            val result = CalculationEngine.raw(value)
                            onHistory((listOf(HistoryEntry(UUID.randomUUID().toString(), CalculationEngine.formatIndianExpression(rawDisplay), CalculationEngine.format(value), System.currentTimeMillis())) + history).take(100))
                            rawDisplay = result
                        }
                        .onFailure { error = it.message ?: "Invalid expression." }
                }
            }
            else -> {
                val operator = key in listOf("+", "-", "*", "/", "%")
                if (rawDisplay == "0") {
                    rawDisplay = when {
                        key.firstOrNull()?.isDigit() == true -> key
                        key == "00" -> "0"
                        key == "." -> "0."
                        operator -> rawDisplay + key
                        else -> rawDisplay
                    }
                } else {
                    val last = rawDisplay.lastOrNull()?.toString().orEmpty()
                    rawDisplay = if (operator && last in listOf("+", "-", "*", "/", "%")) rawDisplay.dropLast(1) + key else rawDisplay + key
                }
            }
        }
    }
    val displayedHistory = remember(history) { history.reversed() }
    val listState = rememberLazyListState()
    LaunchedEffect(displayedHistory.size) {
        if (displayedHistory.isNotEmpty()) {
            listState.animateScrollToItem(displayedHistory.size)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PageHeader("Original Calculator") { rawDisplay = "0"; error = ""; onHistory(emptyList()) }
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            val maxHistoryHeight = maxHeight
            ReferenceCard(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().heightIn(max = maxHistoryHeight).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Text("CALCULATION HISTORY", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                    if (displayedHistory.isEmpty()) {
                        item {
                            Text(
                                "No calculations yet",
                                color = Muted,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(displayedHistory, key = { it.id }) { item ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.label, color = Muted, fontSize = 13.sp)
                                    Text("= ${item.value}", color = DeepNavy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Box(Modifier.fillMaxWidth().height(1.dp).background(Line.copy(alpha = 0.6f)))
                            }
                        }
                    }
                }
            }
        }
        ReferenceCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(9.dp)) {
                Text(
                    CalculationEngine.formatIndianExpression(rawDisplay),
                    color = DeepNavy,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().modernBoxSurface(RoundedCornerShape(12.dp), 3.dp).padding(10.dp)
                )
                if (error.isNotBlank()) Text(error, color = AppRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                val isDark = IsDarkMode
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().height(308.dp).padding(top = 8.dp),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(keys) { key ->
                        val action = key in listOf("/", "*", "-", "+")
                        val reset = key == "C"
                        val equal = key == "="
                        val percent = key == "%"
                        if (reset) {
                            val resetShape = RoundedCornerShape(10.dp)
                            val cGradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color(0xFFF87171), Color(0xFFE11D48))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .shadow(2.dp, resetShape, spotColor = Color(0x25E11D48))
                                    .clip(resetShape)
                                    .background(cGradient)
                                    .pointerInput(key) { detectTapGestures(onTap = { press(key) }) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("C", color = Color.White, fontWeight = FontWeight.Black, fontSize = 21.sp)
                            }
                        } else {
                            val containerColor = when {
                                equal -> Navy
                                percent -> TagAmberBg
                                action -> TagBlueBg
                                else -> SoftField
                            }
                            val contentColor = when {
                                equal -> Color.White
                                percent -> TagAmberText
                                action -> TagBlueText
                                else -> DeepNavy
                            }
                            val borderColor = when {
                                equal -> Color.Transparent
                                percent -> TagAmberBorder
                                action -> TagBlueBorder
                                else -> Line
                            }
                            Button(
                                onClick = { press(key) },
                                modifier = Modifier.height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = containerColor,
                                    contentColor = contentColor
                                ),
                                border = BorderStroke(1.dp, borderColor),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
                            ) {
                                Text(when (key) { "/" -> "÷"; "*" -> "×"; else -> key }, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FourValueScreen(state: AppState, viewModel: CalculatorViewModel) {
    val mode = state.fourValueMode
    val financeMode = mode == FourValueMode.EMI || mode == FourValueMode.INTEREST
    var answer by remember(mode) { mutableStateOf<CalculationResult?>(null) }
    var emiAdvanced by rememberSaveable { mutableStateOf(false) }
    fun field(index: Int) = state.toolInputs[mode.valueKey(index)] ?: mode.defaultValues[index]
    fun unit(index: Int) = state.toolInputs[mode.unitKey(index)] ?: mode.defaultUnits[index]
    fun updateField(index: Int, value: String) {
        answer = null
        viewModel.input(mode.valueKey(index), CalculationEngine.rawTyping(value))
    }
    fun updateUnit(index: Int, value: String) {
        answer = null
        viewModel.input(mode.unitKey(index), value)
        if (mode == FourValueMode.DAILY && index in listOf(1, 3)) {
            val other = if (index == 1) 3 else 1
            if (CalculationEngine.compatibleDailyUnit(unit(other)) != CalculationEngine.compatibleDailyUnit(value)) {
                viewModel.input(mode.unitKey(other), CalculationEngine.compatibleDailyUnit(value))
            }
        }
    }
    fun calculate() {
        val raw = List(mode.fieldCount, ::field)
        val units = List(mode.fieldCount, ::unit)
        val result = when (mode) {
            FourValueMode.EMI -> CalculationEngine.smartEmi(raw, unit(2))
            FourValueMode.PROFIT -> CalculationEngine.smartProfit(raw)
            FourValueMode.INTEREST -> CalculationEngine.smartInterest(
                raw,
                state.toolInputs["four-interest-type"] ?: "simple",
                state.toolInputs["four-interest-frequency"]?.toIntOrNull() ?: 1,
                unit(2)
            )
            else -> CalculationEngine.smartRatio(mode, raw, units)
        }
        answer = result
        if (result.error == null && result.solvedIndex != null && result.value != null) {
            viewModel.input(mode.valueKey(result.solvedIndex), CalculationEngine.raw(result.value))
        }
    }
    Box(Modifier.fillMaxSize()) {
      ScreenList {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            PageHeader(
                title = if (financeMode) "4 Value Calculator" else mode.heading,
                reset = {
                    answer = null
                    viewModel.resetFourValueMode()
                }
            )
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            FourValueModeSelector(
                selected = mode,
                financeSelected = financeMode,
                onSelect = {
                    answer = null
                    viewModel.selectFourValueMode(it)
                }
            )
        }
        if (financeMode) {
            item { Spacer(Modifier.height(6.dp)) }
            item {
                FinanceTypeSelector(
                    mode = mode,
                    interestType = state.toolInputs["four-interest-type"] ?: "simple",
                    onEmi = { answer = null; emiAdvanced = false; viewModel.switchFourValueMode(FourValueMode.EMI) },
                    onSimple = { answer = null; viewModel.switchFourValueMode(FourValueMode.INTEREST); viewModel.input("four-interest-type", "simple") },
                    onCompound = { answer = null; viewModel.switchFourValueMode(FourValueMode.INTEREST); viewModel.input("four-interest-type", "compound") }
                )
            }
            item { Spacer(Modifier.height(6.dp)) }
            if (mode == FourValueMode.EMI && !emiAdvanced) {
                item {
                    FinanceQuickEmiPanel(
                        principal = field(0), rate = field(1), tenure = field(2), tenureUnit = unit(2),
                        onPrincipal = { updateField(0, it) }, onRate = { updateField(1, it) }, onTenure = { updateField(2, it) },
                        onTenureUnit = { updateUnit(2, it) },
                        onAdvanced = { emiAdvanced = true; answer = null },
                        onReset = { answer = null; viewModel.resetFourValueMode() }
                    )
                }
                return@ScreenList
            }
            item {
                FinanceAdvancedPanel(
                    mode = mode,
                    answer = answer,
                    values = (0 until 4).map { field(it) },
                    units = (0 until 4).map { unit(it) },
                    interestFrequency = state.toolInputs["four-interest-frequency"] ?: "1",
                    showFrequency = mode == FourValueMode.INTEREST && state.toolInputs["four-interest-type"] == "compound",
                    onValue = ::updateField,
                    onUnit = ::updateUnit,
                    onFrequency = { answer = null; viewModel.input("four-interest-frequency", it) },
                    onCalculate = ::calculate,
                    onReset = { answer = null; viewModel.resetFourValueMode() },
                    onQuick = if (mode == FourValueMode.EMI) ({ emiAdvanced = false; answer = null }) else null
                )
            }
            return@ScreenList
        }
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                mode.fieldLabels.indices.chunked(2).forEach { pair ->
                    FourValueSurface {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            pair.forEach { index ->
                                FourValueField(
                                    label = mode.fieldLabels[index],
                                    value = field(index),
                                    unit = unit(index),
                                    options = CalculationEngine.smartUnitOptions(mode, index),
                                    onValue = { updateField(index, it) },
                                    onUnit = { updateUnit(index, it) },
                                    modifier = Modifier.weight(1f),
                                    fieldHeight = 42.dp,
                                    unitWidth = 44.dp,
                                    labelAlignment = Alignment.Start,
                                    fieldTextAlign = TextAlign.Start
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            FourValueSurface {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("RESULT", color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    val shown = answer
                    val answerText = when {
                        shown == null -> "—"
                        shown.error != null -> shown.error
                        shown.solvedIndex != null && shown.value != null -> CalculationEngine.formatSmart(shown.value, unit(shown.solvedIndex))
                        else -> shown.display
                    }
                    Text(answerText, color = if (shown?.error == null) Navy else AppRed, fontSize = if (shown?.error == null) 22.sp else 13.sp, fontWeight = FontWeight.Black)
                    Text(shown?.details?.joinToString(" · ") ?: "Fill any 3 values — the empty value is calculated.", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Normal)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            val isDark = IsDarkMode
            val buttonShape = RoundedCornerShape(12.dp)
            val buttonBrush = if (isDark) {
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF2DD4BF), Color(0xFF0D9488))
                )
            } else {
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F766E), Color(0xFF115E59))
                )
            }
            Button(
                onClick = ::calculate,
                modifier = Modifier.padding(start = 1.dp, end = 8.dp).fillMaxWidth().height(54.dp).shadow(
                    elevation = 6.dp,
                    shape = buttonShape,
                    spotColor = Color(0x33000000),
                    ambientColor = Color(0x18000000)
                ).clip(buttonShape).background(buttonBrush),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Text("CALCULATE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
      }
    }
}

@Composable
private fun FourValueModeSelector(selected: FourValueMode, financeSelected: Boolean, onSelect: (FourValueMode) -> Unit) {
    val isDark = IsDarkMode
    Column(Modifier.padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FourValueMode.entries.filterNot { it == FourValueMode.INTEREST }.chunked(4).forEach { group ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                group.forEach { item ->
                    val itemSelected = if (item == FourValueMode.EMI) financeSelected else selected == item
                    val icon = when (item) {
                        FourValueMode.DAILY -> Icons.Default.CurrencyRupee
                        FourValueMode.MARKS -> Icons.Default.School
                        FourValueMode.PERCENT -> Icons.Default.Percent
                        FourValueMode.EMI -> Icons.Default.AccountBalance
                        FourValueMode.PROFIT -> Icons.AutoMirrored.Filled.TrendingUp
                        FourValueMode.INTEREST -> Icons.Default.Savings
                        FourValueMode.GENERAL -> Icons.Default.Tune
                    }
                    val iconColor = when (item) {
                        FourValueMode.DAILY -> if (isDark) Color(0xFF34D399) else Color(0xFF167A62)
                        FourValueMode.MARKS -> if (isDark) Color(0xFFA78BFA) else Color(0xFF6A45A8)
                        FourValueMode.PERCENT -> if (isDark) Color(0xFF60A5FA) else Color(0xFF2C71DE)
                        FourValueMode.EMI -> if (isDark) Color(0xFF2DD4BF) else Color(0xFF008A75)
                        FourValueMode.PROFIT -> if (isDark) Color(0xFF34D399) else Color(0xFF18A673)
                        FourValueMode.INTEREST -> if (isDark) Color(0xFF38BDF8) else Color(0xFF008B9A)
                        FourValueMode.GENERAL -> if (isDark) Color(0xFFA78BFA) else Color(0xFF8A5CC8)
                    }
                    val shape = RoundedCornerShape(12.dp)
                    val chipWidth = when (item) {
                        FourValueMode.DAILY, FourValueMode.PROFIT -> 84.dp
                        FourValueMode.MARKS -> 90.dp
                        FourValueMode.PERCENT -> 95.dp
                        FourValueMode.EMI, FourValueMode.INTEREST -> 89.dp
                        FourValueMode.GENERAL -> 96.dp
                    }
                    val selectedBrush = if (isDark) {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color(0xFF0D9488), Color(0xFF047857))
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F766E), Color(0xFF047857))
                        )
                    }
                    OutlinedButton(
                        onClick = { onSelect(item) },
                        modifier = Modifier.width(chipWidth).height(46.dp).shadow(
                            elevation = if (itemSelected) 4.dp else 1.dp,
                            shape = shape,
                            spotColor = Color(0x20000000),
                            ambientColor = Color(0x10000000)
                        ).clip(shape).then(if (itemSelected) Modifier.background(selectedBrush) else Modifier),
                        shape = shape,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 3.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (itemSelected) Color.Transparent else SoftField,
                            contentColor = if (itemSelected) Color.White else DeepNavy
                        ),
                        border = BorderStroke(1.dp, if (itemSelected) Navy else Line)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = item.label, tint = if (itemSelected) Color.White else iconColor, modifier = Modifier.size(14.dp))
                            Text(if (item == FourValueMode.EMI) "Finance" else item.label, color = if (itemSelected) Color.White else DeepNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FourValueSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val isDark = IsDarkMode
    Card(
        modifier = modifier.padding(start = 2.dp, end = 6.dp).fillMaxWidth().then(
            if (!isDark) {
                Modifier.shadow(
                    elevation = 4.dp,
                    shape = shape,
                    spotColor = Color(0x14000000),
                    ambientColor = Color(0x0A000000)
                )
            } else {
                Modifier.shadow(
                    elevation = 2.dp,
                    shape = shape,
                    spotColor = Color(0x33000000),
                    ambientColor = Color(0x18000000)
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

@Composable
private fun FinanceTypeSelector(
    mode: FourValueMode,
    interestType: String,
    onEmi: () -> Unit,
    onSimple: () -> Unit,
    onCompound: () -> Unit
) {
    ReferenceCard {
        Row(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            InterestTypeButton("Loan EMI", mode == FourValueMode.EMI, Modifier.weight(1f), onEmi)
            InterestTypeButton("Simple Interest", mode == FourValueMode.INTEREST && interestType != "compound", Modifier.weight(1f), onSimple)
            InterestTypeButton("Compound", mode == FourValueMode.INTEREST && interestType == "compound", Modifier.weight(1f), onCompound)
        }
    }
}

@Composable
private fun FinanceQuickEmiPanel(
    principal: String,
    rate: String,
    tenure: String,
    tenureUnit: String,
    onPrincipal: (String) -> Unit,
    onRate: (String) -> Unit,
    onTenure: (String) -> Unit,
    onTenureUnit: (String) -> Unit,
    onAdvanced: () -> Unit,
    onReset: () -> Unit
) {
    var calculateRequested by rememberSaveable { mutableStateOf(false) }
    val months = ((tenure.number() * if (tenureUnit == "years") 12.0 else 1.0).roundToInt()).coerceAtLeast(0)
    val summary = CalculationEngine.emiSummary(principal.number(), rate.number(), months)
    val principalPercent = (summary?.principalPercent ?: 0.0).toFloat().coerceIn(0f, 100f)
    val interestPercent = (summary?.interestPercent ?: 0.0).toFloat().coerceIn(0f, 100f)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReferenceCard {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Loan amount", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("₹", color = DeepNavy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    CompactTextField(
                        value = principal,
                        onValueChange = { calculateRequested = false; onPrincipal(CalculationEngine.rawTyping(it)) },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        height = 42.dp,
                        plainWhenIdle = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = DeepNavy, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    )
                    IconButton(onClick = onAdvanced, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = "Advanced", tint = Navy, modifier = Modifier.size(19.dp))
                    }
                }
                HorizontalDivider(color = Line)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FinanceRateField(
                        value = rate,
                        onValue = { calculateRequested = false; onRate(it) },
                        modifier = Modifier.weight(1f)
                    )
                    FinanceTenureField(
                        value = tenure,
                        tenureUnit = tenureUnit,
                        onValue = { calculateRequested = false; onTenure(it) },
                        onUnit = { calculateRequested = false; onTenureUnit(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (calculateRequested && summary == null) {
                    Text("Enter a valid loan amount, rate and tenure.", color = AppRed, fontSize = 11.sp)
                }
            }
        }

        ReferenceCard {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Your Monthly EMI", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(
                    "₹ ${CalculationEngine.format(summary?.monthlyEmi ?: 0.0)}",
                    color = Navy,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text("Payable every month", color = Muted, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FinanceMetric("Principal", summary?.principal ?: 0.0, Modifier.weight(1f))
                    FinanceMetric("Total Interest", summary?.totalInterest ?: 0.0, Modifier.weight(1f))
                    FinanceMetric("Total payment", summary?.totalPayment ?: 0.0, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${CalculationEngine.format(principalPercent.toDouble())}% Principal", color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${CalculationEngine.format(interestPercent.toDouble())}% Interest", color = TagAmberText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                val isDark = IsDarkMode
                LinearProgressIndicator(
                    progress = { principalPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
                    color = Navy,
                    trackColor = if (isDark) Color(0xFF334155) else Color(0xFFFFD8B5)
                )
            }
        }

        Button(
            onClick = { calculateRequested = true },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White)
        ) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text("CALCULATE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        ResetButton(onClick = { calculateRequested = false; onReset() }, modifier = Modifier.fillMaxWidth().height(44.dp))

        if (summary != null) {
            ReferenceCard {
                Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("YEAR-WISE BREAKUP", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    summary.yearlyRows.forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("Year ${row.year}", color = DeepNavy, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                Text("Principal ₹ ${CalculationEngine.format(row.principalPaid)}", color = Muted, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Interest ₹ ${CalculationEngine.format(row.interestPaid)}", color = TagAmberText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Balance ₹ ${CalculationEngine.format(row.closingBalance)}", color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceRateField(value: String, onValue: (String) -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Annual interest rate", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Row(
            Modifier.fillMaxWidth().height(43.dp).modernBoxSurface(RoundedCornerShape(9.dp), 2.dp).padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("%", color = Muted, fontSize = 15.sp)
            CompactTextField(
                value = value,
                onValueChange = { onValue(CalculationEngine.rawTyping(it)) },
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                height = 41.dp,
                plainWhenIdle = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = DeepNavy, fontSize = 16.sp, fontWeight = FontWeight.Black)
            )
        }
    }
}

@Composable
private fun FinanceTenureField(
    value: String,
    tenureUnit: String,
    onValue: (String) -> Unit,
    onUnit: (String) -> Unit,
    modifier: Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Tenure", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
        Row(
            Modifier.fillMaxWidth().height(43.dp).modernBoxSurface(RoundedCornerShape(9.dp), 2.dp).padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactTextField(
                value = value,
                onValueChange = { onValue(CalculationEngine.rawTyping(it)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                height = 41.dp,
                plainWhenIdle = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = DeepNavy, fontSize = 16.sp, fontWeight = FontWeight.Black)
            )
            FinanceUnitButton("Years", tenureUnit == "years") { onUnit("years") }
            Spacer(Modifier.width(2.dp))
            FinanceUnitButton("Months", tenureUnit != "years") { onUnit("months") }
        }
    }
}

@Composable
private fun FinanceUnitButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(if (text == "Months") 51.dp else 44.dp).height(32.dp),
        shape = RoundedCornerShape(7.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Navy else Color.Transparent,
            contentColor = if (selected) Color.White else Muted
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { Text(text, color = if (selected) Color.White else Muted, fontSize = 7.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
}

@Composable
private fun FinanceMetric(label: String, value: Double, modifier: Modifier) {
    Column(modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1)
        Text("₹ ${CalculationEngine.format(value)}", color = DeepNavy, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun FinanceAdvancedPanel(
    mode: FourValueMode,
    answer: CalculationResult?,
    values: List<String>,
    units: List<String>,
    interestFrequency: String,
    showFrequency: Boolean,
    onValue: (Int, String) -> Unit,
    onUnit: (Int, String) -> Unit,
    onFrequency: (String) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onQuick: (() -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReferenceCard {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (mode == FourValueMode.EMI) "Advanced EMI" else if (showFrequency) "Compound Interest" else "Simple Interest",
                            color = DeepNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(if (mode == FourValueMode.EMI) "Leave one value empty to calculate it." else "Fill any 4 values — the empty value is calculated.", color = Muted, fontSize = 10.sp)
                    }
                    if (onQuick != null) TextButton(onClick = onQuick) { Text("Quick EMI", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                if (showFrequency) {
                    Picker("Compounding: ${frequencyName(interestFrequency)}", listOf("1", "2", "4", "12"), interestFrequency, { frequencyName(it) }, onFrequency)
                }
                mode.fieldLabels.indices.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                        pair.forEach { index ->
                            FourValueField(
                                label = mode.fieldLabels[index],
                                value = values[index],
                                unit = units[index],
                                options = if (mode == FourValueMode.INTEREST && index == 2) listOf("years", "months") else CalculationEngine.smartUnitOptions(mode, index),
                                onValue = { onValue(index, it) },
                                onUnit = { onUnit(index, it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        ReferenceCard {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(if (mode == FourValueMode.EMI) "RESULT" else "Interest Summary", color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                val display = when {
                    answer == null -> "₹ 0"
                    answer.error != null -> answer.error
                    answer.solvedIndex != null && answer.value != null -> CalculationEngine.formatSmart(answer.value, units[answer.solvedIndex])
                    else -> answer.display
                }
                Text(display, color = if (answer?.error == null) Navy else AppRed, fontSize = if (answer?.error == null) 26.sp else 12.sp, fontWeight = FontWeight.Black)
                Text(answer?.details?.joinToString(" · ") ?: "Enter values and calculate.", color = Muted, fontSize = 10.sp)
            }
        }
        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White)
        ) {
            Text("CALCULATE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        ResetButton(onClick = onReset, modifier = Modifier.fillMaxWidth().height(44.dp))
    }
}

private fun frequencyName(value: String): String = when (value) {
    "2" -> "Half-Yearly"
    "4" -> "Quarterly"
    "12" -> "Monthly"
    else -> "Yearly"
}

@Composable
private fun InterestTypeButton(text: String, selected: Boolean, modifier: Modifier, click: () -> Unit) {
    OutlinedButton(
        onClick = click,
        modifier = modifier.height(42.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Navy else SoftField, contentColor = if (selected) Color.White else DeepNavy),
        border = BorderStroke(1.dp, if (selected) Navy else Line)
    ) { Text(text, color = if (selected) Color.White else DeepNavy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
}

@Composable
private fun FourValueField(
    label: String,
    value: String,
    unit: String,
    options: List<String>,
    onValue: (String) -> Unit,
    onUnit: (String) -> Unit,
    modifier: Modifier,
    fieldHeight: androidx.compose.ui.unit.Dp = 42.dp,
    unitWidth: androidx.compose.ui.unit.Dp = 44.dp,
    labelAlignment: Alignment.Horizontal = Alignment.Start,
    fieldTextAlign: TextAlign = TextAlign.Start
) {
    Column(modifier, horizontalAlignment = labelAlignment, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), color = Navy, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 0.5.sp, textAlign = fieldTextAlign)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            val controlShape = RoundedCornerShape(10.dp)
            CompactTextField(
                value = value,
                onValueChange = onValue,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                height = fieldHeight,
                shape = controlShape,
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = fieldTextAlign, color = DeepNavy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )
            if (unit.isNotBlank()) {
                if (options.size > 1) {
                    CompactPicker(unit, options, { CalculationEngine.smartUnitLabel(it) }, onUnit, Modifier.width(unitWidth), fieldHeight)
                } else {
                    UnitPill(CalculationEngine.smartUnitLabel(unit), Modifier.width(unitWidth), fieldHeight)
                }
            }
        }
    }
}

@Composable
private fun CompactPicker(
    selected: String,
    options: List<String>,
    label: (String) -> String,
    onSelect: (String) -> Unit,
    modifier: Modifier,
    height: androidx.compose.ui.unit.Dp = 42.dp
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        val shape = RoundedCornerShape(10.dp)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(height).modernBoxSurface(shape, 1.dp, Line),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp),
            shape = shape,
            border = BorderStroke(0.dp, Color.Transparent),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = DeepNavy)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label(selected), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Navy, modifier = Modifier.size(15.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 180.dp).heightIn(max = 300.dp),
            shape = RoundedCornerShape(14.dp),
            containerColor = PageWhite,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Line)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option), color = DeepNavy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.heightIn(min = 44.dp),
                    onClick = { onSelect(option); expanded = false },
                    leadingIcon = { if (option == selected) Icon(Icons.Default.Check, contentDescription = null, tint = Navy) }
                )
            }
        }
    }
}

@Composable
private fun UnitPill(value: String, modifier: Modifier, height: androidx.compose.ui.unit.Dp = 42.dp) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.height(height).modernBoxSurface(shape, 1.dp, Line),
        contentAlignment = Alignment.Center
    ) {
        Text(value, color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp), maxLines = 1)
    }
}

@Composable
fun Input(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Decimal) {
    CompactTextField(
        value = value,
        onValueChange = { onChange(CalculationEngine.rawTyping(it)) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

/** Shared native Material dropdown used by all More tools. */
@Composable
fun Picker(label: String, options: List<String>, selected: String, display: (String) -> String = { it }, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(42.dp).modernBoxSurface(RoundedCornerShape(10.dp), 1.5.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(0.dp, Color.Transparent),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = DeepNavy)
        ) { Text(label, maxLines = 1) }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 250.dp).heightIn(max = 330.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = PageWhite,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, Line)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option), color = DeepNavy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    modifier = Modifier.heightIn(min = 50.dp),
                    onClick = { onSelect(option); expanded = false },
                    leadingIcon = { if (option == selected) Icon(Icons.Default.Check, contentDescription = null, tint = Navy) }
                )
            }
        }
    }
}

