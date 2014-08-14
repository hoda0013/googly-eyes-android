package com.djh.googlyeyes.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class BaseActivity extends Activity {

    protected void replaceFragment(Fragment frag, String tag, int container, boolean addToBackstack){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(container, frag, tag);
        if(addToBackstack){
            ft.addToBackStack(tag);
        }
        ft.commit();
    }
}
