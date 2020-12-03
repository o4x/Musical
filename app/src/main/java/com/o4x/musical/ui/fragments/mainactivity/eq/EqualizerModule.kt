package com.o4x.musical.ui.fragments.mainactivity.eq

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class EqualizerModule {
    @Binds
    @IntoMap
    @ViewModelKey(EqualizerFragmentViewModel::class)
    internal abstract fun provideEditFragmentViewModel(viewModel: EqualizerFragmentViewModel): ViewModel
}