package com.naresh.smartcalculatornote

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

private val localizationKeys = listOf(
    "CAL", "4 VALUE", "CASH", "ORIGINAL", "MORE", "Calculator", "Smart Note", "More Tools", "Settings",
    "App language", "Font size", "Appearance", "System", "Light", "Dark", "Smart Notes", "New note", "No notes yet",
    "Edit", "Done", "Reminder", "Once", "Daily", "Weekly", "Save note", "Delete note", "Quick EMI", "Advanced",
    "Loan amount", "Annual interest rate %", "Tenure", "Months", "Years", "MONTHLY EMI", "YEAR-WISE BREAKUP",
    "Total payment", "Balance", "Cancel", "Title", "Details"
)

private val localizedValues = mapOf(
    "gu" to listOf("ગણતરી", "4 કિંમત", "રોકડ", "મૂળ", "વધુ", "કેલ્ક્યુલેટર", "સ્માર્ટ નોંધ", "વધુ સાધનો", "સેટિંગ્સ", "એપ ભાષા", "ફોન્ટ કદ", "દેખાવ", "સિસ્ટમ", "લાઇટ", "ડાર્ક", "સ્માર્ટ નોંધો", "નવી નોંધ", "હજી કોઈ નોંધ નથી", "સંપાદિત કરો", "પૂર્ણ", "રીમાઇન્ડર", "એકવાર", "દરરોજ", "દર અઠવાડિયે", "નોંધ સાચવો", "નોંધ કાઢો", "ઝડપી EMI", "એડવાન્સ્ડ", "લોન રકમ", "વાર્ષિક વ્યાજ દર %", "મુદત", "મહિના", "વર્ષ", "માસિક EMI", "વર્ષવાર વિગત", "કુલ ચુકવણી", "બાકી રકમ", "રદ કરો", "શીર્ષક", "વિગત"),
    "hi" to listOf("गणना", "4 मान", "नकद", "मूल", "अधिक", "कैलकुलेटर", "स्मार्ट नोट", "अधिक टूल", "सेटिंग्स", "ऐप भाषा", "फ़ॉन्ट आकार", "दिखावट", "सिस्टम", "लाइट", "डार्क", "स्मार्ट नोट्स", "नया नोट", "अभी कोई नोट नहीं", "संपादित करें", "पूर्ण", "रिमाइंडर", "एक बार", "प्रतिदिन", "साप्ताहिक", "नोट सहेजें", "नोट हटाएँ", "क्विक EMI", "एडवांस्ड", "लोन राशि", "वार्षिक ब्याज दर %", "अवधि", "महीने", "वर्ष", "मासिक EMI", "वर्षवार विवरण", "कुल भुगतान", "शेष", "रद्द करें", "शीर्षक", "विवरण"),
    "bn" to listOf("হিসাব", "৪ মান", "নগদ", "মূল", "আরও", "ক্যালকুলেটর", "স্মার্ট নোট", "আরও টুল", "সেটিংস", "অ্যাপ ভাষা", "ফন্ট আকার", "চেহারা", "সিস্টেম", "লাইট", "ডার্ক", "স্মার্ট নোটস", "নতুন নোট", "এখনও নোট নেই", "সম্পাদনা", "সম্পন্ন", "রিমাইন্ডার", "একবার", "প্রতিদিন", "সাপ্তাহিক", "নোট সংরক্ষণ", "নোট মুছুন", "দ্রুত EMI", "উন্নত", "ঋণের পরিমাণ", "বার্ষিক সুদের হার %", "মেয়াদ", "মাস", "বছর", "মাসিক EMI", "বছরভিত্তিক বিবরণ", "মোট পরিশোধ", "বাকি", "বাতিল", "শিরোনাম", "বিবরণ"),
    "mr" to listOf("गणना", "4 मूल्य", "रोख", "मूळ", "अधिक", "कॅल्क्युलेटर", "स्मार्ट नोट", "अधिक साधने", "सेटिंग्स", "अॅप भाषा", "फॉन्ट आकार", "दिसणे", "सिस्टम", "लाइट", "डार्क", "स्मार्ट नोट्स", "नवीन नोट", "अजून नोट नाही", "संपादित करा", "पूर्ण", "रिमाइंडर", "एकदा", "दररोज", "साप्ताहिक", "नोट जतन करा", "नोट हटवा", "जलद EMI", "प्रगत", "कर्ज रक्कम", "वार्षिक व्याजदर %", "कालावधी", "महिने", "वर्षे", "मासिक EMI", "वर्षनिहाय तपशील", "एकूण भरणा", "शिल्लक", "रद्द", "शीर्षक", "तपशील"),
    "pa" to listOf("ਗਣਨਾ", "4 ਮੁੱਲ", "ਨਕਦ", "ਮੂਲ", "ਹੋਰ", "ਕੈਲਕੁਲੇਟਰ", "ਸਮਾਰਟ ਨੋਟ", "ਹੋਰ ਟੂਲ", "ਸੈਟਿੰਗਾਂ", "ਐਪ ਭਾਸ਼ਾ", "ਫੋਂਟ ਆਕਾਰ", "ਦਿੱਖ", "ਸਿਸਟਮ", "ਲਾਈਟ", "ਡਾਰਕ", "ਸਮਾਰਟ ਨੋਟਸ", "ਨਵਾਂ ਨੋਟ", "ਹਾਲੇ ਕੋਈ ਨੋਟ ਨਹੀਂ", "ਸੋਧੋ", "ਮੁਕੰਮਲ", "ਰੀਮਾਈਂਡਰ", "ਇੱਕ ਵਾਰ", "ਰੋਜ਼ਾਨਾ", "ਹਫ਼ਤਾਵਾਰੀ", "ਨੋਟ ਸੰਭਾਲੋ", "ਨੋਟ ਮਿਟਾਓ", "ਤੇਜ਼ EMI", "ਉੱਨਤ", "ਕਰਜ਼ਾ ਰਕਮ", "ਸਾਲਾਨਾ ਵਿਆਜ ਦਰ %", "ਮਿਆਦ", "ਮਹੀਨੇ", "ਸਾਲ", "ਮਾਸਿਕ EMI", "ਸਾਲਾਨਾ ਵੇਰਵਾ", "ਕੁੱਲ ਭੁਗਤਾਨ", "ਬਕਾਇਆ", "ਰੱਦ", "ਸਿਰਲੇਖ", "ਵੇਰਵਾ"),
    "ta" to listOf("கணக்கு", "4 மதிப்பு", "பணம்", "அசல்", "மேலும்", "கணிப்பான்", "ஸ்மார்ட் குறிப்பு", "மேலும் கருவிகள்", "அமைப்புகள்", "செயலி மொழி", "எழுத்தளவு", "தோற்றம்", "சிஸ்டம்", "லைட்", "டார்க்", "ஸ்மார்ட் குறிப்புகள்", "புதிய குறிப்பு", "குறிப்புகள் இல்லை", "திருத்து", "முடிந்தது", "நினைவூட்டல்", "ஒருமுறை", "தினசரி", "வாரந்தோறும்", "குறிப்பை சேமி", "குறிப்பை நீக்கு", "விரைவு EMI", "மேம்பட்ட", "கடன் தொகை", "ஆண்டு வட்டி விகிதம் %", "காலம்", "மாதங்கள்", "ஆண்டுகள்", "மாத EMI", "ஆண்டு விவரம்", "மொத்த செலுத்தல்", "மீதம்", "ரத்து", "தலைப்பு", "விவரங்கள்"),
    "te" to listOf("లెక్క", "4 విలువ", "నగదు", "అసలు", "మరిన్ని", "కాలిక్యులేటర్", "స్మార్ట్ నోట్", "మరిన్ని సాధనాలు", "సెట్టింగ్స్", "యాప్ భాష", "ఫాంట్ పరిమాణం", "రూపం", "సిస్టమ్", "లైట్", "డార్క్", "స్మార్ట్ నోట్స్", "కొత్త నోట్", "ఇంకా నోట్స్ లేవు", "సవరించు", "పూర్తి", "రిమైండర్", "ఒక్కసారి", "రోజువారీ", "వారానికి", "నోట్ సేవ్", "నోట్ తొలగించు", "త్వరిత EMI", "అడ్వాన్స్‌డ్", "రుణ మొత్తం", "వార్షిక వడ్డీ రేటు %", "కాలపరిమితి", "నెలలు", "సంవత్సరాలు", "నెలవారీ EMI", "సంవత్సర వివరాలు", "మొత్తం చెల్లింపు", "బాకీ", "రద్దు", "శీర్షిక", "వివరాలు"),
    "kn" to listOf("ಲೆಕ್ಕ", "4 ಮೌಲ್ಯ", "ನಗದು", "ಮೂಲ", "ಇನ್ನಷ್ಟು", "ಕ್ಯಾಲ್ಕುಲೇಟರ್", "ಸ್ಮಾರ್ಟ್ ನೋಟ್", "ಇನ್ನಷ್ಟು ಉಪಕರಣಗಳು", "ಸೆಟ್ಟಿಂಗ್‌ಗಳು", "ಆ್ಯಪ್ ಭಾಷೆ", "ಅಕ್ಷರ ಗಾತ್ರ", "ಗೋಚರತೆ", "ಸಿಸ್ಟಮ್", "ಲೈಟ್", "ಡಾರ್ಕ್", "ಸ್ಮಾರ್ಟ್ ನೋಟ್‌ಗಳು", "ಹೊಸ ನೋಟ್", "ಇನ್ನೂ ನೋಟ್ ಇಲ್ಲ", "ತಿದ್ದು", "ಮುಗಿದಿದೆ", "ಜ್ಞಾಪನೆ", "ಒಮ್ಮೆ", "ಪ್ರತಿದಿನ", "ವಾರಕ್ಕೊಮ್ಮೆ", "ನೋಟ್ ಉಳಿಸಿ", "ನೋಟ್ ಅಳಿಸಿ", "ತ್ವರಿತ EMI", "ಸುಧಾರಿತ", "ಸಾಲದ ಮೊತ್ತ", "ವಾರ್ಷಿಕ ಬಡ್ಡಿ ದರ %", "ಅವಧಿ", "ತಿಂಗಳು", "ವರ್ಷ", "ಮಾಸಿಕ EMI", "ವಾರ್ಷಿಕ ವಿವರ", "ಒಟ್ಟು ಪಾವತಿ", "ಬಾಕಿ", "ರದ್ದು", "ಶೀರ್ಷಿಕೆ", "ವಿವರಗಳು"),
    "ml" to listOf("കണക്കു", "4 മൂല്യം", "പണം", "ഒറിജിനൽ", "കൂടുതൽ", "കാൽക്കുലേറ്റർ", "സ്മാർട്ട് നോട്ട്", "കൂടുതൽ ടൂളുകൾ", "ക്രമീകരണങ്ങൾ", "ആപ്പ് ഭാഷ", "ഫോണ്ട് വലുപ്പം", "രൂപം", "സിസ്റ്റം", "ലൈറ്റ്", "ഡാർക്ക്", "സ്മാർട്ട് നോട്ടുകൾ", "പുതിയ നോട്ട്", "നോട്ടുകളില്ല", "തിരുത്തുക", "പൂർത്തിയായി", "ഓർമ്മപ്പെടുത്തൽ", "ഒരിക്കൽ", "ദിവസേന", "ആഴ്ചതോറും", "നോട്ട് സേവ്", "നോട്ട് നീക്കം", "വേഗ EMI", "അഡ്വാൻസ്ഡ്", "വായ്പ തുക", "വാർഷിക പലിശ നിരക്ക് %", "കാലാവധി", "മാസങ്ങൾ", "വർഷങ്ങൾ", "മാസ EMI", "വർഷ വിശദാംശം", "ആകെ അടവ്", "ബാക്കി", "റദ്ദാക്കുക", "ശീർഷകം", "വിശദാംശങ്ങൾ"),
    "es" to listOf("CÁLC.", "4 VALORES", "EFECTIVO", "ORIGINAL", "MÁS", "Calculadora", "Nota inteligente", "Más herramientas", "Ajustes", "Idioma de la app", "Tamaño de fuente", "Apariencia", "Sistema", "Claro", "Oscuro", "Notas inteligentes", "Nueva nota", "Aún no hay notas", "Editar", "Hecho", "Recordatorio", "Una vez", "Diario", "Semanal", "Guardar nota", "Eliminar nota", "EMI rápido", "Avanzado", "Importe del préstamo", "Interés anual %", "Plazo", "Meses", "Años", "EMI MENSUAL", "DESGLOSE ANUAL", "Pago total", "Saldo", "Cancelar", "Título", "Detalles"),
    "fr" to listOf("CALC.", "4 VALEURS", "ESPÈCES", "ORIGINAL", "PLUS", "Calculatrice", "Note intelligente", "Plus d’outils", "Paramètres", "Langue de l’app", "Taille du texte", "Apparence", "Système", "Clair", "Sombre", "Notes intelligentes", "Nouvelle note", "Aucune note", "Modifier", "Terminé", "Rappel", "Une fois", "Quotidien", "Hebdomadaire", "Enregistrer", "Supprimer", "EMI rapide", "Avancé", "Montant du prêt", "Taux annuel %", "Durée", "Mois", "Années", "EMI MENSUEL", "DÉTAIL ANNUEL", "Paiement total", "Solde", "Annuler", "Titre", "Détails"),
    "de" to listOf("RECHNER", "4 WERTE", "BARGELD", "ORIGINAL", "MEHR", "Rechner", "Smart-Notiz", "Weitere Tools", "Einstellungen", "App-Sprache", "Schriftgröße", "Darstellung", "System", "Hell", "Dunkel", "Smart-Notizen", "Neue Notiz", "Noch keine Notizen", "Bearbeiten", "Erledigt", "Erinnerung", "Einmal", "Täglich", "Wöchentlich", "Notiz speichern", "Notiz löschen", "Schnelle EMI", "Erweitert", "Darlehensbetrag", "Jahreszins %", "Laufzeit", "Monate", "Jahre", "MONATLICHE EMI", "JAHRESÜBERSICHT", "Gesamtzahlung", "Restbetrag", "Abbrechen", "Titel", "Details"),
    "ar" to listOf("حساب", "4 قيم", "نقد", "الأصلية", "المزيد", "الآلة الحاسبة", "ملاحظة ذكية", "أدوات إضافية", "الإعدادات", "لغة التطبيق", "حجم الخط", "المظهر", "النظام", "فاتح", "داكن", "ملاحظات ذكية", "ملاحظة جديدة", "لا توجد ملاحظات", "تعديل", "تم", "تذكير", "مرة واحدة", "يوميًا", "أسبوعيًا", "حفظ الملاحظة", "حذف الملاحظة", "EMI سريع", "متقدم", "مبلغ القرض", "الفائدة السنوية %", "المدة", "أشهر", "سنوات", "EMI الشهري", "التفصيل السنوي", "إجمالي الدفع", "الرصيد", "إلغاء", "العنوان", "التفاصيل"),
    "pt" to listOf("CÁLC.", "4 VALORES", "DINHEIRO", "ORIGINAL", "MAIS", "Calculadora", "Nota inteligente", "Mais ferramentas", "Definições", "Idioma da app", "Tamanho da fonte", "Aparência", "Sistema", "Claro", "Escuro", "Notas inteligentes", "Nova nota", "Ainda sem notas", "Editar", "Concluído", "Lembrete", "Uma vez", "Diário", "Semanal", "Guardar nota", "Eliminar nota", "EMI rápido", "Avançado", "Valor do empréstimo", "Juro anual %", "Prazo", "Meses", "Anos", "EMI MENSAL", "DETALHE ANUAL", "Pagamento total", "Saldo", "Cancelar", "Título", "Detalhes"),
    "zh" to listOf("计算", "4 个值", "现金", "原始", "更多", "计算器", "智能便签", "更多工具", "设置", "应用语言", "字体大小", "外观", "系统", "浅色", "深色", "智能便签", "新建便签", "暂无便签", "编辑", "完成", "提醒", "一次", "每天", "每周", "保存便签", "删除便签", "快速 EMI", "高级", "贷款金额", "年利率 %", "期限", "月", "年", "每月 EMI", "年度明细", "总付款", "余额", "取消", "标题", "详情"),
    "ja" to listOf("計算", "4 値", "現金", "標準", "その他", "電卓", "スマートノート", "その他のツール", "設定", "アプリの言語", "文字サイズ", "外観", "システム", "ライト", "ダーク", "スマートノート", "新しいノート", "ノートはありません", "編集", "完了", "リマインダー", "1回", "毎日", "毎週", "ノートを保存", "ノートを削除", "クイック EMI", "詳細", "ローン金額", "年利率 %", "期間", "か月", "年", "月々の EMI", "年別内訳", "総支払額", "残高", "キャンセル", "タイトル", "詳細")
)

private val additionalKeys = listOf(
    "Works offline", "Applies to the whole app", "Choose light, dark or phone setting", "Reminder access",
    "Permission needed for precise reminders", "Notifications: Allowed", "Notifications: Not allowed",
    "Exact reminders: Allowed", "Exact reminders: Approximate time", "Allow exact reminders", "← All tools",
    "Create a note and add an optional reminder.", "Show a phone notification", "Mark as done", "My Calculation",
    "Original Calculator", "CALCULATION HISTORY", "No calculations yet", "Add Row", "RESET", "RESULT", "CALCULATE",
    "Length", "Weight", "Area", "Discount", "Split Bill", "Qty × Rate", "Mileage", "Temperature", "Time",
    "Daily Price", "Age / Date", "Percentage", "GST", "EMI / Loan", "Currency", "History", "Share",
    "Saved History", "Clear", "No saved results yet.", "Share summary"
)

private val additionalValues = mapOf(
    "gu" to listOf("ઓફલાઇન કામ કરે છે", "આખી એપમાં લાગુ પડે છે", "લાઇટ, ડાર્ક અથવા ફોન સેટિંગ પસંદ કરો", "રીમાઇન્ડર પરવાનગી", "ચોક્કસ રીમાઇન્ડર માટે પરવાનગી જરૂરી", "નોટિફિકેશન: મંજૂર", "નોટિફિકેશન: મંજૂર નથી", "ચોક્કસ રીમાઇન્ડર: મંજૂર", "ચોક્કસ રીમાઇન્ડર: અંદાજિત સમય", "ચોક્કસ રીમાઇન્ડર મંજૂર કરો", "← બધા સાધનો", "નોંધ બનાવો અને ઇચ્છો તો રીમાઇન્ડર ઉમેરો.", "ફોન નોટિફિકેશન બતાવો", "પૂર્ણ તરીકે ચિહ્નિત કરો", "મારી ગણતરી", "મૂળ કેલ્ક્યુલેટર", "ગણતરી ઇતિહાસ", "હજી કોઈ ગણતરી નથી", "હરોળ ઉમેરો", "રીસેટ", "પરિણામ", "ગણતરી કરો", "લંબાઈ", "વજન", "વિસ્તાર", "ડિસ્કાઉન્ટ", "બિલ વહેંચો", "જથ્થો × દર", "માઇલેજ", "તાપમાન", "સમય", "દૈનિક ભાવ", "ઉંમર / તારીખ", "ટકાવારી", "GST", "EMI / લોન", "ચલણ", "ઇતિહાસ", "શેર", "સાચવેલો ઇતિહાસ", "સાફ કરો", "હજી કોઈ સાચવેલું પરિણામ નથી.", "સારાંશ શેર કરો"),
    "hi" to listOf("ऑफलाइन काम करता है", "पूरे ऐप पर लागू", "लाइट, डार्क या फोन सेटिंग चुनें", "रिमाइंडर अनुमति", "सटीक रिमाइंडर के लिए अनुमति चाहिए", "नोटिफिकेशन: अनुमति है", "नोटिफिकेशन: अनुमति नहीं", "सटीक रिमाइंडर: अनुमति है", "सटीक रिमाइंडर: अनुमानित समय", "सटीक रिमाइंडर की अनुमति दें", "← सभी टूल", "नोट बनाएँ और चाहें तो रिमाइंडर जोड़ें।", "फोन नोटिफिकेशन दिखाएँ", "पूर्ण चिह्नित करें", "मेरी गणना", "मूल कैलकुलेटर", "गणना इतिहास", "अभी कोई गणना नहीं", "पंक्ति जोड़ें", "रीसेट", "परिणाम", "गणना करें", "लंबाई", "वज़न", "क्षेत्रफल", "छूट", "बिल बाँटें", "मात्रा × दर", "माइलेज", "तापमान", "समय", "दैनिक मूल्य", "आयु / तारीख", "प्रतिशत", "GST", "EMI / लोन", "मुद्रा", "इतिहास", "शेयर", "सहेजा इतिहास", "साफ करें", "अभी कोई सहेजा परिणाम नहीं।", "सारांश शेयर करें"),
    "bn" to listOf("অফলাইনে কাজ করে", "পুরো অ্যাপে প্রযোজ্য", "লাইট, ডার্ক বা ফোন সেটিং বাছুন", "রিমাইন্ডার অনুমতি", "সঠিক রিমাইন্ডারের জন্য অনুমতি দরকার", "নোটিফিকেশন: অনুমোদিত", "নোটিফিকেশন: অনুমোদিত নয়", "সঠিক রিমাইন্ডার: অনুমোদিত", "সঠিক রিমাইন্ডার: আনুমানিক সময়", "সঠিক রিমাইন্ডার অনুমোদন", "← সব টুল", "নোট তৈরি করুন এবং চাইলে রিমাইন্ডার দিন।", "ফোন নোটিফিকেশন দেখান", "সম্পন্ন হিসেবে চিহ্নিত", "আমার হিসাব", "মূল ক্যালকুলেটর", "হিসাবের ইতিহাস", "এখনও হিসাব নেই", "সারি যোগ করুন", "রিসেট", "ফলাফল", "হিসাব করুন", "দৈর্ঘ্য", "ওজন", "এলাকা", "ছাড়", "বিল ভাগ", "পরিমাণ × হার", "মাইলেজ", "তাপমাত্রা", "সময়", "দৈনিক মূল্য", "বয়স / তারিখ", "শতাংশ", "GST", "EMI / ঋণ", "মুদ্রা", "ইতিহাস", "শেয়ার", "সংরক্ষিত ইতিহাস", "পরিষ্কার", "কোনো সংরক্ষিত ফল নেই।", "সারাংশ শেয়ার"),
    "mr" to listOf("ऑफलाइन चालते", "संपूर्ण अॅपला लागू", "लाइट, डार्क किंवा फोन सेटिंग निवडा", "रिमाइंडर परवानगी", "अचूक रिमाइंडरसाठी परवानगी हवी", "सूचना: परवानगी आहे", "सूचना: परवानगी नाही", "अचूक रिमाइंडर: परवानगी आहे", "अचूक रिमाइंडर: अंदाजे वेळ", "अचूक रिमाइंडरला परवानगी", "← सर्व साधने", "नोट तयार करा आणि हवे असल्यास रिमाइंडर जोडा.", "फोन सूचना दाखवा", "पूर्ण म्हणून चिन्हांकित", "माझी गणना", "मूळ कॅल्क्युलेटर", "गणना इतिहास", "अजून गणना नाही", "ओळ जोडा", "रीसेट", "निकाल", "गणना करा", "लांबी", "वजन", "क्षेत्रफळ", "सवलत", "बिल विभागा", "प्रमाण × दर", "मायलेज", "तापमान", "वेळ", "दैनिक किंमत", "वय / तारीख", "टक्केवारी", "GST", "EMI / कर्ज", "चलन", "इतिहास", "शेअर", "जतन इतिहास", "साफ करा", "जतन निकाल नाही.", "सारांश शेअर"),
    "pa" to listOf("ਆਫਲਾਈਨ ਕੰਮ ਕਰਦਾ ਹੈ", "ਪੂਰੀ ਐਪ ਤੇ ਲਾਗੂ", "ਲਾਈਟ, ਡਾਰਕ ਜਾਂ ਫੋਨ ਸੈਟਿੰਗ ਚੁਣੋ", "ਰੀਮਾਈਂਡਰ ਇਜਾਜ਼ਤ", "ਸਹੀ ਰੀਮਾਈਂਡਰ ਲਈ ਇਜਾਜ਼ਤ ਚਾਹੀਦੀ", "ਨੋਟੀਫਿਕੇਸ਼ਨ: ਮਨਜ਼ੂਰ", "ਨੋਟੀਫਿਕੇਸ਼ਨ: ਮਨਜ਼ੂਰ ਨਹੀਂ", "ਸਹੀ ਰੀਮਾਈਂਡਰ: ਮਨਜ਼ੂਰ", "ਸਹੀ ਰੀਮਾਈਂਡਰ: ਅੰਦਾਜ਼ਨ ਸਮਾਂ", "ਸਹੀ ਰੀਮਾਈਂਡਰ ਮਨਜ਼ੂਰ ਕਰੋ", "← ਸਾਰੇ ਟੂਲ", "ਨੋਟ ਬਣਾਓ ਅਤੇ ਚਾਹੋ ਤਾਂ ਰੀਮਾਈਂਡਰ ਜੋੜੋ।", "ਫੋਨ ਨੋਟੀਫਿਕੇਸ਼ਨ ਦਿਖਾਓ", "ਮੁਕੰਮਲ ਨਿਸ਼ਾਨ ਲਾਓ", "ਮੇਰੀ ਗਣਨਾ", "ਮੂਲ ਕੈਲਕੁਲੇਟਰ", "ਗਣਨਾ ਇਤਿਹਾਸ", "ਹਾਲੇ ਗਣਨਾ ਨਹੀਂ", "ਕਤਾਰ ਜੋੜੋ", "ਰੀਸੈੱਟ", "ਨਤੀਜਾ", "ਗਣਨਾ ਕਰੋ", "ਲੰਬਾਈ", "ਭਾਰ", "ਖੇਤਰ", "ਛੂਟ", "ਬਿੱਲ ਵੰਡੋ", "ਮਾਤਰਾ × ਦਰ", "ਮਾਈਲੇਜ", "ਤਾਪਮਾਨ", "ਸਮਾਂ", "ਰੋਜ਼ਾਨਾ ਕੀਮਤ", "ਉਮਰ / ਮਿਤੀ", "ਪ੍ਰਤੀਸ਼ਤ", "GST", "EMI / ਕਰਜ਼ਾ", "ਮੁਦਰਾ", "ਇਤਿਹਾਸ", "ਸਾਂਝਾ", "ਸੰਭਾਲਿਆ ਇਤਿਹਾਸ", "ਸਾਫ਼", "ਕੋਈ ਸੰਭਾਲਿਆ ਨਤੀਜਾ ਨਹੀਂ।", "ਸਾਰ ਸਾਂਝਾ"),
    "ta" to listOf("ஆஃப்லைனில் இயங்கும்", "முழு செயலிக்கும் பொருந்தும்", "லைட், டார்க் அல்லது போன் அமைப்பைத் தேர்வு செய்க", "நினைவூட்டல் அனுமதி", "துல்லிய நினைவூட்டலுக்கு அனுமதி தேவை", "அறிவிப்புகள்: அனுமதி", "அறிவிப்புகள்: அனுமதி இல்லை", "துல்லிய நினைவூட்டல்: அனுமதி", "துல்லிய நினைவூட்டல்: தோராய நேரம்", "துல்லிய நினைவூட்டலை அனுமதி", "← அனைத்து கருவிகள்", "குறிப்பை உருவாக்கி விருப்பமெனில் நினைவூட்டல் சேர்க்கவும்.", "போன் அறிவிப்பைக் காட்டு", "முடிந்ததாக குறி", "என் கணக்கு", "அசல் கணிப்பான்", "கணக்கு வரலாறு", "கணக்குகள் இல்லை", "வரிசை சேர்", "மீட்டமை", "முடிவு", "கணக்கிடு", "நீளம்", "எடை", "பரப்பு", "தள்ளுபடி", "பில் பிரிப்பு", "அளவு × விலை", "மைலேஜ்", "வெப்பநிலை", "நேரம்", "தினசரி விலை", "வயது / தேதி", "சதவீதம்", "GST", "EMI / கடன்", "நாணயம்", "வரலாறு", "பகிர்", "சேமித்த வரலாறு", "அழி", "சேமித்த முடிவுகள் இல்லை.", "சுருக்கம் பகிர்"),
    "te" to listOf("ఆఫ్‌లైన్‌లో పనిచేస్తుంది", "మొత్తం యాప్‌కు వర్తిస్తుంది", "లైట్, డార్క్ లేదా ఫోన్ సెట్టింగ్ ఎంచుకోండి", "రిమైండర్ అనుమతి", "ఖచ్చిత రిమైండర్‌కు అనుమతి అవసరం", "నోటిఫికేషన్లు: అనుమతి", "నోటిఫికేషన్లు: అనుమతి లేదు", "ఖచ్చిత రిమైండర్: అనుమతి", "ఖచ్చిత రిమైండర్: సుమారు సమయం", "ఖచ్చిత రిమైండర్లకు అనుమతి", "← అన్ని సాధనాలు", "నోట్ తయారు చేసి అవసరమైతే రిమైండర్ జోడించండి.", "ఫోన్ నోటిఫికేషన్ చూపించు", "పూర్తిగా గుర్తించు", "నా లెక్క", "అసలు కాలిక్యులేటర్", "లెక్కల చరిత్ర", "ఇంకా లెక్కలు లేవు", "వరుస జోడించు", "రీసెట్", "ఫలితం", "లెక్కించు", "పొడవు", "బరువు", "వైశాల్యం", "డిస్కౌంట్", "బిల్ విభజన", "పరిమాణం × రేటు", "మైలేజ్", "ఉష్ణోగ్రత", "సమయం", "రోజువారీ ధర", "వయస్సు / తేదీ", "శాతం", "GST", "EMI / రుణం", "కరెన్సీ", "చరిత్ర", "షేర్", "సేవ్ చేసిన చరిత్ర", "క్లియర్", "సేవ్ ఫలితాలు లేవు.", "సారాంశం షేర్"),
    "kn" to listOf("ಆಫ್‌ಲೈನ್‌ನಲ್ಲಿ ಕೆಲಸ ಮಾಡುತ್ತದೆ", "ಇಡೀ ಆ್ಯಪ್‌ಗೆ ಅನ್ವಯ", "ಲೈಟ್, ಡಾರ್ಕ್ ಅಥವಾ ಫೋನ್ ಸೆಟ್ಟಿಂಗ್ ಆಯ್ಕೆ", "ಜ್ಞಾಪನೆ ಅನುಮತಿ", "ನಿಖರ ಜ್ಞಾಪನೆಗೆ ಅನುಮತಿ ಬೇಕು", "ಅಧಿಸೂಚನೆ: ಅನುಮತಿಸಲಾಗಿದೆ", "ಅಧಿಸೂಚನೆ: ಅನುಮತಿ ಇಲ್ಲ", "ನಿಖರ ಜ್ಞಾಪನೆ: ಅನುಮತಿ", "ನಿಖರ ಜ್ಞಾಪನೆ: ಅಂದಾಜು ಸಮಯ", "ನಿಖರ ಜ್ಞಾಪನೆ ಅನುಮತಿಸಿ", "← ಎಲ್ಲಾ ಉಪಕರಣಗಳು", "ನೋಟ್ ರಚಿಸಿ ಮತ್ತು ಬೇಕಾದರೆ ಜ್ಞಾಪನೆ ಸೇರಿಸಿ.", "ಫೋನ್ ಅಧಿಸೂಚನೆ ತೋರಿಸಿ", "ಮುಗಿದಂತೆ ಗುರುತಿಸಿ", "ನನ್ನ ಲೆಕ್ಕ", "ಮೂಲ ಕ್ಯಾಲ್ಕುಲೇಟರ್", "ಲೆಕ್ಕದ ಇತಿಹಾಸ", "ಇನ್ನೂ ಲೆಕ್ಕ ಇಲ್ಲ", "ಸಾಲು ಸೇರಿಸಿ", "ರೀಸೆಟ್", "ಫಲಿತಾಂಶ", "ಲೆಕ್ಕ ಹಾಕಿ", "ಉದ್ದ", "ತೂಕ", "ವಿಸ್ತೀರ್ಣ", "ರಿಯಾಯಿತಿ", "ಬಿಲ್ ಹಂಚಿಕೆ", "ಪ್ರಮಾಣ × ದರ", "ಮೈಲೇಜ್", "ತಾಪಮಾನ", "ಸಮಯ", "ದೈನಂದಿನ ಬೆಲೆ", "ವಯಸ್ಸು / ದಿನಾಂಕ", "ಶೇಕಡಾವಾರು", "GST", "EMI / ಸಾಲ", "ಕರೆನ್ಸಿ", "ಇತಿಹಾಸ", "ಹಂಚಿ", "ಉಳಿಸಿದ ಇತಿಹಾಸ", "ತೆರವು", "ಉಳಿಸಿದ ಫಲಿತಾಂಶ ಇಲ್ಲ.", "ಸಾರಾಂಶ ಹಂಚಿ"),
    "ml" to listOf("ഓഫ്‌ലൈനിൽ പ്രവർത്തിക്കുന്നു", "മുഴുവൻ ആപ്പിനും ബാധകം", "ലൈറ്റ്, ഡാർക്ക് അല്ലെങ്കിൽ ഫോൺ ക്രമീകരണം തിരഞ്ഞെടുക്കുക", "ഓർമ്മപ്പെടുത്തൽ അനുമതി", "കൃത്യ ഓർമ്മപ്പെടുത്തലിന് അനുമതി വേണം", "അറിയിപ്പുകൾ: അനുവദിച്ചു", "അറിയിപ്പുകൾ: അനുവദിച്ചിട്ടില്ല", "കൃത്യ ഓർമ്മപ്പെടുത്തൽ: അനുവദിച്ചു", "കൃത്യ ഓർമ്മപ്പെടുത്തൽ: ഏകദേശ സമയം", "കൃത്യ ഓർമ്മപ്പെടുത്തൽ അനുവദിക്കുക", "← എല്ലാ ടൂളുകളും", "നോട്ട് ഉണ്ടാക്കി വേണമെങ്കിൽ ഓർമ്മപ്പെടുത്തൽ ചേർക്കുക.", "ഫോൺ അറിയിപ്പ് കാണിക്കുക", "പൂർത്തിയായി അടയാളപ്പെടുത്തുക", "എന്റെ കണക്ക്", "ഒറിജിനൽ കാൽക്കുലേറ്റർ", "കണക്കു ചരിത്രം", "കണക്കുകളില്ല", "വരി ചേർക്കുക", "റീസെറ്റ്", "ഫലം", "കണക്കാക്കുക", "നീളം", "ഭാരം", "വിസ്തീർണം", "കിഴിവ്", "ബിൽ വിഭജിക്കുക", "അളവ് × നിരക്ക്", "മൈലേജ്", "താപനില", "സമയം", "ദൈനംദിന വില", "പ്രായം / തീയതി", "ശതമാനം", "GST", "EMI / വായ്പ", "നാണയം", "ചരിത്രം", "പങ്കിടുക", "സേവ് ചെയ്ത ചരിത്രം", "മായ്ക്കുക", "സേവ് ഫലങ്ങളില്ല.", "സംഗ്രഹം പങ്കിടുക"),
    "es" to listOf("Funciona sin conexión", "Se aplica a toda la app", "Elige claro, oscuro o el ajuste del teléfono", "Acceso a recordatorios", "Se necesita permiso para recordatorios precisos", "Notificaciones: permitidas", "Notificaciones: no permitidas", "Recordatorios exactos: permitidos", "Recordatorios exactos: hora aproximada", "Permitir recordatorios exactos", "← Todas las herramientas", "Crea una nota y añade un recordatorio opcional.", "Mostrar notificación del teléfono", "Marcar como hecho", "Mi cálculo", "Calculadora original", "HISTORIAL DE CÁLCULO", "Aún no hay cálculos", "Añadir fila", "REINICIAR", "RESULTADO", "CALCULAR", "Longitud", "Peso", "Área", "Descuento", "Dividir cuenta", "Cant. × Tarifa", "Kilometraje", "Temperatura", "Tiempo", "Precio diario", "Edad / Fecha", "Porcentaje", "GST", "EMI / Préstamo", "Moneda", "Historial", "Compartir", "Historial guardado", "Borrar", "Aún no hay resultados guardados.", "Compartir resumen"),
    "fr" to listOf("Fonctionne hors ligne", "S’applique à toute l’app", "Choisissez clair, sombre ou le réglage du téléphone", "Accès aux rappels", "Autorisation requise pour les rappels précis", "Notifications : autorisées", "Notifications : refusées", "Rappels exacts : autorisés", "Rappels exacts : heure approximative", "Autoriser les rappels exacts", "← Tous les outils", "Créez une note et ajoutez un rappel facultatif.", "Afficher une notification", "Marquer comme terminé", "Mon calcul", "Calculatrice originale", "HISTORIQUE DES CALCULS", "Aucun calcul", "Ajouter une ligne", "RÉINITIALISER", "RÉSULTAT", "CALCULER", "Longueur", "Poids", "Surface", "Remise", "Partager l’addition", "Qté × Tarif", "Kilométrage", "Température", "Temps", "Prix quotidien", "Âge / Date", "Pourcentage", "GST", "EMI / Prêt", "Devise", "Historique", "Partager", "Historique enregistré", "Effacer", "Aucun résultat enregistré.", "Partager le résumé"),
    "de" to listOf("Funktioniert offline", "Gilt für die gesamte App", "Hell, dunkel oder Telefoneinstellung wählen", "Erinnerungszugriff", "Berechtigung für genaue Erinnerungen nötig", "Benachrichtigungen: erlaubt", "Benachrichtigungen: nicht erlaubt", "Genaue Erinnerungen: erlaubt", "Genaue Erinnerungen: ungefähre Zeit", "Genaue Erinnerungen erlauben", "← Alle Tools", "Notiz erstellen und optional erinnern lassen.", "Telefonbenachrichtigung anzeigen", "Als erledigt markieren", "Meine Berechnung", "Original-Rechner", "BERECHNUNGSVERLAUF", "Noch keine Berechnungen", "Zeile hinzufügen", "ZURÜCKSETZEN", "ERGEBNIS", "BERECHNEN", "Länge", "Gewicht", "Fläche", "Rabatt", "Rechnung teilen", "Menge × Preis", "Kilometerstand", "Temperatur", "Zeit", "Tagespreis", "Alter / Datum", "Prozent", "GST", "EMI / Darlehen", "Währung", "Verlauf", "Teilen", "Gespeicherter Verlauf", "Löschen", "Keine gespeicherten Ergebnisse.", "Zusammenfassung teilen"),
    "ar" to listOf("يعمل دون اتصال", "ينطبق على التطبيق كله", "اختر الفاتح أو الداكن أو إعداد الهاتف", "إذن التذكيرات", "يلزم إذن للتذكيرات الدقيقة", "الإشعارات: مسموحة", "الإشعارات: غير مسموحة", "التذكيرات الدقيقة: مسموحة", "التذكيرات الدقيقة: وقت تقريبي", "السماح بالتذكيرات الدقيقة", "← كل الأدوات", "أنشئ ملاحظة وأضف تذكيرًا اختياريًا.", "إظهار إشعار الهاتف", "وضع علامة مكتمل", "حسابي", "الحاسبة الأصلية", "سجل الحسابات", "لا توجد حسابات", "إضافة صف", "إعادة ضبط", "النتيجة", "احسب", "الطول", "الوزن", "المساحة", "الخصم", "تقسيم الفاتورة", "الكمية × السعر", "المسافة", "الحرارة", "الوقت", "السعر اليومي", "العمر / التاريخ", "النسبة", "GST", "EMI / قرض", "العملة", "السجل", "مشاركة", "السجل المحفوظ", "مسح", "لا توجد نتائج محفوظة.", "مشاركة الملخص"),
    "pt" to listOf("Funciona offline", "Aplica-se a toda a app", "Escolha claro, escuro ou a definição do telefone", "Acesso a lembretes", "É necessária permissão para lembretes precisos", "Notificações: permitidas", "Notificações: não permitidas", "Lembretes exatos: permitidos", "Lembretes exatos: hora aproximada", "Permitir lembretes exatos", "← Todas as ferramentas", "Crie uma nota e adicione um lembrete opcional.", "Mostrar notificação do telefone", "Marcar como concluído", "O meu cálculo", "Calculadora original", "HISTÓRICO DE CÁLCULOS", "Ainda sem cálculos", "Adicionar linha", "REPOR", "RESULTADO", "CALCULAR", "Comprimento", "Peso", "Área", "Desconto", "Dividir conta", "Qtd. × Taxa", "Quilometragem", "Temperatura", "Tempo", "Preço diário", "Idade / Data", "Percentagem", "GST", "EMI / Empréstimo", "Moeda", "Histórico", "Partilhar", "Histórico guardado", "Limpar", "Ainda sem resultados guardados.", "Partilhar resumo"),
    "zh" to listOf("可离线使用", "应用于整个应用", "选择浅色、深色或手机设置", "提醒权限", "精确提醒需要权限", "通知：已允许", "通知：未允许", "精确提醒：已允许", "精确提醒：大致时间", "允许精确提醒", "← 所有工具", "创建便签并可选择添加提醒。", "显示手机通知", "标记为完成", "我的计算", "原始计算器", "计算历史", "暂无计算", "添加行", "重置", "结果", "计算", "长度", "重量", "面积", "折扣", "分摊账单", "数量 × 单价", "里程", "温度", "时间", "每日价格", "年龄 / 日期", "百分比", "GST", "EMI / 贷款", "货币", "历史", "分享", "已保存历史", "清除", "暂无保存结果。", "分享摘要"),
    "ja" to listOf("オフラインで動作", "アプリ全体に適用", "ライト、ダーク、端末設定から選択", "リマインダー権限", "正確なリマインダーには権限が必要", "通知：許可済み", "通知：未許可", "正確なリマインダー：許可済み", "正確なリマインダー：おおよその時刻", "正確なリマインダーを許可", "← すべてのツール", "ノートを作成し、必要ならリマインダーを追加します。", "端末の通知を表示", "完了としてマーク", "自分の計算", "標準電卓", "計算履歴", "計算はまだありません", "行を追加", "リセット", "結果", "計算", "長さ", "重さ", "面積", "割引", "割り勘", "数量 × 単価", "燃費", "温度", "時間", "日別価格", "年齢 / 日付", "パーセント", "GST", "EMI / ローン", "通貨", "履歴", "共有", "保存履歴", "クリア", "保存結果はありません。", "概要を共有")
)

private val inlineTermKeys = listOf("4 Value Calculator", "Price", "Marks", "Profit", "General", "Principal", "Interest", "Total payment", "Year", "Balance")
private val inlineTermValues = mapOf(
    "gu" to listOf("4 કિંમત કેલ્ક્યુલેટર", "ભાવ", "ગુણ", "નફો", "સામાન્ય", "મૂડી", "વ્યાજ", "કુલ ચુકવણી", "વર્ષ", "બાકી"),
    "hi" to listOf("4 मान कैलकुलेटर", "मूल्य", "अंक", "लाभ", "सामान्य", "मूलधन", "ब्याज", "कुल भुगतान", "वर्ष", "शेष"),
    "bn" to listOf("৪ মান ক্যালকুলেটর", "মূল্য", "নম্বর", "লাভ", "সাধারণ", "মূলধন", "সুদ", "মোট পরিশোধ", "বছর", "বাকি"),
    "mr" to listOf("4 मूल्य कॅल्क्युलेटर", "किंमत", "गुण", "नफा", "सामान्य", "मुद्दल", "व्याज", "एकूण भरणा", "वर्ष", "शिल्लक"),
    "pa" to listOf("4 ਮੁੱਲ ਕੈਲਕੁਲੇਟਰ", "ਕੀਮਤ", "ਅੰਕ", "ਲਾਭ", "ਆਮ", "ਮੂਲ ਰਕਮ", "ਵਿਆਜ", "ਕੁੱਲ ਭੁਗਤਾਨ", "ਸਾਲ", "ਬਕਾਇਆ"),
    "ta" to listOf("4 மதிப்பு கணிப்பான்", "விலை", "மதிப்பெண்கள்", "லாபம்", "பொது", "அசல்", "வட்டி", "மொத்த செலுத்தல்", "ஆண்டு", "மீதம்"),
    "te" to listOf("4 విలువ కాలిక్యులేటర్", "ధర", "మార్కులు", "లాభం", "సాధారణ", "అసలు", "వడ్డీ", "మొత్తం చెల్లింపు", "సంవత్సరం", "బాకీ"),
    "kn" to listOf("4 ಮೌಲ್ಯ ಕ್ಯಾಲ್ಕುಲೇಟರ್", "ಬೆಲೆ", "ಅಂಕಗಳು", "ಲಾಭ", "ಸಾಮಾನ್ಯ", "ಅಸಲು", "ಬಡ್ಡಿ", "ಒಟ್ಟು ಪಾವತಿ", "ವರ್ಷ", "ಬಾಕಿ"),
    "ml" to listOf("4 മൂല്യ കാൽക്കുലേറ്റർ", "വില", "മാർക്ക്", "ലാഭം", "പൊതു", "മുതൽ", "പലിശ", "ആകെ അടവ്", "വർഷം", "ബാക്കി"),
    "es" to listOf("Calculadora de 4 valores", "Precio", "Notas", "Beneficio", "General", "Capital", "Interés", "Pago total", "Año", "Saldo"),
    "fr" to listOf("Calculatrice à 4 valeurs", "Prix", "Notes", "Bénéfice", "Général", "Capital", "Intérêt", "Paiement total", "Année", "Solde"),
    "de" to listOf("4-Werte-Rechner", "Preis", "Punkte", "Gewinn", "Allgemein", "Kapital", "Zinsen", "Gesamtzahlung", "Jahr", "Restbetrag"),
    "ar" to listOf("حاسبة 4 قيم", "السعر", "الدرجات", "الربح", "عام", "أصل القرض", "الفائدة", "إجمالي الدفع", "السنة", "الرصيد"),
    "pt" to listOf("Calculadora de 4 valores", "Preço", "Notas", "Lucro", "Geral", "Capital", "Juros", "Pagamento total", "Ano", "Saldo"),
    "zh" to listOf("4 值计算器", "价格", "分数", "利润", "通用", "本金", "利息", "总付款", "第年", "余额"),
    "ja" to listOf("4 値計算", "価格", "点数", "利益", "一般", "元金", "利息", "総支払額", "年", "残高")
)

@Composable
fun localizeUiText(text: String): String {
    val language = LocalConfiguration.current.locales[0].language
    val values = localizedValues[language] ?: return text
    val index = localizationKeys.indexOfFirst { it.equals(text, ignoreCase = true) }
    if (index in values.indices) return values[index]
    val more = additionalValues[language].orEmpty()
    val moreIndex = additionalKeys.indexOfFirst { it.equals(text, ignoreCase = true) }
    if (moreIndex in more.indices) return more[moreIndex]
    if (text == "Edit note") return "${localizeUiText("Edit")} ${localizeUiText("Smart Note")}".trim()
    if (text == "Phone language") return when (language) {
        "gu" -> "ફોનની ભાષા"; "hi" -> "फोन की भाषा"; "bn" -> "ফোনের ভাষা"; "mr" -> "फोनची भाषा"; "pa" -> "ਫੋਨ ਦੀ ਭਾਸ਼ਾ"; "ta" -> "போன் மொழி"; "te" -> "ఫోన్ భాష"; "kn" -> "ಫೋನ್ ಭಾಷೆ"; "ml" -> "ഫോൺ ഭാഷ"; "es" -> "Idioma del teléfono"; "fr" -> "Langue du téléphone"; "de" -> "Telefonsprache"; "ar" -> "لغة الهاتف"; "pt" -> "Idioma do telefone"; "zh" -> "手机语言"; "ja" -> "端末の言語"; else -> text
    }
    if (text.startsWith("Preview:")) {
        val label = when (language) { "gu" -> "પૂર્વદર્શન"; "hi", "mr" -> "पूर्वावलोकन"; "bn" -> "পূর্বরূপ"; "pa" -> "ਝਲਕ"; "ta" -> "முன்னோட்டம்"; "te" -> "ముందస్తు చూపు"; "kn" -> "ಮುನ್ನೋಟ"; "ml" -> "പ്രിവ്യൂ"; "es" -> "Vista previa"; "fr" -> "Aperçu"; "de" -> "Vorschau"; "ar" -> "معاينة"; "pt" -> "Pré-visualização"; "zh" -> "预览"; "ja" -> "プレビュー"; else -> "Preview" }
        return "$label:${text.substringAfter(':')}"
    }
    val terms = inlineTermValues[language].orEmpty()
    val exactTerm = inlineTermKeys.indexOfFirst { it.equals(text, ignoreCase = true) }
    if (exactTerm in terms.indices) return terms[exactTerm]
    var replaced = text
    inlineTermKeys.drop(5).forEachIndexed { offset, key ->
        val valueIndex = offset + 5
        if (valueIndex in terms.indices) replaced = replaced.replace(key, terms[valueIndex], ignoreCase = true)
    }
    if (replaced != text) return replaced
    if (text.endsWith(" Converter", ignoreCase = true)) {
        val base = text.dropLast(" Converter".length)
        val translatedBase = localizeUiText(base)
        val converter = when (language) { "gu" -> "કન્વર્ટર"; "hi", "mr" -> "कन्वर्टर"; "bn" -> "কনভার্টার"; "pa" -> "ਕਨਵਰਟਰ"; "ta" -> "மாற்றி"; "te" -> "కన్వర్టర్"; "kn" -> "ಪರಿವರ್ತಕ"; "ml" -> "കൺവേർട്ടർ"; "es" -> "Conversor"; "fr" -> "Convertisseur"; "de" -> "Umrechner"; "ar" -> "محوّل"; "pt" -> "Conversor"; "zh" -> "换算器"; "ja" -> "変換"; else -> "Converter" }
        return "$translatedBase $converter"
    }
    return text
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    MaterialText(
        text = localizeUiText(text), modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
        fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        minLines = minLines, onTextLayout = onTextLayout, style = style
    )
}
