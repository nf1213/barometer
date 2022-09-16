package com.gihub.nf1213.barometer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import com.gihub.nf1213.barometer.db.PressureEntry
import com.gihub.nf1213.barometer.ui.theme.BarometerTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PeriodicWorker.schedule(this)

        val factory = BarometerViewModelFactory(applicationContext)
        val viewModel = ViewModelProvider(this, factory)[BarometerViewModel::class.java]

        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current
            val logsLifecycleAware = remember(viewModel.logs, lifecycleOwner) {
                viewModel.logs.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            }
            val logs: List<PressureEntry> by logsLifecycleAware.collectAsState(initial = emptyList())
            Screen(
                pressure = logs.firstOrNull()?.value ?: 0f,
                logs = logs.map { "${formatDate(it.dateTime)} ${it.value}" }
            )
        }
    }
}

@Composable
fun Screen(pressure: Float, logs: List<String>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        BarometerTheme {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Pressure(pressure)
                Logs(logs)
            }
        }
    }
}

fun formatDate(dateTime: LocalDateTime): String {
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(dateTime)
}

@Composable
fun Pressure(pressure: Float) {
    Text(
        text = "%.2f inHg".format(pressure),
        fontSize = 32.sp
    )
}

@Composable
fun Logs(logs: List<String>) {
    LazyColumn {
        items(logs) { log ->
            Text(text = log)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Screen(pressure = 0f, listOf("30.0", "29.9"))
}
