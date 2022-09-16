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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gihub.nf1213.barometer.db.AppDatabase
import com.gihub.nf1213.barometer.db.PressureEntry
import kotlinx.coroutines.delay
import java.time.LocalDateTime
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
        val db = AppDatabase.getInstance(applicationContext)

        var retries = 0
        val maxRetry = 5
        var value: Float? = null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                if (value == null) {
                    val hPaPressure = p0?.values?.get(0) ?: 0f
                    val inHgPressure = hPaPressure * HPA_TO_INHG_CONVERSION
                    value = inHgPressure
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

        }
        sensorManager.registerListener(listener, pressureSensor, SensorManager.SENSOR_DELAY_FASTEST)
        while (value == null && retries < maxRetry) {
            delay(1000)
            retries++
            Log.d("PeriodicWorker", "value $value, retries $retries")
        }
        sensorManager.unregisterListener(listener)
        return if (value != null) {
            db.pressureDao().insert(PressureEntry(dateTime = LocalDateTime.now(), value = value!!))
            Result.success()
        } else {
            Result.retry()
        }
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
