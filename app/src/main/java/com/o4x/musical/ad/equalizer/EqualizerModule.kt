package com.o4x.musical.ad.equalizer

import android.os.Build
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import com.o4x.musical.ad.equalizer.bassboost.BassBoostImpl
import com.o4x.musical.ad.equalizer.bassboost.BassBoostProxy
import com.o4x.musical.ad.equalizer.bassboost.IBassBoost
import com.o4x.musical.ad.equalizer.bassboost.IBassBoostInternal
import com.o4x.musical.ad.equalizer.equalizer.*
import com.o4x.musical.ad.equalizer.equalizer.EqualizerImpl
import com.o4x.musical.ad.equalizer.equalizer.EqualizerImpl28
import com.o4x.musical.ad.equalizer.equalizer.EqualizerProxy
import com.o4x.musical.ad.equalizer.equalizer.IEqualizerInternal
import com.o4x.musical.ad.equalizer.virtualizer.IVirtualizer
import com.o4x.musical.ad.equalizer.virtualizer.IVirtualizerInternal
import com.o4x.musical.ad.equalizer.virtualizer.VirtualizerImpl
import com.o4x.musical.ad.equalizer.virtualizer.VirtualizerProxy
import javax.inject.Singleton

@Module
abstract class EqualizerModule {

    // proxies

    @Binds
    @Singleton
    internal abstract fun provideEqualizer(impl: EqualizerProxy): IEqualizer

    @Binds
    @Singleton
    internal abstract fun provideBassBoost(impl: BassBoostProxy): IBassBoost

    @Binds
    @Singleton
    internal abstract fun provideVirtualizer(impl: VirtualizerProxy): IVirtualizer



    // implementation

    @Binds
    internal abstract fun provideBassBoostInternal(impl: BassBoostImpl): IBassBoostInternal

    @Binds
    internal abstract fun provideVirtualizerInternal(impl: VirtualizerImpl): IVirtualizerInternal

    @Module
    companion object {

        @Provides
        @JvmStatic
        internal fun provideInternalEqualizer(
            equalizerImpl: Lazy<EqualizerImpl>,
            equalizerImpl28: Lazy<EqualizerImpl28>
        ): IEqualizerInternal {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    // crashes on some devices
                    return equalizerImpl28.get()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    return equalizerImpl.get()
                }
            }
            return equalizerImpl.get()
        }

    }

}