package github.o4x.m2

import github.o4x.m2.db.roomModule
import github.o4x.m2.network.*
import github.o4x.m2.repository.dataModule
import github.o4x.m2.ui.viewmodel.viewModules
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