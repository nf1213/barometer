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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.gihub.nf1213.barometer.ui.theme.BarometerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PeriodicWorker.schedule(this)

        val logs = applicationContext.getSharedPreferences(LOGS_PREFS, Context.MODE_PRIVATE).all
        val logsFormatted: List<String> = logs.map { "${it.key} ${it.value}" }

        setContent {
            Screen(
                pressure = logs.values.map { it.toString() }.lastOrNull()?.toFloatOrNull() ?: 0.0f,
                logs = logsFormatted.dropLast(1).reversed().take(10)
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
