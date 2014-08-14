package com.djh.googlyeyes.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class HomeFragment extends BaseFragment {
    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.HomeFragment.FRAG_TAG";
    private static final String KEY_HAS_SEEN_SLIDESHOW = "com.djh.googlyeyes.fragments.HomeFragment.KEY_HAS_SEEN_SLIDESHOW";
    private TextView fromCamera;
    private TextView fromGallery;
    private TextView viewer;
    private RelativeLayout instructionSlide;

    private Listener mListener;
    private boolean hasSeenPreviewSlide = false;

    public interface Listener {
        public void onCameraClicked();
        public void onGalleryClicked();
        public void onViewerClicked();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fromCamera = (TextView) view.findViewById(R.id.camera);
        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Launch default camera
                mListener.onCameraClicked();
            }
        });

        fromGallery = (TextView) view.findViewById(R.id.gallery);
        fromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show default gallery
                mListener.onGalleryClicked();
            }
        });

        viewer = (TextView) view.findViewById(R.id.viewer);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show preview fragment
                mListener.onViewerClicked();
            }
        });

        instructionSlide = (RelativeLayout) view.findViewById(R.id.instructionSlide);

        getActivity().getActionBar().setHomeButtonEnabled(false);
        getActivity().getActionBar().setTitle("GooglyEyes");
        getActivity().getActionBar().setLogo(R.drawable.ic_actionbar_icon);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        hasSeenPreviewSlide = sp.getBoolean(KEY_HAS_SEEN_SLIDESHOW, false);

        if (!hasSeenPreviewSlide) {
            //add slide overlay
            instructionSlide.setVisibility(View.VISIBLE);
            instructionSlide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instructionSlide.setVisibility(View.GONE);
                    //Mark slide as having been seen before
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(KEY_HAS_SEEN_SLIDESHOW, true).apply();
                }
            });
        } else {
            instructionSlide.setVisibility(View.GONE);
        }
    }
}
