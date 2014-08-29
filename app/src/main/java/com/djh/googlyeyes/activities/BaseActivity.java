package com.djh.googlyeyes.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class BaseActivity extends Activity {

    protected void replaceFragment(Fragment frag, String tag, int container, boolean addToBackstack, ProgressBar progressBar){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_r_to_l, R.animator.slide_out_r_to_l, R.animator.slide_pop_enter, R.animator.slide_pop_exit);
        ft.replace(container, frag, tag);
        if(addToBackstack){
            ft.addToBackStack(tag);
        }
        ft.commit();
    }
}
