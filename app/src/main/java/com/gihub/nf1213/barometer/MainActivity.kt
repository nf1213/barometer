package com.gihub.nf1213.barometer

import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.gihub.nf1213.barometer.ui.theme.BarometerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pressure = mutableStateOf(0f)
    private var logs = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PeriodicWorker.schedule(this)

        // add a delay to make sure the worker runs the first time -- will have a database and live data in the future
        lifecycleScope.launch {
            delay(1000)
            val allLogs = applicationContext.getSharedPreferences(LOGS_PREFS, Context.MODE_PRIVATE).all
            pressure.value = allLogs.values.map { it.toString() }.lastOrNull()?.toFloatOrNull() ?: 0.0f
            logs.clear()
            logs.addAll(allLogs.map { "${it.key} ${it.value}" }
                .dropLast(1) // the last one is our main value, so skip it
                .reversed() // newest at the top of the list
                .take(10) // just show 10 most recent
            )
        }

        setContent {
            Screen(
                pressure = pressure.value,
                logs = logs
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
