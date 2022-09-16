package com.gihub.nf1213.barometer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit

class PeriodicWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        fun schedule(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORKER_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    PeriodicWorkRequestBuilder<PeriodicWorker>(15, TimeUnit.MINUTES)
                        .build()
                )
        }
    }

    override suspend fun doWork(): Result {
        setForeground(ForegroundInfo(0, createNotification()))
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val sharedPref = applicationContext.getSharedPreferences(LOGS_PREFS, Context.MODE_PRIVATE)
        var valueRead = false
        var retries = 0
        val maxRetry = 5
        val timestamp = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(LocalDateTime.now())
        val listener = object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                if (!valueRead) {
                    val hPaPressure = p0?.values?.get(0) ?: 0f
                    val inHgPressure = hPaPressure * HPA_TO_INHG_CONVERSION
                    sharedPref.edit().putString(
                        timestamp,
                        "%.2f".format(inHgPressure)
                    ).apply()
                    valueRead = true
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

        }
        sensorManager.registerListener(listener, pressureSensor, SensorManager.SENSOR_DELAY_FASTEST)
        while (!valueRead && retries < maxRetry) {
            delay(1000)
            sensorManager.unregisterListener(listener)
            retries++
        }
        if (!valueRead) {
            sharedPref.edit().putString(timestamp, "Failure").apply()
        }
        return Result.success()
    }

    private fun createNotification(): Notification {
        val id = "barometer_app_periodic_logger"
        val title = "Barometer app"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        val descriptionText = "logging"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(id, title, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        return NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("logging...")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setOngoing(true)
            .build()
    }
}
