package com.o4x.musical.ad.equalizer

import android.os.Build
import com.o4x.musical.ad.equalizer.bassboost.BassBoostImpl
import com.o4x.musical.ad.equalizer.bassboost.BassBoostProxy
import com.o4x.musical.ad.equalizer.bassboost.IBassBoost
import com.o4x.musical.ad.equalizer.bassboost.IBassBoostInternal
import com.o4x.musical.ad.equalizer.equalizer.*
import com.o4x.musical.ad.equalizer.virtualizer.IVirtualizer
import com.o4x.musical.ad.equalizer.virtualizer.IVirtualizerInternal
import com.o4x.musical.ad.equalizer.virtualizer.VirtualizerImpl
import com.o4x.musical.ad.equalizer.virtualizer.VirtualizerProxy
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