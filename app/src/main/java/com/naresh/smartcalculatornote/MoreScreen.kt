package com.naresh.smartcalculatornote

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

private data class ToolInfo(
    val id: String,
    val label: String,
    val hint: String,
    val emoji: String
)

private val moreTools = listOf(
    ToolInfo("length", "Length", "All length units", "📏"),
    ToolInfo("weight", "Weight", "All weight units", "⚖️"),
    ToolInfo("area", "Area", "Land & surface", "📐"),
    ToolInfo("discount", "Discount", "Price saving", "🏷️"),
    ToolInfo("split", "Split Bill", "Per person", "👥"),
    ToolInfo("rate", "Qty × Rate", "Quick total", "📦"),
    ToolInfo("mileage", "Mileage", "km/L & cost", "⛽"),
    ToolInfo("temperature", "Temperature", "°C °F K", "🌡️"),
    ToolInfo("time", "Time", "Convert time", "⏱️"),
    ToolInfo("daily", "Daily Price", "Unit price", "💰"),
    ToolInfo("age", "Age / Date", "Age & date gap", "🎂"),
    ToolInfo("percentage", "Percentage", "Percent tools", "%"),
    ToolInfo("gst", "GST", "Add / remove GST", "🧾"),
    ToolInfo("emi", "EMI / Loan", "Monthly EMI", "🏦"),
    ToolInfo("currency", "Currency", "Manual exchange", "💱"),
    ToolInfo("history", "History", "Saved results", "🕒"),
    ToolInfo("share", "Share", "Share summary", "📤")
)

@Composable
fun MoreScreen(state: AppState, viewModel: CalculatorViewModel, onShare: () -> Unit) {
    val selected = state.selectedMoreTool
    val metrics = LocalReferenceLayoutMetrics.current
    if (selected != null) {
        val tool = moreTools.firstOrNull { it.id == selected }
        if (tool != null) {
            ScreenList {
                item {
                    MoreToolHeader(
                        title = if (tool.id in setOf("length", "weight", "area", "time", "temperature", "currency")) "${tool.label} Converter" else tool.label,
                        onBack = { viewModel.selectMoreTool(null) }
                    )
                }
                item { MoreTool(tool.id, state, viewModel, onShare) }
            }
            return
        }
    }
    ScreenList {
        item { PageHeader("More Tools") }
        moreTools.chunked(metrics.moreColumns).forEach { pair ->
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { tool ->
                        MoreToolTile(tool, Modifier.weight(1f)) {
                            if (tool.id == "share") {
                                onShare()
                            } else {
                                viewModel.selectMoreTool(tool.id)
                            }
                        }
                    }
                    repeat(metrics.moreColumns - pair.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun MoreToolTile(tool: ToolInfo, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(78.dp).shadow(
            elevation = 3.5.dp,
            shape = RoundedCornerShape(13.dp),
            spotColor = Color(0x18000000),
            ambientColor = Color(0x0C000000)
        ),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = PageWhite),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(vertical = 7.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                tool.emoji,
                fontSize = if (tool.emoji == "%") 23.sp else 25.sp,
                fontWeight = if (tool.emoji == "%") FontWeight.Bold else FontWeight.Normal,
                color = if (tool.emoji == "%") Navy else Color.Unspecified,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(tool.label, color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
        }
    }
}

@Composable
private fun MoreToolHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = DeepNavy, fontSize = 20.sp, fontWeight = FontWeight.Black)
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.height(34.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Line)
        ) { Text("← All tools", color = Navy, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
    }
}

@Composable
private fun MoreTool(tool: String, state: AppState, viewModel: CalculatorViewModel, onShare: () -> Unit) {
    val values = state.toolInputs
    fun value(key: String, fallback: String = "") = values[key] ?: fallback
    fun change(key: String, text: String) = viewModel.input(key, text)
    fun numericChange(key: String, text: String) = change(key, CalculationEngine.formatTyping(text))
    when (tool) {
        "history" -> HistoryTool((state.history + state.originalHistory).sortedByDescending { it.createdAt }, viewModel::clearHistory, onShare)
        "length" -> ConverterTool("Length Converter", "length", CalculationEngine.lengthUnits, "Meter (m)", "Foot (ft)", "1", values, ::numericChange, ::change)
        "weight" -> ConverterTool("Weight Converter", "weight", CalculationEngine.weightUnits, "Kilogram (kg)", "Pound (lb)", "1", values, ::numericChange, ::change)
        "area" -> ConverterTool("Area Converter", "area", CalculationEngine.areaUnits, "Square Meter (m²)", "Square Foot (ft²)", "1", values, ::numericChange, ::change)
        "time" -> ConverterTool("Time Converter", "time", CalculationEngine.timeUnits, "Hour", "Minute", "1", values, ::numericChange, ::change)
        "temperature" -> TemperatureTool(values, ::numericChange, ::change)
        "discount" -> {
            val result = CalculationEngine.discount(value("discountPrice").number(), value("discountPercent").number())
            ToolCard("Discount", result.display, result.error, result.details.joinToString(" · ")) {
                Input(value("discountPrice"), { numericChange("discountPrice", it) }, "Price")
                Input(value("discountPercent"), { numericChange("discountPercent", it) }, "Discount %")
            }
        }
        "split" -> {
            val result = CalculationEngine.splitBill(value("splitTotal").number(), value("splitPeople", "2").number())
            ToolCard("Split Bill", result.display, result.error, result.details.joinToString(" · ")) {
                Input(value("splitTotal"), { numericChange("splitTotal", it) }, "Total")
                Input(value("splitPeople", "2"), { numericChange("splitPeople", it) }, "People")
            }
        }
        "rate" -> {
            val result = CalculationEngine.quantityRate(value("rateQuantity").number(), value("ratePrice").number())
            ToolCard("Quantity × Rate", result.display, result.error) {
                Input(value("rateQuantity"), { numericChange("rateQuantity", it) }, "Quantity")
                Input(value("ratePrice"), { numericChange("ratePrice", it) }, "Rate")
            }
        }
        "mileage" -> MileageTool(values, ::numericChange)
        "daily" -> DailyPriceTool(values, ::numericChange, ::change)
        "age" -> AgeTool(values, ::change)
        "percentage" -> PercentageTool(values, ::numericChange, ::change)
        "gst" -> GstTool(values, ::numericChange, ::change)
        "emi" -> StandardEmiTool(values, ::numericChange)
        "currency" -> CurrencyTool(values, ::numericChange, ::change)
    }
}

@Composable
private fun ConverterTool(
    title: String,
    prefix: String,
    units: Map<String, Double>,
    defaultFrom: String,
    defaultTo: String,
    defaultValue: String,
    values: Map<String, String>,
    onNumericChange: (String, String) -> Unit,
    onChange: (String, String) -> Unit
) {
    val names = units.keys.toList()
    val value = values["${prefix}Value"] ?: defaultValue
    val from = values["${prefix}From"]?.takeIf { it in units } ?: defaultFrom
    val to = values["${prefix}To"]?.takeIf { it in units } ?: defaultTo
    val result = CalculationEngine.convert(value.number(), from, to, units)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ConverterPanel(
            value = value,
            from = from,
            to = to,
            result = result,
            options = names,
            onValueChange = { onNumericChange("${prefix}Value", it) },
            onFromChange = { onChange("${prefix}From", it) },
            onToChange = { onChange("${prefix}To", it) },
            onSwap = {
                onChange("${prefix}From", to)
                onChange("${prefix}To", from)
                onNumericChange("${prefix}Value", CalculationEngine.raw(result.value ?: 0.0))
            }
        )
        AllUnitValues(
            "${CalculationEngine.format(value.number())} ${converterUnitSymbol(from)} vs All ${title.removeSuffix(" Converter")} Units",
            value.number(),
            from,
            CalculationEngine.allUnitValues(value.number(), from, units)
        )
    }
}

@Composable
private fun TemperatureTool(values: Map<String, String>, onNumericChange: (String, String) -> Unit, onChange: (String, String) -> Unit) {
    val value = values["tempValue"] ?: "0"
    val options = listOf("Celsius", "Fahrenheit", "Kelvin")
    val from = values["tempFrom"]?.takeIf { it in options } ?: "Celsius"
    val to = values["tempTo"]?.takeIf { it in options } ?: "Fahrenheit"
    val result = CalculationEngine.temperature(value.number(), from, to)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ConverterPanel(
            value = value,
            from = from,
            to = to,
            result = result,
            options = options,
            onValueChange = { onNumericChange("tempValue", it) },
            onFromChange = { onChange("tempFrom", it) },
            onToChange = { onChange("tempTo", it) },
            onSwap = {
                onChange("tempFrom", to)
                onChange("tempTo", from)
                onNumericChange("tempValue", CalculationEngine.raw(result.value ?: 0.0))
            }
        )
        AllUnitValues(
            "${CalculationEngine.format(value.number())} ${converterUnitSymbol(from)} vs All Temperature Units",
            value.number(),
            from,
            CalculationEngine.allTemperatureValues(value.number(), from)
        )
    }
}

@Composable
private fun ConverterPanel(
    value: String,
    from: String,
    to: String,
    result: CalculationResult,
    options: List<String>,
    onValueChange: (String) -> Unit,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSwap: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReferenceCard {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConverterValueField(value, onValueChange, Modifier.weight(.4f))
                    ConverterUnitPicker(from, options, Modifier.weight(.6f), onFromChange)
                }
                Button(
                    onClick = onSwap,
                    modifier = Modifier.size(35.dp).align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) { Text("↕", fontSize = 22.sp, fontWeight = FontWeight.Black) }
                Card(
                    shape = RoundedCornerShape(13.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftField),
                    border = BorderStroke(1.dp, Line),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            result.error ?: result.display,
                            modifier = Modifier.weight(.4f),
                            color = if (result.error == null) Navy else AppRed,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                        ConverterUnitPicker(to, options, Modifier.weight(.6f), onToChange)
                    }
                }
            }
        }
        Text(
            "${CalculationEngine.format(value.number())} ${converterUnitSymbol(from)} = ${result.error ?: result.display} ${converterUnitSymbol(to)}",
            color = Muted,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConverterValueField(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    CompactTextField(
        value = value,
        onValueChange = { onValueChange(CalculationEngine.formatTyping(it)) },
        modifier = modifier,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        height = 48.dp,
        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End, color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    )
}

@Composable
private fun ConverterUnitPicker(selected: String, options: List<String>, modifier: Modifier, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Line),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
        ) {
            Text(converterUnitShort(selected), modifier = Modifier.weight(1f), color = DeepNavy, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("⌄", color = DeepNavy, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
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
                    text = { Text(option, color = DeepNavy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    modifier = Modifier.heightIn(min = 50.dp),
                    onClick = { onSelect(option); expanded = false },
                    leadingIcon = { if (option == selected) Text("✓", color = Navy, fontWeight = FontWeight.Black) }
                )
            }
        }
    }
}

private fun converterUnitShort(unit: String): String = when (unit) {
    "Traditional Tola (11.6638 g)" -> "Tola"
    "Metric Ton (t)" -> "Ton (t)"
    else -> unit
}

private fun converterUnitSymbol(unit: String): String = when (unit) {
    "Traditional Tola (11.6638 g)" -> "Tola"
    "Celsius" -> "°C"
    "Fahrenheit" -> "°F"
    "Kelvin" -> "K"
    else -> Regex("\\(([^)]+)\\)").find(unit)?.groupValues?.get(1) ?: unit
}

@Composable
private fun MileageTool(values: Map<String, String>, onNumericChange: (String, String) -> Unit) {
    val distance = values["mileageDistance"].orEmpty()
    val fuel = values["mileageFuel"].orEmpty()
    val price = values["mileagePrice"].orEmpty()
    val populated = distance.isNotBlank() || fuel.isNotBlank() || price.isNotBlank()
    val result = if (populated) CalculationEngine.mileage(distance.number(), fuel.number(), price.number()) else CalculationResult(0.0, "0", listOf("Fuel cost 0"))
    ToolCard("Mileage", "${result.display} km/L", result.error, result.details.joinToString(" · ")) {
        Input(distance, { onNumericChange("mileageDistance", it) }, "Distance (km)")
        Input(fuel, { onNumericChange("mileageFuel", it) }, "Fuel (L)")
        Input(price, { onNumericChange("mileagePrice", it) }, "Fuel price / L")
    }
}

@Composable
private fun DailyPriceTool(values: Map<String, String>, onNumericChange: (String, String) -> Unit, onChange: (String, String) -> Unit) {
    val price = values["dailyPrice"] ?: "100"
    val baseQuantity = values["dailyBaseQty"] ?: "1"
    val baseUnit = values["dailyBaseUnit"]?.takeIf { it in CalculationEngine.dailyPriceBaseUnits } ?: "kg"
    val targetQuantity = values["dailyTargetQty"] ?: "100"
    val availableTargets = CalculationEngine.compatibleDailyPriceUnits(baseUnit)
    val targetUnit = values["dailyTargetUnit"]?.takeIf { it in availableTargets } ?: availableTargets.firstOrNull().orEmpty()
    val result = CalculationEngine.dailyPrice(price.number(), baseQuantity.number(), baseUnit, targetQuantity.number(), targetUnit)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ToolCard("Daily Price Calculator", result.display, result.error, result.details.joinToString(" · ")) {
            Text("Example: ₹100 = 1 kg → 100 g = ?", color = Muted, fontSize = 12.sp)
            Input(price, { onNumericChange("dailyPrice", it) }, "Price")
            Input(baseQuantity, { onNumericChange("dailyBaseQty", it) }, "Base quantity")
            Picker("Base unit: ${dailyPriceUnitLabel(baseUnit)}", CalculationEngine.dailyPriceBaseUnits, baseUnit, ::dailyPriceUnitLabel) { chosen ->
                onChange("dailyBaseUnit", chosen)
                val compatible = CalculationEngine.compatibleDailyPriceUnits(chosen)
                if (targetUnit !in compatible) onChange("dailyTargetUnit", compatible.firstOrNull().orEmpty())
            }
            Text("Find", fontWeight = FontWeight.Black, color = Muted)
            Input(targetQuantity, { onNumericChange("dailyTargetQty", it) }, "Target quantity")
            Picker("Target unit: ${dailyPriceUnitLabel(targetUnit)}", availableTargets, targetUnit, ::dailyPriceUnitLabel) { onChange("dailyTargetUnit", it) }
        }
        val suggestions = CalculationEngine.dailySuggestions(price.number(), baseQuantity.number(), baseUnit)
        ReferenceCard {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quick Daily Values", color = DeepNavy, fontWeight = FontWeight.Black)
                suggestions.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { suggestion ->
                            Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SoftField), shape = RoundedCornerShape(9.dp)) {
                                Column(Modifier.padding(8.dp)) {
                                    Text("${CalculationEngine.format(suggestion.quantity)} ${dailyPriceUnitLabel(suggestion.unit)}", fontSize = 12.sp, color = Muted)
                                    Text(CalculationEngine.format(suggestion.price), fontWeight = FontWeight.Black, color = DeepNavy)
                                }
                            }
                        }
                        if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun dailyPriceUnitLabel(unit: String): String = when (unit) {
    "quintal" -> "Quintal (q)"
    "kg" -> "kg"
    "g" -> "g"
    "tola" -> "Traditional Tola (11.6638 g)"
    "l" -> "Litre"
    "ml" -> "ml"
    "m" -> "Meter (m)"
    "gaj" -> "Gaj"
    "piece" -> "Piece"
    "dozen" -> "Dozen"
    else -> unit
}

@Composable
private fun AgeTool(values: Map<String, String>, onChange: (String, String) -> Unit) {
    val start = values["ageStart"].orEmpty()
    val end = values["ageEnd"].orEmpty()
    val result = CalculationEngine.age(start, end)
    ToolCard("Age / Date Calculator", result.display, result.error, if (result.error == null) "Total ${CalculationEngine.format(result.days.toDouble())} days" else "Select dates") {
        DatePickerField("Birth / Start Date", start) { onChange("ageStart", it) }
        DatePickerField("End Date", end) { onChange("ageEnd", it) }
    }
}

@Composable
private fun DatePickerField(label: String, value: String, onChange: (String) -> Unit) {
    val context = LocalContext.current
    val selected = runCatching { LocalDate.parse(value) }.getOrDefault(LocalDate.now())
    OutlinedButton(
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day -> onChange(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)) },
                selected.year,
                selected.monthValue - 1,
                selected.dayOfMonth
            ).show()
        },
        modifier = Modifier.fillMaxWidth().height(42.dp)
    ) { Text("$label: ${value.ifBlank { "Select date" }}") }
}

@Composable
private fun PercentageTool(values: Map<String, String>, onNumericChange: (String, String) -> Unit, onChange: (String, String) -> Unit) {
    val mode = values["percentageMode"]?.takeIf { it in setOf("of", "what", "change") } ?: "of"
    val result = CalculationEngine.percentage(values["percentageA"].number(), values["percentageB"].number(), mode)
    val (first, second) = when (mode) {
        "what" -> "Part" to "Total"
        "change" -> "Old Value" to "New Value"
        else -> "Percentage %" to "Value"
    }
    ToolCard("Percentage Calculator", result.display + if (mode == "of") "" else "%", result.error) {
        Picker("Mode: ${when (mode) { "what" -> "X is what % of Y?"; "change" -> "Percentage Change"; else -> "X% of Y" }}", listOf("of", "what", "change"), mode) { onChange("percentageMode", it) }
        Input(values["percentageA"].orEmpty(), { onNumericChange("percentageA", it) }, first)
        Input(values["percentageB"].orEmpty(), { onNumericChange("percentageB", it) }, second)
    }
}

@Composable
private fun GstTool(values: Map<String, String>, onNumericChange: (String, String) -> Unit, onChange: (String, String) -> Unit) {
    val add = values["gstMode"] != "remove"
    val amount = values["gstAmount"].orEmpty()
    val rate = values["gstRate"] ?: "18"
    val result = CalculationEngine.gst(amount.number(), rate.number(), add)
    ToolCard("GST Calculator", CalculationEngine.format(if (add) result.total else result.base), result.error, "GST ${CalculationEngine.format(result.tax)} · CGST ${CalculationEngine.format(result.tax / 2)} · SGST ${CalculationEngine.format(result.tax / 2)}") {
        Input(amount, { onNumericChange("gstAmount", it) }, "Amount")
        Input(rate, { onNumericChange("gstRate", it) }, "GST rate %")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GstModeButton("+ Add GST", add, Modifier.weight(1f)) { onChange("gstMode", "add") }
            GstModeButton("− Remove GST", !add, Modifier.weight(1f)) { onChange("gstMode", "remove") }
        }
    }
}

@Composable
private fun GstModeButton(text: String, selected: Boolean, modifier: Modifier, action: () -> Unit) {
    OutlinedButton(
        onClick = action,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Navy else PageWhite, contentColor = if (selected) Color.White else DeepNavy)
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
}

@Composable
private fun StandardEmiTool(values: Map<String, String>, onNumericChange: (String, String) -> Unit) {
    val principal = values["loanAmount"].orEmpty()
    val rate = values["loanRate"].orEmpty()
    val months = values["loanMonths"].orEmpty()
    val populated = principal.isNotBlank() || rate.isNotBlank() || months.isNotBlank()
    val result = if (populated) CalculationEngine.emi(principal.number(), rate.number(), months.toIntOrNull() ?: 0) else CalculationResult(0.0, "0", listOf("Total interest 0", "Total payment 0"))
    ToolCard("EMI / Loan Calculator", "₹ ${result.display}", result.error, result.details.joinToString(" · ")) {
        Input(principal, { onNumericChange("loanAmount", it) }, "Loan amount")
        Input(rate, { onNumericChange("loanRate", it) }, "Annual rate %")
        Input(months, { onNumericChange("loanMonths", it.filter(Char::isDigit)) }, "Months")
    }
}

@Composable
private fun CurrencyTool(values: Map<String, String>, onNumericChange: (String, String) -> Unit, onChange: (String, String) -> Unit) {
    val amount = values["currencyAmount"] ?: "1"
    val currencies = CalculationEngine.manualCurrencies
    val from = values["currencyFrom"]?.takeIf { it in currencies } ?: "USD"
    val to = values["currencyTo"]?.takeIf { it in currencies } ?: "INR"
    val rate = values["currencyRate"] ?: "1"
    val result = CalculationEngine.currency(amount.number(), rate.number())
    ToolCard("Manual Currency Converter", result.display, result.error, "Manual rate · no internet used") {
        Input(amount, { onNumericChange("currencyAmount", it) }, "Amount")
        Picker("From: $from", currencies, from) { onChange("currencyFrom", it) }
        OutlinedButton(
            onClick = {
                onChange("currencyFrom", to)
                onChange("currencyTo", from)
                onNumericChange("currencyRate", CalculationEngine.raw(if (rate.number() == 0.0) 0.0 else 1.0 / rate.number()))
            },
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) { Text("⇅ Swap") }
        Picker("To: $to", currencies, to) { onChange("currencyTo", it) }
        Input(rate, { onNumericChange("currencyRate", it) }, "Rate: 1 $from = ? $to")
    }
}

@Composable
private fun AllUnitValues(title: String, sourceValue: Double, sourceUnit: String, rows: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(), color = Muted, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))
        ReferenceCard {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                rows.forEachIndexed { index, (unit, result) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${CalculationEngine.format(sourceValue)} ${converterUnitSymbol(sourceUnit)} = $result ${converterUnitSymbol(unit)}",
                            color = DeepNavy,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(unit, color = Muted, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(.85f))
                    }
                    if (index != rows.lastIndex) androidx.compose.material3.HorizontalDivider(color = Line)
                }
            }
        }
    }
}

@Composable
private fun ToolCard(title: String, result: String, error: String? = null, details: String = "", content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HeroCard(title.uppercase(), error ?: result, if (error == null) details else "Check input")
        ReferenceCard { Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content) }
    }
}

@Composable
private fun HistoryTool(history: List<HistoryEntry>, clear: () -> Unit, share: () -> Unit) {
    ReferenceCard {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saved History", fontSize = 20.sp, fontWeight = FontWeight.Black, color = DeepNavy)
                TextButton(onClick = clear) { Text("Clear") }
            }
            if (history.isEmpty()) {
                Text("No saved results yet.", color = Muted)
            } else {
                history.take(100).forEach { entry ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.label, color = DeepNavy)
                            Text(DateFormat.getDateTimeInstance().format(Date(entry.createdAt)), fontSize = 12.sp, color = Muted)
                        }
                        Text(entry.value, color = DeepNavy, fontWeight = FontWeight.Black)
                    }
                }
            }
            Button(onClick = share, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                Icon(Icons.Default.Share, null)
                Text("Share summary")
            }
        }
    }
}
