package com.naresh.smartcalculatornote

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : AppCompatActivity() {
    private val openNoteId = mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openNoteId.value = intent.getStringExtra(ReminderScheduler.EXTRA_NOTE_ID)
        enableEdgeToEdge()
        setContent {
            val calculatorViewModel: CalculatorViewModel = viewModel { CalculatorViewModel(CalculatorRepository(applicationContext)) }
            SmartCalculatorApp(calculatorViewModel, openNoteId.value)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openNoteId.value = intent.getStringExtra(ReminderScheduler.EXTRA_NOTE_ID)
    }
}
