package com.gihub.nf1213.barometer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gihub.nf1213.barometer.db.AppDatabase
import com.gihub.nf1213.barometer.db.PressureEntry
import kotlinx.coroutines.flow.Flow

class BarometerViewModel(applicationContext: Context): ViewModel() {
    val logs: Flow<List<PressureEntry>>

    init {
        val db = AppDatabase.getInstance(applicationContext)
        logs = db.pressureDao().getAll()
    }
}

@Suppress("UNCHECKED_CAST")
class BarometerViewModelFactory(private val application: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BarometerViewModel(application) as T
    }
}
