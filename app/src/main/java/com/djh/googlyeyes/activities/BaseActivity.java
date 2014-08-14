package com.djh.googlyeyes.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class BaseActivity extends Activity {

    protected void replaceFragment(Fragment frag, String tag, int container, boolean addToBackstack){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_r_to_l, R.animator.slide_out_r_to_l, R.animator.slide_pop_enter, R.animator.slide_pop_exit);
        ft.replace(container, frag, tag);
        if(addToBackstack){
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

    ProgressDialog pd;

    public void showProgress(String message) {
        if (pd == null) {
            pd = new ProgressDialog(this);
            pd.setMessage(message);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }
    }

    public void hideProgress() {
        if (pd != null) {
            try {
                pd.dismiss();
            } catch (IllegalArgumentException e) {
                //FIXME: I think this works but is bad practice
                //Got the idea here: http://stackoverflow.com/questions/2745061/java-lang-illegalargumentexception-view-not-attached-to-window-manager
                //After getting an error from this line
            } finally {
                pd = null;
            }
        }
    }
}
