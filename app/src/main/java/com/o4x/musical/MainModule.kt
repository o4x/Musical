package com.o4x.musical

import com.o4x.musical.equalizer.data.eqDataModule
import com.o4x.musical.equalizer.equalizer.equalizerModule
import com.o4x.musical.db.roomModule
import com.o4x.musical.network.*
import com.o4x.musical.repository.dataModule
import com.o4x.musical.ui.viewmodel.viewModules
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val mainModule = module {
    single {
        androidContext().contentResolver
    }
}

val appModules = listOf(
    mainModule,
    dataModule,
    viewModules,
    networkModule,
    roomModule,

    equalizerModule,
    eqDataModule
)