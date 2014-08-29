package com.djh.googlyeyes.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.view.animation.AnimationUtils;

import com.djh.googlyeyes.R;
import com.djh.googlyeyes.activities.MainActivity;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class BaseFragment extends Fragment {

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        Animator animator;
        if (enter) {
            animator = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_in_r_to_l);
        } else {
            animator = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_out_r_to_l);
        }

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                ((MainActivity)getActivity()).showProgress();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ((MainActivity)getActivity()).hideProgress();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animator;
    }
}
