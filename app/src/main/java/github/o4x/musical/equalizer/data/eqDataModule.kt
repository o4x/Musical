package github.o4x.musical.equalizer.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import github.o4x.musical.equalizer.core.gateway.EqualizerGateway
import github.o4x.musical.equalizer.core.prefs.EqualizerPreferencesGateway
import github.o4x.musical.equalizer.data.db.AppDatabase
import github.o4x.musical.equalizer.data.db.EqualizerPresetsDao
import github.o4x.musical.equalizer.data.prefs.EqualizerPreferenceImpl
import github.o4x.musical.equalizer.data.repository.EqualizerRepository
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