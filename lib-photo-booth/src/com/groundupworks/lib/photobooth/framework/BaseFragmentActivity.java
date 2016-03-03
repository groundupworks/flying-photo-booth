package com.groundupworks.lib.photobooth.framework;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.groundupworks.lib.photobooth.R;

/**
 * An abstract base {@link FragmentActivity} that provides basic convenient methods for {@link Fragment} management.
 *
 * @author Benedict Lau
 */
public abstract class BaseFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //
    // Public methods.
    //

    /**
     * Adds a {@link Fragment} to the container.
     *
     * @param fragment       the new {@link Fragment} to add.
     * @param addToBackStack true to add transaction to back stack; false otherwise.
     */
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    /**
     * Replaces a {@link Fragment} in the container.
     *
     * @param fragment         the new {@link Fragment} used to replace the current.
     * @param addToBackStack   true to add transaction to back stack; false otherwise.
     * @param popPreviousState true to pop the previous state from the back stack; false otherwise.
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack, boolean popPreviousState) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (popPreviousState) {
            fragmentManager.popBackStack();
        }

        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    /**
     * Shows a {@link DialogFragment}.
     *
     * @param fragment the new {@link DialogFragment} to show.
     */
    public void showDialogFragment(DialogFragment fragment) {
        fragment.show(getSupportFragmentManager(), null);
    }
}
