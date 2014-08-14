package com.djh.googlyeyes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class HomeFragment extends BaseFragment {
    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.HomeFragment.FRAG_TAG";
    private TextView addImageTV;
    private Listener mListener;

    public interface Listener {
        public void onAddImageClick();
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
        addImageTV = (TextView) view.findViewById(R.id.addImage);
        addImageTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show image picker
                mListener.onAddImageClick();
            }
        });

        getActivity().getActionBar().setHomeButtonEnabled(false);
        getActivity().getActionBar().setTitle("GooglyEyes");
        getActivity().getActionBar().setLogo(R.drawable.ic_actionbar_icon);

        return view;
    }


}
