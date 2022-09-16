package com.gihub.nf1213.barometer.db

import androidx.room.TypeConverter
import java.time.LocalDateTime

object LocalDateTimeConverter {
    @TypeConverter
    fun toDate(dateString: String?): LocalDateTime? {
        return if (dateString == null) {
            null
        } else {
            LocalDateTime.parse(dateString)
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDateTime?): String? {
        return date?.toString()
    }
}
