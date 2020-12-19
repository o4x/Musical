package com.o4x.musical.ad.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.o4x.musical.ad.core.gateway.EqualizerGateway
import com.o4x.musical.ad.core.prefs.EqualizerPreferencesGateway
import com.o4x.musical.ad.data.db.AppDatabase
import com.o4x.musical.ad.data.db.EqualizerPresetsDao
import com.o4x.musical.ad.data.prefs.EqualizerPreferenceImpl
import com.o4x.musical.ad.data.repository.EqualizerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val eqDataModule = module {

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        EqualizerRepository(get(), get())
    } bind EqualizerGateway::class

    factory {
        get<AppDatabase>().equalizerPresetsDao()
    } bind EqualizerPresetsDao::class

    single {
        EqualizerPreferenceImpl(get())
    } bind EqualizerPreferencesGateway::class

    single {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    } bind SharedPreferences::class
}