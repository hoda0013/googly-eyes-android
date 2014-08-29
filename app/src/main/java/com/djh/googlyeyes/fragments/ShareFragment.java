package com.djh.googlyeyes.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.djh.googlyeyes.R;
import com.djh.googlyeyes.activities.MainActivity;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class ShareFragment extends BaseFragment{

    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.PreviewFragment.FRAG_TAG";
    public static final String KEY_HAS_SEEN_SLIDESHOW = "com.djh.googlyeyes.fragments.ShareFragment.KEY_HAS_SEEN_SLIDESHOW";

    private Uri mImageUri;
    private ImageView mImageView;
    private RelativeLayout instructionSlide;
    private Button doneButton;
    private Listener mListener;

    private boolean hasSeenPreviewSlide = false;

    public interface Listener {
        public void onDoneClicked();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public static ShareFragment newInstance(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(EyeFragment.KEY_URI, uri.toString());
        ShareFragment frag = new ShareFragment();
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
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mImageView.setImageURI(mImageUri);
        instructionSlide = (RelativeLayout) view.findViewById(R.id.instructionSlide);
        doneButton = (Button) view.findViewById(R.id.button);

        getActivity().getActionBar().setTitle("SHARE");
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setLogo(R.drawable.ic_back_button);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        hasSeenPreviewSlide = sp.getBoolean(KEY_HAS_SEEN_SLIDESHOW, false);

        if (!hasSeenPreviewSlide) {
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

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDoneClicked();
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.preview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            //send to intent
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, mImageUri);
            shareIntent.setType("image/jpeg");
            startActivityForResult(Intent.createChooser(shareIntent, "Share image..."), 300);
//            startActivity(Intent.createChooser(shareIntent, "Share image..."));
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 300) {
            Log.e("CHOOSER RESULT", "300");
        } else {
            Log.e("CHOOSER RESULT", "NOT 300");

        }
    }
}
