package com.djh.googlyeyes.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class PreviewFragment extends BaseFragment{

    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.PreviewFragment.FRAG_TAG";
    private Uri mImageUri;
    private ImageView mImageView;

    public static PreviewFragment newInstance(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(EyeFragment.KEY_URI, uri.toString());
        PreviewFragment frag = new PreviewFragment();
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().getString(EyeFragment.KEY_URI) != null) {
            mImageUri = Uri.parse(getArguments().getString(EyeFragment.KEY_URI));
        }

        if (savedInstanceState != null) {
            mImageUri = Uri.parse(savedInstanceState.getString(EyeFragment.KEY_URI));
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EyeFragment.KEY_URI, mImageUri.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mImageView.setImageURI(mImageUri);
        getActivity().getActionBar().setTitle("PREVIEW");
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setLogo(R.drawable.ic_back_button);
        return view;

    }


}
