package com.gihub.nf1213.barometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gihub.nf1213.barometer.ui.theme.BarometerTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val CONVERSION = 0.02953f
    }

    private var pressure = mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(object: SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                val hPaPressure = p0?.values?.get(0) ?: 0f
                pressure.value = hPaPressure * CONVERSION
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) { }

        }, pressureSensor, SensorManager.SENSOR_DELAY_FASTEST)

        setContent {
            Screen(pressure = pressure.value)
        }
    }
}

@Composable
fun Screen(pressure: Float) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Pressure(pressure)
        }
    }
}

@Composable
fun Pressure(pressure: Float) {
    Text(text = "%.2f inHg".format(pressure))
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Screen(pressure = 0f)
}
