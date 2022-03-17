package github.o4x.musical.equalizer.equalizer

import android.os.Build
import github.o4x.musical.equalizer.equalizer.bassboost.BassBoostImpl
import github.o4x.musical.equalizer.equalizer.bassboost.BassBoostProxy
import github.o4x.musical.equalizer.equalizer.bassboost.IBassBoost
import github.o4x.musical.equalizer.equalizer.bassboost.IBassBoostInternal
import github.o4x.musical.equalizer.equalizer.equalizer.*
import github.o4x.musical.equalizer.equalizer.virtualizer.IVirtualizer
import github.o4x.musical.equalizer.equalizer.virtualizer.IVirtualizerInternal
import github.o4x.musical.equalizer.equalizer.virtualizer.VirtualizerImpl
import github.o4x.musical.equalizer.equalizer.virtualizer.VirtualizerProxy
import org.koin.dsl.bind
import org.koin.dsl.module

val equalizerModule = module {

    single {
        val equalizerInternal: IEqualizerInternal =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    // crashes on some devices
                    EqualizerImpl28(get(), get())
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    EqualizerImpl(get(), get())
                }
            } else {
                EqualizerImpl(get(), get())
            }

        equalizerInternal
    } bind IEqualizerInternal::class

    single {
        EqualizerProxy(get(), get())
    } bind IEqualizer::class

    single {
        BassBoostProxy(get(), get())
    } bind IBassBoost::class

    single {
        VirtualizerProxy(get(), get())
    } bind IVirtualizer::class

    factory {
        BassBoostImpl(get())
    } bind IBassBoostInternal::class

    factory {
        VirtualizerImpl(get())
    } bind IVirtualizerInternal::class
}