package com.naresh.smartcalculatornote

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.icu.util.ULocale
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private data class LocalizedMeasure(val unit: MeasureUnit, val symbol: String? = null)

private val standardUnits = linkedMapOf(
    "Micrometer (µm)" to LocalizedMeasure(MeasureUnit.MICROMETER, "µm"),
    "Millimeter (mm)" to LocalizedMeasure(MeasureUnit.MILLIMETER, "mm"),
    "Centimeter (cm)" to LocalizedMeasure(MeasureUnit.CENTIMETER, "cm"),
    "Nautical Mile (nmi)" to LocalizedMeasure(MeasureUnit.NAUTICAL_MILE, "nmi"),
    "Kilometer (km)" to LocalizedMeasure(MeasureUnit.KILOMETER, "km"),
    "Meter (m)" to LocalizedMeasure(MeasureUnit.METER, "m"),
    "Inch (in)" to LocalizedMeasure(MeasureUnit.INCH, "in"),
    "Foot (ft)" to LocalizedMeasure(MeasureUnit.FOOT, "ft"),
    "Yard (yd)" to LocalizedMeasure(MeasureUnit.YARD, "yd"),
    "Mile (mi)" to LocalizedMeasure(MeasureUnit.MILE, "mi"),
    "Milligram (mg)" to LocalizedMeasure(MeasureUnit.MILLIGRAM, "mg"),
    "Kilogram (kg)" to LocalizedMeasure(MeasureUnit.KILOGRAM, "kg"),
    "Gram (g)" to LocalizedMeasure(MeasureUnit.GRAM, "g"),
    "Ounce (oz)" to LocalizedMeasure(MeasureUnit.OUNCE, "oz"),
    "Pound (lb)" to LocalizedMeasure(MeasureUnit.POUND, "lb"),
    "Metric Ton (t)" to LocalizedMeasure(MeasureUnit.METRIC_TON, "t"),
    "Metric Ton" to LocalizedMeasure(MeasureUnit.METRIC_TON),
    "Ton (t)" to LocalizedMeasure(MeasureUnit.METRIC_TON, "t"),
    "Square Kilometer (km²)" to LocalizedMeasure(MeasureUnit.SQUARE_KILOMETER, "km²"),
    "Square Meter (m²)" to LocalizedMeasure(MeasureUnit.SQUARE_METER, "m²"),
    "Square Foot (ft²)" to LocalizedMeasure(MeasureUnit.SQUARE_FOOT, "ft²"),
    "Square Yard (yd²)" to LocalizedMeasure(MeasureUnit.SQUARE_YARD, "yd²"),
    "Acre" to LocalizedMeasure(MeasureUnit.ACRE),
    "Hectare" to LocalizedMeasure(MeasureUnit.HECTARE),
    "Second" to LocalizedMeasure(MeasureUnit.SECOND),
    "Minute" to LocalizedMeasure(MeasureUnit.MINUTE),
    "Hour" to LocalizedMeasure(MeasureUnit.HOUR),
    "Day" to LocalizedMeasure(MeasureUnit.DAY),
    "Week" to LocalizedMeasure(MeasureUnit.WEEK),
    "Months" to LocalizedMeasure(MeasureUnit.MONTH),
    "Years" to LocalizedMeasure(MeasureUnit.YEAR),
    "Celsius" to LocalizedMeasure(MeasureUnit.CELSIUS, "°C"),
    "Fahrenheit" to LocalizedMeasure(MeasureUnit.FAHRENHEIT, "°F"),
    "Kelvin" to LocalizedMeasure(MeasureUnit.KELVIN, "K"),
    "Litre" to LocalizedMeasure(MeasureUnit.LITER)
)

private val customUnitKeys = listOf("Gaj", "Traditional Tola", "Quintal", "Cent", "Are", "Guntha", "Marks", "Piece", "Dozen", "Tola")

private fun customUnits(vararg values: String): Map<String, String> {
    require(values.size == customUnitKeys.size)
    return customUnitKeys.zip(values).toMap()
}

private val customUnitsByLanguage = mapOf(
    "gu" to customUnits("ગજ", "પરંપરાગત તોલા", "ક્વિન્ટલ", "સેન્ટ", "આર", "ગુંઠા", "ગુણ", "નંગ", "ડઝન", "તોલા"),
    "hi" to customUnits("गज", "पारंपरिक तोला", "क्विंटल", "सेंट", "आर", "गुंठा", "अंक", "नग", "दर्जन", "तोला"),
    "bn" to customUnits("গজ", "ঐতিহ্যবাহী তোলা", "কুইন্টাল", "সেন্ট", "আর", "গুন্থা", "নম্বর", "টি", "ডজন", "তোলা"),
    "mr" to customUnits("गज", "पारंपरिक तोळा", "क्विंटल", "सेंट", "आर", "गुंठा", "गुण", "नग", "डझन", "तोळा"),
    "pa" to customUnits("ਗਜ਼", "ਰਵਾਇਤੀ ਤੋਲਾ", "ਕੁਇੰਟਲ", "ਸੈਂਟ", "ਆਰ", "ਗੁੰਠਾ", "ਅੰਕ", "ਨਗ", "ਦਰਜਨ", "ਤੋਲਾ"),
    "ta" to customUnits("கஜம்", "பாரம்பரிய தோலா", "குவிண்டால்", "சென்ட்", "ஆர்", "குண்டா", "மதிப்பெண்கள்", "துண்டு", "டஜன்", "தோலா"),
    "te" to customUnits("గజం", "సాంప్రదాయ తులం", "క్వింటాల్", "సెంట్", "ఆర్", "గుంట", "మార్కులు", "నగ", "డజను", "తులం"),
    "kn" to customUnits("ಗಜ", "ಸಾಂಪ್ರದಾಯಿಕ ತೊಲಾ", "ಕ್ವಿಂಟಲ್", "ಸೆಂಟ್", "ಆರ್", "ಗುಂಟೆ", "ಅಂಕಗಳು", "ನಗ", "ಡಜನ್", "ತೊಲಾ"),
    "ml" to customUnits("ഗജം", "പരമ്പരാഗത തോല", "ക്വിന്റൽ", "സെന്റ്", "ആർ", "ഗുണ്ട", "മാർക്ക്", "എണ്ണം", "ഡസൻ", "തോല"),
    "es" to customUnits("Gaj", "Tola tradicional", "Quintal", "Cent", "Are", "Guntha", "Puntos", "Unidad", "Docena", "Tola"),
    "fr" to customUnits("Gaj", "Tola traditionnel", "Quintal", "Cent", "Are", "Guntha", "Points", "Unité", "Douzaine", "Tola"),
    "de" to customUnits("Gaj", "Traditioneller Tola", "Zentner", "Cent", "Ar", "Guntha", "Punkte", "Stück", "Dutzend", "Tola"),
    "ar" to customUnits("غاج", "تولا تقليدية", "قنطار", "سنت", "آر", "غونثا", "درجات", "قطعة", "دزينة", "تولا"),
    "pt" to customUnits("Gaj", "Tola tradicional", "Quintal", "Cent", "Are", "Guntha", "Pontos", "Unidade", "Dúzia", "Tola"),
    "zh" to customUnits("加吉", "传统托拉", "公担", "分地", "公亩", "贡塔", "分数", "件", "打", "托拉"),
    "ja" to customUnits("ガジ", "伝統的トラ", "キンタル", "セント", "アール", "グンタ", "点", "個", "ダース", "トラ")
)

private val customAliases = mapOf(
    "Gaj" to Pair("Gaj", null),
    "Traditional Tola (11.6638 g)" to Pair("Traditional Tola", "11.6638 g"),
    "Traditional Tola" to Pair("Traditional Tola", null),
    "Tola" to Pair("Tola", null),
    "Quintal (q)" to Pair("Quintal", "q"),
    "Quintal" to Pair("Quintal", null),
    "Cent" to Pair("Cent", null),
    "Are (a)" to Pair("Are", "a"),
    "Guntha" to Pair("Guntha", null),
    "Marks" to Pair("Marks", null),
    "Piece" to Pair("Piece", null),
    "Dozen" to Pair("Dozen", null)
)

private val unitCache = ConcurrentHashMap<String, Map<String, String>>()

private fun unitMap(language: String): Map<String, String> = unitCache.getOrPut(language) {
    val locale = Locale.forLanguageTag(language)
    val formatter = MeasureFormat.getInstance(ULocale.forLocale(locale), MeasureFormat.FormatWidth.WIDE)
    val one = NumberFormat.getNumberInstance(locale).format(1)
    buildMap {
        standardUnits.forEach { (english, spec) ->
            val name = formatter.format(Measure(1, spec.unit)).replaceFirst(one, "").trim()
            put(english, if (spec.symbol == null) name else "$name (${spec.symbol})")
        }
        val custom = customUnitsByLanguage[locale.language].orEmpty()
        customAliases.forEach { (english, alias) ->
            val name = custom[alias.first] ?: alias.first
            put(english, if (alias.second == null) name else "$name (${alias.second})")
        }
    }
}

internal fun localizeUnitNames(text: String, language: String): String {
    if (language == "en") return text
    val names = unitMap(language)
    names[text]?.let { return it }
    var result = text
    names.entries.sortedByDescending { it.key.length }.forEach { (english, localized) ->
        val unitPattern = Regex("(?<![\\p{L}\\p{N}])${Regex.escape(english)}(?![\\p{L}\\p{N}])")
        result = unitPattern.replace(result, localized)
    }
    return result
}
