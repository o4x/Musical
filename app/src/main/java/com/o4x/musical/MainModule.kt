package com.o4x.musical

import android.content.ComponentName
import com.o4x.musical.db.roomModule
import com.o4x.musical.network.*
import com.o4x.musical.notifications.notificationModule
import com.o4x.musical.permissions.permissionsModule
import com.o4x.musical.playback.MediaSessionConnection
import com.o4x.musical.playback.RealMediaSessionConnection
import com.o4x.musical.playback.TimberMusicService
import com.o4x.musical.playback.mediaModule
import com.o4x.musical.repository.dataModule
import com.o4x.musical.ui.viewmodel.viewModules
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

private val mainModule = module {
    single {
        androidContext().contentResolver
    }

    single {
        val component = ComponentName(get(), TimberMusicService::class.java)
        RealMediaSessionConnection(get(), component)
    } bind MediaSessionConnection::class
}

val appModules = listOf(
    mainModule,
    dataModule,
    viewModules,
    networkModule,
    roomModule,
    mediaModule,
    permissionsModule,
    notificationModule
)