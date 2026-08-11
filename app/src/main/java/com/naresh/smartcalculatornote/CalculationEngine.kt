package com.naresh.smartcalculatornote

import java.text.NumberFormat
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

enum class Operator(val symbol: String) {
    ADD("+"), SUBTRACT("-"), MULTIPLY("×"), DIVIDE("÷")
}

data class CalcRow(
    val id: String,
    val label: String = "",
    val amount: String = "",
    val operator: Operator = Operator.ADD
)

data class HistoryEntry(val id: String, val label: String, val value: String, val createdAt: Long)

/**
 * One result contract is used by the normal calculators and the four/five-value solver.
 * [solvedIndex] and [completedValues] are set only when an empty field was solved.
 */
data class CalculationResult(
    val value: Double? = null,
    val display: String = "—",
    val details: List<String> = emptyList(),
    val error: String? = null,
    val solvedIndex: Int? = null,
    val completedValues: List<Double?> = emptyList()
)

enum class MainTab(val label: String) {
    CAL("CAL"), FOUR_VALUE("4 VALUE"), CASH("CASH"), ORIGINAL("ORIGINAL"), MORE("MORE")
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class FourValueMode(
    val key: String,
    val label: String,
    val fieldLabels: List<String>,
    val defaultValues: List<String>,
    val defaultUnits: List<String>
) {
    DAILY(
        "daily", "Price", listOf("PRICE", "QUANTITY", "PRICE", "QUANTITY"),
        listOf("", "", "", ""), listOf("₹", "kg", "₹", "g")
    ),
    MARKS(
        "marks", "Marks", listOf("TOTAL MARKS", "FULL %", "OBTAINED", "PERCENT"),
        listOf("", "100", "", ""), listOf("marks", "%", "marks", "%")
    ),
    PERCENT(
        "percent", "% Percent", listOf("BASE AMOUNT", "FULL %", "PART AMOUNT", "PERCENT"),
        listOf("", "100", "", ""), listOf("", "%", "", "%")
    ),
    EMI(
        "emi", "EMI / Loan", listOf("LOAN AMOUNT", "ANNUAL RATE", "TENURE", "MONTHLY EMI"),
        listOf("", "", "", ""), listOf("₹", "%", "months", "₹")
    ),
    PROFIT(
        "profit", "Profit", listOf("COST PRICE", "SELLING PRICE", "PROFIT / LOSS", "MARGIN"),
        listOf("", "", "", ""), listOf("₹", "₹", "₹", "%")
    ),
    INTEREST(
        "interest", "₹ Interest", listOf("PRINCIPAL", "ANNUAL RATE", "TIME", "INTEREST", "TOTAL AMOUNT"),
        listOf("", "", "", "", ""), listOf("₹", "%", "years", "₹", "₹")
    ),
    GENERAL(
        "general", "General", listOf("VALUE 1", "VALUE 2", "VALUE 3", "VALUE 4"),
        listOf("", "", "", ""), listOf("", "", "", "")
    );

    val fieldCount: Int get() = fieldLabels.size
    val heading: String get() = if (this == INTEREST) "5 Value Interest Calculator" else "4 Value Calculator"
    val rule: String
        get() = when (this) {
            DAILY -> "Smart Unit Price"
            MARKS -> "Marks to Percentage"
            PERCENT -> "Percent of Amount"
            EMI -> "Smart Loan Calculator"
            PROFIT -> "Profit and Margin"
            INTEREST -> "Simple Interest"
            GENERAL -> "Same Ratio"
        }

    fun valueKey(index: Int) = "four-$key-$index"
    fun unitKey(index: Int) = "four-$key-unit-$index"
    fun inputDefaults(): Map<String, String> = buildMap {
        defaultValues.forEachIndexed { index, value -> put(valueKey(index), value) }
        defaultUnits.forEachIndexed { index, unit -> put(unitKey(index), unit) }
        if (this@FourValueMode == INTEREST) {
            put("four-interest-type", "simple")
            put("four-interest-frequency", "1")
        }
    }

    companion object {
        fun fromKey(value: String?) = entries.firstOrNull { it.key == value } ?: DAILY
    }
}

data class AppState(
    val activeTab: MainTab = MainTab.CAL,
    val rows: List<CalcRow> = listOf(
        CalcRow("milk", "Milk", "60"),
        CalcRow("vegetables", "Vegetables", "150"),
        CalcRow("petrol", "Petrol", "500")
    ),
    val cash: Map<Int, String> = listOf(500, 200, 100, 50, 20, 10).associateWith { "" },
    val toolInputs: Map<String, String> = emptyMap(),
    val fourValueMode: FourValueMode = FourValueMode.DAILY,
    val selectedMoreTool: String? = null,
    val history: List<HistoryEntry> = emptyList(),
    val originalHistory: List<HistoryEntry> = emptyList(),
    val theme: ThemeMode = ThemeMode.SYSTEM
)

data class SmartUnit(val id: String, val label: String, val group: String, val base: Double = 1.0)

data class DailySuggestion(val quantity: Double, val unit: String, val price: Double)

object CalculationEngine {
    private val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 3
        minimumFractionDigits = 0
    }

    /** Reference unit names and conversion factors (base: metre / kilogram / square metre / second). */
    val lengthUnits = linkedMapOf(
        "Micrometer (µm)" to 0.000001,
        "Millimeter (mm)" to 0.001,
        "Centimeter (cm)" to 0.01,
        "Inch (in)" to 0.0254,
        "Foot (ft)" to 0.3048,
        "Yard (yd)" to 0.9144,
        "Gaj" to 0.9144,
        "Meter (m)" to 1.0,
        "Kilometer (km)" to 1000.0,
        "Mile (mi)" to 1609.344,
        "Nautical Mile (nmi)" to 1852.0
    )
    val weightUnits = linkedMapOf(
        "Milligram (mg)" to 0.000001,
        "Gram (g)" to 0.001,
        "Traditional Tola (11.6638 g)" to 0.0116638,
        "Ounce (oz)" to 0.028349523125,
        "Pound (lb)" to 0.45359237,
        "Kilogram (kg)" to 1.0,
        "Quintal (q)" to 100.0,
        "Metric Ton (t)" to 1000.0
    )
    val areaUnits = linkedMapOf(
        "Square Foot (ft²)" to 0.09290304,
        "Square Yard (yd²)" to 0.83612736,
        "Square Meter (m²)" to 1.0,
        "Cent" to 40.468564224,
        "Are (a)" to 100.0,
        "Guntha" to 101.17141056,
        "Acre" to 4046.8564224,
        "Hectare" to 10000.0,
        "Square Kilometer (km²)" to 1000000.0
    )
    val timeUnits = linkedMapOf(
        "Second" to 1.0,
        "Minute" to 60.0,
        "Hour" to 3600.0,
        "Day" to 86400.0,
        "Week" to 604800.0
    )

    private val smartUnits = listOf(
        SmartUnit("₹", "₹", "fixed"), SmartUnit("%", "%", "fixed"), SmartUnit("marks", "Marks", "fixed"),
        SmartUnit("", "", "fixed"), SmartUnit("months", "Months", "time"), SmartUnit("years", "Years", "time"),
        SmartUnit("mg", "mg", "mass", 0.001), SmartUnit("g", "g", "mass", 1.0),
        SmartUnit("tola", "Traditional Tola", "mass", 11.6638), SmartUnit("kg", "kg", "mass", 1000.0),
        SmartUnit("quintal", "Quintal", "mass", 100000.0), SmartUnit("ton", "Metric Ton", "mass", 1000000.0),
        SmartUnit("piece", "Piece", "count", 1.0), SmartUnit("dozen", "Dozen", "count", 12.0)
    ).associateBy { it.id }

    private val dailyUnitBase = mapOf(
        "quintal" to 100000.0, "kg" to 1000.0, "g" to 1.0, "tola" to 11.6638,
        "l" to 1000.0, "ml" to 1.0, "m" to 100.0, "gaj" to 91.44, "piece" to 1.0, "dozen" to 12.0
    )
    private val dailyUnitLabel = mapOf(
        "quintal" to "Quintal (q)", "kg" to "kg", "g" to "g", "tola" to "Traditional Tola (11.6638 g)",
        "l" to "Litre", "ml" to "ml", "m" to "Meter (m)", "gaj" to "Gaj", "piece" to "Piece", "dozen" to "Dozen"
    )
    private val dailyGroups = mapOf(
        "quintal" to listOf("g", "tola", "kg", "quintal"), "kg" to listOf("g", "tola", "kg", "quintal"),
        "g" to listOf("g", "tola", "kg", "quintal"), "tola" to listOf("g", "tola", "kg", "quintal"),
        "l" to listOf("ml", "l"), "ml" to listOf("ml", "l"), "m" to listOf("m", "gaj"),
        "gaj" to listOf("m", "gaj"), "piece" to listOf("piece", "dozen"), "dozen" to listOf("piece", "dozen")
    )

    val dailyPriceBaseUnits: List<String> = listOf("quintal", "kg", "g", "tola", "l", "ml", "m", "gaj", "piece", "dozen")
    val manualCurrencies = listOf("INR", "USD", "EUR", "GBP", "JPY", "AED", "AUD", "CAD", "CHF", "CNY", "SGD", "SAR")

    fun format(value: Double): String {
        if (!value.isFinite()) return "0"
        val rounded = round((value + Math.ulp(value)) * 1000.0) / 1000.0
        return formatter.format(rounded)
    }

    fun raw(value: Double): String = format(value).replace(",", "")

    /** Keeps a decimal being typed intact while grouping its whole portion in Indian style. */
    fun formatTyping(text: String): String {
        val value = text.replace(",", "")
        if (value.isBlank() || value == "-" || value == "." || value == "-.") return value
        if (!value.matches(Regex("-?\\d*(\\.\\d*)?"))) return text
        val negative = value.startsWith("-")
        val unsigned = if (negative) value.drop(1) else value
        val parts = unsigned.split('.', limit = 2)
        val whole = (parts.firstOrNull().orEmpty().ifBlank { "0" }).replace(Regex("^0+(?=\\d)"), "")
        val grouped = formatIndianWhole(whole)
        return buildString {
            if (negative) append('-')
            append(grouped)
            if (parts.size == 2) append('.').append(parts[1])
        }
    }

    fun formatIndianExpression(expression: String): String =
        Regex("-?\\d+(?:\\.\\d+)?").replace(expression) { formatTyping(it.value) }

    private fun formatIndianWhole(value: String): String {
        if (value.length <= 3) return value
        val lastThree = value.takeLast(3)
        val rest = value.dropLast(3)
        val groups = rest.reversed().chunked(2).map { it.reversed() }.reversed()
        return (groups + lastThree).joinToString(",")
    }

    private fun ok(
        value: Double,
        details: List<String> = emptyList(),
        solvedIndex: Int? = null,
        completedValues: List<Double?> = emptyList()
    ): CalculationResult = if (!value.isFinite()) {
        fail("Unable to calculate a finite result.")
    } else {
        CalculationResult(value, format(value), details, solvedIndex = solvedIndex, completedValues = completedValues)
    }

    private fun fail(message: String) = CalculationResult(error = message)

    private fun parseOptional(text: String): Double? = text.replace(",", "").trim().toDoubleOrNull()?.takeIf { it.isFinite() }

    private fun parseValues(raw: List<String>): Pair<List<Double?>, String?> {
        val values = raw.map { text -> if (text.trim().isBlank()) null else parseOptional(text) }
        return values to if (values.indices.any { raw[it].trim().isNotBlank() && values[it] == null }) "Enter valid values." else null
    }

    private fun finite(vararg values: Double): Boolean = values.all { it.isFinite() }

    fun rows(rows: List<CalcRow>): CalculationResult {
        val filled = rows.filter { it.amount.isNotBlank() }
        if (filled.isEmpty()) return ok(0.0)
        var total = 0.0
        filled.forEachIndexed { index, row ->
            val value = parseOptional(row.amount) ?: return fail("Enter a valid amount.")
            if (index == 0) {
                total = if (row.operator == Operator.SUBTRACT) -value else value
            } else {
                when (row.operator) {
                    Operator.ADD -> total += value
                    Operator.SUBTRACT -> total -= value
                    Operator.MULTIPLY -> total *= value
                    Operator.DIVIDE -> if (value == 0.0) return fail("Cannot divide by zero.") else total /= value
                }
            }
        }
        // The CAL preview mirrors the reference app: it shows the typed operation
        // sequence separately from the calculated total shown in the summary pill.
        val expression = filled.joinToString(" ") { row ->
            "${row.operator.symbol} ${format(parseOptional(row.amount) ?: 0.0)}"
        }
        return ok(total, details = listOf(expression))
    }

    fun ratio(values: List<Double?>): CalculationResult {
        if (values.size != 4 || values.count { it == null } != 1) return fail("Leave exactly 1 box empty.")
        val missing = values.indexOf(null)
        val (a, b, c, d) = values.map { it ?: 0.0 }
        val answer = when (missing) {
            0 -> if (d == 0.0) return fail("Cannot divide by zero.") else b * c / d
            1 -> if (c == 0.0) return fail("Cannot divide by zero.") else a * d / c
            2 -> if (b == 0.0) return fail("Cannot divide by zero.") else a * d / b
            else -> if (a == 0.0) return fail("Cannot divide by zero.") else b * c / a
        }
        val completed = values.toMutableList().also { it[missing] = answer }
        return ok(answer, listOf("Missing Value ${missing + 1}"), missing, completed)
    }

    fun smartUnitLabel(id: String): String = smartUnits[id]?.label ?: id

    fun smartUnitOptions(mode: FourValueMode, index: Int): List<String> = when (mode) {
        FourValueMode.DAILY -> if (index == 1 || index == 3) listOf("quintal", "kg", "g", "mg", "tola", "ton", "piece", "dozen") else listOf("₹")
        FourValueMode.MARKS -> if (index == 0 || index == 2) listOf("marks") else listOf("%")
        FourValueMode.PERCENT -> if (index == 1 || index == 3) listOf("%") else listOf("")
        FourValueMode.EMI -> if (index == 2) listOf("months", "years") else listOf("₹", "%", "months", "₹")[index].let(::listOf)
        FourValueMode.PROFIT -> listOf("₹", "₹", "₹", "%")[index].let(::listOf)
        FourValueMode.INTEREST -> listOf("₹", "%", "years", "₹", "₹")[index].let(::listOf)
        FourValueMode.GENERAL -> listOf("")
    }

    fun compatibleDailyUnit(unit: String): String = when (smartUnits[unit]?.group) {
        "mass" -> "g"
        "count" -> "piece"
        else -> unit
    }

    private fun canConvertSmart(first: String, second: String): Boolean {
        val left = smartUnits[first] ?: return false
        val right = smartUnits[second] ?: return false
        return left.group == right.group && left.group in setOf("mass", "count")
    }

    private fun convertSmart(value: Double, from: String, to: String): Double {
        val source = smartUnits[from] ?: return value
        val target = smartUnits[to] ?: return value
        return value * source.base / target.base
    }

    fun smartRatio(mode: FourValueMode, raw: List<String>, units: List<String>): CalculationResult {
        if (raw.size != 4 || units.size != 4) return fail("Enter four values.")
        if (units.indices.any { units[it] !in smartUnitOptions(mode, it) }) return fail("Select valid units.")
        val (parsed, parseError) = parseValues(raw)
        if (parseError != null) return fail(parseError)
        val missing = parsed.indices.filter { parsed[it] == null }
        if (missing.size != 1) return fail("Leave exactly 1 box empty.")
        val index = missing.single()
        val normalized = parsed.toMutableList()
        if (normalized[0] != null && normalized[2] != null && canConvertSmart(units[0], units[2])) {
            normalized[2] = convertSmart(normalized[2] ?: return fail("Enter valid values."), units[2], units[0])
        }
        if (normalized[1] != null && normalized[3] != null && canConvertSmart(units[1], units[3])) {
            normalized[3] = convertSmart(normalized[3] ?: return fail("Enter valid values."), units[3], units[1])
        }
        val a = normalized[0]
        val b = normalized[1]
        val c = normalized[2]
        val d = normalized[3]
        var answer = when (index) {
            0 -> {
                val knownB = b ?: return fail("Enter valid values.")
                val knownC = c ?: return fail("Enter valid values.")
                val knownD = d ?: return fail("Enter valid values.")
                if (knownD == 0.0) return fail("Cannot divide by zero.")
                knownB * knownC / knownD
            }
            1 -> {
                val knownA = a ?: return fail("Enter valid values.")
                val knownC = c ?: return fail("Enter valid values.")
                val knownD = d ?: return fail("Enter valid values.")
                if (knownC == 0.0) return fail("Cannot divide by zero.")
                knownA * knownD / knownC
            }
            2 -> {
                val knownA = a ?: return fail("Enter valid values.")
                val knownB = b ?: return fail("Enter valid values.")
                val knownD = d ?: return fail("Enter valid values.")
                if (knownB == 0.0) return fail("Cannot divide by zero.")
                knownA * knownD / knownB
            }
            else -> {
                val knownA = a ?: return fail("Enter valid values.")
                val knownB = b ?: return fail("Enter valid values.")
                val knownC = c ?: return fail("Enter valid values.")
                if (knownA == 0.0) return fail("Cannot divide by zero.")
                knownB * knownC / knownA
            }
        }
        val counterpart = when (index) { 0 -> 2; 1 -> 3; 2 -> 0; else -> 1 }
        if (canConvertSmart(units[counterpart], units[index])) answer = convertSmart(answer, units[counterpart], units[index])
        val completed = parsed.toMutableList().also { it[index] = answer }
        val details = when (mode) {
            FourValueMode.DAILY -> "${format(completed[if (index < 2) 1 else 3] ?: 0.0)} ${smartUnitLabel(units[if (index < 2) 1 else 3])} costs ₹ ${format(completed[if (index < 2) 0 else 2] ?: 0.0)}"
            FourValueMode.MARKS -> "${format(completed[2] ?: 0.0)} out of ${format(completed[0] ?: 0.0)} = ${format(completed[3] ?: 0.0)}%"
            FourValueMode.PERCENT -> "${format(completed[2] ?: 0.0)} is ${format(completed[3] ?: 0.0)}% of ${format(completed[0] ?: 0.0)}"
            else -> "Missing Value ${index + 1} = ${formatSmart(answer, units[index])}"
        }
        return ok(answer, listOf(details), index, completed)
    }

    fun formatSmart(value: Double, unit: String): String = when (unit) {
        "₹" -> "₹ ${format(value)}"
        "%" -> "${format(value)}%"
        "" -> format(value)
        else -> "${format(value)} ${smartUnitLabel(unit)}"
    }

    private fun monthlyPayment(principal: Double, annualRate: Double, months: Double): Double {
        if (!principal.isFinite() || !annualRate.isFinite() || !months.isFinite() || principal < 0.0 || annualRate < 0.0 || months <= 0.0) return Double.NaN
        val monthlyRate = annualRate / 1200.0
        if (monthlyRate == 0.0) return principal / months
        val power = (1.0 + monthlyRate).pow(months)
        return (principal * monthlyRate * power / (power - 1.0)).takeIf { it.isFinite() } ?: Double.NaN
    }

    fun smartEmi(raw: List<String>, tenureUnit: String = "months"): CalculationResult {
        if (raw.size != 4) return fail("Enter four loan values.")
        val (parsed, parseError) = parseValues(raw)
        if (parseError != null || parsed.any { it != null && it < 0.0 }) return fail("Enter valid positive loan values.")
        val missing = parsed.indices.filter { parsed[it] == null }
        if (missing.size != 1) return fail("Leave exactly 1 box empty.")
        val index = missing.single()
        var principal = parsed[0]
        var rate = parsed[1]
        var tenureInput = parsed[2]
        var emi = parsed[3]
        var months = tenureInput?.let { if (tenureUnit == "years") it * 12.0 else it }
        val answer = when (index) {
            0 -> {
                if (rate == null || rate < 0 || months == null || months <= 0 || emi == null || emi <= 0) return fail("Enter rate, tenure and monthly EMI.")
                val monthlyRate = rate / 1200.0
                if (monthlyRate == 0.0) emi * months else emi * ((1.0 + monthlyRate).pow(months) - 1.0) / (monthlyRate * (1.0 + monthlyRate).pow(months))
            }
            1 -> {
                if (principal == null || principal <= 0 || months == null || months <= 0 || emi == null || emi <= 0) return fail("Enter loan amount, tenure and monthly EMI.")
                val zeroRateEmi = principal / months
                if (emi < zeroRateEmi) return fail("Monthly EMI cannot be less than zero-interest EMI.")
                if (abs(emi - zeroRateEmi) < 1e-9) 0.0 else {
                    var low = 0.0
                    var high = 100.0
                    while (monthlyPayment(principal, high, months) < emi && high < 1_000_000.0) high *= 2.0
                    if (monthlyPayment(principal, high, months) < emi) return fail("Unable to find a valid interest rate.")
                    repeat(80) {
                        val middle = (low + high) / 2.0
                        if (monthlyPayment(principal, middle, months) < emi) low = middle else high = middle
                    }
                    (low + high) / 2.0
                }
            }
            2 -> {
                if (principal == null || principal <= 0 || rate == null || rate < 0 || emi == null || emi <= 0) return fail("Enter loan amount, rate and monthly EMI.")
                val monthlyRate = rate / 1200.0
                if (monthlyRate == 0.0) principal / emi else {
                    val difference = emi - principal * monthlyRate
                    if (difference <= 0.0) return fail("Monthly EMI is too low for this interest rate.")
                    ln(emi / difference) / ln(1.0 + monthlyRate)
                }
            }
            else -> {
                if (principal == null || principal <= 0 || rate == null || rate < 0 || months == null || months <= 0) return fail("Enter loan amount, rate and tenure.")
                monthlyPayment(principal, rate, months)
            }
        }
        if (!answer.isFinite() || (answer <= 0.0 && index != 1)) return fail("Unable to calculate a valid loan value.")
        when (index) { 0 -> principal = answer; 1 -> rate = answer; 2 -> months = answer; else -> emi = answer }
        val finalPrincipal = principal ?: return fail("Unable to calculate a valid loan value.")
        val finalRate = rate ?: return fail("Unable to calculate a valid loan value.")
        val finalMonths = months ?: return fail("Unable to calculate a valid loan value.")
        val finalEmi = emi ?: return fail("Unable to calculate a valid loan value.")
        val fieldAnswer = if (index == 2 && tenureUnit == "years") answer / 12.0 else answer
        val tenureForField = if (tenureUnit == "years") finalMonths / 12.0 else finalMonths
        val tenureLabel = if (tenureUnit == "years") "${format(tenureForField)} ${if (abs(tenureForField - 1.0) < 1e-9) "Year" else "Years"}" else "${format(finalMonths)} ${if (abs(finalMonths - 1.0) < 1e-9) "Month" else "Months"}"
        val total = finalMonths * finalEmi
        val interest = (total - finalPrincipal).coerceAtLeast(0.0)
        val label = listOf("Loan amount", "Annual interest rate", "Loan tenure", "Monthly EMI")[index]
        val completedValues = listOf(finalPrincipal, finalRate, tenureForField, finalEmi)
        return ok(
            fieldAnswer,
            listOf("$label = ${when (index) { 1 -> "${format(fieldAnswer)}%"; 2 -> tenureLabel; else -> "₹ ${format(fieldAnswer)}" }}", "Tenure $tenureLabel", "Interest ₹ ${format(interest)}", "Total ₹ ${format(total)}"),
            index,
            completedValues
        )
    }

    fun emi(principal: Double, annualRate: Double, months: Int): CalculationResult {
        if (!principal.isFinite() || !annualRate.isFinite() || principal < 0 || annualRate < 0 || months <= 0) return fail("Enter valid positive loan values.")
        val value = monthlyPayment(principal, annualRate, months.toDouble())
        if (!value.isFinite()) return fail("Unable to calculate a valid loan value.")
        val total = value * months
        return ok(value, listOf("Total interest ${format((total - principal).coerceAtLeast(0.0))}", "Total payment ${format(total)}"))
    }

    fun smartProfit(raw: List<String>): CalculationResult {
        if (raw.size != 4) return fail("Enter four profit values.")
        val (parsed, parseError) = parseValues(raw)
        if (parseError != null) return fail(parseError)
        val missing = parsed.indices.filter { parsed[it] == null }
        if (missing.size != 1) return fail("Leave exactly 1 box empty.")
        val index = missing.single()
        var cost = parsed[0]
        var selling = parsed[1]
        var profit = parsed[2]
        var margin = parsed[3]
        when (index) {
            0 -> {
                val knownSelling = selling ?: return fail("Enter valid cost and selling prices.")
                val knownProfit = profit ?: return fail("Enter valid cost and selling prices.")
                cost = knownSelling - knownProfit
            }
            1 -> {
                val knownCost = cost ?: return fail("Enter valid cost and selling prices.")
                val knownProfit = profit ?: return fail("Enter valid cost and selling prices.")
                selling = knownCost + knownProfit
            }
            2 -> {
                val knownSelling = selling ?: return fail("Enter valid cost and selling prices.")
                val knownCost = cost ?: return fail("Enter valid cost and selling prices.")
                profit = knownSelling - knownCost
            }
            else -> {
                if (selling == null || selling <= 0.0) return fail("Selling price must be greater than zero.")
                val knownProfit = profit ?: return fail("Enter valid cost and selling prices.")
                margin = knownProfit / selling * 100.0
            }
        }
        val finalCost = cost ?: return fail("Enter valid cost and selling prices.")
        val finalSelling = selling ?: return fail("Enter valid cost and selling prices.")
        val finalProfit = profit ?: return fail("Enter valid cost and selling prices.")
        val finalMargin = margin ?: return fail("Enter valid cost and selling prices.")
        if (listOf(finalCost, finalSelling, finalProfit, finalMargin).any { !it.isFinite() } || finalCost < 0.0 || finalSelling <= 0.0) return fail("Enter valid cost and selling prices.")
        val expectedProfit = finalSelling - finalCost
        val expectedMargin = expectedProfit / finalSelling * 100.0
        if (differs(finalProfit, expectedProfit) || differs(finalMargin, expectedMargin)) return fail("Values do not match the profit and margin.")
        val answer = listOf(finalCost, finalSelling, finalProfit, finalMargin)[index]
        val profitText = if (finalProfit >= 0) "Profit ₹ ${format(finalProfit)}" else "Loss ₹ ${format(abs(finalProfit))}"
        return ok(answer, listOf("Cost ₹ ${format(finalCost)}", "Selling ₹ ${format(finalSelling)}", profitText, "Margin ${format(finalMargin)}%"), index, listOf(finalCost, finalSelling, finalProfit, finalMargin))
    }

    fun profit(cost: Double, selling: Double): CalculationResult {
        if (!finite(cost, selling) || cost < 0 || selling <= 0) return fail("Enter valid cost and selling prices.")
        val value = selling - cost
        return ok(value, listOf("Margin ${format(value / selling * 100)}%", if (value >= 0) "Profit" else "Loss"))
    }

    fun smartInterest(raw: List<String>, type: String, frequency: Int, timeUnit: String): CalculationResult {
        if (raw.size != 5) return fail("Enter five interest values.")
        if (type !in setOf("simple", "compound") || frequency !in setOf(1, 2, 4, 12) || timeUnit !in setOf("years", "months")) return fail("Select valid interest options.")
        val (parsed, parseError) = parseValues(raw)
        if (parseError != null || parsed.any { it != null && it < 0.0 }) return fail("Enter valid positive interest values.")
        val missing = parsed.indices.filter { parsed[it] == null }
        if (missing.size != 1) return fail("Leave exactly 1 box empty.")
        val index = missing.single()
        var principal = parsed[0]
        var rate = parsed[1]
        var years = parsed[2]?.let { if (timeUnit == "months") it / 12.0 else it }
        var interest = parsed[3]
        var total = parsed[4]
        val periods = frequency.coerceIn(1, 12)
        val compound = type == "compound"
        val answer = when (index) {
            0 -> {
                val knownTotal = total ?: return fail("Enter valid interest values.")
                val knownInterest = interest ?: return fail("Enter valid interest values.")
                knownTotal - knownInterest
            }
            1 -> {
                val knownPrincipal = principal ?: return fail("Principal and time must be greater than zero.")
                val knownYears = years ?: return fail("Principal and time must be greater than zero.")
                val knownInterest = interest ?: return fail("Enter valid interest values.")
                if (knownPrincipal <= 0 || knownYears <= 0) return fail("Principal and time must be greater than zero.")
                if (!compound) knownInterest * 100.0 / (knownPrincipal * knownYears) else periods * ((1.0 + knownInterest / knownPrincipal).pow(1.0 / (periods * knownYears)) - 1.0) * 100.0
            }
            2 -> {
                val knownPrincipal = principal ?: return fail("Principal and rate must be greater than zero.")
                val knownRate = rate ?: return fail("Principal and rate must be greater than zero.")
                val knownInterest = interest ?: return fail("Enter valid interest values.")
                if (knownPrincipal <= 0 || knownRate <= 0) return fail("Principal and rate must be greater than zero.")
                if (!compound) knownInterest * 100.0 / (knownPrincipal * knownRate) else ln(1.0 + knownInterest / knownPrincipal) / (periods * ln(1.0 + knownRate / (100.0 * periods)))
            }
            3 -> {
                val knownTotal = total ?: return fail("Enter valid interest values.")
                val knownPrincipal = principal ?: return fail("Enter valid interest values.")
                knownTotal - knownPrincipal
            }
            else -> {
                val knownPrincipal = principal ?: return fail("Enter valid interest values.")
                val knownInterest = interest ?: return fail("Enter valid interest values.")
                knownPrincipal + knownInterest
            }
        }
        if (!answer.isFinite() || answer < 0.0) return fail("Unable to calculate a valid interest value.")
        when (index) { 0 -> principal = answer; 1 -> rate = answer; 2 -> years = answer; 3 -> interest = answer; else -> total = answer }
        if (listOf(principal, rate, years, interest, total).any { it == null || !it.isFinite() || it < 0.0 }) return fail("Enter valid positive interest values.")
        val finalPrincipal = principal ?: return fail("Enter valid positive interest values.")
        val finalRate = rate ?: return fail("Enter valid positive interest values.")
        val finalYears = years ?: return fail("Enter valid positive interest values.")
        val finalInterest = interest ?: return fail("Enter valid positive interest values.")
        val finalTotal = total ?: return fail("Enter valid positive interest values.")
        val expectedInterest = if (!compound) finalPrincipal * finalRate * finalYears / 100.0 else finalPrincipal * ((1.0 + finalRate / (100.0 * periods)).pow(periods * finalYears) - 1.0)
        val expectedTotal = finalPrincipal + finalInterest
        if (differs(finalInterest, expectedInterest) || differs(finalTotal, expectedTotal)) return fail("Values do not match the selected interest calculation.")
        val timeForField = if (timeUnit == "months") finalYears * 12.0 else finalYears
        val timeLabel = if (timeUnit == "months") "${format(timeForField)} ${if (abs(timeForField - 1.0) < 1e-9) "Month" else "Months"}" else "${format(timeForField)} ${if (abs(timeForField - 1.0) < 1e-9) "Year" else "Years"}"
        val periodName = when (periods) { 2 -> "Half-Yearly"; 4 -> "Quarterly"; 12 -> "Monthly"; else -> "Yearly" }
        val typeName = if (compound) "Compound $periodName" else "Simple Interest"
        val fieldAnswer = if (index == 2 && timeUnit == "months") answer * 12.0 else answer
        return ok(
            fieldAnswer,
            listOf(typeName, "Principal ₹ ${format(finalPrincipal)}", "Rate ${format(finalRate)}%", timeLabel, "Interest ₹ ${format(finalInterest)}", "Total ₹ ${format(finalTotal)}"),
            index,
            listOf(finalPrincipal, finalRate, timeForField, finalInterest, finalTotal)
        )
    }

    fun interest(principal: Double, rate: Double, years: Double, compound: Boolean, periods: Int = 1): CalculationResult {
        if (!finite(principal, rate, years) || principal < 0 || rate < 0 || years < 0 || periods <= 0) return fail("Enter valid positive interest values.")
        val value = if (compound) principal * ((1 + rate / (100 * periods)).pow(periods * years) - 1) else principal * rate * years / 100
        return ok(value, listOf("Total ${format(principal + value)}"))
    }

    private fun differs(first: Double, second: Double): Boolean = abs(first - second) > maxOf(0.01, abs(second) * 0.0001)

    fun cash(cash: Map<Int, String>, add: Double = 0.0, less: Double = 0.0): CalculationResult {
        if (!finite(add, less) || add < 0.0 || less < 0.0) return fail("Cash adjustment cannot be negative.")
        var count = 0L
        var subtotal = 0.0
        cash.forEach { (denomination, text) ->
            if (denomination <= 0) return fail("Enter valid cash denominations.")
            val quantity = text.trim().toIntOrNull()
            if (text.isNotBlank() && (quantity == null || quantity < 0)) return fail("Enter whole note quantities only.")
            val safeQuantity = quantity ?: 0
            count += safeQuantity.toLong()
            subtotal += denomination * safeQuantity
        }
        return ok(subtotal + add - less, listOf("$count notes", "Notes value ${format(subtotal)}", "Add ₹ ${format(add)} · Less ₹ ${format(less)}"))
    }

    fun indianWords(amount: Double): String {
        if (!amount.isFinite()) return "Zero"
        var whole = floor(abs(amount)).toLong()
        if (whole / 10_000_000L > Int.MAX_VALUE.toLong()) return format(amount)
        val paise = ((abs(amount) - whole) * 100.0).roundToInt().let { if (it == 100) { whole += 1; 0 } else it }
        if (whole / 10_000_000L > Int.MAX_VALUE.toLong()) return format(amount)
        if (whole == 0L && paise == 0) return "Zero"
        val chunks = listOf(10_000_000L to "Crore", 100_000L to "Lakh", 1_000L to "Thousand", 100L to "Hundred")
        val words = mutableListOf<String>()
        if (amount < 0) words += "Minus"
        chunks.forEach { (divisor, label) ->
            if (whole >= divisor) {
                words += twoDigitWords((whole / divisor).toInt())
                words += label
                whole %= divisor
            }
        }
        if (whole > 0) words += twoDigitWords(whole.toInt())
        if (paise > 0) words += "and ${twoDigitWords(paise)} Paise"
        return words.joinToString(" ")
    }

    private fun twoDigitWords(number: Int): String {
        val belowTwenty = listOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
        val tens = listOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
        return when {
            number < 20 -> belowTwenty[number]
            number < 100 -> listOf(tens[number / 10], belowTwenty[number % 10]).filter { it.isNotBlank() }.joinToString(" ")
            else -> "${belowTwenty[number / 100]} Hundred ${twoDigitWords(number % 100)}".trim()
        }
    }

    fun convert(value: Double, from: String, to: String, units: Map<String, Double>): CalculationResult {
        val source = units[from] ?: return fail("Select valid units.")
        val target = units[to] ?: return fail("Select valid units.")
        if (!finite(value, source, target) || source <= 0.0 || target <= 0.0) return fail("Enter a valid conversion value.")
        return ok(value * source / target)
    }

    fun allUnitValues(value: Double, from: String, units: Map<String, Double>): List<Pair<String, String>> =
        units.keys.map { unit -> unit to (convert(value, from, unit, units).display) }

    fun temperature(value: Double, from: String, to: String): CalculationResult {
        if (!value.isFinite()) return fail("Enter a valid temperature.")
        if (from == "Kelvin" && value < 0.0) return fail("Temperature cannot be below absolute zero.")
        val celsius = when (from) {
            "Celsius" -> value
            "Fahrenheit" -> (value - 32.0) * 5.0 / 9.0
            "Kelvin" -> value - 273.15
            else -> return fail("Select valid units.")
        }
        val result = when (to) {
            "Celsius" -> celsius
            "Fahrenheit" -> celsius * 9.0 / 5.0 + 32.0
            "Kelvin" -> celsius + 273.15
            else -> return fail("Select valid units.")
        }
        if (to == "Kelvin" && result < 0.0) return fail("Temperature cannot be below absolute zero.")
        return ok(result)
    }

    fun allTemperatureValues(value: Double, from: String): List<Pair<String, String>> =
        listOf("Celsius", "Fahrenheit", "Kelvin").map { it to temperature(value, from, it).display }

    data class GstResult(val base: Double, val tax: Double, val total: Double, val error: String? = null)

    fun gst(amount: Double, rate: Double, add: Boolean): GstResult {
        if (!finite(amount, rate) || amount < 0 || rate < 0) return GstResult(0.0, 0.0, 0.0, "Enter valid GST values.")
        val total = if (add) amount * (1.0 + rate / 100.0) else amount
        val base = if (add) amount else amount / (1.0 + rate / 100.0)
        if (!finite(base, total)) return GstResult(0.0, 0.0, 0.0, "Unable to calculate GST.")
        return GstResult(base, total - base, total)
    }

    fun discount(price: Double, percent: Double): CalculationResult {
        if (!finite(price, percent) || price < 0 || percent < 0 || percent > 100) return fail("Enter valid price and discount.")
        val saving = price * percent / 100.0
        return ok(price - saving, listOf("Saving ${format(saving)}"))
    }

    fun splitBill(total: Double, people: Double): CalculationResult {
        if (!finite(total, people) || total < 0 || people <= 0) return fail("People must be greater than zero.")
        return ok(total / people, listOf("Per person"))
    }

    fun quantityRate(quantity: Double, rate: Double): CalculationResult {
        if (!finite(quantity, rate) || quantity < 0 || rate < 0) return fail("Enter valid quantity and rate.")
        return ok(quantity * rate)
    }

    fun mileage(distance: Double, fuel: Double, price: Double): CalculationResult {
        if (!finite(distance, fuel, price) || distance < 0 || fuel < 0 || price < 0) return fail("Enter valid mileage values.")
        if (fuel == 0.0) return fail("Fuel cannot be zero.")
        return ok(distance / fuel, listOf("Fuel cost ${format(fuel * price)}"))
    }

    fun percentage(a: Double, b: Double, mode: String): CalculationResult {
        if (!finite(a, b)) return fail("Enter valid percentage values.")
        return when (mode) {
        "of" -> ok(a * b / 100.0)
        "what" -> if (b == 0.0) fail("Total cannot be zero.") else ok(a / b * 100.0)
        "change" -> if (a == 0.0) fail("Old value cannot be zero.") else ok((b - a) / a * 100.0)
        else -> fail("Select a percentage mode.")
        }
    }

    data class AgeResult(val display: String, val days: Long, val error: String? = null)

    fun age(start: String, end: String): AgeResult = try {
        var first = LocalDate.parse(start)
        var second = LocalDate.parse(end)
        if (second < first) {
            val temporary = first
            first = second
            second = temporary
        }
        val period = Period.between(first, second)
        AgeResult("${period.years} Years • ${period.months} Months • ${period.days} Days", ChronoUnit.DAYS.between(first, second))
    } catch (_: Exception) {
        AgeResult("—", 0, "Select valid dates.")
    }

    fun dailyPrice(basePrice: Double, baseQuantity: Double, baseUnit: String, targetQuantity: Double, targetUnit: String): CalculationResult {
        val base = dailyUnitBase[baseUnit] ?: return fail("Select valid base unit.")
        val target = dailyUnitBase[targetUnit] ?: return fail("Select valid target unit.")
        if (targetUnit !in dailyGroups[baseUnit].orEmpty()) return fail("Select a compatible target unit.")
        if (!finite(basePrice, baseQuantity, targetQuantity) || basePrice < 0 || baseQuantity <= 0 || targetQuantity < 0) return fail("Enter valid daily price values.")
        val answer = basePrice * (targetQuantity * target) / (baseQuantity * base)
        return ok(answer, listOf("${format(targetQuantity)} ${dailyUnitLabel[targetUnit]} = ${format(answer)}"))
    }

    fun compatibleDailyPriceUnits(baseUnit: String): List<String> = dailyGroups[baseUnit].orEmpty()

    fun dailySuggestions(basePrice: Double, baseQuantity: Double, baseUnit: String): List<DailySuggestion> {
        if (!finite(basePrice, baseQuantity) || basePrice < 0 || baseQuantity <= 0 || baseUnit !in dailyUnitBase) return emptyList()
        val options = when (baseUnit) {
            "quintal", "kg", "g", "tola" -> listOf(50.0 to "g", 100.0 to "g", 250.0 to "g", 500.0 to "g", 1.0 to "kg", 1.0 to "tola")
            "l", "ml" -> listOf(100.0 to "ml", 250.0 to "ml", 500.0 to "ml", 1.0 to "l", 2.0 to "l", 5.0 to "l")
            "m", "gaj" -> listOf(1.0 to "m", 1.0 to "gaj", 2.0 to "m", 2.0 to "gaj", 5.0 to "m", 10.0 to "gaj")
            else -> listOf(1.0 to "piece", 2.0 to "piece", 6.0 to "piece", 12.0 to "piece", 1.0 to "dozen", 2.0 to "dozen")
        }
        return options.mapNotNull { (quantity, unit) ->
            dailyPrice(basePrice, baseQuantity, baseUnit, quantity, unit).value?.let { DailySuggestion(quantity, unit, it) }
        }
    }

    fun currency(amount: Double, rate: Double): CalculationResult {
        if (!finite(amount, rate) || amount < 0 || rate < 0) return fail("Enter a valid amount and exchange rate.")
        return ok(amount * rate, listOf("Manual rate · works offline"))
    }

    private fun expandPercentExpression(expression: String): String {
        var value = expression.replace(",", "")
        val number = "(\\d*\\.?\\d+)"
        value = Regex("$number\\s*\\+\\s*$number\\s*%").replace(value) { "(${it.groupValues[1]}+(${it.groupValues[1]}*${it.groupValues[2]}/100))" }
        value = Regex("$number\\s*-\\s*$number\\s*%").replace(value) { "(${it.groupValues[1]}-(${it.groupValues[1]}*${it.groupValues[2]}/100))" }
        value = Regex("$number\\s*%\\s*$number").replace(value) { "((${it.groupValues[1]}*${it.groupValues[2]})/100)" }
        return Regex("$number\\s*%").replace(value) { "(${it.groupValues[1]}/100)" }
    }

    /** Validated shunting-yard parser. It deliberately never evaluates source text as code. */
    fun evaluate(expression: String): Double {
        val source = expandPercentExpression(expression)
        require(source.isNotBlank()) { "Invalid expression." }
        val tokenPattern = Regex("\\d*\\.?\\d+|[()+\\-*/]")
        val matches = tokenPattern.findAll(source).toList()
        var position = 0
        matches.forEach { match ->
            require(source.substring(position, match.range.first).isBlank()) { "Invalid expression." }
            position = match.range.last + 1
        }
        require(source.substring(position).isBlank()) { "Invalid expression." }
        val output = mutableListOf<String>()
        val operators = mutableListOf<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)
        var previous: String? = null
        matches.map { it.value }.forEach { token ->
            when {
                token.firstOrNull()?.isDigit() == true || token.startsWith(".") -> output += token
                token == "(" -> operators += token
                token == ")" -> {
                    while (operators.lastOrNull() != null && operators.last() != "(") output += operators.removeAt(operators.lastIndex)
                    require(operators.lastOrNull() == "(") { "Mismatched brackets." }
                    operators.removeAt(operators.lastIndex)
                }
                else -> {
                    if ((previous == null || previous == "(" || precedence.containsKey(previous)) && token == "-") output += "0"
                    while (operators.lastOrNull() != null && operators.last() != "(" && precedence.getValue(operators.last()) >= precedence.getValue(token)) {
                        output += operators.removeAt(operators.lastIndex)
                    }
                    operators += token
                }
            }
            previous = token
        }
        while (operators.isNotEmpty()) {
            val operator = operators.removeAt(operators.lastIndex)
            require(operator != "(") { "Mismatched brackets." }
            output += operator
        }
        val stack = mutableListOf<Double>()
        output.forEach { token ->
            if (!precedence.containsKey(token)) {
                stack += token.toDouble()
            } else {
                val right = stack.removeLastOrNull() ?: error("Invalid expression.")
                val left = stack.removeLastOrNull() ?: error("Invalid expression.")
                if (token == "/" && right == 0.0) error("Cannot divide by zero.")
                stack += when (token) { "+" -> left + right; "-" -> left - right; "*" -> left * right; else -> left / right }
            }
        }
        require(stack.size == 1 && stack[0].isFinite()) { "Invalid expression." }
        return stack.single()
    }

    /** Retained for the original calculator's immediate percent key behaviour. */
    fun percent(expression: String): String {
        val match = Regex("(\\d*\\.?\\d+)$").find(expression.replace(",", "")) ?: return expression
        val operand = match.value.toDouble()
        val prefix = expression.dropLast(match.value.length)
        val operator = prefix.lastOrNull()
        val base = prefix.dropLast(1)
        val percentage = if ((operator == '+' || operator == '-') && base.any(Char::isDigit)) evaluate(base) * operand / 100.0 else operand / 100.0
        return prefix + percentage.toString()
    }
}
