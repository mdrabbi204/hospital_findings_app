package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        HospitalEntity::class,
        DoctorEntity::class,
        DiagnosticTestEntity::class,
        RecentViewEntity::class,
        SearchHistoryEntity::class,
        HospitalDiagnosticPriceEntity::class,
        DiagnosticPriceHistoryEntity::class,
        HospitalPriceSyncMetaEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(HealthcareConverters::class)
abstract class MedCareRoomDatabase : RoomDatabase() {

    abstract fun hospitalDao(): HospitalDao
    abstract fun doctorDao(): DoctorDao
    abstract fun diagnosticTestDao(): DiagnosticTestDao
    abstract fun localActivityDao(): LocalActivityDao
    abstract fun diagnosticPriceDao(): DiagnosticPriceDao

    companion object {
        @Volatile
        private var INSTANCE: MedCareRoomDatabase? = null

        fun getInstance(context: Context): MedCareRoomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MedCareRoomDatabase::class.java,
                    "medcare_bd.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
