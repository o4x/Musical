package com.o4x.musical

import android.content.ComponentName
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
    roomModule
)