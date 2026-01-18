package com.enduranceflow.core.data.database

import androidx.room.TypeConverter
import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.core.domain.model.Sport
import com.enduranceflow.core.domain.model.TargetType
import java.time.DayOfWeek

/**
 * Type Converters pour Room
 *
 * Room ne peut stocker que des types primitifs (Int, String, etc.)
 * Les TypeConverters convertissent nos types custom (Enum, DayOfWeek)
 * en types que Room peut stocker (String)
 *
 * Gender → String : "FEMALE" / "MALE"
 * Sport → String : "RUNNING" / "CYCLING"
 * TargetType → String : "POWER" / "PACE" / "SPEED" / "HEART_RATE"
 * DayOfWeek → String : "MONDAY" / "TUESDAY" / etc.
 */
class Converters {

    // ========== Gender ==========

    @TypeConverter
    fun fromGender(gender: Gender): String {
        return gender.name
    }

    @TypeConverter
    fun toGender(value: String): Gender {
        return Gender.valueOf(value)
    }

    // ========== Sport ==========

    @TypeConverter
    fun fromSport(sport: Sport): String {
        return sport.name
    }

    @TypeConverter
    fun toSport(value: String): Sport {
        return Sport.valueOf(value)
    }

    // ========== TargetType ==========

    @TypeConverter
    fun fromTargetType(targetType: TargetType): String {
        return targetType.name
    }

    @TypeConverter
    fun toTargetType(value: String): TargetType {
        return TargetType.valueOf(value)
    }

    // ========== DayOfWeek ==========

    @TypeConverter
    fun fromDayOfWeek(dayOfWeek: DayOfWeek): String {
        return dayOfWeek.name
    }

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek {
        return DayOfWeek.valueOf(value)
    }
}
