package github.o4x.musical.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import androidx.loader.app.LoaderManager;

import github.o4x.musical.ui.fragments.AbsMusicServiceFragment;
import github.o4x.musical.ui.fragments.mainactivity.library.LibraryFragment;
import github.o4x.musical.ui.viewmodel.LibraryViewModel;

public class AbsLibraryPagerFragment extends AbsMusicServiceFragment {

    public AbsLibraryPagerFragment(int layout) {
        super(layout);
    }

    /* http://stackoverflow.com/a/2888433 */
    @Override
    public LoaderManager getLoaderManager() {
        return getParentFragment().getLoaderManager();
    }

    public LibraryFragment getLibraryFragment() {
        return (LibraryFragment) getParentFragment();
    }

    public LibraryViewModel getLibraryViewModel() {
        return getLibraryFragment().getLibraryViewModel();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
