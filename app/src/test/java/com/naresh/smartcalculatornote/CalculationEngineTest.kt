package com.naresh.smartcalculatornote

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationEngineTest {
    private val fixtures by lazy {
        val raw = requireNotNull(javaClass.classLoader?.getResourceAsStream("parity-fixtures.json")).bufferedReader().use { it.readText() }
        JSONObject(raw).getJSONArray("cases")
    }
    private fun case(id: String): JSONObject = (0 until fixtures.length()).map { fixtures.getJSONObject(it) }.first { it.getString("id") == id }
    private fun assertNumber(expected: Double, actual: Double?, delta: Double = 0.000001) = assertEquals(expected, actual!!, delta)

    @Test
    fun rawTypingKeepsDigitOrderWhileIndianDisplayAddsGrouping() {
        var raw = ""
        "123456".forEach { digit -> raw = CalculationEngine.rawTyping(raw + digit) }
        assertEquals("123456", raw)
        assertEquals("1,23,456", CalculationEngine.formatTyping(raw))
        assertEquals("1234567.89", CalculationEngine.rawTyping("12,34,567.89"))
    }

    @Test
    fun emiSummaryMatchesPaymentAndYearlyTotals() {
        val summary = requireNotNull(CalculationEngine.emiSummary(100000.0, 12.0, 12))
        assertEquals(8884.878867834166, summary.monthlyEmi, 0.000001)
        assertEquals(summary.principal, summary.yearlyRows.sumOf { it.principalPaid }, 0.01)
        assertEquals(summary.totalInterest, summary.yearlyRows.sumOf { it.interestPaid }, 0.01)
        assertEquals(summary.totalPayment, summary.principal + summary.totalInterest, 0.01)
    }

    @Test
    fun sharedFixtureFileAndDefaultRowsMatch() {
        assertEquals(1, JSONObject(requireNotNull(javaClass.classLoader?.getResourceAsStream("calculation-contract.json")).bufferedReader().readText()).getInt("schemaVersion"))
        val fixture = case("calc-basic")
        val input = fixture.getJSONObject("input")
        val values = input.getJSONArray("values")
        val ops = input.getJSONArray("operators")
        val rows = (0 until values.length()).map { index ->
            CalcRow(index.toString(), amount = values.getDouble(index).toString(), operator = when (ops.getString(index)) {
                "-" -> Operator.SUBTRACT
                "*" -> Operator.MULTIPLY
                "/" -> Operator.DIVIDE
                else -> Operator.ADD
            })
        }
        assertNumber(fixture.getDouble("expected"), CalculationEngine.rows(rows).value)
        assertNumber(710.0, CalculationEngine.rows(AppState().rows).value)
        assertEquals("12,34,567.5", CalculationEngine.formatTyping("1234567.5"))
        assertEquals(
            "+ 10 - 2 × 3",
            CalculationEngine.rows(
                listOf(
                    CalcRow("1", amount = "10"),
                    CalcRow("2", amount = "2", operator = Operator.SUBTRACT),
                    CalcRow("3", amount = "3", operator = Operator.MULTIPLY)
                )
            ).details.single()
        )
    }

    @Test
    fun rowsAndGenericRatioRejectInvalidCases() {
        assertEquals(case("calc-div-zero").getString("error"), CalculationEngine.rows(listOf(CalcRow("1", amount = "10"), CalcRow("2", amount = "0", operator = Operator.DIVIDE))).error)
        assertEquals("Enter a valid amount.", CalculationEngine.rows(listOf(CalcRow("x", amount = "bad"))).error)
        assertNumber(50.0, CalculationEngine.ratio(listOf(100.0, 20.0, 250.0, null)).value)
        assertEquals("Leave exactly 1 box empty.", CalculationEngine.ratio(listOf(1.0, null, null, 4.0)).error)
        assertEquals("Cannot divide by zero.", CalculationEngine.ratio(listOf(1.0, 0.0, null, 4.0)).error)
    }

    @Test
    fun fourValuePriceUsesAutomaticCompatibleUnitsForEveryUnknown() {
        val units = listOf("₹", "kg", "₹", "g")
        assertNumber(10.0, CalculationEngine.smartRatio(FourValueMode.DAILY, listOf("100", "1", "", "100"), units).value)
        assertNumber(100.0, CalculationEngine.smartRatio(FourValueMode.DAILY, listOf("", "1", "10", "100"), units).value)
        assertNumber(1.0, CalculationEngine.smartRatio(FourValueMode.DAILY, listOf("100", "", "10", "100"), units).value)
        assertNumber(100.0, CalculationEngine.smartRatio(FourValueMode.DAILY, listOf("100", "1", "10", ""), units).value)
        assertEquals("Cannot divide by zero.", CalculationEngine.smartRatio(FourValueMode.DAILY, listOf("100", "0", "", "100"), units).error)
        assertEquals("piece", CalculationEngine.compatibleDailyUnit("dozen"))
        assertEquals("g", CalculationEngine.compatibleDailyUnit("kg"))
    }

    @Test
    fun marksPercentAndGeneralModesUseSameReferenceRatio() {
        assertNumber(75.0, CalculationEngine.smartRatio(FourValueMode.MARKS, listOf("200", "100", "150", ""), listOf("marks", "%", "marks", "%")).value)
        assertNumber(200.0, CalculationEngine.smartRatio(FourValueMode.PERCENT, listOf("800", "100", "", "25"), listOf("", "%", "", "%")).value)
        assertNumber(50.0, CalculationEngine.smartRatio(FourValueMode.GENERAL, listOf("100", "20", "250", ""), listOf("", "", "", "")).value)
    }

    @Test
    fun smartEmiSolvesAllFourUnknownPositionsAndZeroInterest() {
        val normal = CalculationEngine.smartEmi(listOf("100000", "12", "12", ""))
        assertNumber(case("emi-normal").getDouble("expected"), normal.value, 0.000001)
        val payment = normal.value!!.toString()
        assertNumber(100000.0, CalculationEngine.smartEmi(listOf("", "12", "12", payment)).value, 0.0001)
        assertNumber(12.0, CalculationEngine.smartEmi(listOf("100000", "", "12", payment)).value, 0.000001)
        assertNumber(12.0, CalculationEngine.smartEmi(listOf("100000", "12", "", payment)).value, 0.000001)
        assertNumber(1000.0, CalculationEngine.smartEmi(listOf("12000", "0", "12", "")).value)
        assertEquals("Monthly EMI cannot be less than zero-interest EMI.", CalculationEngine.smartEmi(listOf("12000", "", "12", "999")).error)
    }

    @Test
    fun profitSolverCoversEachUnknownAndMismatchValidation() {
        val margin = (20.0 / 120.0 * 100.0).toString()
        assertNumber(100.0, CalculationEngine.smartProfit(listOf("", "120", "20", margin)).value)
        assertNumber(120.0, CalculationEngine.smartProfit(listOf("100", "", "20", margin)).value)
        assertNumber(20.0, CalculationEngine.smartProfit(listOf("100", "120", "", margin)).value)
        assertNumber(20.0 / 120.0 * 100.0, CalculationEngine.smartProfit(listOf("100", "120", "20", "")).value)
        assertEquals("Values do not match the profit and margin.", CalculationEngine.smartProfit(listOf("100", "120", "10", "")).error)
        assertEquals("Enter valid cost and selling prices.", CalculationEngine.smartProfit(listOf("-1", "120", "", "0")).error)
    }

    @Test
    fun interestSolverCoversFiveUnknownsSimpleCompoundAndMonths() {
        val simple = listOf("10000", "10", "2", "2000", "12000")
        simple.indices.forEach { index ->
            val input = simple.toMutableList().also { it[index] = "" }
            val expected = listOf(10000.0, 10.0, 2.0, 2000.0, 12000.0)[index]
            assertNumber(expected, CalculationEngine.smartInterest(input, "simple", 1, "years").value, 0.000001)
        }
        assertNumber(2100.0, CalculationEngine.smartInterest(listOf("10000", "10", "2", "", "12100"), "compound", 1, "years").value)
        assertNumber(2100.0, CalculationEngine.smartInterest(listOf("10000", "10", "24", "", "12100"), "compound", 1, "months").value)
        assertEquals("Values do not match the selected interest calculation.", CalculationEngine.smartInterest(listOf("10000", "10", "2", "", "11900"), "simple", 1, "years").error)
        assertEquals("Leave exactly 1 box empty.", CalculationEngine.smartInterest(listOf("10000", "", "", "2000", "12000"), "simple", 1, "years").error)
    }

    @Test
    fun allConvertersAndDailyPriceCoverUnitsSwapTargetsAndBoundaries() {
        assertNumber(case("length").getDouble("expected"), CalculationEngine.convert(1.0, "Meter (m)", "Foot (ft)", CalculationEngine.lengthUnits).value)
        assertNumber(1000.0, CalculationEngine.convert(1.0, "Kilogram (kg)", "Gram (g)", CalculationEngine.weightUnits).value)
        assertNumber(4046.8564224, CalculationEngine.convert(1.0, "Acre", "Square Meter (m²)", CalculationEngine.areaUnits).value)
        assertNumber(60.0, CalculationEngine.convert(1.0, "Hour", "Minute", CalculationEngine.timeUnits).value)
        assertNumber(32.0, CalculationEngine.temperature(0.0, "Celsius", "Fahrenheit").value)
        assertNumber(273.15, CalculationEngine.temperature(0.0, "Celsius", "Kelvin").value)
        assertNumber(10.0, CalculationEngine.dailyPrice(100.0, 1.0, "kg", 100.0, "g").value)
        assertEquals("Select a compatible target unit.", CalculationEngine.dailyPrice(100.0, 1.0, "kg", 1.0, "ml").error)
        assertTrue(CalculationEngine.dailySuggestions(100.0, 1.0, "kg").any { it.quantity == 100.0 && it.unit == "g" && it.price == 10.0 })
        assertEquals(CalculationEngine.lengthUnits.size, CalculationEngine.allUnitValues(1.0, "Meter (m)", CalculationEngine.lengthUnits).size)
    }

    @Test
    fun moreToolsMathValidationAndIndianCashWordsMatchContract() {
        assertNumber(600.0, CalculationEngine.discount(800.0, 25.0).value)
        assertNumber(250.0, CalculationEngine.splitBill(1000.0, 4.0).value)
        assertNumber(36.0, CalculationEngine.quantityRate(12.0, 3.0).value)
        assertNumber(20.0, CalculationEngine.mileage(100.0, 5.0, 100.0).value)
        assertEquals("Fuel cannot be zero.", CalculationEngine.mileage(100.0, 0.0, 100.0).error)
        assertNumber(100.0, CalculationEngine.percentage(20.0, 500.0, "of").value)
        assertNumber(20.0, CalculationEngine.percentage(100.0, 500.0, "what").value)
        assertNumber(25.0, CalculationEngine.percentage(100.0, 125.0, "change").value)
        assertEquals("Old value cannot be zero.", CalculationEngine.percentage(0.0, 125.0, "change").error)
        assertNumber(835.0, CalculationEngine.currency(10.0, 83.5).value)
        val cash = CalculationEngine.cash(mapOf(500 to "2", 200 to "1", 100 to "", 50 to "", 20 to "", 10 to ""), 10.0, 5.0)
        assertNumber(1205.0, cash.value)
        assertTrue(CalculationEngine.indianWords(1205.0).contains("One Thousand"))
        assertEquals("Enter whole note quantities only.", CalculationEngine.cash(mapOf(500 to "-1"), 0.0, 0.0).error)
    }

    @Test
    fun gstAgeAndReferenceInterestFixturesMatch() {
        assertEquals(1180.0, CalculationEngine.gst(1000.0, 18.0, true).total, 0.0000001)
        assertEquals(1000.0, CalculationEngine.gst(1180.0, 18.0, false).base, 0.0000001)
        assertEquals("Enter valid GST values.", CalculationEngine.gst(-1.0, 18.0, true).error)
        assertNumber(2000.0, CalculationEngine.interest(10000.0, 10.0, 2.0, false).value)
        assertNumber(2100.0, CalculationEngine.interest(10000.0, 10.0, 2.0, true).value)
        val forward = CalculationEngine.age("2020-01-15", "2022-03-20")
        val reverse = CalculationEngine.age("2022-03-20", "2020-01-15")
        assertEquals(forward, reverse)
        assertEquals("Select valid dates.", CalculationEngine.age("not-a-date", "2022-01-01").error)
    }

    @Test
    fun invalidAndExtremeInputsReturnErrorsInsteadOfCrashing() {
        assertEquals("Select valid units.", CalculationEngine.smartRatio(FourValueMode.DAILY, listOf("100", "1", "10", "100"), listOf("bad", "kg", "₹", "g")).error)
        assertEquals("Select valid interest options.", CalculationEngine.smartInterest(listOf("10000", "10", "2", "2000", ""), "simple", 3, "years").error)
        assertEquals("Temperature cannot be below absolute zero.", CalculationEngine.temperature(-1.0, "Kelvin", "Celsius").error)
        assertEquals("Enter valid price and discount.", CalculationEngine.discount(100.0, 101.0).error)
        assertEquals("Enter valid positive loan values.", CalculationEngine.emi(Double.POSITIVE_INFINITY, 10.0, 12).error)
        assertTrue(CalculationEngine.indianWords(Double.MAX_VALUE).isNotBlank())
    }

    @Test
    fun safeParserSupportsReferencePercentSemanticsWithoutCodeExecution() {
        assertNumber(12.0, CalculationEngine.evaluate("2+3*4-(2)"))
        assertNumber(220.0, CalculationEngine.evaluate("200+10%"))
        assertNumber(180.0, CalculationEngine.evaluate("200-10%"))
        assertNumber(1.0, CalculationEngine.evaluate("20%5"))
        assertNumber(0.1, CalculationEngine.evaluate("10%"))
        assertEquals("200+20.0", CalculationEngine.percent("200+10"))
        assertTrue(runCatching { CalculationEngine.evaluate("alert(1)") }.isFailure)
        assertEquals("Cannot divide by zero.", runCatching { CalculationEngine.evaluate("4/0") }.exceptionOrNull()?.message)
    }
}
